package com.hortonworks.orendainx.trucking.topology.bolts

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util

import com.hortonworks.orendainx.trucking.shared.models.{TrafficData, TruckAndTrafficData, TruckData}
import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.{AvroSnapshotDeserializer, AvroSnapshotSerializer}
import com.hortonworks.registries.schemaregistry.{SchemaCompatibility, SchemaMetadata}
import com.typesafe.scalalogging.Logger
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.nifi.storm.NiFiDataPacket
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.{Fields, Values}
import org.apache.storm.windowing.TupleWindow

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

/**
  * Bolt responsible for routing data to multiple streams.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class TruckAndTrafficMergeBolt() extends BaseWindowedBolt {

  private lazy val log = Logger(this.getClass)

  private var outputCollector: OutputCollector = _

  // Declare schema-related fields to be initialized when this component's prepare() method is called
  private var schemaRegistryClient: SchemaRegistryClient = _
  private var serializer: AvroSnapshotSerializer = _
  private var deserializer: AvroSnapshotDeserializer = _

  // Define necessary schema metadata
//  private lazy val truckDataSchemaMetadata = new SchemaMetadata.Builder("TruckData")
//    .`type`(AvroSchemaProvider.TYPE).schemaGroup("trucking").description("Truck events being emitted from truck sensors on the edge")
//    .compatibility(SchemaCompatibility.BACKWARD).build()
//  private lazy val trafficDataSchemaMetadata = new SchemaMetadata.Builder("TrafficData")
//    .`type`(AvroSchemaProvider.TYPE).schemaGroup("trucking").description("Traffic data on routes being streamed in from an outside source")
//    .compatibility(SchemaCompatibility.BACKWARD).build()
//  private lazy val mergedSchemaMetadata = new SchemaMetadata.Builder("TruckAndTrafficData")
//    .`type`(AvroSchemaProvider.TYPE).schemaGroup("trucking").description("Merged data")
//    .compatibility(SchemaCompatibility.BACKWARD).build()

  // Define necessary schema metadata
  private var truckDataSchemaMetadata: SchemaMetadata = _
  private var trafficDataSchemaMetadata: SchemaMetadata = _
  private var mergedSchemaMetadata: SchemaMetadata = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector

    // Initialize schema-related fields
    val schemaRegistryUrl = stormConf.get(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name()).toString
    val clientConfig = Map(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name() -> schemaRegistryUrl)
    log.debug(s"prepared: ${stormConf.toString}")
    log.debug(s"prepared2: $schemaRegistryUrl")

    schemaRegistryClient = new SchemaRegistryClient(clientConfig)

    truckDataSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("TruckData").getSchemaMetadata
    trafficDataSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("TrafficData").getSchemaMetadata
    mergedSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("TruckAndTrafficData").getSchemaMetadata

    serializer = schemaRegistryClient.getDefaultSerializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotSerializer]
    deserializer = schemaRegistryClient.getDefaultDeserializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotDeserializer]
    serializer.init(clientConfig)
    deserializer.init(clientConfig)
  }

  override def execute(inputWindow: TupleWindow): Unit = {

    // Collections to collect data into
    val truckDataPerRoute = mutable.HashMap.empty[Int, ListBuffer[TruckData]]
    val trafficDataPerRoute = mutable.HashMap.empty[Int, ListBuffer[TrafficData]]

    // Process each one of the tuples captured in the input window, separating data into bins according to routeId
    inputWindow.get().foreach { tuple =>
      val dp = tuple.getValueByField("nifiDataPacket").asInstanceOf[NiFiDataPacket]

      // Deserialize each tuple and convert it into its proper case class (e.g. TruckData or TrafficData)
      dp.getAttributes.get("dataType") match {
        case "TruckData" =>
          val data: TruckData = deserializer.deserialize(new ByteArrayInputStream(dp.getContent), truckDataSchemaMetadata, null).asInstanceOf[GenericData.Record]
          truckDataPerRoute += (data.routeId -> (truckDataPerRoute.getOrElse(data.routeId, ListBuffer.empty[TruckData]) += data))

        case "TrafficData" =>
          val data: TrafficData = deserializer.deserialize(new ByteArrayInputStream(dp.getContent), trafficDataSchemaMetadata, null).asInstanceOf[GenericData.Record]
          trafficDataPerRoute += (data.routeId -> (trafficDataPerRoute.getOrElse(data.routeId, ListBuffer.empty[TrafficData]) += data))
      }
    }

    // For each TruckData object, find the TrafficData object with the closest timestamp
    truckDataPerRoute.foreach { case (routeId, truckDataList) =>
      trafficDataPerRoute.get(routeId) match {
        case None => // No traffic data for this routeId, so drop/ignore truck data
        case Some(trafficDataList) =>
          truckDataList foreach { truckData =>
            trafficDataList.sortBy(data => math.abs(data.eventTime - truckData.eventTime)).headOption match {
              case None => // Window didn't capture any traffic data for this truck's route
              case Some(trafficData) =>
                // Get latest version of merged schema and merge the two data objects into one record
                val mergedSchemaInfo = schemaRegistryClient.getLatestSchemaVersionInfo("TruckAndTrafficData")
                //val mergedRecord = mergedTruckAndTrafficRecord(new GenericData.Record(new Schema.Parser().parse(mergedSchemaInfo.getSchemaText)), truckData, trafficData)
                val mergedRecord = mergedTruckAndTrafficData(truckData, trafficData)
                log.debug(s"Unserialized data: ${mergedRecord.toString}")

                // TODO: Temporarily emit non-serialized data instead of serialized
                outputCollector.emit(new Values(mergedRecord.toCSV))

                // Serialize the merged record and emit it
                //val serializedData = serializer.serialize(mergedRecord, mergedSchemaMetadata)
                //log.debug(s"Serialized data: ${new String(serializedData, StandardCharsets.UTF_8)}")


                //outputCollector.emit(new Values(new String(serializedData, StandardCharsets.UTF_8)))

            }
          }
      }
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("serializedData"))

  private def mergedTruckAndTrafficRecord(record: GenericRecord, truckData: TruckData, trafficData: TrafficData) = {
    record.put("eventTime", truckData.eventTime.toString)
    record.put("truckId", truckData.truckId.toString)
    record.put("driverId", truckData.driverId.toString)
    record.put("driverName", truckData.driverName)
    record.put("routeId", truckData.routeId.toString)
    record.put("routeName", truckData.routeName)
    record.put("latitude", truckData.latitude.toString)
    record.put("longitude", truckData.longitude.toString)
    record.put("speed", truckData.speed.toString)
    record.put("eventType", truckData.eventType)
    record.put("congestionLevel", trafficData.congestionLevel.toString)
    record
  }

  private def mergedTruckAndTrafficData(truckData: TruckData, trafficData: TrafficData) = {
    TruckAndTrafficData(
      truckData.eventTime,
      truckData.truckId,
      truckData.driverId,
      truckData.driverName,
      truckData.routeId,
      truckData.routeName,
      truckData.latitude,
      truckData.longitude,
      truckData.speed,
      truckData.eventType,
      trafficData.congestionLevel
    )
  }

  private implicit def genericRecordToTruckData(record: GenericRecord): TruckData =
    TruckData(
      record.get("eventTime").toString.toLong,
      record.get("truckId").toString.toInt,
      record.get("driverId").toString.toInt,
      record.get("driverName").toString,
      record.get("routeId").toString.toInt,
      record.get("routeName").toString,
      record.get("latitude").toString.toDouble,
      record.get("longitude").toString.toDouble,
      record.get("speed").toString.toInt,
      record.get("eventType").toString
    )

  private implicit def genericRecordToTrafficData(record: GenericRecord): TrafficData =
    TrafficData(
      record.get("eventTime").toString.toLong,
      record.get("routeId").toString.toInt,
      record.get("congestionLevel").toString.toInt
    )
}

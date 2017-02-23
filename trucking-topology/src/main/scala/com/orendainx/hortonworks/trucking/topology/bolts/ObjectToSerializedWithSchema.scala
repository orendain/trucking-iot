package com.orendainx.hortonworks.trucking.topology.bolts

import java.util

import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.AvroSnapshotSerializer
import com.hortonworks.registries.schemaregistry.{SchemaMetadata, SchemaVersionInfo}
import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckAndTrafficData, WindowedDriverStats}
import com.typesafe.scalalogging.Logger
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

import scala.collection.JavaConverters._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class ObjectToSerializedWithSchema extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  // Declare schema-related fields to be initialized when this component's prepare() method is called
  private var schemaRegistryClient: SchemaRegistryClient = _
  private var serializer: AvroSnapshotSerializer = _

  private var joinedSchemaMetadata: SchemaMetadata = _
  private var joinedSchemaInfo: SchemaVersionInfo = _
  private var driverStatsSchemaMetadata: SchemaMetadata = _
  private var driverStatsJoinedSchemaInfo: SchemaVersionInfo = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector

    val schemaRegistryUrl = stormConf.get(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name()).toString
    val clientConfig = Map(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name() -> schemaRegistryUrl).asJava

    schemaRegistryClient = new SchemaRegistryClient(clientConfig)

    joinedSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("EnrichedTruckAndTrafficData").getSchemaMetadata
    joinedSchemaInfo = schemaRegistryClient.getLatestSchemaVersionInfo("EnrichedTruckAndTrafficData")

    driverStatsSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("WindowedDriverStats").getSchemaMetadata
    driverStatsJoinedSchemaInfo = schemaRegistryClient.getLatestSchemaVersionInfo("WindowedDriverStats")

    serializer = schemaRegistryClient.getDefaultSerializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotSerializer]
    serializer.init(clientConfig)
  }

  override def execute(tuple: Tuple): Unit = {

    val serializedBytes = tuple.getStringByField("dataType") match {
      case "EnrichedTruckAndTrafficData" =>
        val record = enrichedTruckAndTrafficToGenericRecord(tuple.getValueByField("data").asInstanceOf[EnrichedTruckAndTrafficData])
        serializer.serialize(record, joinedSchemaMetadata)
      case "WindowedDriverStats" =>
        val record = enrichedTruckAndTrafficToGenericRecord(tuple.getValueByField("data").asInstanceOf[WindowedDriverStats])
        serializer.serialize(record, driverStatsSchemaMetadata)
    }

    outputCollector.emit(new Values(serializedBytes))
    outputCollector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("data"))

  private def enrichedTruckAndTrafficToGenericRecord(data: EnrichedTruckAndTrafficData) = {
    val record = new GenericData.Record(new Schema.Parser().parse(joinedSchemaInfo.getSchemaText))
    record.put("eventTime", data.eventTime.toString)
    record.put("truckId", data.truckId.toString)
    record.put("driverId", data.driverId.toString)
    record.put("driverName", data.driverName)
    record.put("routeId", data.routeId.toString)
    record.put("routeName", data.routeName)
    record.put("latitude", data.latitude.toString)
    record.put("longitude", data.longitude.toString)
    record.put("speed", data.speed.toString)
    record.put("eventType", data.eventType)
    record.put("foggy", data.foggy)
    record.put("rainy", data.rainy)
    record.put("windy", data.windy)
    record.put("congestionLevel", data.congestionLevel.toString)
    record
  }

  private def enrichedTruckAndTrafficToGenericRecord(data: WindowedDriverStats) = {
    val record = new GenericData.Record(new Schema.Parser().parse(driverStatsJoinedSchemaInfo.getSchemaText))
    record.put("driverId", data.driverId)
    record.put("averageSpeed", data.averageSpeed)
    record.put("totalFog", data.totalFog)
    record.put("totalRain", data.totalRain)
    record.put("totalWind", data.totalWind)
    record.put("totalViolations", data.totalViolations)
    record
  }
}

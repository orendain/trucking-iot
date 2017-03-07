package com.orendainx.hortonworks.trucking.storm.bolts

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util

import com.hortonworks.registries.schemaregistry.SchemaMetadata
import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.AvroSnapshotDeserializer
import com.orendainx.hortonworks.trucking.commons.models.{EnrichedTruckData, TrafficData}
import com.typesafe.scalalogging.Logger
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

import scala.collection.JavaConversions._

/**
  * Convert Tuples in the form of NiFiDataPackets, serialized with Schema Registry, into Tuples of their respective JVM objects.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class BytesWithSchemaToObject extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  // Declare schema-related fields to be initialized when this component's prepare() method is called
  private var schemaRegistryClient: SchemaRegistryClient = _
  private var deserializer: AvroSnapshotDeserializer = _
  private var truckDataSchemaMetadata: SchemaMetadata = _
  private var trafficDataSchemaMetadata: SchemaMetadata = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {

    outputCollector = collector

    val schemaRegistryUrl = stormConf.get(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name()).toString
    val clientConfig = Map(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name() -> schemaRegistryUrl)

    schemaRegistryClient = new SchemaRegistryClient(clientConfig)
    truckDataSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("EnrichedTruckData").getSchemaMetadata
    trafficDataSchemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("TrafficData").getSchemaMetadata
    deserializer = schemaRegistryClient.getDefaultDeserializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotDeserializer]
    deserializer.init(clientConfig)
  }

  override def execute(tuple: Tuple): Unit = {

    // Deserialize each tuple and convert it into its proper case class (e.g. EnrichedTruckData or TrafficData)
    log.info(s"str2: ${tuple.getStringByField("data")}")
    val bytes = new ByteArrayInputStream(tuple.getBinaryByField("data"))
    log.info(s"bytes: $bytes")
    val (dataType, data) = tuple.getStringByField("dataType") match {
      case typ @ "EnrichedTruckData" =>
        log.info(s"des: ${deserializer.deserialize(bytes, truckDataSchemaMetadata, null)}")
        (typ, recordToEnrichedTruckData(deserializer.deserialize(bytes, truckDataSchemaMetadata, null).asInstanceOf[GenericData.Record]))
      case typ @ "TrafficData" =>
        log.info(s"des: ${deserializer.deserialize(bytes, trafficDataSchemaMetadata, null)}")
        (typ, recordToTrafficData(deserializer.deserialize(bytes, trafficDataSchemaMetadata, null).asInstanceOf[GenericData.Record]))
    }

    outputCollector.emit(new Values(data, dataType))
    outputCollector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("data", "dataType"))

  // Helper function to convert GenericRecord (result of deserializing via Schema Registry) into JVM object
  private def recordToEnrichedTruckData(r: GenericRecord): EnrichedTruckData =
    EnrichedTruckData(
      r.get("eventTime").toString.toLong,
      r.get("truckId").toString.toInt,
      r.get("driverId").toString.toInt,
      r.get("driverName").toString,
      r.get("routeId").toString.toInt,
      r.get("routeName").toString,
      r.get("latitude").toString.toDouble,
      r.get("longitude").toString.toDouble,
      r.get("speed").toString.toInt,
      r.get("eventType").toString,
      r.get("foggy").toString.toInt,
      r.get("rainy").toString.toInt,
      r.get("windy").toString.toInt)

  // Helper function to convert GenericRecord (result of deserializing via Schema Registry) into JVM object
  private def recordToTrafficData(r: GenericRecord): TrafficData =
    TrafficData(r.get("eventTime").toString.toLong, r.get("routeId").toString.toInt, r.get("congestionLevel").toString.toInt)
}

package com.orendainx.hortonworks.trucking.topology.bolts

import java.io.ByteArrayInputStream
import java.util

import com.hortonworks.registries.schemaregistry.SchemaMetadata
import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.{AvroSnapshotDeserializer, AvroSnapshotSerializer}
import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckData, TrafficData}
import com.typesafe.scalalogging.Logger
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.nifi.storm.NiFiDataPacket
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class DeserializerBolt extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  // Declare schema-related fields to be initialized when this component's prepare() method is called
  private var schemaRegistryClient: SchemaRegistryClient = _
  private var deserializer: AvroSnapshotDeserializer = _

  // Define necessary schema metadata
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
    val dp = tuple.getValueByField("nifiDataPacket").asInstanceOf[NiFiDataPacket]

    // Deserialize each tuple and convert it into its proper case class (e.g. EnrichedTruckData or TrafficData)
    val (dataType, data) = dp.getAttributes.get("dataType") match {
      case t @ "EnrichedTruckData" => (t, recordToEnrichedTruckData(deserializer.deserialize(new ByteArrayInputStream(dp.getContent), truckDataSchemaMetadata, null).asInstanceOf[GenericData.Record]))
      case t @ "TrafficData" => (t, recordToTrafficData(deserializer.deserialize(new ByteArrayInputStream(dp.getContent), trafficDataSchemaMetadata, null).asInstanceOf[GenericData.Record]))
    }

    outputCollector.emit(new Values(data, dataType))
    outputCollector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("data", "dataType"))

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

  private def recordToTrafficData(r: GenericRecord): TrafficData =
    TrafficData(r.get("eventTime").toString.toLong, r.get("routeId").toString.toInt, r.get("congestionLevel").toString.toInt)
}

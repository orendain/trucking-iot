package com.orendainx.hortonworks.trucking.topology.bolts

import java.io.ByteArrayInputStream
import java.util

import com.hortonworks.registries.schemaregistry.{SchemaMetadata, SchemaVersionInfo}
import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.{AvroSnapshotDeserializer, AvroSnapshotSerializer}
import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckAndTrafficData, EnrichedTruckData, TrafficData}
import com.typesafe.scalalogging.Logger
import org.apache.avro.Schema
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
class SerializerBolt extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  // Declare schema-related fields to be initialized when this component's prepare() method is called
  private var schemaRegistryClient: SchemaRegistryClient = _
  private var schemaMetadata: SchemaMetadata = _
  private var schemaInfo: SchemaVersionInfo = _
  private var serializer: AvroSnapshotSerializer = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {

    outputCollector = collector

    val schemaRegistryUrl = stormConf.get(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name()).toString
    val clientConfig = Map(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name() -> schemaRegistryUrl)

    schemaRegistryClient = new SchemaRegistryClient(clientConfig)
    schemaMetadata = schemaRegistryClient.getSchemaMetadataInfo("EnrichedTruckAndTrafficData").getSchemaMetadata
    schemaInfo = schemaRegistryClient.getLatestSchemaVersionInfo("EnrichedTruckAndTrafficData")
    serializer = schemaRegistryClient.getDefaultSerializer(AvroSchemaProvider.TYPE).asInstanceOf[AvroSnapshotSerializer]
    serializer.init(clientConfig)
  }

  override def execute(tuple: Tuple): Unit = {

    val data = tuple.getValue(0).asInstanceOf[EnrichedTruckAndTrafficData]
    val record = new GenericData.Record(new Schema.Parser().parse(schemaInfo.getSchemaText))

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

    outputCollector.ack(tuple)
    outputCollector.emit(new Values(serializer.serialize(record, schemaMetadata)))
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("serializedData"))
}

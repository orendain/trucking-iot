package com.orendainx.hortonworks.trucking.topology.bolts

import java.util

import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.AvroSnapshotSerializer
import com.hortonworks.registries.schemaregistry.{SchemaMetadata, SchemaVersionInfo}
import com.orendainx.hortonworks.trucking.common.models.EnrichedTruckAndTrafficData
import com.typesafe.scalalogging.Logger
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

import scala.collection.JavaConversions._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class StringSerializerBolt extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector
  }

  override def execute(tuple: Tuple): Unit = {
    outputCollector.emit(new Values(tuple.getValue(0).asInstanceOf[EnrichedTruckAndTrafficData].toCSV))
    outputCollector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("stringSerializedData"))
}

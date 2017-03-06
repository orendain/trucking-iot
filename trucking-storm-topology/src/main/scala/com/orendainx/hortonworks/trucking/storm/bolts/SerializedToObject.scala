package com.orendainx.hortonworks.trucking.storm.bolts

import java.util

import com.orendainx.hortonworks.trucking.commons.models.{EnrichedTruckData, TrafficData}
import com.typesafe.scalalogging.Logger
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

/**
  * Convert Tuples in the form of NiFiDataPackets into Tuples of their respective JVM objects.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class SerializedToObject extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector
  }

  override def execute(tuple: Tuple): Unit = {

    // Convert each string into its proper case class instance (e.g. EnrichedTruckData or TrafficData)
    val (dataType, data) = tuple.getStringByField("dataType") match {
      case typ @ "EnrichedTruckData" => (typ, EnrichedTruckData.fromCSV(tuple.getStringByField("data")))
      case typ @ "TrafficData" => (typ, TrafficData.fromCSV(tuple.getStringByField("data")))
    }

    outputCollector.emit(new Values(dataType, data))
    outputCollector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("dataType", "data"))
}

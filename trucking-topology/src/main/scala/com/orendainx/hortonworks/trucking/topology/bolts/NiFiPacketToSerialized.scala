package com.orendainx.hortonworks.trucking.topology.bolts

import java.nio.charset.StandardCharsets
import java.util

import com.typesafe.scalalogging.Logger
import org.apache.nifi.storm.NiFiDataPacket
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

/**
  * Convert Tuples in the form of NiFiDataPackets into Tuples of their respective JVM objects.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class NiFiPacketToSerialized extends BaseRichBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector
  }

  override def execute(tuple: Tuple): Unit = {
    val dp = tuple.getValueByField("nifiDataPacket").asInstanceOf[NiFiDataPacket]

    outputCollector.emit(new Values(dp.getAttributes.get("dataType"), new String(dp.getContent, StandardCharsets.UTF_8)))
    outputCollector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("dataType", "data"))
}

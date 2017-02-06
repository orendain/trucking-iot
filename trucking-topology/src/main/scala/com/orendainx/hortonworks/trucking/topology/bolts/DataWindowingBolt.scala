package com.orendainx.hortonworks.trucking.topology.bolts

import java.nio.charset.StandardCharsets
import java.util

import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckAndTrafficData, EnrichedTruckData}
import com.typesafe.scalalogging.Logger
import org.apache.nifi.storm.NiFiDataPacket
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.windowing.TupleWindow

import scala.collection.JavaConversions._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class DataWindowingBolt extends BaseWindowedBolt {

  private lazy val log = Logger(this.getClass)

  private var outputCollector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector
  }

  /*
  avg speed
  total violations
  total fog/rain/wind
   */
  override def execute(inputWindow: TupleWindow): Unit = {

    val driverMap = inputWindow.get()
      .map(t => new String(t.getValueByField("nifiDataPacket").asInstanceOf[NiFiDataPacket].getContent, StandardCharsets.UTF_8))
      .map { str =>
        val Array(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType, foggy, rainy, windy, congestionLevel) = str.split("\\|")
        EnrichedTruckAndTrafficData(eventTime.toLong, truckId.toInt, driverId.toInt, driverName, routeId.toInt,
          routeName, latitude.toDouble, longitude.toDouble, speed.toInt, eventType, foggy.toInt, rainy.toInt, windy.toInt, congestionLevel.toInt)
      }
      .groupBy(d => d.driverId)

    //driverMap.
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("windowedData"))

}

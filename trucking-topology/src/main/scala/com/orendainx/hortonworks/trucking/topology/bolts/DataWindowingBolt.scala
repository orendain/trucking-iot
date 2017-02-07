package com.orendainx.hortonworks.trucking.topology.bolts

import java.nio.charset.StandardCharsets
import java.util

import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckAndTrafficData, EnrichedTruckData, TruckEventTypes, WindowedDriverStats}
import com.typesafe.scalalogging.Logger
import org.apache.nifi.storm.NiFiDataPacket
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.{Fields, Values}
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
  driverId,
  avg speed,
  total fog
  total rain,
  total wind,
  total violations
   */
  override def execute(inputWindow: TupleWindow): Unit = {

    // TODO: need to ack
    //outputCollector.ack(t)

    val driverTotals = inputWindow.get()
      // Tuple => String
      //.map(t => new String(t.getValueByField("nifiDataPacket").asInstanceOf[NiFiDataPacket].getContent, StandardCharsets.UTF_8))
      .map(t => t.getValueByField("mergedData").asInstanceOf[EnrichedTruckAndTrafficData])
//      .map { str => // String => EnrichedTruckAndTrafficData
//        val Array(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType, foggy, rainy, windy, congestionLevel) = str.split("\\|")
//        EnrichedTruckAndTrafficData(eventTime.toLong, truckId.toInt, driverId.toInt, driverName, routeId.toInt,
//          routeName, latitude.toDouble, longitude.toDouble, speed.toInt, eventType, foggy.toInt, rainy.toInt, windy.toInt, congestionLevel.toInt)
//      }
      .groupBy(d => d.driverId) // List[EnrichedTruckAndTrafficData] => Map[driverId, List[EnrichedTruckAndTrafficData]]
      .mapValues({ lst => // List[EnrichedTruckAndTrafficData] => Map[driverId, (tupleOfTotals)]
        val sums = lst.map(e => (e.speed, e.foggy, e.rainy, e.windy, if (e.eventType == TruckEventTypes.Normal) 0 else 1))
          .foldLeft((0, 0, 0, 0, 0))((sums, valu) => (sums._1 + valu._1, sums._2 + valu._2, sums._3 + valu._3, sums._4 + valu._4, sums._5 + valu._5))
        (sums._1 / lst.size, sums._2, sums._3, sums._4, sums._5)
      })

    driverTotals.foreach({case (driverId, valu) =>
      //val v = (driverId, sums._1 / driverTotals.size, sums._2, sums._3, sums._4, sums._5)
      //outputCollector.emit(new Values(WindowedDriverStats(driverId, sums._1 / driverTotals.size, sums._2, sums._3, sums._4, sums._5)))
      outputCollector.emit(new Values(WindowedDriverStats(driverId, valu._1, valu._2, valu._3, valu._4, valu._5).toCSV))
      //outputCollector.emit(new Values(s"${v._1}|${v._2}|${v._3}|${v._4}|${v._5}|${v._6}"))
    })
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("windowedDriverStats"))
}

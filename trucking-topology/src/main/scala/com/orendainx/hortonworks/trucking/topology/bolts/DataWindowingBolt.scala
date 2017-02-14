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
  * Takes EnrichedTruckAndTrafficData and generates driver statistics.  It emits WindowedDriverStats onto its stream.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class DataWindowingBolt extends BaseWindowedBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector
  }

  override def execute(inputWindow: TupleWindow): Unit = {

    val driverStats = inputWindow.get()
      .map { t => // List[Tuple] => List[EnrichedTruckAndTrafficData]
        outputCollector.ack(t)
        t.getValueByField("joinedData").asInstanceOf[EnrichedTruckAndTrafficData]
      }
      .groupBy(d => d.driverId) // List[EnrichedTruckAndTrafficData] => Map[driverId, List[EnrichedTruckAndTrafficData]]
      .mapValues({ dataLst => // Map[driverId, List[EnrichedTruckAndTrafficData]] => Map[driverId, (tupleOfStats)]
        val sums = dataLst
          .map(e => (e.speed, e.foggy, e.rainy, e.windy, if (e.eventType == TruckEventTypes.Normal) 0 else 1))
          .foldLeft((0, 0, 0, 0, 0))((s, v) => (s._1 + v._1, s._2 + v._2, s._3 + v._3, s._4 + v._4, s._5 + v._5))
        (sums._1 / dataLst.size, sums._2, sums._3, sums._4, sums._5)
      })

    /*
     * At this point, driverStats is a map where its values are the following over the span of the window:
     * - Average speed
     * - Total fog
     * - Total rain
     * - Total wind
     * - Total violations
     */

    driverStats.foreach({case (driverId, s) =>
      outputCollector.emit(new Values(WindowedDriverStats(driverId, s._1, s._2, s._3, s._4, s._5)))
    })
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("windowedDriverStats"))
}

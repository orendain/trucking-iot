package com.orendainx.hortonworks.trucking.topology.bolts

import java.util

import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckAndTrafficData, EnrichedTruckData, TrafficData}
import com.typesafe.scalalogging.Logger
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.{Fields, Values}
import org.apache.storm.windowing.TupleWindow

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.collection.{Map, mutable}
import scala.language.implicitConversions

/**
  * Bolt responsible for routing data to multiple streams.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class TruckAndTrafficJoinBolt() extends BaseWindowedBolt {

  private lazy val log = Logger(this.getClass)
  private var outputCollector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    outputCollector = collector
  }

  override def execute(inputWindow: TupleWindow): Unit = {

    // Collections to collect data into
    val truckDataPerRoute = mutable.HashMap.empty[Int, ListBuffer[EnrichedTruckData]].withDefaultValue(ListBuffer.empty[EnrichedTruckData])
    val trafficDataPerRoute = mutable.HashMap.empty[Int, ListBuffer[TrafficData]].withDefaultValue(ListBuffer.empty[TrafficData])

    // Process each one of the tuples captured in the input window, separating data according to routeId
    inputWindow.get().foreach { tuple =>
      tuple.getStringByField("dataType") match {
        case "EnrichedTruckData" =>
          val data = tuple.getValueByField("data").asInstanceOf[EnrichedTruckData]
          truckDataPerRoute += ((data.routeId, truckDataPerRoute(data.routeId) += data))
        case "TrafficData" =>
          val data = tuple.getValueByField("data").asInstanceOf[TrafficData]
          trafficDataPerRoute += ((data.routeId, trafficDataPerRoute(data.routeId) += data))
      }
      outputCollector.ack(tuple)
    }

    processAndEmitData(truckDataPerRoute, trafficDataPerRoute)
  }

  /**
    * Correlate the two sets of data so that traffic data is merged with truck data.
    * After correlation, emit the data into an output stream.
    *
    * Note: the specific inner-workings of this method aren't important, except for how we emit the resulting
    * tuple using outputCollector.emit()
    */
  private def processAndEmitData(truckDataPerRoute: Map[Int, ListBuffer[EnrichedTruckData]],
                                 trafficDataPerRoute: Map[Int, ListBuffer[TrafficData]]) {

    // For each EnrichedTruckData object, find the TrafficData object with the closest timestamp
    truckDataPerRoute.foreach { case (routeId, truckDataList) =>
      trafficDataPerRoute.get(routeId) match {
        case None => // No traffic data for this routeId, so drop/ignore truck data
        case Some(trafficDataList) =>
          truckDataList foreach { truckData =>
            trafficDataList.sortBy(data => math.abs(data.eventTime - truckData.eventTime)).headOption match {
              case None => // Window didn't capture any traffic data for this truck's route
              case Some(trafficData) =>

                val joinedData = EnrichedTruckAndTrafficData(truckData.eventTime, truckData.truckId, truckData.driverId, truckData.driverName,
                  truckData.routeId, truckData.routeName, truckData.latitude, truckData.longitude, truckData.speed,
                  truckData.eventType, truckData.foggy, truckData.rainy, truckData.windy, trafficData.congestionLevel)

                outputCollector.emit(new Values(joinedData))
            }
          }
      }
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = declarer.declare(new Fields("joinedData"))
}

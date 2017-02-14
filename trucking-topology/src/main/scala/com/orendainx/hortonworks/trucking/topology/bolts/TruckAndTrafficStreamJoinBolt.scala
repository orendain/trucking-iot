package com.orendainx.hortonworks.trucking.topology.bolts

import java.util

import com.hortonworks.orendainx.trucking.topology.models.{TruckGeoEvent, TruckGeoSpeedEvent, TruckSpeedEvent}
import com.typesafe.scalalogging.Logger
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.windowing.TupleWindow

import scala.collection.JavaConversions._

/**
  * Bolt responsible for joining geo and speed events into a single set of fields.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckAndTrafficStreamJoinBolt {
  val OutputFields = new Fields("eventTime", "truckId", "driverId", "driverName", "routeId", "routeName", "eventType", "latitude", "longitude", "correlationId", "eventKey", "speed")
}

class TruckAndTrafficStreamJoinBolt() extends BaseWindowedBolt {

  lazy val logger = Logger(this.getClass)

  /*
   * Definition and implicit of type 'Values' that acts as a bridge between Scala and Storm's Java [[Values]]
   */
  class Values(val args: Any*)
  implicit def scalaValues2StormValues(v: Values): org.apache.storm.tuple.Values = {
    new org.apache.storm.tuple.Values(v.args.map(_.toString))
  }

  // TODO: wut.
  var collector: OutputCollector = _

  override def prepare(stormConf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    // TODO: Code review: Best practice to keep in super calls? Even if the super call is a NOOP?
    super.prepare(stormConf, context, collector)
    this.collector = collector

    logger.info("Preparations finished")
  }

  override def execute(inputWindow: TupleWindow): Unit = {

    logger.info("Executing")

    // Extract all of the tuples from the TupleWindow
    val tuples = inputWindow.get().toList

    // Filter for tuples from specific streams, parse them into proper events and sort the resulting list by time of event
    val geoEvents = tuples.filter(_.getSourceComponent == "truckGeoEvents").map(TruckGeoEvent(_)).sortBy(_.eventTime.getTime)
    val speedEvents = tuples.filter(_.getSourceComponent == "truckSpeedEvents").map(TruckSpeedEvent(_)).sortBy(_.eventTime.getTime)

    // Zip corresponding geo/speed events together and combine them into a geospeed event before emitting that data downstream
    geoEvents.zip(speedEvents).map(z => TruckGeoSpeedEvent(z._1, z._2)).foreach { e =>
      collector.emit(new Values(e.eventTime, e.truckId, e.driverId, e.driverName, e.routeId, e.routeName, e.status, e.latitude, e.longitude, e.correlationId, e.eventKey, e.speed))
      if (e.status != "Normal")
        collector.emit("anomalies", new Values(e.eventTime, e.truckId, e.driverId, e.driverName, e.routeId, e.routeName, e.status, e.latitude, e.longitude, e.correlationId, e.eventKey, e.speed))
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    // Declare a stream with the default id, and one with a custom id
    declarer.declare(TruckAndTrafficStreamJoinBolt.OutputFields)
    declarer.declareStream("anomalousEvents", TruckAndTrafficStreamJoinBolt.OutputFields)
  }
}
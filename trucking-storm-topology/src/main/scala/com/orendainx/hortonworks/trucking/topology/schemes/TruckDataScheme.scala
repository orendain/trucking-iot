package com.orendainx.hortonworks.trucking.topology.schemes

import java.nio.ByteBuffer

import com.orendainx.hortonworks.trucking.topology.schemes.TrafficDataScheme.deserializeStringAndSplit
import org.apache.storm.tuple.{Fields, Values}

/**
  * Scheme for parsing geo events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckDataScheme extends DelimitedScheme("\\|") {

  override def deserialize(buffer: ByteBuffer): Values = {

    // Extract data from buffer
    val strings = deserializeStringAndSplit(buffer)
    val eventTime = strings(0)
    val truckId = strings(1)
    val driverId = strings(2)
    val driverName = strings(3)
    val routeId = strings(4)
    val routeName = strings(5)
    val latitude = strings(6)
    val longitude = strings(7)
    val speed = strings(8)
    val eventType = strings(9)

    // TODO: Q: Feed strings directly into Values()?  Benefit to unpackaging string into each field's appropriate type?
    new Values(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType)
  }

  override def getOutputFields: Fields =
    new Fields("eventTime", "truckId", "driverId", "driverName", "routeId", "routeName", "latitude", "longitude", "speed", "eventType")
}

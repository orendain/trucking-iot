package com.orendainx.hortonworks.trucking.webapp

import scala.scalajs.js.Date

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */

object PrettyTruckAndTrafficDataFactory {

  def apply(str: String): PrettyTruckAndTrafficData = {

    Console.println("Applying")
    // Parse message string and generate pretty versions of fields
    // TODO: DecimalFormat available in next version of scalajs
    val Array(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType, congestionLevel) = str.split("\\|")
    Console.println(s"Arrayed: $eventTime $truckId")
    val prettyEventTime = new Date(eventTime.toLong).toLocaleTimeString()
    Console.println(s"pt $prettyEventTime")
    val prettyLatitude = (math.floor(latitude.toDouble * 100)/100).toString
    val prettyLongitude = (math.floor(longitude.toDouble * 100)/100).toString

    Console.println(s"pll $prettyLongitude")

    PrettyTruckAndTrafficData(eventTime.toLong, truckId.toInt, driverId.toInt, driverName, routeId.toInt, routeName,
      latitude.toDouble, longitude.toDouble, speed.toInt, eventType, congestionLevel.toInt,
      prettyEventTime, prettyLatitude, prettyLongitude)
  }
}

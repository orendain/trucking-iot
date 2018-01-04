package com.orendainx.trucking.webapplication.models

import scala.scalajs.js.Date

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */

object PrettyEnrichedTruckAndTrafficDataFactory {
  def apply(str: String): PrettyEnrichedTruckAndTrafficData = {
    // Parse message string and generate pretty versions of fields
    // TODO: DecimalFormat available in next version of scalajs
    val Array(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType,
      foggy, rainy, windy, congestionLevel) = str.split("\\|")
    val prettyEventTime = new Date(eventTime.toLong).toLocaleTimeString()
    val prettyLatitude = (math.floor(latitude.toDouble * 100)/100).toString
    val prettyLongitude = (math.floor(longitude.toDouble * 100)/100).toString

    PrettyEnrichedTruckAndTrafficData(eventTime.toLong, truckId.toInt, driverId.toInt, driverName, routeId.toInt, routeName,
      latitude.toDouble, longitude.toDouble, speed.toInt, eventType, foggy.toInt, rainy.toInt, windy.toInt,
      congestionLevel.toInt, prettyEventTime, prettyLatitude, prettyLongitude)
  }
}

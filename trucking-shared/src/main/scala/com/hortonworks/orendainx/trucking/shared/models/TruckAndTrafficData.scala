package com.hortonworks.orendainx.trucking.shared.models

/**
  * The model for a truck event originating from a truck's onboard computer.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class TruckAndTrafficData(eventTime: Long, truckId: Int, driverId: Int, driverName: String,
                      routeId: Int, routeName: String, latitude: Double, longitude: Double,
                      speed: Int, eventType: String, congestionLevel: Int) extends TruckingData {

  lazy val toCSV: String = s"$eventTime|$truckId|$driverId|$driverName|$routeId|$routeName|$latitude|$longitude|$speed|$eventType|$congestionLevel"
}

object TruckAndTrafficData {
  def apply(str: String): TruckAndTrafficData = {
    val Array(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType, congestionLevel) = str.split("|")
    TruckAndTrafficData(eventTime.toLong, truckId.toInt, driverId.toInt, driverName, routeId.toInt, routeName, latitude.toDouble, longitude.toDouble, speed.toInt, eventType, congestionLevel.toInt)
  }
}

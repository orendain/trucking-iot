package com.orendainx.trucking.commons.models

/**
  * The model for a truck event originating from a truck's onboard computer.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class EnrichedTruckData(eventTime: Long, truckId: Int, driverId: Int, driverName: String,
                             routeId: Int, routeName: String, latitude: Double, longitude: Double,
                             speed: Int, eventType: String, foggy: Int, rainy: Int, windy: Int) extends TruckingData {

  lazy val toCSV: String = s"$eventTime|$truckId|$driverId|$driverName|$routeId|$routeName|$latitude|$longitude|$speed|$eventType|$foggy|$rainy|$windy"
}

object EnrichedTruckData {

  /**
    * Parse and create an EnrichedTruckData object from a CSV representation
    */
  def fromCSV(str: String): EnrichedTruckData = {
    val Array(eventTime, truckId, driverId, driverName, routeId, routeName, latitude, longitude, speed, eventType, foggy, rainy, windy) = str.split("\\|")
    EnrichedTruckData(eventTime.toLong, truckId.toInt, driverId.toInt, driverName, routeId.toInt, routeName, latitude.toDouble, longitude.toDouble, speed.toInt, eventType, foggy.toInt, rainy.toInt, windy.toInt)
  }

  /**
    * Create an EnrichedTruckData object from TruckData object and enriching arguments
    */
  def apply(truckData: TruckData, foggy: Int, rainy: Int, windy: Int): EnrichedTruckData =
    EnrichedTruckData(truckData.eventTime, truckData.truckId, truckData.driverId, truckData.driverName, truckData.routeId,
      truckData.routeName, truckData.latitude, truckData.longitude, truckData.speed, truckData.eventType, foggy, rainy, windy)
}

package com.orendainx.hortonworks.trucking.commons.models

/**
  * The model for a truck event originating from a truck's onboard computer.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class EnrichedTruckAndTrafficData(eventTime: Long, truckId: Int, driverId: Int, driverName: String,
                                       routeId: Int, routeName: String, latitude: Double, longitude: Double,
                                       speed: Int, eventType: String, foggy: Int, rainy: Int, windy: Int, congestionLevel: Int) extends TruckingData {

  lazy val toCSV: String = s"$eventTime|$truckId|$driverId|$driverName|$routeId|$routeName|$latitude|$longitude|$speed|$eventType|$foggy|$rainy|$windy|$congestionLevel"
}

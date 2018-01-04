package com.orendainx.trucking.webapplication.models

import angulate2.std.Data

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Data
case class PrettyEnrichedTruckAndTrafficData(eventTime: Long, truckId: Int, driverId: Int, driverName: String,
                                     routeId: Int, routeName: String, latitude: Double, longitude: Double,
                                     speed: Int, eventType: String, foggy: Int, rainy: Int, windy: Int, congestionLevel: Int,
                                     prettyEventTime: String, prettyLatitude: String, prettyLongitude: String)

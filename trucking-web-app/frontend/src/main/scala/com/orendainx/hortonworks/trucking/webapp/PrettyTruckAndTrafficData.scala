package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.Data

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Data
case class PrettyTruckAndTrafficData(eventTime: Long, truckId: Int, driverId: Int, driverName: String,
                                     routeId: Int, routeName: String, latitude: Double, longitude: Double,
                                     speed: Int, eventType: String, congestionLevel: Int,
                                     prettyEventTime: String, prettyLatitude: String, prettyLongitude: String)
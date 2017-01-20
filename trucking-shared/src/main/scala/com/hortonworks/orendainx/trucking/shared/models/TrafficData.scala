package com.hortonworks.orendainx.trucking.shared.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class TrafficData(eventTime: Long, routeId: Int, congestionLevel: Int) extends TruckingData {

  lazy val toCSV = s"$eventTime|$routeId|$congestionLevel"
}

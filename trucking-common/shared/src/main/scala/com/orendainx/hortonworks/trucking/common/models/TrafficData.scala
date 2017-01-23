package com.orendainx.hortonworks.trucking.common.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class TrafficData(eventTime: Long, routeId: Int, congestionLevel: Int) extends TruckingData {

  lazy val toCSV = s"$eventTime|$routeId|$congestionLevel"
}

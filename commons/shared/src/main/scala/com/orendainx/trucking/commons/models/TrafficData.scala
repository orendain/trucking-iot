package com.orendainx.trucking.commons.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class TrafficData(eventTime: Long, routeId: Int, congestionLevel: Int) extends TruckingData {

  lazy val toCSV = s"$eventTime|$routeId|$congestionLevel"
}

object TrafficData {

  /**
    * Parse and create a TrafficData object from a CSV representation
    */
  def fromCSV(str: String): TrafficData = {
    val Array(eventTime, routeId, congestionLevel) = str.split("\\|")
    TrafficData(eventTime.toLong, routeId.toInt, congestionLevel.toInt)
  }
}

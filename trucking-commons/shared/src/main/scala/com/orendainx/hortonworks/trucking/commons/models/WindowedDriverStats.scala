package com.orendainx.hortonworks.trucking.commons.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class WindowedDriverStats(driverId: Int, averageSpeed: Int, totalFog: Int, totalRain: Int, totalWind: Int, totalViolations: Int) {
  lazy val toCSV = s"$driverId|$averageSpeed|$totalFog|$totalRain|$totalWind|$totalViolations"
}

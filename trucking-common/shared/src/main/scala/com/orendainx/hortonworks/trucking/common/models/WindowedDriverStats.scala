package com.orendainx.hortonworks.trucking.common.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class WindowedDriverStats(driverId: Int, averageSpeed: Int, totalFog: Int, totalRain: Int, totalWind: Int, totalViolations: Int)

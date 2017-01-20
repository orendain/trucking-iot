package com.hortonworks.orendainx.trucking.shared.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckDataTypes {
  val Normal = "Normal"
  val Speeding = "Speeding"
  val LaneDeparture = "Lane Departure"
  val UnsafeFollowDistance = "Unsafe Follow Distance"
  val UnsafeTailDistance = "Unsafe Tail Distance"

  val NonNormalTypes = Seq(Speeding, LaneDeparture, UnsafeFollowDistance, UnsafeTailDistance)
}

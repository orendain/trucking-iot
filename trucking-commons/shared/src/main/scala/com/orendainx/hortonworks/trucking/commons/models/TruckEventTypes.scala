package com.orendainx.hortonworks.trucking.commons.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckEventTypes {
  val Normal = "Normal"
  val Speeding = "Speeding"
  val LaneDeparture = "Lane Departure"
  val UnsafeFollowDistance = "Unsafe Follow Distance"
  val UnsafeTailDistance = "Unsafe Tail Distance"

  val NonNormalTypes = Seq(Speeding, LaneDeparture, UnsafeFollowDistance, UnsafeTailDistance)
}

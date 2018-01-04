package com.orendainx.trucking.simulator.models

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class Truck(id: Int) extends Resource

object EmptyTruck extends Truck(0)

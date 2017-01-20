package com.hortonworks.orendainx.trucking.simulator.models

/**
  * The model for a driving pattern.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class DrivingPattern(name: String, minSpeed: Int, maxSpeed: Int, riskFrequency: Int)

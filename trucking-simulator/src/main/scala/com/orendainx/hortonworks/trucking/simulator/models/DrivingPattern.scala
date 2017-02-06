package com.orendainx.hortonworks.trucking.simulator.models

/**
  * The model for a driving pattern.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class DrivingPattern(name: String, minSpeed: Int, maxSpeed: Int, spreeFrequency: Int, spreeLength: Int, violationPercentage: Int)

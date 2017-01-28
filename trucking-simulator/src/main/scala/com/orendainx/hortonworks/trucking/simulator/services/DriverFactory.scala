package com.orendainx.hortonworks.trucking.simulator.services

import com.orendainx.hortonworks.trucking.simulator.models.{Driver, DrivingPattern}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._
import scala.util.Random

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DriverFactory {

  /**
    * Generate a list of drivers using values from the supplied [[com.typesafe.config.Config]].
    */
  def drivers(implicit config: Config): Seq[Driver] = {

    // Generate driving patterns
    val patterns = config.getConfigList("driver.driving-patterns").map { conf =>
      val name = conf.getString("name")
      (name, DrivingPattern(name, conf.getInt("min-speed"), conf.getInt("max-speed"), conf.getInt("risk-frequency")))
    }.toMap

    // First, initialize all special drivers
    val specialDrivers = config.getConfigList("driver.special-drivers").map { conf =>
      Driver(conf.getInt("id"), conf.getString("name"), patterns(conf.getString("pattern")))
    }

    // If we need more drivers, generate "normal" drivers. Or if we need to remove some special drivers, do so.
    val driverCount = config.getInt("driver.driver-count")
    if (specialDrivers.length < driverCount)
      specialDrivers ++ ((specialDrivers.length+1) to driverCount).map { newId =>
        Driver(newId, Random.alphanumeric.take(config.getInt("driver-name-length")).mkString, patterns("normal"))
      }
    else
      specialDrivers.take(driverCount)
  }
}

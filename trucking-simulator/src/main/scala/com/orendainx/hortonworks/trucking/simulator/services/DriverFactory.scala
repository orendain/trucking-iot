package com.orendainx.hortonworks.trucking.simulator.services

import com.orendainx.hortonworks.trucking.simulator.models.{Driver, DrivingPattern}
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DriverFactory {

  /**
    * Generate a list of drivers using values from the supplied [[Config]].
    */
  def drivers(implicit config: Config): Seq[Driver] = {

    // Generate driving patterns
    val patterns = config.getConfigList("driver.driving-patterns").asScala.map { conf =>
      val name = conf.getString("name")
      (name, DrivingPattern(name, conf.getInt("min-speed"), conf.getInt("max-speed"), conf.getInt("spree-frequency"), conf.getInt("spree-length"), conf.getInt("violation-percentage")))
    }.toMap

    // First, initialize all special drivers
    val specialDrivers = config.getConfigList("driver.special-drivers").asScala.map { conf =>
      Driver(conf.getInt("id"), conf.getString("name"), patterns(conf.getString("pattern")))
    }

    // If we need more drivers, generate "normal" drivers. Or if we need to remove some special drivers, do so.
    val driverCount = config.getInt("driver.driver-count")
    if (specialDrivers.lengthCompare(driverCount) < 0)
      specialDrivers ++ ((specialDrivers.length+1) to driverCount).map { newId =>
        Driver(newId, Random.alphanumeric.take(config.getInt("driver-name-length")).mkString, patterns("normal"))
      }
    else
      specialDrivers.take(driverCount)
  }
}

package com.orendainx.hortonworks.trucking.enrichment

import com.orendainx.hortonworks.trucking.commons.models.TruckEventTypes
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Random

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object WeatherAPI {
  lazy val default = new WeatherAPI()
}

class WeatherAPI(config: Config) {

  def this() = this(ConfigFactory.load())

  private implicit val combinedConfig = ConfigFactory.defaultOverrides()
    .withFallback(config)
    .withFallback(ConfigFactory.defaultReference())
    .getConfig("trucking-enrichment.weatherapi")

  /** Queries the weatherAPI for fog status.
    *
    * @param eventType The type of a driving event (e.g. "normal", "speeding", etc.)
    * @return true if the weather is foggy, false otherwise
    */
  def isFoggy(eventType: String): Boolean =
    if (eventType == TruckEventTypes.Normal) Random.nextInt(100) < combinedConfig.getInt("foggy.normal-chance")
    else Random.nextInt(100) < combinedConfig.getInt("foggy.anomalous-chance")

  /** Queries the weatherAPI for rain status.
    *
    * @param eventType The type of a driving event (e.g. "normal", "speeding", etc.)
    * @return true if the weather is rainy, false otherwise
    */
  def isRainy(eventType: String): Boolean =
    if (eventType == TruckEventTypes.Normal) Random.nextInt(100) < combinedConfig.getInt("rainy.normal-chance")
    else Random.nextInt(100) < combinedConfig.getInt("rainy.anomalous-chance")

  /** Queries the weatherAPI for wind status.
    *
    * @param eventType The type of a driving event (e.g. "normal", "speeding", etc.)
    * @return true if the weather is windy, false otherwise
    */
  def isWindy(eventType: String): Boolean =
    if (eventType == TruckEventTypes.Normal) Random.nextInt(100) < combinedConfig.getInt("windy.normal-chance")
    else Random.nextInt(100) < combinedConfig.getInt("windy.anomalous-chance")
}

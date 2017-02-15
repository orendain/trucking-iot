package com.orendainx.hortonworks.trucking.topology

import better.files.File
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.orendainx.hortonworks.trucking.topology.bolts.{DataWindowingBolt, TruckAndTrafficJoinBolt}
import com.orendainx.hortonworks.trucking.topology.nifi.DataPacketBuilder
import com.typesafe.config.{ConfigFactory, Config => TypeConfig}
import com.typesafe.scalalogging.Logger
import org.apache.nifi.remote.client.SiteToSiteClient
import org.apache.nifi.storm.{NiFiBolt, NiFiSpout}
import org.apache.storm.generated.StormTopology
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.{Config, StormSubmitter}

import scala.concurrent.duration._

/**
  * Companion object to [[TruckingTopology]] class.
  * Provides an entry point for passing in a custom configuration.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckingTopology {

  def main(args: Array[String]): Unit = {
    // Build and submit the Storm config and topology
    val (stormConfig, topology) = buildDefaultStormConfigAndTopology()
    StormSubmitter.submitTopology("truckingTopology", stormConfig, topology)
  }

  /**
    * Build a Storm Config and Topology with the default configuration.
    *
    * @return A 2-tuple ([[Config]], [[StormTopology]])
    */
  def buildDefaultStormConfigAndTopology(): (Config, StormTopology) = {
    val config = ConfigFactory.load()

    // Set up configuration for the Storm Topology
    val stormConfig = new Config()
    stormConfig.setDebug(config.getBoolean(Config.TOPOLOGY_DEBUG))
    stormConfig.setMessageTimeoutSecs(config.getInt(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS))
    stormConfig.setNumWorkers(config.getInt(Config.TOPOLOGY_WORKERS))
    stormConfig.put(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name(), config.getString("schema-registry.url"))
    stormConfig.put("emptyConfig", new java.util.HashMap[String, String])

    (stormConfig, new TruckingTopology(config).buildTopology())
  }
}

/**
  * Create a topology with the following components.
  *
  * Spouts:
  *   - NiFiSpout (for injesting EnrichedTruckData from NiFi)
  *   - NiFiSpout (for injesting TrafficData from NiFi)
  * Bolt:
  *   - TruckAndTrafficJoinBolt (for joining EnrichedTruckData and TrafficData streams into one)
  *   - DataWindowingBolt (generating driver stats from trucking data)
  *   - NiFiBolt (for sending data back out to NiFi)
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class TruckingTopology(config: TypeConfig) {

  private lazy val logger = Logger(classOf[TruckingTopology])
  private lazy val NiFiUrl: String = config.getString("nifi.url")

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {

    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()

    // Build Nifi Spouts to ingest trucking data
    buildNifiTruckDataSpout()
    buildNifiTrafficDataSpout()

    // Build Bolt to merge windowed data streams, and then generate sliding windowed driving stats
    buildJoinBolt()
    buildWindowedDriverStatsBolt()

    // Two bolts to push back to NiFi
    buildJoinedDataToNifiBolt()
    buildDriverStatsNiFiBolt()

    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }

  def buildNifiTruckDataSpout()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val duration = config.getLong(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)
    val nifiPort = config.getString("nifi.truck-data.port-name")

    // This assumes that the data is text data, as it will map the byte array received from NiFi to a UTF-8 Encoded string.
    // Attempt to sync up with the join bolt, keeping back pressure in NiFi
    val clientConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(nifiPort)
      .requestBatchDuration(duration, MILLISECONDS).buildConfig()

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("enrichedTruckData", new NiFiSpout(clientConfig), taskCount)
  }

  def buildNifiTrafficDataSpout()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val duration = config.getLong(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)
    val nifiPort = config.getString("nifi.traffic-data.port-name")

    // This assumes that the data is text data, as it will map the byte array received from NiFi to a UTF-8 Encoded string.
    // Attempt to sync up with the join bolt, keeping back pressure in NiFi
    val clientConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(nifiPort)
      .requestBatchDuration(duration, MILLISECONDS).buildConfig()

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("trafficData", new NiFiSpout(clientConfig), taskCount)
  }

  def buildJoinBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val duration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a bolt with a tumbling window
    val bolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(duration, MILLISECONDS))

    // Place the bolt in the topology blueprint
    builder.setBolt("joinedData", bolt, taskCount).globalGrouping("enrichedTruckData").globalGrouping("trafficData")
  }

  def buildWindowedDriverStatsBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val intervalCount = config.getInt(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_COUNT)

    // Create a tuple count based window that slides with every incoming tuple
    val bolt = new DataWindowingBolt().withWindow(new BaseWindowedBolt.Count(intervalCount))

    // Place the bolt in the topology blueprint
    builder.setBolt("windowedDriverStats", bolt, taskCount).shuffleGrouping("joinedData")
  }

  def buildJoinedDataToNifiBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val nifiPort = config.getString("nifi.truck-and-traffic-data.port-name")
    val frequency = config.getInt("nifi.truck-and-traffic-data.tick-frequency")
    val batchSize = config.getInt("nifi.truck-and-traffic-data.batch-size")

    // Construct a clientConfig and a NiFi bolt
    val clientConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(nifiPort).buildConfig()
    val nifiBolt = new NiFiBolt(clientConfig, new DataPacketBuilder(), frequency).withBatchSize(batchSize)

    builder.setBolt("joinedDataToNiFi", nifiBolt, taskCount).shuffleGrouping("joinedData")
  }

  def buildDriverStatsNiFiBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val nifiPort = config.getString("nifi.driver-stats.port-name")
    val frequency = config.getInt("nifi.driver-stats.tick-frequency")
    val batchSize = config.getInt("nifi.driver-stats.batch-size")

    // Construct a clientConfig and a NiFi bolt
    val clientConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(nifiPort).buildConfig()
    val nifiBolt = new NiFiBolt(clientConfig, new DataPacketBuilder(), frequency).withBatchSize(batchSize)

    builder.setBolt("driverStatsToNifi", nifiBolt, taskCount).shuffleGrouping("windowedDriverStats")
  }
}

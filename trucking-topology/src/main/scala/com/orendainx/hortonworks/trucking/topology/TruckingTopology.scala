package com.orendainx.hortonworks.trucking.topology

import better.files.File
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.orendainx.hortonworks.trucking.topology.bolts.TruckAndTrafficMergeBolt
import com.orendainx.hortonworks.trucking.topology.nifi.MergedDataPacketBuilder
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
  * Provides an entry point for passing in a custom configuration file.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckingTopology {

  // NiFi constants
  private val NiFiUrl = "nifi.url"
  private val NiFiInputPortName = "nifi.input.port-name"
  private val NiFiInputBatchSize = "nifi.input.batch-size"
  private val NiFiInputTickFrequency = "nifi.input.tick-frequency"

  // TODO: http://storm.apache.org/releases/1.0.2/SECURITY.html

  def main(args: Array[String]): Unit = {
    // Build and submit the Storm config and topology
    val (stormConfig, topology) = buildStormConfigAndTopology(if (args.nonEmpty) args(0) else "")
    StormSubmitter.submitTopology("truckingTopology", stormConfig, topology)
  }

  // TODO: Default string as param? Eww - clean up before v1.0 release
  def buildStormConfigAndTopology(configPath: String = "") = {
    // Either read in a path to a config file, or use the default one
    val config = if (configPath.nonEmpty) ConfigFactory.parseFile(File(configPath).toJava) else ConfigFactory.load()

    // Set up configuration for the Storm Topology
    val stormConfig = new Config()
    stormConfig.setDebug(config.getBoolean(Config.TOPOLOGY_DEBUG))
    stormConfig.setMessageTimeoutSecs(config.getInt(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS))
    stormConfig.setNumWorkers(config.getInt(Config.TOPOLOGY_WORKERS))
    stormConfig.put(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name(), config.getString("schema-registry.url"))
    stormConfig.put("emptyConfig", new java.util.HashMap[String, String])
    // TODO: would be nice if storm.Config had "setProperty" to hide hashmap implementation

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
  *   - TruckAndTrafficMergeBolt (for merging EnrichedTruckData and TrafficData streams into one)
  *   - NiFiBolt (for sending merged data back out to NiFi)
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class TruckingTopology(config: TypeConfig) {

  private lazy val logger = Logger(classOf[TruckingTopology])

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {

    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()

    // Build Nifi Spouts to ingest trucking data
    buildNifiEnrichedTruckDataSpout()
    buildNifiTrafficDataSpout()

    // Build Bolt to merge data streams together with windowing and another to push back out to NiFi
    buildMergeBolt()
    buildNifiBolt()

    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }

  def buildNifiEnrichedTruckDataSpout()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val nifiUrl = config.getString(TruckingTopology.NiFiUrl)
    val nifiPortName = config.getString("nifi.truck-data.port-name")
    val batchSize = config.getInt("nifi.truck-data.batch-size")
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)

    // This assumes that the data is text data, as it will map the byte array received from NiFi to a UTF-8 Encoded string.
    val client = new SiteToSiteClient.Builder().url(nifiUrl).portName(nifiPortName).requestBatchCount(batchSize).buildConfig()

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("enrichedTruckData", new NiFiSpout(client), taskCount)
  }

  def buildNifiTrafficDataSpout()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val nifiUrl = config.getString(TruckingTopology.NiFiUrl)
    val nifiPortName = config.getString("nifi.traffic-data.port-name")
    val batchSize = config.getInt("nifi.traffic-data.batch-size")
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)

    // This assumes that the data is text data, as it will map the byte array received from NiFi to a UTF-8 Encoded string.
    val client = new SiteToSiteClient.Builder().url(nifiUrl).portName(nifiPortName).requestBatchCount(batchSize).buildConfig()

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("trafficData", new NiFiSpout(client), taskCount)
  }

  def buildMergeBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val duration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a bolt with a tumbling window
    val bolt = new TruckAndTrafficMergeBolt().withTumblingWindow(new BaseWindowedBolt.Duration(duration, MILLISECONDS))

    // Place the bolt in the topology blueprint
    builder.setBolt("mergeData", bolt, taskCount).shuffleGrouping("enrichedTruckData").shuffleGrouping("trafficData")
  }

  def buildNifiBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val nifiUrl = config.getString(TruckingTopology.NiFiUrl)
    val nifiPortName = config.getString(TruckingTopology.NiFiInputPortName)
    val tickFrequency = config.getInt(TruckingTopology.NiFiInputTickFrequency)
    val batchSize = config.getInt(TruckingTopology.NiFiInputBatchSize)
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)

    // Construct a clientConfig and then the NiFi bolt
    val clientConfig = new SiteToSiteClient.Builder().url(nifiUrl).portName(nifiPortName).buildConfig()
    val nifiBolt = new NiFiBolt(clientConfig, new MergedDataPacketBuilder(), tickFrequency).withBatchSize(batchSize)

    builder.setBolt("toNifi", nifiBolt, taskCount).shuffleGrouping("mergeData")
  }
}

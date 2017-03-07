package com.orendainx.hortonworks.trucking.storm.topologies

import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.orendainx.hortonworks.trucking.storm.bolts._
import com.orendainx.hortonworks.trucking.storm.nifi.ByteArrayToNiFiPacket
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
  * Companion object to [[NiFiToNiFi]] class.
  * Provides an entry point for passing in a custom configuration.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object NiFiToNiFi {

  def main(args: Array[String]): Unit = {
    // Build and submit the Storm config and topology
    val (stormConfig, topology) = buildDefaultStormConfigAndTopology()
    StormSubmitter.submitTopologyWithProgressBar("NiFiToNiFi", stormConfig, topology)
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

    (stormConfig, new NiFiToNiFi(config).buildTopology())
  }
}

/**
  * Create a topology with the following components.
  *
  * Spouts:
  *   - NiFiSpout (for injesting EnrichedTruckData from NiFi)
  *   - NiFiSpout (for injesting TrafficData from NiFi)
  * Bolt:
  *   - NiFiPacketToObject (for converting from NiFi packet to JVM object)
  *   - TruckAndTrafficJoinBolt (for joining EnrichedTruckData and TrafficData streams into EnrichedTruckAndTrafficData)
  *   - DataWindowingBolt (for generating driver stats from trucking data)
  *   - ObjectToSerialized (for serializing JVM object into array of bytes)
  *   - NiFiBolt (for sending data back out to NiFi)
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class NiFiToNiFi(config: TypeConfig) {

  private lazy val logger = Logger(this.getClass)
  private lazy val NiFiUrl: String = config.getString("nifi.url")

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {

    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()

    // Default number of tasks (instances) of components to spawn
    val defaultTaskCount = config.getInt(Config.TOPOLOGY_TASKS)





    /*
     * Build Nifi spouts to ingest truck and traffic data separately
     */
    val batchDuration = config.getLong(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)
    val truckNifiPort = config.getString("nifi.truck-data.port-name")
    val trafficNifiPort = config.getString("nifi.traffic-data.port-name")

    // This assumes that the data is text data, as it will map the byte array received from NiFi to a UTF-8 Encoded byteBuffer.
    // Attempt to sync up with the join bolt, keeping back pressure in NiFi
    val truckSpoutConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(truckNifiPort)
      .requestBatchDuration(batchDuration, MILLISECONDS).buildConfig()
    val trafficSpoutConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(trafficNifiPort)
      .requestBatchDuration(batchDuration, MILLISECONDS).buildConfig()

    // Create a spout with the specified configuration, and place it in the, now empty, topology blueprint
    builder.setSpout("enrichedTruckData", new NiFiSpout(truckSpoutConfig), defaultTaskCount)
    builder.setSpout("trafficData", new NiFiSpout(trafficSpoutConfig), defaultTaskCount)



    //
    builder.setBolt("serializedData", new NiFiPacketToSerialized(), defaultTaskCount).shuffleGrouping("enrichedTruckData").shuffleGrouping("trafficData")


    builder.setBolt("unpackagedData", new SerializedToObject(), defaultTaskCount).shuffleGrouping("serializedData")


    /*
     * Build bolt to merge windowed data streams, and then generate sliding windowed driving stats
     */
    val windowDuration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a bolt with a tumbling window and place the bolt in the topology blueprint, connected to the "enrichedTruckData"
    // and "trafficData" streams. globalGrouping suggests that data from both streams be sent to *each* instance of this bolt
    // (in case there are more than one in the cluster)
    val joinBolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(windowDuration, MILLISECONDS))
    builder.setBolt("joinedData", joinBolt, defaultTaskCount).globalGrouping("unpackagedData")





    /*
     * Build bolt to generate driver stats from data collected in a window.
     * Creates a tuple count based window bolt that slides with every incoming tuple.
     */
    val intervalCount = config.getInt(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_COUNT)

    // Build bold and then place in the topology blueprint connected to the "joinedData" stream.  ShuffleGrouping suggests
    // that tuples from that stream are distributed across this bolt's tasks (instances), so as to keep load levels even.
    val statsBolt = new DataWindowingBolt().withWindow(new BaseWindowedBolt.Count(intervalCount))
    builder.setBolt("windowedDriverStats", statsBolt, defaultTaskCount).shuffleGrouping("joinedData")




    /*
     * Serialize data before pushing out to anywhere.
     */
    builder.setBolt("serializedJoinedData", new ObjectToSerialized()).shuffleGrouping("joinedData")
    builder.setBolt("serializedDriverStats", new ObjectToSerialized()).shuffleGrouping("windowedDriverStats")





    /*
     * Build bolts to push data back out to NiFi
     */
    val joinedNifiPort = config.getString("nifi.truck-and-traffic-data.port-name")
    val joinedNififrequency = config.getInt("nifi.truck-and-traffic-data.tick-frequency")
    val joinNifiBatchSize = config.getInt("nifi.truck-and-traffic-data.batch-size")

    // Construct a clientConfig and a NiFi bolt
    val joinedBoltConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(joinedNifiPort).buildConfig()
    val joinedNifiBolt = new NiFiBolt(joinedBoltConfig, new ByteArrayToNiFiPacket(), joinedNififrequency).withBatchSize(joinNifiBatchSize)

    builder.setBolt("joinedDataToNiFi", joinedNifiBolt, defaultTaskCount).shuffleGrouping("serializedJoinedData")


    val statsNifiPort = config.getString("nifi.driver-stats.port-name")
    val statsNifiFrequency = config.getInt("nifi.driver-stats.tick-frequency")
    val statsNifiBatchSize = config.getInt("nifi.driver-stats.batch-size")

    // Construct a clientConfig and a NiFi bolt
    val statsBoltConfig = new SiteToSiteClient.Builder().url(NiFiUrl).portName(statsNifiPort).buildConfig()
    val statsNifiBolt = new NiFiBolt(statsBoltConfig, new ByteArrayToNiFiPacket(), statsNifiFrequency).withBatchSize(statsNifiBatchSize)

    builder.setBolt("driverStatsToNifi", statsNifiBolt, defaultTaskCount).shuffleGrouping("serializedDriverStats")




    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }
}

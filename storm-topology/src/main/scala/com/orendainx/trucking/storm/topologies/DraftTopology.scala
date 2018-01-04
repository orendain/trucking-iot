package com.orendainx.trucking.storm.topologies

import com.typesafe.config.{ConfigFactory, Config => TypeConfig}
import com.typesafe.scalalogging.Logger
import org.apache.storm.generated.StormTopology
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.{Config, StormSubmitter}

/**
  * Companion object to [[NiFiToNiFi]] class.
  * Provides an entry point for passing in a custom configuration.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DraftTopology {

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
    stormConfig.put("emptyConfig", new java.util.HashMap[String, String])
    // TODO: would be nice if storm.Config had "setProperty" to hide hashmap implementation

    (stormConfig, new DraftTopology(config).buildTopology())
  }
}

/**
  * A NON-FUNCTIONING topology meant to simply hold examples of spouts/bolts yet to be used in other topologies.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class DraftTopology(config: TypeConfig) {

  private lazy val logger = Logger(classOf[NiFiToNiFi])
  private lazy val NiFiUrl: String = config.getString("nifi.url")

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {

    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)




    /*



    // Create an HBaseMapper that maps Storm tuples to HBase columns
    val mapper = new SimpleHBaseMapper()
      .withRowKeyField("") // TODO: not connected to HBase yet - implement later
      .withColumnFamily(config.getString("hbase.column-family"))
      .withColumnFields(new Fields("joinedData"))

    // Create a bolt, with its configurations stored under the configuration keyed "emptyConfig"
    val bolt = new HBaseBolt(config.getString(config.getString("hbase.trucking-data.table")), mapper).withConfigKey("emptyConfig")

    // Place the bolt in the topology builder
    builder.setBolt("joinedDataToHBase", bolt, taskCount).shuffleGrouping("joinedData")



    */





    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }
}

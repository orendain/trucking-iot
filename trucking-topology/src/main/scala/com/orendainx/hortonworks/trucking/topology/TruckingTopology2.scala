package com.orendainx.hortonworks.trucking.topology

import java.util.Properties

import com.orendainx.hortonworks.trucking.topology.bolts.TruckAndTrafficJoinBolt
import com.orendainx.hortonworks.trucking.topology.schemes.{TrafficDataScheme, TruckDataScheme}
import com.typesafe.config.{ConfigFactory, Config => TypeConfig}
import com.typesafe.scalalogging.Logger
import org.apache.storm.generated.StormTopology
import org.apache.storm.hbase.bolt.HBaseBolt
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.kafka.{KafkaSpout, SpoutConfig, ZkHosts}
import org.apache.storm.spout.SchemeAsMultiScheme
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.{Config, StormSubmitter}

import scala.concurrent.duration._

/**
  * Companion object to [[TruckingTopology2]] class.
  * Provides an entry point for building the default topology.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckingTopology2 {

  def main(args: Array[String]): Unit = {
    // Build and submit the Storm config and topology
    val (stormConfig, topology) = buildDefaultStormConfigAndTopology()
    StormSubmitter.submitTopology("TruckingTopology2", stormConfig, topology)
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

    (stormConfig, new TruckingTopology2(config).buildTopology())
  }
}

/**
  * Create a topology with the following components.
  *
  * Spouts:
  *   - KafkaSpout (for injesting TruckData)
  *   - KafkaSpout (for injesting TrafficData)
  * Bolt:
  *   - TruckAndTrafficStreamJoinBolt (for joining streams together)
  *   - HBaseBolt (persist events to HDFS)
  *   - KafkaBolt (push to messaging hub)
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class TruckingTopology2(config: TypeConfig) {

  lazy val logger = Logger(this.getClass)

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {
    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()

    // Build Kafka spouts for ingesting trucking data
    buildTruckDataSpout()
    buildTrafficDataSpout()

    // Build bolt to join all incoming streams
    buildStreamJoinBolt()

    // Build HBase bolts to persist joined data
    buildJoinedDataToHBaseBolt()

    // Build Kafka bolt for pushing values to a messaging hub
    buildJoinedDataToKafkaBolt()

    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }

  def buildTruckDataSpout()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val zkHosts = new ZkHosts(config.getString(Config.STORM_ZOOKEEPER_SERVERS))
    val zkRoot = config.getString(Config.STORM_ZOOKEEPER_ROOT)
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val topic = config.getString("kafka.truck-data.topic")
    val groupId = config.getString("kafka.group-id")

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val spoutConfig = new SpoutConfig(zkHosts, topic, zkRoot, groupId)
    spoutConfig.scheme = new SchemeAsMultiScheme(TruckDataScheme)

    // Create a spout with the specified configuration, and place it in the topology blueprint
    val kafkaSpout = new KafkaSpout(spoutConfig)
    builder.setSpout("truckData", kafkaSpout, taskCount)
  }

  def buildTrafficDataSpout()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val zkHosts = new ZkHosts(config.getString(Config.STORM_ZOOKEEPER_SERVERS))
    val zkRoot = config.getString(Config.STORM_ZOOKEEPER_ROOT)
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val topic = config.getString("kafka.traffic-data.topic")
    val groupId = config.getString("kafka.group-id")

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val spoutConfig = new SpoutConfig(zkHosts, topic, zkRoot, groupId)
    spoutConfig.scheme = new SchemeAsMultiScheme(TrafficDataScheme)
    spoutConfig.ignoreZkOffsets = true // Force the spout to ignore where it left off during previous runs

    // Create a spout with the specified configuration, and place it in the topology blueprint
    val kafkaSpout = new KafkaSpout(spoutConfig)
    builder.setSpout("trafficData", kafkaSpout, taskCount)
  }

  def buildStreamJoinBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val duration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a bolt with a tumbling window
    val bolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(duration, MILLISECONDS))

    // Place the bolt in the topology blueprint
    builder.setBolt("joinedData", bolt, taskCount).globalGrouping("truckData").globalGrouping("trafficData")
  }

  def buildJoinedDataToHBaseBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)

    // Create an HBaseMapper that maps Storm tuples to HBase columns
    val mapper = new SimpleHBaseMapper()
      .withRowKeyField("") // TODO: not connected to actual HBase yet - implement when ready
      .withColumnFamily(config.getString("hbase.column-family"))
      .withColumnFields(new Fields("joinedData"))

    // Create a bolt, with its configurations stored under the configuration keyed "emptyConfig"
    val bolt = new HBaseBolt(config.getString(config.getString("hbase.trucking-data.table")), mapper).withConfigKey("emptyConfig")

    // Place the bolt in the topology builder
    builder.setBolt("joinedDataToHBase", bolt, taskCount).shuffleGrouping("joinedData")
  }

  def buildJoinedDataToKafkaBolt()(implicit builder: TopologyBuilder): Unit = {
    // Extract values from config
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)

    // Define properties to pass along to the KafkaBolt
    val props = new Properties()
    props.setProperty("bootstrap.servers", config.getString("kafka.bootstrap-servers"))
    props.setProperty("key.serializer", config.getString("kafka.key-serializer"))
    props.setProperty("value.serializer", config.getString("kafka.value-serializer"))

    // Build a KafkaBolt
    val bolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.trucking-data.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper())
      .withProducerProperties(props)

    builder.setBolt("joinedDataToKafka", bolt, taskCount).shuffleGrouping("joinedData")
  }
}

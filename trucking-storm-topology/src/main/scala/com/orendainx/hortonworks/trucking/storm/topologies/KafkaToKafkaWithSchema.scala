package com.orendainx.hortonworks.trucking.storm.topologies

import java.util.Properties

import com.orendainx.hortonworks.trucking.storm.bolts._
import com.orendainx.hortonworks.trucking.storm.schemes.BytesToStringScheme
import com.typesafe.config.{ConfigFactory, Config => TypeConfig}
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.storm.generated.StormTopology
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.kafka.{KafkaSpout, SpoutConfig, ZkHosts}
import org.apache.storm.spout.SchemeAsMultiScheme
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.{Config, StormSubmitter}

import scala.concurrent.duration._

/**
  * Companion object to [[KafkaToKafka]] class.
  * Provides an entry point for building the default topology.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object KafkaToKafkaWithSchema {

  def main(args: Array[String]): Unit = {
    // Build and submit the Storm config and topology
    val (stormConfig, topology) = buildDefaultStormConfigAndTopology()
    StormSubmitter.submitTopologyWithProgressBar("KafkaToKafkaWithSchema", stormConfig, topology)
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

    (stormConfig, new KafkaToKafkaWithSchema(config).buildTopology())
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
  *   - KafkaBolt (push to messaging hub)
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class KafkaToKafkaWithSchema(config: TypeConfig) {

  private lazy val logger = Logger(this.getClass)

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {
    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()


    // Default number of tasks (instances) of components to spawn
    val taskCount = config.getInt(Config.TOPOLOGY_TASKS)
    val zkHosts = new ZkHosts(config.getString(Config.STORM_ZOOKEEPER_SERVERS))
    val zkRoot = config.getString(Config.STORM_ZOOKEEPER_ROOT)
    val groupId = config.getString("kafka.group-id")

    // Define properties to pass along to the KafkaBolt
    val kafkaBoltProps = new Properties()
    kafkaBoltProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap-servers"))
    kafkaBoltProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("kafka.key-serializer"))
    kafkaBoltProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("kafka.value-serializer"))









    // TODO: TruckDataScheme to know how to read serialized version of data

    // Build Kafka spouts for ingesting trucking data
    // Extract values from config
    val truckTopic = config.getString("kafka.truck-data.topic")

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val truckSpoutConfig = new SpoutConfig(zkHosts, truckTopic, zkRoot, groupId)
    truckSpoutConfig.scheme = new SchemeAsMultiScheme(new BytesToStringScheme("EnrichedTruckData"))
    truckSpoutConfig.ignoreZkOffsets = true // Force the spout to ignore where it left off during previous runs

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("enrichedTruckData", new KafkaSpout(truckSpoutConfig), taskCount)







    // Extract values from config
    val trafficTopic = config.getString("kafka.traffic-data.topic")

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val trafficSpoutConfig = new SpoutConfig(zkHosts, trafficTopic, zkRoot, groupId)
    trafficSpoutConfig.scheme = new SchemeAsMultiScheme(new BytesToStringScheme("TrafficData"))
    trafficSpoutConfig.ignoreZkOffsets = true // Force the spout to ignore where it left off during previous runs

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("trafficData", new KafkaSpout(trafficSpoutConfig), taskCount)






    // Ser
    builder.setBolt("unpackagedData", new SerializedWithSchemaToObject(), taskCount).shuffleGrouping("enrichedTruckData").shuffleGrouping("trafficData")





    val windowDuration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a bolt with a tumbling window and place the bolt in the topology blueprint, connected to the "enrichedTruckData"
    // and "trafficData" streams. globalGrouping suggests that data from both streams be sent to *each* instance of this bolt
    // (in case there are more than one in the cluster)
    val joinBolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(windowDuration, MILLISECONDS))
    builder.setBolt("joinedData", joinBolt, taskCount).globalGrouping("unpackagedData")





    /*
     * Build bolt to generate driver stats from data collected in a window.
     * Creates a tuple count based window bolt that slides with every incoming tuple.
     */
    val intervalCount = config.getInt(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_COUNT)

    // Build bold and then place in the topology blueprint connected to the "joinedData" stream.  ShuffleGrouping suggests
    // that tuples from that stream are distributed across this bolt's tasks (instances), so as to keep load levels even.
    val statsBolt = new DataWindowingBolt().withWindow(new BaseWindowedBolt.Count(intervalCount))
    builder.setBolt("windowedDriverStats", statsBolt, taskCount).shuffleGrouping("joinedData")




    /*
     * Serialize data before pushing out to anywhere.
     */
    builder.setBolt("serializedJoinedData", new ObjectToBytesWithSchema()).shuffleGrouping("joinedData")
    builder.setBolt("serializedDriverStats", new ObjectToBytesWithSchema()).shuffleGrouping("windowedDriverStats")










    /*
     * Push driver stats to Kafka
     */

    // Build a KafkaBolt
    val truckingKafkaBolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.trucking-data.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper())
      .withProducerProperties(kafkaBoltProps)

    builder.setBolt("joinedDataToKafka", truckingKafkaBolt, taskCount).shuffleGrouping("serializedJoinedData")







    // Build a KafkaBolt
    val statsKafkaBolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.driverstats-data.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "stringSerializedData"))
      .withProducerProperties(kafkaBoltProps)

    builder.setBolt("driverStatsToKafka", statsKafkaBolt, taskCount).shuffleGrouping("serializedDriverStats")














    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }
}

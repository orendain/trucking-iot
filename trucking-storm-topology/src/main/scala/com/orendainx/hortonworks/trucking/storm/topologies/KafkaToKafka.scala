package com.orendainx.hortonworks.trucking.storm.topologies

import java.util.Properties

import better.files.File
import com.orendainx.hortonworks.trucking.storm.bolts._
import com.orendainx.hortonworks.trucking.storm.schemes.BufferToStringScheme
import com.typesafe.config.{ConfigFactory, Config => TypeConfig}
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.storm.generated.StormTopology
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.kafka.spout._
//import org.apache.storm.kafka.{KafkaSpout, SpoutConfig, ZkHosts}
import org.apache.storm.spout.SchemeAsMultiScheme
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.{Fields, Values}
import org.apache.storm.{Config, StormSubmitter}

import scala.concurrent.duration._

/**
  * Companion object to [[KafkaToKafka]] class.
  * Provides an entry point for building the topology.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object KafkaToKafka {

  def main(args: Array[String]): Unit = {

    def main(args: Array[String]): Unit = {
      val topology =
        if (args.length > 0) new KafkaToKafka(ConfigFactory.parseFile(File(args(0)).toJava))
        else new KafkaToKafka()

      StormSubmitter.submitTopologyWithProgressBar("KafkaToKafka", topology.stormConfig, topology.buildTopology())
    }

    /*
    // Temp
    val config = ConfigFactory.load()
    val stormConf2 = Utils.readStormConfig()
    stormConf2.put("nimbus.seeds", List("nimbus.storm-app-1.root.hwx.site").asJava)
    stormConf2.putAll(config)
    stormConf2.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, "60")
    val stormClient2 = NimbusClient.getConfiguredClient(stormConf2).getClient
    //val inputJar = "/trucking-iot/trucking-storm-topology/target/scala-2.11/trucking-storm-topology-assembly-0.4.0-SNAPSHOT.jar"
    val inputJar = "/Users/eorendain/Documents/trucking/trucking-iot/trucking-storm-topology/target/scala-2.11/trucking-storm-topology-assembly-0.4.0-SNAPSHOT.jar"
    val nimbus = new NimbusClient(stormConf2, "nimbus.storm-app-1.root.hwx.site")
    val uploadedJarLocation = StormSubmitter.submitJar(stormConf2, inputJar)
    val jsonConf = JSONValue.toJSONString(stormConf2)
    nimbus.getClient.submitTopology("K2K", uploadedJarLocation, jsonConf, topology)
*/

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
class KafkaToKafka(config: TypeConfig) {

  def this() = this(ConfigFactory.load())

  private implicit val combinedConfig = ConfigFactory.defaultOverrides()
    .withFallback(config)
    .withFallback(ConfigFactory.defaultReference())
    .getConfig("trucking-storm-topology")

  private lazy val logger = Logger(this.getClass)

  /**
    *
    * @return a built StormTopology
    */
  def buildTopology(): StormTopology = {
    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()


    // Default number of tasks (instances) of components to spawn
    val defaultTaskCount = combinedConfig.getInt(Config.TOPOLOGY_TASKS)
    //val zkHosts = new ZkHosts(combinedConfig.getString(Config.STORM_ZOOKEEPER_SERVERS))
    //val zkRoot = combinedConfig.getString(Config.STORM_ZOOKEEPER_ROOT)
    //val zkHosts = new ZkHosts(combinedConfig.getString(Config.STORM_ZOOKEEPER_SERVERS))
    val zkRoot = "/services/slider/users/root/kafka-app-2"
    val groupId = combinedConfig.getString("kafka.group-id")

    // Define properties to pass along to the KafkaBolt
    val kafkaBoltProps = new Properties()
    kafkaBoltProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, combinedConfig.getString("kafka.bootstrap-servers"))
    kafkaBoltProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, combinedConfig.getString("kafka.key-serializer"))
    kafkaBoltProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, combinedConfig.getString("kafka.value-serializer"))







    // Build Kafka spouts for ingesting trucking data
    // Extract values from config
    val truckTopic = combinedConfig.getString("kafka.truck-data.topic")


    class HFunc extends Func[ConsumerRecord[String, String], java.util.List[AnyRef]] {
      def apply(record: ConsumerRecord[String, String]) = new Values("EnrichedTruckData", record.value())
    }

    val f: Func[ConsumerRecord[String, String], java.util.List[AnyRef]] = new HFunc

//    val f1: Func[ConsumerRecord[String, String], java.util.List[AnyRef]] = new Func[ConsumerRecord[String, String], java.util.List[AnyRef]] {
//      (r: ConsumerRecord[String, String]) => new Values("EnrichedTruckData", r.value())
//    }


//    val f: Func[ConsumerRecord[String, String], java.util.List[AnyRef]] =
//      (r: ConsumerRecord[String, String]) => new Values("EnrichedTruckData", r.value())
//


    class HFunc2 extends Func[ConsumerRecord[String, String], java.util.List[AnyRef]] {
      def apply(record: ConsumerRecord[String, String]) = new Values("EnrichedTruckData", record.value())
    }
    val f2: Func[ConsumerRecord[String, String], java.util.List[AnyRef]] = new HFunc2

    //val nr = new RecordTranslator[String, String](r => new Values(r.topic, r.key, r.value))

//    val f2: ByTopicRecordTranslator[String, String] = new ByTopicRecordTranslator(f, new Fields("dataType", "data"))

//    val newTrans: ByTopicRecordTranslator[String, String] = new ByTopicRecordTranslator[String, String](
//      r => new Values("EnrichedTruckData", r.value()),
//      new Fields("dataType", "data")
//    )


    val truckSpoutConfig: KafkaSpoutConfig[String, String] = KafkaSpoutConfig.builder(combinedConfig.getString("kafka.bootstrap-servers"), truckTopic)
      .setRecordTranslator(f, new Fields("dataType", "data"))
      //.setRecordTranslator(f, new Fields("dataType", "data"))
      .setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST)
      .setGroupId(groupId)
      .build()

    ///

    /*
    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val truckSpoutConfig = new SpoutConfig(zkHosts, truckTopic, zkRoot, groupId)
    truckSpoutConfig.scheme = new SchemeAsMultiScheme(new BufferToStringScheme("EnrichedTruckData"))
    truckSpoutConfig.ignoreZkOffsets = true // Force the spout to ignore wher

    */

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("enrichedTruckData", new KafkaSpout(truckSpoutConfig), defaultTaskCount)







    // Extract values from config
    val trafficTopic = combinedConfig.getString("kafka.traffic-data.topic")


    val trafficSpoutConfig = KafkaSpoutConfig.builder(combinedConfig.getString("kafka.bootstrap-servers"), trafficTopic)
      .setRecordTranslator(f2, new Fields("dataType", "data"))
      //.setRecordTranslator(f, new Fields("dataType", "data"))
      .setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST)
      .setGroupId(groupId)
      .build()

/*

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val trafficSpoutConfig = new SpoutConfig(zkHosts, trafficTopic, zkRoot, groupId)
    trafficSpoutConfig.scheme = new SchemeAsMultiScheme(new BufferTopStringScheme("TrafficData"))
    trafficSpoutConfig.ignoreZkOffsets = true // Force the spout to ignore where it left off during previous runs // TODO: for testing
*/
    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("trafficData", new KafkaSpout(trafficSpoutConfig), defaultTaskCount)






    // Ser
    builder.setBolt("unpackagedData", new CSVStringToObject(), defaultTaskCount).shuffleGrouping("enrichedTruckData").shuffleGrouping("trafficData")





    val windowDuration = combinedConfig.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a bolt with a tumbling window and place the bolt in the topology blueprint, connected to the "enrichedTruckData"
    // and "trafficData" streams. globalGrouping suggests that data from both streams be sent to *each* instance of this bolt
    // (in case there are more than one in the cluster)
    val joinBolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(windowDuration, MILLISECONDS))
    builder.setBolt("joinedData", joinBolt, defaultTaskCount).globalGrouping("unpackagedData")





    /*
     * Build bolt to generate driver stats from data collected in a window.
     * Creates a tuple count based window bolt that slides with every incoming tuple.
     */
    val intervalCount = combinedConfig.getInt(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_COUNT)

    // Build bold and then place in the topology blueprint connected to the "joinedData" stream.  ShuffleGrouping suggests
    // that tuples from that stream are distributed across this bolt's tasks (instances), so as to keep load levels even.
    val statsBolt = new DataWindowingBolt().withWindow(new BaseWindowedBolt.Count(intervalCount))
    builder.setBolt("windowedDriverStats", statsBolt, defaultTaskCount).shuffleGrouping("joinedData")




    /*
     * Serialize data before pushing out to anywhere.
     */
    builder.setBolt("serializedJoinedData", new ObjectToCSVString()).shuffleGrouping("joinedData")
    builder.setBolt("serializedDriverStats", new ObjectToCSVString()).shuffleGrouping("windowedDriverStats")










    /*
     * Push driver stats to Kafka
     */

    // Build a KafkaBolt
    val truckingKafkaBolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(combinedConfig.getString("kafka.joined-data.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "data"))
      .withProducerProperties(kafkaBoltProps)

    builder.setBolt("joinedDataToKafka", truckingKafkaBolt, defaultTaskCount).shuffleGrouping("serializedJoinedData")







    // Build a KafkaBolt
    val statsKafkaBolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(combinedConfig.getString("kafka.driver-stats.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "data"))
      .withProducerProperties(kafkaBoltProps)

    builder.setBolt("driverStatsToKafka", statsKafkaBolt, defaultTaskCount).shuffleGrouping("serializedDriverStats")














    logger.info("Storm topology finished building.")

    // Finally, create the topology
    builder.createTopology()
  }


  /**
    * Build a Storm Config
    */
  lazy val stormConfig: Config = {
    val stConfig = new Config()
    stConfig.setDebug(combinedConfig.getBoolean(Config.TOPOLOGY_DEBUG))
    stConfig.setMessageTimeoutSecs(combinedConfig.getInt(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS))
    stConfig.setNumWorkers(combinedConfig.getInt(Config.TOPOLOGY_WORKERS))
    stConfig
  }

}

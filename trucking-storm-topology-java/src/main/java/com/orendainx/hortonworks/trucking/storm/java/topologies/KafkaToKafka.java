package com.orendainx.hortonworks.trucking.storm.java.topologies;

import com.orendainx.hortonworks.trucking.storm.java.bolts.CSVStringToObject;
import com.orendainx.hortonworks.trucking.storm.java.bolts.DataWindowingBolt;
import com.orendainx.hortonworks.trucking.storm.java.bolts.ObjectToCSVString;
import com.orendainx.hortonworks.trucking.storm.java.bolts.TruckAndTrafficJoinBolt;
import com.orendainx.hortonworks.trucking.storm.java.schemes.BufferToStringScheme;

import com.typesafe.config.ConfigFactory;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;

import java.util.Properties;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class KafkaToKafka {

  private static com.typesafe.config.Config config = ConfigFactory.load();

  public static void main(String[] args) {
    Config stormConfig = new Config();

    stormConfig.setDebug(config.getBoolean(Config.TOPOLOGY_DEBUG));
    stormConfig.setMessageTimeoutSecs(config.getInt(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS));
    stormConfig.setNumWorkers(config.getInt(Config.TOPOLOGY_WORKERS));

    StormTopology topology = new KafkaToKafka().buildTopology();

    try {
      StormSubmitter.submitTopologyWithProgressBar("KafkaToKafka", stormConfig, topology);
    } catch (AlreadyAliveException|InvalidTopologyException|AuthorizationException e) {
      e.printStackTrace();
    }
  }

  public StormTopology buildTopology() {

    TopologyBuilder builder = new TopologyBuilder();

    // Default number of tasks (instances) of components to spawn
    int defaultTaskCount = config.getInt(Config.TOPOLOGY_TASKS);
    ZkHosts zkHosts = new ZkHosts(config.getString(Config.STORM_ZOOKEEPER_SERVERS));
    String zkRoot = config.getString(Config.STORM_ZOOKEEPER_ROOT);
    String groupId = config.getString("kafka.group-id");

    // Define properties to pass along to the KafkaBolt
    Properties kafkaBoltProps = new Properties();
    kafkaBoltProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap-servers"));
    kafkaBoltProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("kafka.key-serializer"));
    kafkaBoltProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("kafka.value-serializer"));






    // Build Kafka spouts for ingesting trucking data
    // Extract values from config
    String truckTopic = config.getString("kafka.truck-data.topic");

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    SpoutConfig truckSpoutConfig = new SpoutConfig(zkHosts, truckTopic, zkRoot, groupId);
    truckSpoutConfig.scheme = new SchemeAsMultiScheme(new BufferToStringScheme("EnrichedTruckData"));
    truckSpoutConfig.ignoreZkOffsets = true; // Force the spout to ignore where it left off during previous runs // TODO: for testing

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("enrichedTruckData", new KafkaSpout(truckSpoutConfig), defaultTaskCount);







    // Extract values from config
    String trafficTopic = config.getString("kafka.traffic-data.topic");

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    SpoutConfig trafficSpoutConfig = new SpoutConfig(zkHosts, trafficTopic, zkRoot, groupId);
    trafficSpoutConfig.scheme = new SchemeAsMultiScheme(new BufferToStringScheme("TrafficData"));
    trafficSpoutConfig.ignoreZkOffsets = true; // Force the spout to ignore where it left off during previous runs // TODO: for testing

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("trafficData", new KafkaSpout(trafficSpoutConfig), defaultTaskCount);






    // Ser
    builder.setBolt("unpackagedData", new CSVStringToObject(), defaultTaskCount).shuffleGrouping("enrichedTruckData").shuffleGrouping("trafficData");





    int windowDuration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS);

    // Create a bolt with a tumbling window and place the bolt in the topology blueprint, connected to the "enrichedTruckData"
    // and "trafficData" streams. globalGrouping suggests that data from both streams be sent to *each* instance of this bolt
    // (in case there are more than one in the cluster)
    BaseWindowedBolt joinBolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(windowDuration, MILLISECONDS));
    builder.setBolt("joinedData", joinBolt, defaultTaskCount).globalGrouping("unpackagedData");





    /*
     * Build bolt to generate driver stats from data collected in a window.
     * Creates a tuple count based window bolt that slides with every incoming tuple.
     */
    int intervalCount = config.getInt(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_COUNT);

    // Build bold and then place in the topology blueprint connected to the "joinedData" stream.  ShuffleGrouping suggests
    // that tuples from that stream are distributed across this bolt's tasks (instances), so as to keep load levels even.
    BaseWindowedBolt statsBolt = new DataWindowingBolt().withWindow(new BaseWindowedBolt.Count(intervalCount));
    builder.setBolt("windowedDriverStats", statsBolt, defaultTaskCount).shuffleGrouping("joinedData");




    /*
     * Serialize data before pushing out to anywhere.
     */
    builder.setBolt("serializedJoinedData", new ObjectToCSVString()).shuffleGrouping("joinedData");
    builder.setBolt("serializedDriverStats", new ObjectToCSVString()).shuffleGrouping("windowedDriverStats");










    /*
     * Push driver stats to Kafka
     */

    // Build a KafkaBolt
    KafkaBolt truckingKafkaBolt = new KafkaBolt<String, String>()
        .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.joined-data.topic")))
        .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<String, String>("key", "data"))
        .withProducerProperties(kafkaBoltProps);

    builder.setBolt("joinedDataToKafka", truckingKafkaBolt, defaultTaskCount).shuffleGrouping("serializedJoinedData");







    // Build a KafkaBolt
    KafkaBolt statsKafkaBolt = new KafkaBolt<String, String>()
        .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.driver-stats.topic")))
        .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<String, String>("key", "data"))
        .withProducerProperties(kafkaBoltProps);

    builder.setBolt("driverStatsToKafka", statsKafkaBolt, defaultTaskCount).shuffleGrouping("serializedDriverStats");














    //logger.info("Storm topology finished building.");

    // Finally, create the topology
    return builder.createTopology();
  }
}

package controllers

import java.util
import javax.inject._

import com.orendainx.hortonworks.trucking.topology.TruckingTopology
import org.apache.storm.{Config, StormSubmitter}
import org.apache.storm.shade.org.json.simple.JSONValue
import org.apache.storm.utils.{NimbusClient, Utils}
import play.api.mvc._

import scala.util.parsing.json.JSONObject

//import scala.util.parsing.json.JSONObject

//import org.apache.storm

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class Storm @Inject() extends Controller {

  /**
    * Create an Action to render an HTML page with the main guiding message.
    */
  def deploy = Action {

    val (stormConfig, stormTopology) = TruckingTopology.buildStormConfigAndTopology()

    stormConfig.put("storm.zookeeper.servers", "sandbox.hortonworks.com")
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    //stormConfig.put("nimbus.seeds", List("sandbox.hortonworks.com").asJava)
    stormConfig.put(Config.NIMBUS_HOST, "sandbox.hortonworks.com")
    stormConfig.setDebug(true)

    //val stormConf2: Map[String, AnyRef] = Utils.readStormConfig()
    //stormConf2.put("nimbus.host", "sandbox.hortonworks.com")

    System.out.println(s"config: ${stormConfig.toString}")

    //val nimbusConfig: com.typesafe.config.Config = ???
    //val nimbusClient: NimbusClient = new NimbusClient(stormConfig, nimbusConfig.getString("nimbus.host"), nimbusConfig.getInt("nimbus.port"))
    //val nimbusClient = new NimbusClient(stormConfig, "sandbox.hortonworks.com", 6627)
    //val nimbusClient = NimbusClient.getConfiguredClient(stormConf2)
    val nimbusClient = new NimbusClient(
      new util.HashMap[String,String](), "sandbox.hortonworks.com", 6627
    )



    // Upload the Storm topology
    val topologyLocation: String = "/Users/eorendain/Documents/trucking/trucking-iot/target/scala-2.11/trucking-topology-assembly-0.3.2.jar"
    val remoteJarLocation = StormSubmitter.submitJar(stormConfig, topologyLocation)

    // Kill Storm if it's up
    //terminate()

    val stormConfigAsJson = JSONValue.toJSONString(stormConfig)

    //nimbusClient.getClient.submitTopology(stormConfig.get("AnyNameIWish,Methinks").toString, remoteJarLocation, stormConfigAsJson, stormTopology)
    nimbusClient.getClient.submitTopology("HiThisIsAName", remoteJarLocation, stormConfigAsJson, stormTopology)

    Ok("Deployed.")
  }


  /*
  def info = Action {
    // TODO: can recycle nimbusClient?
    val nimbusClient: NimbusClient = ???
    val clustorInfo = nimbusClient.getClient.getClusterInfo

    Ok("fetched")
  }

  def terminate = Action {
    val topologyName: String = ???
    val nimbusClient: NimbusClient = ???
    //val clusterInfo = nimbusClient.getClient.getClusterInfo.get_topologies().toList.exists(_.get_name() == topologyName)

    if (???) {
      nimbusClient.getClient.killTopology(topologyName)
    }

    Ok("terminated")
  }
  */

}

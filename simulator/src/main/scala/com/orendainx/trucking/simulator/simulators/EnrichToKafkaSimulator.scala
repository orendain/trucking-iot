package com.orendainx.trucking.simulator.simulators

import akka.actor.{Actor, ActorSystem, Inbox, Props}
import better.files.File
import com.orendainx.trucking.commons.models._
import com.orendainx.trucking.enrichment.WeatherAPI
import com.orendainx.trucking.simulator.coordinators.AutomaticCoordinator
import com.orendainx.trucking.simulator.depots.NoSharingDepot
import com.orendainx.trucking.simulator.flows.SharedFlowManager
import com.orendainx.trucking.simulator.generators.TruckAndTrafficGenerator
import com.orendainx.trucking.simulator.services.DriverFactory
import com.orendainx.trucking.simulator.transmitters.{ActorTransmitter, DataTransmitter, KafkaTransmitter}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * This simulator begins processing data as soon as it is created.  Generated data is first passed to a private helper
  * actor (EnrichmentActor) for enrichment before being routed to the appropriate KafkaTransmitters.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object EnrichToKafkaSimulator {
  def main(args: Array[String]): Unit = {
    if (args.length > 0) new EnrichToKafkaSimulator(ConfigFactory.parseFile(File(args(1)).toJava))
    else new EnrichToKafkaSimulator()
  }
}

class EnrichToKafkaSimulator(val config: Config) extends Simulator {

  def this() = this(ConfigFactory.load())

  // Wrap 'config' with the default config and system defaults
  private implicit val combinedConfig: Config = ConfigFactory.load(config).getConfig("trucking-simulator")

  private val system = ActorSystem("EnrichToKafkaSimulator")

  // Generate the drivers to be used in the simulation and create an Inbox for accepting messages
  private val drivers = DriverFactory.drivers
  private val inbox = Inbox.create(system)

  // Generate the different actors in the simulation
  private val depot = system.actorOf(NoSharingDepot.props())
  private val kafkaTruckTransmitter = system.actorOf(KafkaTransmitter.props("trucking_data_truck_enriched"))
  private val kafkaTrafficTransmitter = system.actorOf(KafkaTransmitter.props("trucking_data_traffic"))
  private val enrichmentActor = system.actorOf(Props(new EnrichmentActor))
  private val actorTransmitter = system.actorOf(ActorTransmitter.props(enrichmentActor))
  private val flowManager = system.actorOf(SharedFlowManager.props(actorTransmitter))
  private val dataGenerators = drivers.map { driver => system.actorOf(TruckAndTrafficGenerator.props(driver, depot, flowManager)) }
  private val coordinator = system.actorOf(AutomaticCoordinator.props(combinedConfig.getInt("simulator.auto-finish.event-count"), dataGenerators, flowManager))

  // Watch for when the AutoCoordinator terminates (signals it's done)
  inbox.watch(coordinator)

  // Ensure that the system is properly terminated when the simulator is shutdown.
  scala.sys.addShutdownHook { stop() }

  /**
    * Manually stop the simulation, terminating the underlying system.
    *
    * @param timeout Time to wait for the system to terminate gracefully, in milliseconds (default: 5000 milliseconds).
    */
  def stop(timeout: Int = 5000): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, timeout.milliseconds)
  }

  /**
    * Private helper Actor
    */
  private class EnrichmentActor extends Actor {
    def receive = {
      case td: TruckData =>
        kafkaTruckTransmitter ! DataTransmitter.Transmit(
          EnrichedTruckData(td,
            WeatherAPI.default.getFog(td.eventType),
            WeatherAPI.default.getRain(td.eventType),
            WeatherAPI.default.getWind(td.eventType))
        )
      case td: TrafficData =>
        kafkaTrafficTransmitter ! DataTransmitter.Transmit(td)
    }
  }
}

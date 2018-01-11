package com.orendainx.trucking.simulator.simulators

import akka.actor.{ActorSystem, Inbox}
import better.files.File
import com.orendainx.trucking.simulator.coordinators.AutomaticCoordinator
import com.orendainx.trucking.simulator.depots.NoSharingDepot
import com.orendainx.trucking.simulator.flows.SharedFlowManager
import com.orendainx.trucking.simulator.generators.TruckAndTrafficGenerator
import com.orendainx.trucking.simulator.services.DriverFactory
import com.orendainx.trucking.simulator.transmitters.FileTransmitter
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * This simulator begins processing data as soon as it is created.  Generated data is flushed to the filesystem.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object AutoFinishSimulator {
  def main(args: Array[String]): Unit = {
    if (args.length > 0) new AutoFinishSimulator(ConfigFactory.parseFile(File(args(0)).toJava))
    else new AutoFinishSimulator()
  }
}

class AutoFinishSimulator(val config: Config) extends Simulator {

  def this() = this(ConfigFactory.load())

  private implicit val combinedConfig: Config = ConfigFactory.load(config).getConfig("trucking-simulator")

  private val system = ActorSystem("AutoFinishSimulator")

  // Generate the drivers to be used in the simulation and create an Inbox for accepting messages
  private val drivers = DriverFactory.drivers
  private val inbox = Inbox.create(system)

  // Generate the different actors in the simulation
  private val depot = system.actorOf(NoSharingDepot.props())
  private val fileTransmitter = system.actorOf(FileTransmitter.props(combinedConfig.getString("simulator.auto-finish.output-filepath")))
  private val flowManager = system.actorOf(SharedFlowManager.props(fileTransmitter))
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
}

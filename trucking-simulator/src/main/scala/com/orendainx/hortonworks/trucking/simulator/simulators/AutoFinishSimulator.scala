package com.orendainx.hortonworks.trucking.simulator.simulators

import akka.actor.{ActorSystem, Inbox, Terminated}
import com.orendainx.hortonworks.trucking.simulator.coordinators.AutomaticCoordinator
import com.orendainx.hortonworks.trucking.simulator.depots.NoSharingDepot
import com.orendainx.hortonworks.trucking.simulator.flows.{FlowManager, SharedFlowManager}
import com.orendainx.hortonworks.trucking.simulator.generators.TruckAndTrafficGenerator
import com.orendainx.hortonworks.trucking.simulator.services.DriverFactory
import com.orendainx.hortonworks.trucking.simulator.transmitters.FileTransmitter
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * This simulator begins processing data as soon as it is created.  Generated data is flushed to the filesystem.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object AutoFinishSimulator {
  def apply() = new AutoFinishSimulator()
}

class AutoFinishSimulator extends Simulator {

  private implicit val config = ConfigFactory.load()
  private val system = ActorSystem("AutoFinishSimulator")

  // Generate the drivers to be used in the simulation and create an Inbox for accepting messages
  private val drivers = DriverFactory.drivers
  private val inbox = Inbox.create(system)

  // Generate the different actors in the simulation
  private val depot = system.actorOf(NoSharingDepot.props())
  private val transmitter = system.actorOf(FileTransmitter.props(config.getString("simulator.auto-finish.output-filepath")))
  private val flowManager = system.actorOf(SharedFlowManager.props(transmitter))
  private val dataGenerators = drivers.map { driver => system.actorOf(TruckAndTrafficGenerator.props(driver, depot, flowManager)) }
  private val coordinator = system.actorOf(AutomaticCoordinator.props(config.getInt("simulator.auto-finish.event-count"), dataGenerators, flowManager))

  // Watch for when the AutoCoordinator terminates (signals it's done)
  inbox.watch(coordinator)

  // Ensure that the system is properly terminated when the simulator is shutdown.
  scala.sys.addShutdownHook { stop() }

  /**
    * Check to see if the simulation has completed.  On completion, the system is terminated.
    */
  def checkFinished(): Unit = {
    inbox.receive(1.millisecond) match {
      case Terminated(`coordinator`) =>
        flowManager ! FlowManager.ShutdownFlow
        inbox.watch(flowManager)
      case Terminated(`flowManager`) =>
        stop()
    }
  }

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

package com.hortonworks.orendainx.trucking.simulator

import akka.actor.{ActorSystem, Inbox, Terminated}
import com.hortonworks.orendainx.trucking.simulator.coordinators.AutomaticCoordinator
import com.hortonworks.orendainx.trucking.simulator.depots.NoSharingDepot
import com.hortonworks.orendainx.trucking.simulator.flows.{FlowManager, SharedFlowManager}
import com.hortonworks.orendainx.trucking.simulator.generators.TruckAndTrafficGenerator
import com.hortonworks.orendainx.trucking.simulator.models.{Driver, DrivingPattern}
import com.hortonworks.orendainx.trucking.simulator.transmitters.FileTransmitter
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

/**
  * This simulator begins processing data as soon as it is created.  Generated data is flushed to the filesystem.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object AutoFinishSimulator {
  def apply() = new AutoFinishSimulator()
}

class AutoFinishSimulator {

  private implicit val config = ConfigFactory.load()
  private val system = ActorSystem("AutoFinishSimulator")

  // Generate the actors to be used in the simulation and create an Inbox for accepting messages
  private val drivers = generateDrivers()
  private val inbox = Inbox.create(system)

  // Generate the different actors in the simulation
  private val depot = system.actorOf(NoSharingDepot.props())
  private val transmitter = system.actorOf(FileTransmitter.props(config.getString("options.filetransmitter.filepath")))
  private val flowManager = system.actorOf(SharedFlowManager.props(transmitter))
  private val dataGenerators = drivers.map { driver => system.actorOf(TruckAndTrafficGenerator.props(driver, depot, flowManager)) }
  private val coordinator = system.actorOf(AutomaticCoordinator.props(dataGenerators, flowManager))

  // Watch for when the AutoCoordinator terminates (signals it's done)
  inbox.watch(coordinator)

  // Ensure that the system is properly terminated when the simulator is shutdown.
  scala.sys.addShutdownHook {
    stop()
  }

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
    * Stop the simulation, terminating the underlying system.
    *
    * @param timeout Time to wait for the system to terminate, in milliseconds (default: 10000 milliseconds).
    */
  def stop(timeout: Int = 10000): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, timeout.milliseconds)
  }

  /**
    * Generate a list of drivers using values from this class's [[com.typesafe.config.Config]] object.
    */
  private def generateDrivers() = {
    // TODO: clean up config abstractions
    // This assumes that special-drivers have sequential ids starting at 1

    // Generate driving patterns
    val patterns = config.getConfigList("simulator.driving-patterns").map { conf =>
      val name = conf.getString("name")
      (name, DrivingPattern(name, conf.getInt("min-speed"), conf.getInt("max-speed"), conf.getInt("risk-frequency")))
    }.toMap

    // First, initialize all special drivers
    val specialDrivers = config.getConfigList("simulator.special-drivers").map { conf =>
      Driver(conf.getInt("id"), conf.getString("name"), patterns(conf.getString("pattern")))
    }

    // If we need more drivers, generate "normal" drivers. Or if we need to remove some special drivers, do so.
    val driverCount = config.getInt("options.driver-count")
    if (specialDrivers.length < driverCount) {
      val newDrivers = ((specialDrivers.length+1) to driverCount).map { newId =>
        val randomDriverName = Random.alphanumeric.take(config.getInt("simulator.driver-name-length")).mkString
        Driver(newId, randomDriverName, patterns("normal"))
      }
      specialDrivers ++ newDrivers
    } else
      specialDrivers.take(driverCount)
  }
}

package com.hortonworks.orendainx.trucking.simulator.generators

import java.sql.Timestamp
import java.time.Instant
import java.util.Date

import akka.actor.{ActorLogging, ActorRef, Props, Stash}
import com.hortonworks.orendainx.trucking.shared.models._
import com.hortonworks.orendainx.trucking.simulator.coordinators.GeneratorCoordinator
import com.hortonworks.orendainx.trucking.simulator.depots.ResourceDepot.{RequestRoute, ReturnRoute}
import com.hortonworks.orendainx.trucking.simulator.generators.DataGenerator.{GenerateData, NewResource}
import com.hortonworks.orendainx.trucking.simulator.models._
import com.hortonworks.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit
import com.typesafe.config.Config

import scala.util.Random

/**
  * TrafficGenerator generates a stream of [[TrafficData]] events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TrafficGenerator {

  /**
    *
    * @param depot ActorRef to a [[com.hortonworks.orendainx.trucking.simulator.depots.ResourceDepot]]
    * @param flowManager ActorRef to a [[com.hortonworks.orendainx.trucking.simulator.flows.FlowManager]]
    * @param config TODO
    * @return
    */
  def props(depot: ActorRef, flowManager: ActorRef)(implicit config: Config) =
    Props(new TrafficGenerator(depot, flowManager))
}

class TrafficGenerator(depot: ActorRef, flowManager: ActorRef)(implicit config: Config) extends DataGenerator with Stash with ActorLogging {

  val NumberOfRoutes = 5 // TODO: hardcoded for demo's sake
  var congestionLevel = 50 // TODO: hardcoded starting point for demo's sake
  var routes = Seq.empty[Route].toBuffer

  // Request NumberOfRoutes routes
  (1 to NumberOfRoutes).foreach(_ => depot ! RequestRoute(EmptyRoute))

  context become waitingOnDepot

  def waitingOnDepot: Receive = {
    case NewResource(newRoute: Route) =>
      routes += newRoute
      unstashAll()
      context become driverActive
      log.info(s"Received new route: ${newRoute.name}")

    case GenerateData =>
      stash()
      log.debug("Received Tick command while waiting on route. Command stashed for later processing.")
  }

  def driverActive: Receive = {
    case GenerateData =>
      routes.foreach { route =>
        // Create traffic data and emit it
        congestionLevel += Random.nextInt(11) - 5 // -5 to 5
        val traffic = TrafficData(Instant.now().toEpochMilli, route.id, congestionLevel)
        flowManager ! Transmit(traffic)
      }

      // Tell the coordinator we've acknowledged the drive command
      sender() ! GeneratorCoordinator.AcknowledgeTick(self)
  }

  def receive = {
    case _ => log.error("This message should never be seen.")
  }

  // When this actor is stopped, release resources it may still be holding onto
  override def postStop(): Unit =
    routes.foreach(ReturnRoute)
}

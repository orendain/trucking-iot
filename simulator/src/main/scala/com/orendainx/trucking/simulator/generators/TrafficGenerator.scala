package com.orendainx.trucking.simulator.generators

import java.time.Instant

import akka.actor.{ActorLogging, ActorRef, Props, Stash}
import com.orendainx.trucking.commons.models._
import com.orendainx.trucking.simulator.coordinators.GeneratorCoordinator
import com.orendainx.trucking.simulator.depots.ResourceDepot.{RequestRoute, ReturnRoute}
import com.orendainx.trucking.simulator.generators.DataGenerator.{GenerateData, NewResource}
import com.orendainx.trucking.simulator.models._
import com.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit
import com.orendainx.trucking.simulator.models.{EmptyRoute, Route}
import com.typesafe.config.Config

import scala.collection.mutable
import scala.util.Random

/**
  * TrafficGenerator generates a stream of [[TrafficData]] events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TrafficGenerator {

  /**
    *
    * @param depot ActorRef to a [[com.orendainx.trucking.simulator.depots.ResourceDepot]]
    * @param flowManager ActorRef to a [[com.orendainx.trucking.simulator.flows.FlowManager]]
    * @return
    */
  def props(depot: ActorRef, flowManager: ActorRef)(implicit config: Config) =
    Props(new TrafficGenerator(depot, flowManager))
}

class TrafficGenerator(depot: ActorRef, flowManager: ActorRef)(implicit config: Config) extends DataGenerator with Stash with ActorLogging {

  // Some settings
  val NumberOfRoutes = config.getInt("generator.routes-to-simulate")
  val CongestionDelta = config.getInt("generator.congestion.delta")

  var congestionLevel = config.getInt("generator.congestion.start")
  var routes = mutable.Buffer.empty[Route]

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
        congestionLevel += -CongestionDelta + Random.nextInt(CongestionDelta*2 + 1)
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

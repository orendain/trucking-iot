package com.orendainx.hortonworks.trucking.simulator.generators

import java.sql.Timestamp
import java.time.Instant
import java.util.Date

import akka.actor.{ActorLogging, ActorRef, Props, Stash}
import com.orendainx.hortonworks.trucking.common.models.{TrafficData, TruckData, TruckEventTypes}
import com.orendainx.hortonworks.trucking.simulator.coordinators.GeneratorCoordinator
import com.orendainx.hortonworks.trucking.simulator.depots.ResourceDepot.{RequestRoute, RequestTruck, ReturnRoute, ReturnTruck}
import com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator.{GenerateData, NewResource}
import com.orendainx.hortonworks.trucking.simulator.models._
import com.orendainx.hortonworks.trucking.simulator.transmitters.DataTransmitter.Transmit
import com.typesafe.config.Config

import scala.util.Random

/**
  * TruckAndTrafficGenerator generates two types of data: [[TruckData]] and [[TrafficData]] and transmits to the
  * specified [[com.orendainx.hortonworks.trucking.simulator.flows.FlowManager]].
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckAndTrafficGenerator {

  /**
    *
    * @param driver The [[Driver]] driving the truck.
    * @param depot ActorRef to a [[com.orendainx.hortonworks.trucking.simulator.depots.ResourceDepot]]
    * @param flowManager ActorRef to a [[com.orendainx.hortonworks.trucking.simulator.flows.FlowManager]]
    * @return
    */
  def props(driver: Driver, depot: ActorRef, flowManager: ActorRef)(implicit config: Config) =
    Props(new TruckAndTrafficGenerator(driver, depot, flowManager))
}

class TruckAndTrafficGenerator(driver: Driver, depot: ActorRef, flowManager: ActorRef)(implicit config: Config) extends DataGenerator with Stash with ActorLogging {

  // Some settings
  val SpeedDelta = config.getInt("generator.speed-delta")
  val SpeedingThreshold = config.getInt("generator.speeding-threshold")
  val MaxRouteCompletedCount = config.getInt("generator.max-route-completed-count")
  val CongestionDelta = config.getInt("generator.congestion.delta")
  val TrafficDataFrequency = config.getInt("generator.traffic-data-frequency")

  // Truck and route being used, locations this driving agent has driven to and congestion level
  var truck: Truck = EmptyTruck
  var route: Route = EmptyRoute
  var locations = List.empty[Location]
  var locationsRemaining = locations.iterator

  // Counters and congestion
  var currentSpeed = (driver.drivingPattern.minSpeed + driver.drivingPattern.maxSpeed)/2
  var spreeRemaining = 0
  var tickCount = 0
  var routeCompletedCount = 0
  var congestionLevel = config.getInt("generator.congestion.start")

  // Query depot for a route and a truck
  depot ! RequestRoute(route)
  depot ! RequestTruck(truck)
  context become waitingOnDepot

  def waitingOnDepot: Receive = {
    case NewResource(newTruck: Truck) =>
      truck = newTruck
      considerDriving()
      log.info(s"Driver (${driver.id}, ${driver.name}) received new truck with id ${newTruck.id}")

    case NewResource(newRoute: Route) =>
      route = newRoute
      locations = route.locations
      locationsRemaining = locations.iterator
      routeCompletedCount = 0
      considerDriving()
      log.info(s"Driver (${driver.id}, ${driver.name}) received new route: ${newRoute.name}")

    case GenerateData =>
      stash()
      log.debug("Received Tick command while waiting on resources. Command stashed for later processing.")
  }

  def driverActive: Receive = {
    case GenerateData =>
      tickCount += 1
      log.debug(s"Driver #${driver.id} processing event #$tickCount")

      val currentLoc = locationsRemaining.next()
      currentSpeed += (Random.nextInt(3) - 1) * SpeedDelta

      if (startSpreeCheck) {
        spreeRemaining = driver.drivingPattern.spreeLength
      }

      // If currently spreeing, generate an appropriate event
      val eventType =
        if (spreeRemaining > 0) {
          spreeRemaining -= 1
          if (Random.nextInt(100) < driver.drivingPattern.violationPercentage)
            TruckEventTypes.NonNormalTypes(Random.nextInt(TruckEventTypes.NonNormalTypes.length))
          else
            TruckEventTypes.Normal
        } else
          TruckEventTypes.Normal


      // Create trucking event and transmit it
      val eventTime = Instant.now().toEpochMilli
      val event = TruckData(eventTime, truck.id, driver.id, driver.name,
        route.id, route.name, currentLoc.latitude, currentLoc.longitude, currentSpeed, eventType)
      flowManager ! Transmit(event)

      // Create traffic data and emit it
      if (tickCount % TrafficDataFrequency == 0) {
        congestionLevel += -CongestionDelta + Random.nextInt(CongestionDelta * 2 + 1)
        val traffic = TrafficData(eventTime, route.id, congestionLevel)
        flowManager ! Transmit(traffic)
      }

      // If driver completed the route, switch trucks
      if (locationsRemaining.isEmpty) {
        depot ! ReturnTruck(truck)
        depot ! RequestTruck(truck)
        truck = EmptyTruck

        // If route traveled enough times, switch routes
        routeCompletedCount += 1
        if (routeCompletedCount > MaxRouteCompletedCount) {
          depot ! ReturnRoute(route)
          depot ! RequestRoute(route)
          route = EmptyRoute
        } else {
          // Else, turn around and travel in the opposite direction
          locations = locations.reverse
          locationsRemaining = locations.iterator
        }

        log.debug("Changing context to waitingOnDepot")
        context become waitingOnDepot
      }

      // Tell the coordinator we've acknowledged the tick message
      sender() ! GeneratorCoordinator.AcknowledgeTick(self)
  }

  def receive = {
    case _ => log.error("This message should never be seen.")
  }

  def startSpreeCheck = tickCount % driver.drivingPattern.spreeFrequency == 0

  //When waiting for resources, make sure we have both a truck and a route before driving
  def considerDriving(): Unit = {
    if (truck != EmptyTruck && route != EmptyRoute) {
      unstashAll()
      context become driverActive
    }
  }

  // When this actor is stopped, release resources it may still be holding onto
  override def postStop(): Unit = {
    if (truck != EmptyTruck) depot ! ReturnTruck(truck)
    if (route != EmptyRoute) depot ! ReturnRoute(route)
  }
}

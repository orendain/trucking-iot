package com.hortonworks.orendainx.trucking.simulator.generators

import java.sql.Timestamp
import java.time.Instant
import java.util.Date

import akka.actor.{ActorLogging, ActorRef, Props, Stash}
import com.hortonworks.orendainx.trucking.shared.models.{TrafficData, TruckData, TruckDataTypes}
import com.hortonworks.orendainx.trucking.simulator.coordinators.GeneratorCoordinator
import com.hortonworks.orendainx.trucking.simulator.depots.ResourceDepot.{RequestRoute, RequestTruck, ReturnRoute, ReturnTruck}
import com.hortonworks.orendainx.trucking.simulator.generators.DataGenerator.{GenerateData, NewResource}
import com.hortonworks.orendainx.trucking.simulator.models._
import com.hortonworks.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit
import com.typesafe.config.Config

import scala.util.Random

/**
  * TruckAndTrafficGenerator generates two types of data: [[TruckData]] and [[TrafficData]] and transmits to the
  * specified [[com.hortonworks.orendainx.trucking.simulator.flows.FlowManager]].
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TruckAndTrafficGenerator {

  /**
    *
    * @param driver The [[Driver]] driving the truck.
    * @param depot ActorRef to a [[com.hortonworks.orendainx.trucking.simulator.depots.ResourceDepot]]
    * @param flowManager ActorRef to a [[com.hortonworks.orendainx.trucking.simulator.flows.FlowManager]]
    * @param config TODO
    * @return
    */
  def props(driver: Driver, depot: ActorRef, flowManager: ActorRef)(implicit config: Config) =
    Props(new TruckAndTrafficGenerator(driver, depot, flowManager))
}

class TruckAndTrafficGenerator(driver: Driver, depot: ActorRef, flowManager: ActorRef)(implicit config: Config) extends DataGenerator with Stash with ActorLogging {

  val SpeedingThreshold = config.getInt("simulator.speeding-threshold")
  val MaxRouteCompletedCount = config.getInt("simulator.max-route-completed-count")

  // Truck and route being used, locations this driving agent has driven to and congestion level
  var truck: Truck = EmptyTruck
  var route: Route = EmptyRoute
  var locations = List.empty[Location]
  var locationsRemaining = locations.iterator
  var congestionLevel = 50 // TODO: hardcoded to start at 50 for now, abstract out to config

  // Counters
  var driveCount = 0
  var routeCompletedCount = 0

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
      driveCount += 1
      log.debug(s"Driver #${driver.id} processing event #$driveCount")

      val currentLoc = locationsRemaining.next()
      val speed =
        driver.drivingPattern.minSpeed + Random.nextInt(driver.drivingPattern.maxSpeed - driver.drivingPattern.minSpeed + 1)

      // If driver is speeding or is set to trigger risky behavior, generate an appropriate TruckDataType
      val eventType =
        if (speed >= SpeedingThreshold || driveCount % driver.drivingPattern.riskFrequency == 0)
          TruckDataTypes.NonNormalTypes(Random.nextInt(TruckDataTypes.NonNormalTypes.length))
        else
          TruckDataTypes.Normal

      // Create trucking event and transmit it
      val eventTime = Instant.now().toEpochMilli
      val event = TruckData(eventTime, truck.id, driver.id, driver.name,
        route.id, route.name, currentLoc.latitude, currentLoc.longitude, speed, eventType)
      flowManager ! Transmit(event)

      // Create traffic data and emit it
      congestionLevel += Random.nextInt(11) - 5 // -5 to 5
      val traffic = TrafficData(eventTime, route.id, congestionLevel)
      flowManager ! Transmit(traffic)

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

package com.hortonworks.orendainx.trucking.simulator.depots

import akka.actor.{ActorLogging, Props, Stash}
import com.hortonworks.orendainx.trucking.simulator.depots.ResourceDepot.{RequestRoute, RequestTruck, ReturnRoute, ReturnTruck}
import com.hortonworks.orendainx.trucking.simulator.generators.DataGenerator.NewResource
import com.hortonworks.orendainx.trucking.simulator.models._
import com.hortonworks.orendainx.trucking.simulator.services.RouteParser
import com.typesafe.config.Config

import scala.util.Random

/**
  * This implementation of a [[ResourceDepot]] disallows [[com.hortonworks.orendainx.trucking.simulator.generators.DataGenerator]]
  * objects from sharing resources.  That is, any resource managed by this depot can only be checked out by a single generator.
  *
  * Sending a [[ResourceDepot.RequestRoute]] or [[ResourceDepot.RequestTruck]] message will return a new resource (that is different
  * than the one specified as an argument in that message) as soon as one is available.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object NoSharingDepot {

  def props()(implicit config: Config) =
    Props(new NoSharingDepot())
}

class NoSharingDepot(implicit config: Config) extends ResourceDepot with Stash with ActorLogging {

  private val trucksAvailable = Random.shuffle(1 to config.getInt("simulator.trucks-available")).toList.map(Truck).toBuffer
  private val routesAvailable = RouteParser(config.getString("options.route-directory")).routes.toBuffer

  log.info("Trucks and routes initialized and ready for deployment")
  log.info(s"${trucksAvailable.length} trucks available.")
  log.info(s"${routesAvailable.length} routes available.")

  def receive = {
    case RequestTruck(previous) if previous != EmptyTruck =>
      val ind = trucksAvailable.indexWhere(_ != previous)
      if (ind >= 0) sender() ! NewResource(trucksAvailable.remove(ind))
      else stash() // None available, stash request for later

    case RequestTruck(_) =>
      if (trucksAvailable.nonEmpty) sender() ! NewResource(trucksAvailable.remove(0))
      else stash()

    case RequestRoute(previous) if previous != EmptyRoute =>
      val ind = routesAvailable.indexWhere(_ != previous)
      if (ind >= 0) sender() ! NewResource(routesAvailable.remove(ind))
      else stash()

    case RequestRoute(_) =>
      if (routesAvailable.nonEmpty) sender() ! NewResource(routesAvailable.remove(0))
      else stash()

    case ReturnTruck(truck) =>
      trucksAvailable.append(truck)
      unstashAll()

    case ReturnRoute(route) =>
      routesAvailable.append(route)
      unstashAll()
  }
}

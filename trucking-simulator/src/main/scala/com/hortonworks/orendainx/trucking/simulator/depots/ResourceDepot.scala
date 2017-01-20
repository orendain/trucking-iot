package com.hortonworks.orendainx.trucking.simulator.depots

import akka.actor.Actor
import com.hortonworks.orendainx.trucking.simulator.models.{Route, Truck}

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object ResourceDepot {
  // TODO: can generalize into RequestResource or ReturnResource
  case class RequestTruck(previous: Truck)
  case class RequestRoute(previous: Route)

  case class ReturnTruck(truck: Truck)
  case class ReturnRoute(route: Route)
}

trait ResourceDepot extends Actor

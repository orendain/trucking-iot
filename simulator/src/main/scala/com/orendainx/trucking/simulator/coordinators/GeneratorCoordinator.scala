package com.orendainx.trucking.simulator.coordinators

import akka.actor.{Actor, ActorRef}

/**
  * [[GeneratorCoordinator]] objects coordinate [[com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator]]
  * objects and how they generate data.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object GeneratorCoordinator {
  case class AcknowledgeTick(generator: ActorRef)
}

trait GeneratorCoordinator extends Actor

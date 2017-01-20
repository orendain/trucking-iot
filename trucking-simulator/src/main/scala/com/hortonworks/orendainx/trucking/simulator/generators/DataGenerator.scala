package com.hortonworks.orendainx.trucking.simulator.generators

import akka.actor.Actor

/**
  * [[DataGenerator]]s are responsible for generating simulation data.
  * They should act on [[com.hortonworks.orendainx.trucking.simulator.generators.DataGenerator.GenerateData]] messages
  * from [[com.hortonworks.orendainx.trucking.simulator.coordinators.GeneratorCoordinator]]s and send back an
  * [[com.hortonworks.orendainx.trucking.simulator.coordinators.GeneratorCoordinator.AcknowledgeTick]] message when the
  * generator is ready to generate more data.
  *
  * Generated data should be sent to a [[com.hortonworks.orendainx.trucking.simulator.flows.FlowManager]] for
  * proper handling.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DataGenerator {
  case object GenerateData

  // TODO: Ungenericize arg?  Would require creating base type of Resource for Truck/Route
  case class NewResource(resource: Any)
}

trait DataGenerator extends Actor

package com.orendainx.trucking.simulator.generators

import akka.actor.Actor
import com.orendainx.trucking.simulator.models.Resource

/**
  * [[DataGenerator]]s are responsible for generating simulation data.
  * They should act on [[com.orendainx.trucking.simulator.generators.DataGenerator.GenerateData]] messages
  * from [[com.orendainx.trucking.simulator.coordinators.GeneratorCoordinator]]s and send back an
  * [[com.orendainx.trucking.simulator.coordinators.GeneratorCoordinator.AcknowledgeTick]] message when the
  * generator is ready to generate more data.
  *
  * Generated data should be sent to a [[com.orendainx.trucking.simulator.flows.FlowManager]] for
  * proper handling.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DataGenerator {
  case object GenerateData

  case class NewResource(resource: Resource)
}

trait DataGenerator extends Actor

package com.orendainx.trucking.simulator.flows

import akka.actor.Actor

/**
  * All implementations of this class should initiate termination of the Akka Actor System upon processing a
  * [[FlowManager.ShutdownFlow]] message.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object FlowManager {
  case object ShutdownFlow
}

trait FlowManager extends Actor

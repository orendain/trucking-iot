package com.hortonworks.orendainx.trucking.simulator.flows

import akka.actor.Actor

/**
  * All implementations of this class should initiate termination of the Akka Actor System upon processing a
  * [[FlowManager.ShutdownFlow]] message.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object FlowManager {
  // TODO: rather than this triggering a system termination, simply kill the flow and have supervising actors respond accordingly?
  case object ShutdownFlow
}

trait FlowManager extends Actor

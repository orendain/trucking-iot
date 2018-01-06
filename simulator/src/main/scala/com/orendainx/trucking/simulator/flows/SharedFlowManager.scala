package com.orendainx.trucking.simulator.flows

import akka.actor.{ActorRef, PoisonPill, Props, Terminated}
import com.orendainx.trucking.simulator.flows.FlowManager.ShutdownFlow
import com.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit

/**
  * The SharedFlowManager routes all [[com.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit]] messages to a single [[com.orendainx.trucking.simulator.transmitters.DataTransmitter]]
  * specified when constructing a Props via [[SharedFlowManager.props]].
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object SharedFlowManager {
  def props(transmitter: ActorRef) =
    Props(new SharedFlowManager(transmitter))
}
class SharedFlowManager(transmitter: ActorRef) extends FlowManager {

  def receive = {
    case msg: Transmit => transmitter ! msg

    case ShutdownFlow =>
      transmitter ! PoisonPill
      context watch transmitter

    case Terminated(`transmitter`) =>
      context stop self
  }
}

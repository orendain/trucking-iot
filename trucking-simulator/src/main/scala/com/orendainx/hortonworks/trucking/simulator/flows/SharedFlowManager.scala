package com.orendainx.hortonworks.trucking.simulator.flows

import akka.actor.{ActorRef, PoisonPill, Props, Terminated}
import com.orendainx.hortonworks.trucking.simulator.flows.FlowManager.ShutdownFlow
import com.orendainx.hortonworks.trucking.simulator.transmitters.DataTransmitter.Transmit

/**
  * The SharedFlowManager routes all [[Transmit]] messages to a single [[com.orendainx.hortonworks.trucking.simulator.transmitters.DataTransmitter]]
  * specified when constructing a Props via [[SharedFlowManager.props()]].
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
      context.watch(transmitter)

    case Terminated(`transmitter`) =>
      context.system.terminate()
  }
}

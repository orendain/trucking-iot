package com.orendainx.hortonworks.trucking.simulator.transmitters

import akka.actor.{ActorLogging, Props}
import com.orendainx.hortonworks.trucking.common.models.TruckingData
import com.orendainx.hortonworks.trucking.simulator.transmitters.AccumulateTransmitter.Fetch
import com.orendainx.hortonworks.trucking.simulator.transmitters.DataTransmitter.Transmit

import scala.collection.mutable

/**
  * AccumulateTransmitter stores data to be polled by an outside source.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object AccumulateTransmitter {
  case object Fetch

  def props() = Props(new AccumulateTransmitter)
}

class AccumulateTransmitter extends DataTransmitter with ActorLogging {

  val buffer = mutable.ListBuffer.empty[TruckingData]

  def receive = {
    case Transmit(data) =>
      buffer += data
      log.debug(s"Data received: buffered ${buffer.size}")

    case Fetch =>
      sender() ! buffer.toList
      log.debug(s"Sent ${buffer.size} data. ${buffer.toString()}")
      buffer.clear()
  }

  override def postStop(): Unit = {
    log.info(s"AccumulateTransmitter stopped with ${buffer.length} items unfetched.")
  }
}

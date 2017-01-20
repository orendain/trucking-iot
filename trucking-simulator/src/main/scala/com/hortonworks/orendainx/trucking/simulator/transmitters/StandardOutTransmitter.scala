package com.hortonworks.orendainx.trucking.simulator.transmitters

import akka.actor.Props
import com.hortonworks.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit

/**
  * StandardOutTransmitter records data to standard output.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object StandardOutTransmitter {
  def props() = Props(new StandardOutTransmitter)
}

class StandardOutTransmitter extends DataTransmitter {

  def receive = {
    case Transmit(data) => println(data.toCSV)
  }

}

package com.orendainx.hortonworks.trucking.topology.bolts

import com.typesafe.scalalogging.Logger
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.windowing.TupleWindow

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class EventWindowingBolt extends BaseWindowedBolt {

  private lazy val log = Logger(this.getClass)

  override def execute(inputWindow: TupleWindow): Unit = {
  }

}

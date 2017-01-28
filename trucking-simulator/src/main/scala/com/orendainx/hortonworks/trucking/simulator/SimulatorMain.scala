package com.orendainx.hortonworks.trucking.simulator

import com.orendainx.hortonworks.trucking.simulator.simulators.{AutoFinishSimulator, ManualTickAndFetchSimulator}

/**
  * Main entry point.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object SimulatorMain {

  def main(args: Array[String]): Unit = {
    runAutoSimulator()
    //runTickAndFetchSimulator()
  }

  def runAutoSimulator(): Unit = {
    val simulator = AutoFinishSimulator()
    //simulator.checkFinished()
  }

  def runTickAndFetchSimulator(): Unit = {
    val simulator = ManualTickAndFetchSimulator()
    simulator.tick()
    simulator.fetch()
  }
}

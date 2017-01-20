package com.hortonworks.orendainx.trucking.simulator

/**
  * Entry point for the simulator.
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
    val simulator = TickAndFetchSimulator()
    simulator.tick()
    simulator.fetch()
  }
}

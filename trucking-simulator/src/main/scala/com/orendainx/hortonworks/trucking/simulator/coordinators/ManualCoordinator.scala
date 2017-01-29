package com.orendainx.hortonworks.trucking.simulator.coordinators

import akka.actor.{ActorLogging, ActorRef, Props}
import com.orendainx.hortonworks.trucking.simulator.coordinators.GeneratorCoordinator.AcknowledgeTick
import com.orendainx.hortonworks.trucking.simulator.coordinators.ManualCoordinator.Tick
import com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator
import com.typesafe.config.Config

import scala.collection.mutable

/**
  * The ManualCoordinator waits for a [[ManualCoordinator.Tick]] message to tell the generators
  * it coordinates to generate data.  [[DataGenerator]] s that process and acknowledge these ticks are
  * requeued for another round of data generation, which happens on the next [[ManualCoordinator.Tick]].
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object ManualCoordinator {
  case object Tick

  /**
    *
    * @param generators A Seq of ActorRef referring to instances of [[DataGenerator]]s.
    * @return A Props for a new [[AutomaticCoordinator]]
    */
  def props(generators: Seq[ActorRef])(implicit config: Config) =
    Props(new ManualCoordinator(generators))
}

class ManualCoordinator(generators: Seq[ActorRef])(implicit config: Config) extends GeneratorCoordinator with ActorLogging {

  // Set all generators as ready
  val generatorsReady = mutable.Set(generators: _*)

  def receive = {
    case AcknowledgeTick(generator) =>
      generatorsReady += generator
      log.debug(s"Generator acknowledged tick - total ready: ${generatorsReady.size}")

    case Tick =>
      generatorsReady.foreach(_ ! DataGenerator.GenerateData)
      generatorsReady.clear()
  }

}

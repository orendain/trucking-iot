package com.orendainx.trucking.simulator.coordinators

import akka.actor.{ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import com.orendainx.hortonworks.trucking.simulator.coordinators.AutomaticCoordinator.TickGenerator
import com.orendainx.hortonworks.trucking.simulator.coordinators.GeneratorCoordinator.AcknowledgeTick
import com.orendainx.hortonworks.trucking.simulator.flows.FlowManager
import com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator
import com.typesafe.config.Config

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Random

/**
  * The AutomaticCoordinator automatically ticks the [[com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator]] actors it gets passed in
  * until some event count has been met.  This works by having each [[com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator]] acknowledge
  * that it has processed a tick and is ready for another.
  *
  * Once all [[com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator]] objects have ticked a certain number of times, this coordinator ends itself.
  * Thus, knowing when this job finished is simply a matter of DeathWatching this actor.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object AutomaticCoordinator {
  case class TickGenerator(generator: ActorRef)

  /**
    *
    * @param generators A Seq of ActorRef referring to instances of [[com.orendainx.hortonworks.trucking.simulator.generators.DataGenerator]]s.
    * @param flowManager ActorRef to the instance of a [[com.orendainx.hortonworks.trucking.simulator.flows.FlowManager]] actor that handles data flow/transmission.
    * @return A Props for a new [[AutomaticCoordinator]]
    */
  def props(eventCount: Int, generators: Seq[ActorRef], flowManager: ActorRef)(implicit config: Config) =
    Props(new AutomaticCoordinator(eventCount, generators, flowManager))
}

class AutomaticCoordinator(eventCount: Int, generators: Seq[ActorRef], flowManager: ActorRef)(implicit config: Config) extends GeneratorCoordinator with ActorLogging {

  // For receive messages and an execution context
  import context.dispatcher

  // Event delay settings, and initialize a counter for each data generator
  val eventDelay = config.getInt("generator.event-delay")
  val eventDelayJitter = config.getInt("generator.event-delay-jitter")
  val generateCounters = mutable.Map(generators.map((_, 0)): _*)

  // Insert each new generator into the simulation (at a random scheduled point) and begin "ticking"
  generators.foreach { generator =>
    context.system.scheduler.scheduleOnce(Random.nextInt(eventDelay + eventDelayJitter).milliseconds, self, TickGenerator(generator))
  }

  def receive = {
    case AcknowledgeTick(generator) =>
      self ! TickGenerator(generator) // Each ack triggers another tick

    case TickGenerator(generator) =>
      generateCounters.update(generator, generateCounters(generator)+1)

      if (generateCounters(generator) <= eventCount) {
        context.system.scheduler.scheduleOnce((eventDelay + Random.nextInt(eventDelayJitter)).milliseconds, generator, DataGenerator.GenerateData)
      } else {
        // Kill the individual generator, since we are done with it.
        generator ! PoisonPill

        // If all other generators have met their count, tell flow manager to shutdown
        if (!generateCounters.values.exists(_ <= eventCount)) {
          flowManager ! FlowManager.ShutdownFlow
          context watch flowManager
        }
      }

    // Once the flow manager and its transmitters terminate, shut it all down
    case Terminated(`flowManager`) =>
      context.system.terminate()
  }
}

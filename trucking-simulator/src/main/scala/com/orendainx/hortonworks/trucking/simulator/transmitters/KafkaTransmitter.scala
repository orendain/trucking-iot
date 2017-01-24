package com.orendainx.hortonworks.trucking.simulator.transmitters

import akka.actor.{ActorLogging, Props}
import com.orendainx.hortonworks.trucking.simulator.transmitters.DataTransmitter.Transmit
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConverters._

/**
  * KafkaTransmitter records data to a Kafka topic.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object KafkaTransmitter {
  def props = Props(new KafkaTransmitter)
}

class KafkaTransmitter extends DataTransmitter with ActorLogging {

  // TODO: extract into config file
  private val producer = new KafkaProducer[String, String](
    Map[String, AnyRef](
      "bootstrap.servers" -> "sandbox.hortonworks.com:9092",
      "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
      "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer"
    ).asJava)

  def receive = {
    // TODO: configurable topic and key
    //case Transmit(data) => producer.send(new ProducerRecord("topic", "key", data.toCSV))
    case Transmit(data) => producer.send(new ProducerRecord("topic", data.toCSV))
  }

  override def postStop(): Unit = {
    producer.close()
    log.info("KafkaTransmitter closed its producer.")
  }
}

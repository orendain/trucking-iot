package com.orendainx.trucking.simulator.transmitters

import java.util.Properties

import akka.actor.{ActorLogging, Props}
import com.orendainx.trucking.simulator.transmitters.DataTransmitter.Transmit
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.sys.SystemProperties
import com.typesafe.config.Config

/**
  * KafkaTransmitter records data to a Kafka topic.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object KafkaTransmitter {
  def props(topic: String)(implicit config: Config) = Props(new KafkaTransmitter(topic))
}

class KafkaTransmitter(topic: String)(implicit config: Config) extends DataTransmitter with ActorLogging {

  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("transmitter.kafka.bootstrap-servers"))
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("transmitter.kafka.key-serializer"))
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("transmitter.kafka.value-serializer"))

  // Enable settings for a secure environment, if necessary.
  // See: http://docs.hortonworks.com/HDPDocuments/HDP2/HDP-2.3.4/bk_secure-kafka-ambari/content/ch_secure-kafka-produce-events.html
  val systemProperties = new SystemProperties
  if (config.getBoolean("transmitter.kafka.security-enabled")) {
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.getString("transmitter.kafka.security-protocol"))
    systemProperties.put("java.security.auth.login.config", config.getString("transmitter.kafka.jaas-file"))
  }

  private val producer = new KafkaProducer[String, String](props)

  def receive = {
    case Transmit(data) => producer.send(new ProducerRecord(topic, data.toCSV))
  }

  override def postStop(): Unit = {
    producer.close()
    log.info("KafkaTransmitter closed its producer.")
  }
}

package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import akka.stream.{Materializer, ThrottleMode}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
//@Singleton
class KafkaWebSocket @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def kafkaWS = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => KafkaWSActor.props(out))
  }

  object KafkaWSActor {
    def props(outRef: ActorRef) = Props(new KafkaWSActor(outRef))
  }

  class KafkaWSActor(outRef: ActorRef) extends Actor {

    val config = ConfigFactory.load()
    val combinedConfig = ConfigFactory.defaultOverrides()
      .withFallback(config)
      .withFallback(ConfigFactory.defaultApplication())
      .getConfig("trucking-web-application.backend")

    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      //.withBootstrapServers("sandbox-hdf.hortonworks.com:6667")
      .withBootstrapServers(combinedConfig.getString("kafka.bootstrap-servers"))
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics("trucking_data_joined"))
      .mapAsync(1) { msg => Future(outRef ! msg.record.value).map(_ => msg) }
      //.mapAsync(1) { msg => msg.committableOffset.commitScaladsl() } // TODO: Disabling commits for debug
      .throttle(1, 250.milliseconds, 1, ThrottleMode.Shaping)
      .runWith(Sink.ignore)

    def receive = {
      case msg: String => outRef ! s"Ack: $msg"
    }
  }

}

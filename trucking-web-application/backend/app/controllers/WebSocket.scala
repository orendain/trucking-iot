package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Singleton
class KafkaWebSocket @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def kafkaWS = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => KafkaWSActor.props(out))
  }

  object KafkaWSActor {
    def props(outRef: ActorRef) = Props(new KafkaWSActor(outRef))
  }

  class KafkaWSActor(outRef: ActorRef) extends Actor {
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("sandbox.hortonworks.com:6667")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics("trucking_data_joined"))
      .mapAsync(1) { msg => Future(outRef ! msg.record.value).map(_ => msg) }
      .mapAsync(1) { msg => msg.committableOffset.commitScaladsl() }
      .runWith(Sink.ignore)

    def receive = {
      case msg: String => outRef ! s"Ack: $msg"
    }
  }

}

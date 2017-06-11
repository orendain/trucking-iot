package com.orendainx.hortonworks.trucking.webapplication

import angulate2.std.Injectable
import com.orendainx.hortonworks.trucking.webapplication.models.{PrettyEnrichedTruckAndTrafficData, PrettyEnrichedTruckAndTrafficDataFactory, PrettyTruckAndTrafficData, PrettyTruckAndTrafficDataFactory}
import com.typesafe.config.ConfigFactory
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent, WebSocket}

import scala.collection.mutable

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Injectable
class WebSocketService {

  private var ws: WebSocket = _
  private var callbacks = mutable.Buffer.empty[(PrettyEnrichedTruckAndTrafficData) => _]
  initialize()

  def initialize(): Unit = {

//    val config = ConfigFactory.load()
//    val combinedConfig = config.getConfig("trucking-web-application.frontend")

//    Console.println("0")
//    Console.println(s"1: ${config}")
//    Console.println(s"2: ${config.cfg}")
//    Console.println(s"3: $combinedConfig")
//    Console.println(s"4: ${combinedConfig.cfg}")

    //ws = new WebSocket(combinedConfig.getString("websocket-uri")) // Play WS


    //ws = new WebSocket("ws://sandbox-hdf.hortonworks.com:17000/trucking-events") // NiFi WS
    ws = new WebSocket("ws://sandbox-hdf.hortonworks.com:15100/ws") // Play WS

    ws.onopen = onOpen _
    ws.onclose = onClose _
    ws.onerror = onError _
    ws.onmessage = onMessage _
  }

  def onOpen(event: Event): Unit = {
    Console.println("WebSocket opened")
  }

  def onClose(event: CloseEvent): Unit = {
    Console.println(s"WebSocket closed: ${event.code} ${event.reason}")
  }

  def onError(event: ErrorEvent): Unit = {
    Console.println(s"WebSocket error.")
  }

  def onMessage(msgEvent: MessageEvent): Unit = {
    callbacks.foreach(_(PrettyEnrichedTruckAndTrafficDataFactory(msgEvent.data.toString)))
  }

  def registerCallback(func: (PrettyEnrichedTruckAndTrafficData) => _): Unit = {
    callbacks += func
  }

}

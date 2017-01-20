package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.Injectable
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent, WebSocket}

import scala.collection.mutable

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Injectable
class WebSocketService {

  private var ws: WebSocket = _
  private var callbacks = mutable.Buffer.empty[(PrettyTruckAndTrafficData) => _]
  initialize()

  def initialize(): Unit = {
    ws = new WebSocket("ws://sandbox.hortonworks.com:8091/trucking-events")
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
    Console.println(s"WebSocket error: ${event.message}")
  }

  def onMessage(msgEvent: MessageEvent): Unit = {
    callbacks.foreach(_(PrettyTruckAndTrafficDataFactory(msgEvent.data.toString)))
  }

  def registerCallback(func: (PrettyTruckAndTrafficData) => _): Unit = {
    callbacks += func
  }

}

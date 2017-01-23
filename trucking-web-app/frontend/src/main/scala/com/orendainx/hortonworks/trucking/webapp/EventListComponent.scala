package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.{Component, OnInit}

import scala.scalajs.js

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Component(
  selector = "event-events-component",
  templateUrl = "/assets/templates/event-events.component.html"
)
class EventListComponent(webSocketService: WebSocketService) extends OnInit {

  private val MaxEvents = 100
  private val events: js.Array[PrettyTruckAndTrafficData] = js.Array()

  override def ngOnInit(): Unit = {
    webSocketService.registerCallback(addEvent _)
  }

  def addEvent(event: PrettyTruckAndTrafficData): Unit = {
    events += event
    if (events.size > MaxEvents) events.trimStart(1)
  }

}

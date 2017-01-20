package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.{Component, OnInit}

import scala.scalajs.js

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Component(
  selector = "event-list-component",
  templateUrl = "/assets/templates/event-list.component.html"
)
class EventListComponent(webSocketService: WebSocketService) extends OnInit {

  val MaxEvents = 100
  var events: js.Array[PrettyTruckAndTrafficData] = js.Array()

  Console.println("EventListComponent built")

  override def ngOnInit() = {
    Console.println("EventListComponent initing")
    webSocketService.registerCallback(addEvent _)

    Console.println("Registered callback")

    val e = PrettyTruckAndTrafficDataFactory("1484348928883|37|3|James|-1552376|Springfield to Kansas City Via Hanibal|39.74943369178244|-91.197509765625|-1552376|unsafe-follow-distance|-29")
    val e2 = PrettyTruckAndTrafficDataFactory("1484348928883|37|3|James|-1552376|Springfield to Kansas City Via Hanibal|39.74943369178244|-91.197509765625|-1552376|unsafe-follow-distance|-29")
    events += e
    events += e2
  }

  def addEvent(event: PrettyTruckAndTrafficData) = {
    if (events.size == MaxEvents) {
      events.trimStart(1)
    }
    events += event
  }

}

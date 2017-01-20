package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.{Component, OnInit}
import com.felstar.scalajs.leaflet._
import com.orendainx.hortonworks.trucking.webapp.PrettyTruckAndTrafficData

import scala.collection.mutable

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Component(
  selector = "map-component",
  templateUrl = "/assets/templates/map.component.html"
)
class MapComponent(webSocketService: WebSocketService) extends OnInit {

  private val MaxMarkers = 30
  // TODO: best collection for trimming/appending?
  private val markers = mutable.Buffer.empty[Layer]
  private var lmap: LMap = _

  private val MarkerTypeColors = Map[String, String](
    //TruckEventTypes.Normal -> "#0f0",
    "normal" -> "#0f0",
    "speeding" -> "#f00"
  )

  override def ngOnInit(): Unit = {
    lmap = create("trucking-map")
    webSocketService.registerCallback(addEvent _)
  }

  // TODO: vs other collections?
  val list = mutable.Buffer.empty[PrettyTruckAndTrafficData]

  def addEvent(event: PrettyTruckAndTrafficData) = {

    //if (event.eventType != "Normal") {
      list += event
      addMarker(event)
    //}
  }

  def create(el: String): LMap = {
    lmap = L.map(el, LMapOptions.scrollWheelZoom(false)).setView((40.0, -90.0), 6)

    val tileLayer =
    //L.tileLayer("https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoib3JlbmRhaW4iLCJhIjoiY2l4cWwwYTJrMDkwZTMwbHZ1MG8wYmkxZiJ9.J0uY8A4pTzlcfhc0oUyebg",
      L.tileLayer("https://api.mapbox.com/styles/v1/orendain/cixwfdcue00112splqmktu0e9/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoib3JlbmRhaW4iLCJhIjoiY2l4cWwwYTJrMDkwZTMwbHZ1MG8wYmkxZiJ9.J0uY8A4pTzlcfhc0oUyebg",
        TileLayerOptions.id("mapbox.streets").maxZoom(7).minZoom(5).attribution("""<a href="https://github.com/orendain">Source Code</a>""".stripMargin)
      ).addTo(lmap)
    lmap
  }

  def addMarker(event: PrettyTruckAndTrafficData): LMap = {

    if (markers.size == MaxMarkers) {
      lmap.removeLayer(markers(0))
      markers.trimStart(1)
    }

    val markup = s"<b>Driver Name: ${event.driverName}</b><br><b>Route Name: ${event.routeName}<br><b>Violation: ${event.eventType}</b>"
    val circle = L.circle((event.latitude.toDouble, event.longitude.toDouble), CircleOptions.color("#0f0").weight(2).fillColor("#0f0").fillOpacity(0.5).radius(10000))
      .bindPopup(markup).addTo(lmap)//.openPopup()

    markers.append(circle)

    lmap
  }

}

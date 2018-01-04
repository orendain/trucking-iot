package com.orendainx.trucking.simulator.models

/**
  * The model for a route.  Includes its id, name and list of [[Location]] points.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
case class Route(id: Int, name: String, locations: List[Location]) extends Resource

object EmptyRoute extends Route(-1, "EmptyRoute", List.empty[Location])

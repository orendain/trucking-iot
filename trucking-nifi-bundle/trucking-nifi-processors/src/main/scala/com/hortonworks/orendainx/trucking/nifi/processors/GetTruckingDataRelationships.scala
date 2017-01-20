package com.hortonworks.orendainx.trucking.nifi.processors

import org.apache.nifi.processor.Relationship

trait GetTruckingDataRelationships {
  val RelSuccess = new Relationship.Builder()
    .name("success")
    .description("All generated data is routed to this relationship.")
    .build

  lazy val relationships = Set(RelSuccess)
}

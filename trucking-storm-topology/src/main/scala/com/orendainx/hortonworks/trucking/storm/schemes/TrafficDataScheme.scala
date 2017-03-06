package com.orendainx.hortonworks.trucking.storm.schemes

import java.nio.ByteBuffer

import org.apache.storm.tuple.{Fields, Values}

/**
  * Scheme for parsing speed events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object TrafficDataScheme extends DelimitedScheme("\\|") {

  override def deserialize(buffer: ByteBuffer): Values = {

    // Extract data from buffer
    val strings = deserializeStringAndSplit(buffer)
    val eventTime = strings(0)
    val routeId = strings(1)
    val congestionLevel = strings(2)

    new Values(eventTime, routeId, congestionLevel)
  }

  override def getOutputFields: Fields = new Fields("eventTime", "routeId", "congestionLevel")
}

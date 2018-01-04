package com.orendainx.trucking.storm.schemes

import java.nio.ByteBuffer

import org.apache.storm.tuple.{Fields, Values}

/**
  * Scheme for parsing speed events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class BufferToStringScheme(dataType: String) extends DelimitedScheme("\\|") {

  override def deserialize(buffer: ByteBuffer): Values = new Values(dataType, deserializeAsString(buffer))

  override def getOutputFields: Fields = new Fields("dataType", "data")
}

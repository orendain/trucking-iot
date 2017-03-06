package com.orendainx.hortonworks.trucking.storm.schemes

import java.nio.ByteBuffer

import org.apache.storm.tuple.{Fields, Values}

/**
  * Scheme for parsing speed events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class BytesToStringScheme(dataType: String) extends DelimitedScheme("\\|") {

  /**
    *
    * @param buffer
    * @return
    */
  override def deserialize(buffer: ByteBuffer): Values = new Values(dataType, deserializeString(buffer))

  override def getOutputFields: Fields = new Fields("dataType", "data")
}

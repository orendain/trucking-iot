package com.orendainx.hortonworks.trucking.storm.schemes

import java.nio.ByteBuffer

import org.apache.storm.tuple.{Fields, Values}

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class BufferToBytesScheme(dataType: String) extends DelimitedScheme("\\|") {

  /**
    * Returns [[Values]], where the first value with id of "dataType" is the string passed in through the constructor
    * and the second value with id of "data" is a raw array of bytes read from the byte buffer.
    */
  override def deserialize(buffer: ByteBuffer): Values = new Values(dataType, deserializeAsBytes(buffer))

  override def getOutputFields: Fields = new Fields("dataType", "data")
}

package com.orendainx.trucking.storm.nifi

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.typesafe.scalalogging.Logger
import org.apache.nifi.storm.{NiFiDataPacket, NiFiDataPacketBuilder, StandardNiFiDataPacket}
import org.apache.storm.tuple.Tuple

import scala.collection.JavaConverters._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class ByteArrayToNiFiPacket extends NiFiDataPacketBuilder with Serializable {

  private lazy val logger = Logger(this.getClass)

  override def createNiFiDataPacket(tuple: Tuple): NiFiDataPacket = {
    val newAttributes = Map("processed" -> "true").asJava
    new StandardNiFiDataPacket(tuple.getBinaryByField("data"), newAttributes)
    //new StandardNiFiDataPacket(tuple.getStringByField("data").getBytes(), newAttributes)
  }
}

/*

// [B cannot be cast to String
tuple.getStringByField("data").getBytes(StandardCharsets.UTF_8)


// String cannot be cast to [B
tuple.getValueByField("data").asInstanceof[array[by]]

 */

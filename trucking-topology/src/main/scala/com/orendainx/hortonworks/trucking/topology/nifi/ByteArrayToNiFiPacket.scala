package com.orendainx.hortonworks.trucking.topology.nifi

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
  }
}

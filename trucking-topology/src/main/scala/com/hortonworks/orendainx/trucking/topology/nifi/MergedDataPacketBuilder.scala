package com.hortonworks.orendainx.trucking.topology.nifi

import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.Logger
import org.apache.nifi.storm.{NiFiDataPacket, NiFiDataPacketBuilder, StandardNiFiDataPacket}
import org.apache.storm.tuple.Tuple

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class MergedDataPacketBuilder extends NiFiDataPacketBuilder with Serializable {

  lazy val logger = Logger(this.getClass)

  override def createNiFiDataPacket(tuple: Tuple): NiFiDataPacket = {
    import scala.collection.JavaConversions._

    val newAttributes = Map("mergedSuccesfully" -> "true")
    new StandardNiFiDataPacket(tuple.getValue(0).toString.getBytes(StandardCharsets.UTF_8), newAttributes)
  }
}

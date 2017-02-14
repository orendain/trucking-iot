package com.orendainx.hortonworks.trucking.topology.nifi

import java.nio.charset.StandardCharsets

import com.orendainx.hortonworks.trucking.common.models.WindowedDriverStats
import com.typesafe.scalalogging.Logger
import org.apache.nifi.storm.{NiFiDataPacket, NiFiDataPacketBuilder, StandardNiFiDataPacket}
import org.apache.storm.tuple.Tuple

import scala.collection.JavaConversions._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class DataPacketBuilder extends NiFiDataPacketBuilder with Serializable {

  lazy val logger = Logger(this.getClass)

  override def createNiFiDataPacket(tuple: Tuple): NiFiDataPacket = {
    val newAttributes = Map("processed" -> "true")

    // When reading in non-serialized data
    new StandardNiFiDataPacket(tuple.getValue(0).asInstanceOf[WindowedDriverStats].toCSV.getBytes(StandardCharsets.UTF_8), newAttributes)

    // When reading from SerializerBolt
    //new StandardNiFiDataPacket(tuple.getValueByField("serializedData").asInstanceOf[Array[Byte]], newAttributes)
  }
}

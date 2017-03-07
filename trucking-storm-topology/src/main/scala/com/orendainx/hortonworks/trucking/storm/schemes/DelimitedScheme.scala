package com.orendainx.hortonworks.trucking.storm.schemes

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import org.apache.storm.spout.Scheme
import org.apache.storm.utils.{Utils => StormUtils}

/**
  * Supertype for schemes that parse based on some delimiter.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
abstract class DelimitedScheme(delimiter: String) extends Scheme {

  /**
    * Deserialize and split a byteBuffer in a [[ByteBuffer]] into an [[Array]] of [[Byte]]s.
    *
    * @param byteBuffer The [[ByteBuffer]] to be parsed as a raw byteBuffer.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  protected def deserializeAsBytes(byteBuffer: ByteBuffer): Array[Byte] = {
    if (byteBuffer.hasArray) {
      val buf = new Array[Byte](byteBuffer.remaining())
      byteBuffer.get(buf, byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining())
      buf
    } else StormUtils.toByteArray(byteBuffer)
  }

  /**
    * Deserialize a byteBuffer in a [[ByteBuffer]] into a [[String]].
    *
    * @param byteBuffer The [[ByteBuffer]] to be parsed as a raw byteBuffer.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  protected def deserializeAsString(byteBuffer: ByteBuffer): String = new String(deserializeAsBytes(byteBuffer), UTF_8)

  /**
    * Deserialize and split a byteBuffer in a [[ByteBuffer]] into an array of strings.
    *
    * @param byteBuffer The [[ByteBuffer]] to be parsed as a raw byteBuffer.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  protected def deserializeStringAndSplit(byteBuffer: ByteBuffer): Array[String] = deserializeAsString(byteBuffer).split(delimiter)
}

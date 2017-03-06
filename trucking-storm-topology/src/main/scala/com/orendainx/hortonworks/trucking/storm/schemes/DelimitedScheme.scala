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
    * Deserialize and split a string in a [[ByteBuffer]] into an array of strings.
    *
    * @param string The [[ByteBuffer]] to be parsed as a raw string.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  def deserializeString(string: ByteBuffer): String = {
    if (string.hasArray) new String(string.array(), string.arrayOffset() + string.position(), string.remaining())
    else new String(StormUtils.toByteArray(string), UTF_8)
  }

  def deserializeStringAndSplit(string: ByteBuffer): Array[String] = {
    val rawString = if (string.hasArray)
      new String(string.array(), string.arrayOffset() + string.position(), string.remaining())
    else
      new String(StormUtils.toByteArray(string), UTF_8)

    rawString.split(delimiter)
  }
}

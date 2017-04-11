package com.orendainx.hortonworks.trucking.storm.java.schemes;

import java.nio.ByteBuffer;

import org.apache.storm.spout.Scheme;
import org.apache.storm.utils.Utils;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
  * Supertype for schemes that parse based on some delimiter.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
abstract class DelimitedScheme implements Scheme {

  private String delimiter;

  public DelimitedScheme(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
    * Deserialize and split a byteBuffer in a [[ByteBuffer]] into an [[Array]] of [[Byte]]s.
    *
    * @param byteBuffer The [[ByteBuffer]] to be parsed as a raw byteBuffer.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  protected byte[] deserializeAsBytes(ByteBuffer byteBuffer) {
    return Utils.toByteArray(byteBuffer);
  }

  /**
    * Deserialize a byteBuffer in a [[ByteBuffer]] into a [[String]].
    *
    * @param byteBuffer The [[ByteBuffer]] to be parsed as a raw byteBuffer.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  protected String deserializeAsString(ByteBuffer byteBuffer) {
      return new String(deserializeAsBytes(byteBuffer), UTF_8);
  }

  /**
    * Deserialize and split a byteBuffer in a [[ByteBuffer]] into an array of strings.
    *
    * @param byteBuffer The [[ByteBuffer]] to be parsed as a raw byteBuffer.
    * @return The array of strings resulting from splitting the [[ByteBuffer]] on the object's specified delimiter.
    */
  protected String[] deserializeStringAndSplit(ByteBuffer byteBuffer) {
    return deserializeAsString(byteBuffer).split(delimiter);
  }
}

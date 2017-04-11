package com.orendainx.hortonworks.trucking.storm.schemes;

import java.nio.ByteBuffer;

import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

/**
  * Scheme for parsing speed events.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class BufferToStringScheme extends DelimitedScheme {

  private String dataType = "";

  public BufferToStringScheme(String dataType) {
    super("\\|");
  }

  public Values deserialize(ByteBuffer buffer) {
    return new Values(dataType, deserializeAsString(buffer));
  }

  public Fields getOutputFields() {
    return new Fields("dataType","data");
  }
}

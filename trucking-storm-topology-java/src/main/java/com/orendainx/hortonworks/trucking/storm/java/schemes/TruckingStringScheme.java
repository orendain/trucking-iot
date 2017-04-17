package com.orendainx.hortonworks.trucking.storm.java.schemes;

import org.apache.storm.kafka.StringScheme;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.nio.ByteBuffer;
import java.util.List;

/**
  * Extend the StringScheme class, which reads in bytes from a ByteBuffer and returns the contents as a string.
  * This class changes the output fields appropriate for the application, returning the data as well as the datatype
  * in the field "dataType".
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class TruckingStringScheme extends StringScheme {

  private String dataType;

  public TruckingStringScheme(String dataType) {
    this.dataType = dataType;
  }

  public List<Object> deserialize(ByteBuffer buffer) {
    return new Values(dataType, deserializeString(buffer));
  }

  public Fields getOutputFields() {
    return new Fields("dataType", "data");
  }
}

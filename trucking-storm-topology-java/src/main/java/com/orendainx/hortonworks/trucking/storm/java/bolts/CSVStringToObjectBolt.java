package com.orendainx.hortonworks.trucking.storm.java.bolts;

import com.orendainx.hortonworks.trucking.commons.models.EnrichedTruckData;
import com.orendainx.hortonworks.trucking.commons.models.TrafficData;
import com.orendainx.hortonworks.trucking.commons.models.TruckingData;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
  * Convert Tuples housing CSV delimited strings into Java objects
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class CSVStringToObjectBolt extends BaseRichBolt {

  private OutputCollector outputCollector;

  /*
   * The prepare method provides the bolt with an OutputCollector that is used for emitting tuples from this bolt.
   * Tuples can be emitted at anytime from the bolt -- in the prepare, execute, or cleanup methods, or even
   * asynchronously in another thread. This prepare implementation simply saves the OutputCollector as an instance
   * variable to be used later on in the execute method.
   */
  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    outputCollector = collector;
  }

  /*
   * The execute method receives a tuple from one of the bolt's inputs.
   */
  public void execute(Tuple tuple) {

    // Convert each string into its proper case class instance (e.g. EnrichedTruckData or TrafficData)
    String dataType = tuple.getStringByField("dataType");
    TruckingData data;
    switch (dataType) {
      case "EnrichedTruckData":
        data = EnrichedTruckData.fromCSV(tuple.getStringByField("data"));
        break;
      case "TrafficData":
        data = TrafficData.fromCSV(tuple.getStringByField("data"));
        break;
      default:
        throw new IllegalArgumentException("Tuple housing unexpected data.");
    }

    outputCollector.emit(new Values(dataType, data));
    outputCollector.ack(tuple);
  }

  /*
   * The declareOutputFields method declares that this bolt emits 2-tuples with fields called "dataType" and "data".
   */
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("dataType", "data"));
  }
}

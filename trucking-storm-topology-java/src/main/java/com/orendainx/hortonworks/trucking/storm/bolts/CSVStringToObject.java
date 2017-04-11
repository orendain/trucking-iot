package com.orendainx.hortonworks.trucking.storm.bolts;

import com.orendainx.hortonworks.trucking.commons.models.EnrichedTruckData;
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
  * Convert Tuples in the form of NiFiDataPackets into Tuples of their respective JVM objects.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class CSVStringToObject extends BaseRichBolt {

  //private Logger = Logger(this.getClass)
  private OutputCollector outputCollector;

  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    outputCollector = collector;
  }

  public void execute(Tuple tuple) {

    // Convert each string into its proper case class instance (e.g. EnrichedTruckData or TrafficData)
    String dataType = tuple.getStringByField("dataType");
    TruckingData data;
    switch (dataType) {
      case "EnrichedTruckData":
        data = EnrichedTruckData.fromCSV(tuple.getStringByField("data"));
        break;
      case "TrafficData":
        data = EnrichedTruckData.fromCSV(tuple.getStringByField("data"));
        break;
      default:
        throw new IllegalArgumentException("Tuple housing unexpected data.");
    }

    outputCollector.emit(new Values(dataType, data));
    outputCollector.ack(tuple);
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("dataType", "data"));
  }
}

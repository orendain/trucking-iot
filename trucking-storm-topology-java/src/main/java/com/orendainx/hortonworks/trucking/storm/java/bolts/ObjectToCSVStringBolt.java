package com.orendainx.hortonworks.trucking.storm.java.bolts;

import com.orendainx.hortonworks.trucking.commons.models.EnrichedTruckAndTrafficData;
import com.orendainx.hortonworks.trucking.commons.models.WindowedDriverStats;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
  * Convert Java objects in the stream to CSV delimited strings.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class ObjectToCSVStringBolt extends BaseRichBolt {

  private OutputCollector outputCollector;

  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    outputCollector = collector;
  }

  public void execute(Tuple tuple) {

    String str;
    switch (tuple.getStringByField("dataType")) {
      case "EnrichedTruckAndTrafficData":
        str = ((EnrichedTruckAndTrafficData)tuple.getValueByField("data")).toCSV();
        break;
      case "WindowedDriverStats":
        str = ((WindowedDriverStats)tuple.getValueByField("data")).toCSV();
        break;
      default:
        throw new IllegalArgumentException("Tuple housing unexpected data.");
    }

    outputCollector.emit(new Values(str));
    outputCollector.ack(tuple);
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("data"));
  }
}

package com.orendainx.hortonworks.trucking.storm.java.bolts;

import com.orendainx.hortonworks.trucking.commons.models.EnrichedTruckAndTrafficData;
import com.orendainx.hortonworks.trucking.commons.models.TruckEventTypes;
import com.orendainx.hortonworks.trucking.commons.models.WindowedDriverStats;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
  * Takes EnrichedTruckAndTrafficData and generates driver statistics.  It emits WindowedDriverStats onto its stream.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class DataWindowingBolt extends BaseWindowedBolt {

  private OutputCollector outputCollector;

  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    outputCollector = collector;
  }

  public void execute(TupleWindow inputWindow) {
    // This should be a single tuple
    List<Tuple> newTuples = inputWindow.getNew();
    if (newTuples.size() > 1) {
      throw new IllegalStateException("Expected only a single new tuple in the TupleWindow.");
    }

    EnrichedTruckAndTrafficData newData = (EnrichedTruckAndTrafficData)newTuples.get(0).getValueByField("data");

    // Find the driverId of the tuple just passed in
    final int driverId = newData.driverId();

    // For each tuple in the window, extract into a EnrichedTruckAndTrafficData instance and filter so as to
    // only keep ones with the same driverId as the newest tuple in the window.
    List<EnrichedTruckAndTrafficData> driverEvents = inputWindow.get().parallelStream()
        .map(t -> (EnrichedTruckAndTrafficData)t.getValueByField("data"))
        .filter(d -> d.driverId() == driverId)
        .collect(Collectors.toList());

    // Loop over the list of EnrichedTruckAndTrafficData, tallying up the different fields
    int speed = 0, totalFog = 0, totalRain = 0, totalWind = 0, totalViolations = 0;
    for (EnrichedTruckAndTrafficData d : driverEvents) {
      speed += d.speed();
      totalFog += d.foggy();
      totalRain += d.rainy();
      totalWind += d.windy();
      totalViolations += (d.eventType().equals(TruckEventTypes.Normal()) ? 0 : 1);
    }

    // Create an instance of WindowedDriverStats and emit it into the stream along with the name of its data type "WindowedDriverStats"
    WindowedDriverStats stats = new WindowedDriverStats(driverId, speed/driverEvents.size(), totalFog, totalRain, totalWind, totalViolations);
    outputCollector.emit(new Values("WindowedDriverStats", stats));

    // Acknowledge that all tuples were processed.  It's best practice to perform this after all processing has been completed.
    inputWindow.get().forEach(outputCollector::ack);
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("dataType", "data"));
  }
}

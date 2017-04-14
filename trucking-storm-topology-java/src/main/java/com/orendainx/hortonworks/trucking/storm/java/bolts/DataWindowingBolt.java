package com.orendainx.hortonworks.trucking.storm.java.bolts;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.orendainx.hortonworks.trucking.commons.models.EnrichedTruckAndTrafficData;
import com.orendainx.hortonworks.trucking.commons.models.TrafficData;
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


/**
  * Takes EnrichedTruckAndTrafficData and generates driver statistics.  It emits WindowedDriverStats onto its stream.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
public class DataWindowingBolt extends BaseWindowedBolt {

  //private lazy val log = Logger(this.getClass)
  private OutputCollector outputCollector;

  private TrafficData latestTrafficData;

  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    outputCollector = collector;
  }

  public void execute(TupleWindow inputWindow) {
/*
    Map<Integer, List<EnrichedTruckAndTrafficData>> enrichedDataMap = inputWindow.get().stream()
        .map(t -> (EnrichedTruckAndTrafficData)t.getValueByField("data"))
        .collect(Collectors.groupingBy(EnrichedTruckAndTrafficData::driverId));




    enrichedDataMap.values().stream().map(dataLst -> {
      int driverId, speed, foggy, rainy, windy, vios = 0;
      dataLst.stream().forEach(e -> {
        speed += e.speed();
        foggy += e.foggy();
        rainy += e.rainy();
        windy += e.windy();
        vios += (e.eventType().equals(TruckEventTypes.Normal()) ? 0 : 1);
      });

      speed /= dataLst.size();

      new WindowedDriverStats()

        })


    //val driverStats = inputWindow.get()
      .map(_.getValueByField("data").asInstanceOf[EnrichedTruckAndTrafficData]) // List[Tuple] => List[EnrichedTruckAndTrafficData]
      .groupBy(d => d.driverId) // List[EnrichedTruckAndTrafficData] => Map[driverId, List[EnrichedTruckAndTrafficData]]
      .mapValues({ dataLst => // Map[driverId, List[EnrichedTruckAndTrafficData]] => Map[driverId, (tupleOfStats)]
        val sums = dataLst
          .map(e => (e.speed, e.foggy, e.rainy, e.windy, if (e.eventType == TruckEventTypes.Normal) 0 else 1))
          .foldLeft((0, 0, 0, 0, 0))((s, v) => (s._1 + v._1, s._2 + v._2, s._3 + v._3, s._4 + v._4, s._5 + v._5))
        (sums._1 / dataLst.size, sums._2, sums._3, sums._4, sums._5)
      })

      */

    /*
     * At this point, driverStats is a map where its values are the following over the span of the window:
     * - Driver id
     * - Average speed
     * - Total fog
     * - Total rain
     * - Total wind
     * - Total violations
     */
    //driverStats.foreach({case (id, s) => outputCollector.emit(new Values("WindowedDriverStats", WindowedDriverStats(id, s._1, s._2, s._3, s._4, s._5)))})

    // This should be a single tuple
    List<Tuple> newTuples = inputWindow.getNew();

    if (newTuples.size() > 1) {
      throw new IllegalStateException("Expected only a single new tuple in the TupleWindow.");
    }

    EnrichedTruckAndTrafficData newData = (EnrichedTruckAndTrafficData)newTuples.get(0).getValueByField("data");

    int driverId = newData.driverId();

    List<EnrichedTruckAndTrafficData> driverEvents = inputWindow.get().parallelStream()
        .map(t -> (EnrichedTruckAndTrafficData)t.getValueByField("data"))
        .filter(d -> d.driverId() == driverId)
        .collect(Collectors.toList());

    int count = 0, speed = 0, totalFog = 0, totalRain = 0, totalWind = 0, totalViolations = 0;
    for(EnrichedTruckAndTrafficData d : driverEvents) {
      count++;
      speed += d.speed();
      totalFog += d.foggy();
      totalRain += d.rainy();
      totalWind += d.windy();
      totalViolations += (d.eventType().equals(TruckEventTypes.Normal()) ? 0 : 1);
    }

    //WindowedDriverStats stats = new WindowedDriverStats(driverId, speed/count, totalFog, totalRain, totalWind, totalViolations);

    // For testing:
    outputCollector.emit(new Values("WindowedDriverStats", new WindowedDriverStats(driverId, speed/count, totalFog, totalRain, totalWind, totalViolations)));
    //outputCollector.emit(new Values("WindowedDriverStats", new WindowedDriverStats(0,0,0,0,0,0)));

    // Acknowledge all tuples processed.  It is best practice to perform this after all processing has been completed.
    inputWindow.get().forEach(outputCollector::ack);
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("dataType", "data"));
  }
}

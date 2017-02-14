package com.orendainx.hortonworks.trucking.topology

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class TruckingTopology3 {

  /**
    * Create a topology with the following components.
    *
    * Spouts:
    *   - NiFiSpout (for injesting EnrichedTruckData from NiFi)
    *   - NiFiSpout (for injesting TrafficData from NiFi)
    * Bolt:
    *   - DeserializerBolt (for deserializing using Schema Registry)
    *   - TruckAndTrafficStreamJoinBolt (for joining EnrichedTruckData and TrafficData streams into one)
    *   - DataWindowingBolt (generating driver stats from trucking data)
    *   - SerializingBolt (for serializing using Schema Registry)
    *   - NiFiBolt (for sending data back out to NiFi)
    *
    * @author Edgar Orendain <edgar@orendainx.com>
    */
}

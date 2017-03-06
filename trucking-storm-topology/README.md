# Trucking Topology

This module defines and builds Storm topologies that can be deployed to Hadoop.

The config file is located at `src/main/resources/application.conf`.



// TODO:
`EnrichedTruckAndTrafficData` looks like `EnrichedTruckData` but with the `congestionLevel` field from `TrafficData` tacked onto the end.
```
1488767711734|26|1|Edgar Orendain|107|Springfield to Kansas City Via Columbia|38.95940879245423|-92.21923828125|65|Speeding|1|0|1|60
```
![EnrichedTruckAndTrafficData fields](readme-assets/enriched-truck-and-traffic-data_fields.png)

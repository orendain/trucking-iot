# Trucking Commons

This subproject houses the shared, common components for the Trucking IoT project.

Among other things, this subproject contains the models of the different generated data (TruckData, TrafficData, etc.)
that flow through the system, providing an single interface for other projects to build/match/serialize this data.

The different trucking data types are:
-   TruckData
-   TrafficData
-   EnrichedTruckData
-   EnrichedTruckAndTrafficData
-   WindowedDriverStats

`TruckData` is defined as:
```
case class TruckData(
  eventTime: Long,
  truckId: Int,
  driverId: Int,
  driverName: String,
  routeId: Int,
  routeName: String,
  latitude: Double,
  longitude: Double,
  speed: Int,
  eventType: String
)
```

`TrafficData` is defined as:
```
case class TrafficData(eventTime: Long, routeId: Int, congestionLevel: Int)
```

`EnrichedTruckData` is defined just like TruckData, but with three fields tacked onto the end:
```
case class EnrichedTruckData(
  ... // other fields from TruckData
  eventType: String,
  foggy: Int,
  rainy: Int,
  windy: Int
)
```

`EnrichedTruckAndTrafficData` is defined just like `EnrichedTruckData`, but with the `congestionLevel` field tacked onto the end.
```
case class EnrichedTruckAndTrafficData(
  ... // other fields from EnrichedTruckData
  windy: Int,
  congestionLevel: Int
)
```

`WindowedDriverStats` is defined as:
```
case class WindowedDriverStats(
  driverId: Int,
  averageSpeed: Int,
  totalFog: Int,
  totalRain: Int,
  totalWind: Int,
  totalViolations: Int
)
```

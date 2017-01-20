# Trucking NiFi Bundle

A NiFi bundle that includes relevant NiFi processors and controller services.

Includes a processor, GetTruckingData, that generates data mimicing sensor logs from truck sensors as well as
traffic congestion data from the edge.  Underneath the hood, this processor leverages the `trucking-simulator`
simulator engine included with the base project.

Upon building this module, a `nifi-trucking-nar-<version>.nar` is created, which should be uploaded to each
NiFi node's `/lib` directory.  Upon a NiFi restart, all processors and controller services will be available for use.

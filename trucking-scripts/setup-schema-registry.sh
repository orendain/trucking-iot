#!/bin/bash

# Start Schema Registry service
nohup /usr/hdf/2.1.0.0-164/registry/bin/registry-server-start.sh /usr/hdf/2.1.0.0-164/registry/conf/registry-dev.yaml &

# Run the module to registry the project's schema
sbt schemaRegistrar/run

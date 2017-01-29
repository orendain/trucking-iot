#!/bin/bash

# Start the Schema Registry webserver
nohup /usr/hdf/2.1.0.0-164/registry/bin/registry-server-start.sh /usr/hdf/2.1.0.0-164/registry/conf/registry-dev.yaml &

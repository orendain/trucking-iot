#!/bin/bash

# Setup the environment before building project components
./setup-environment.sh

# Find the registry webservice and start it
./registry-restart.sh

# Build and install the trucking-nifi-bundle project, generating a NiFi nar for use.
./builds/nifi-bundle.sh

# Build and run the trucking-schema-registrar project, registering schema with the registry
./builds/schema-registrar.sh

# Build and deploy the Storm topology
./builds/storm-topology.sh


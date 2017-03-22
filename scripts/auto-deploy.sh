#!/bin/bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

# Setup the environment before building project components
$scriptDir/setup-environment.sh

# Find the registry webservice and start it
$scriptDir/registry-restart.sh

# Build and install the trucking-nifi-bundle project, generating a NiFi nar for use.
$scriptDir/builds/nifi-bundle.sh

# Build and run the trucking-schema-registrar project, registering schema with the registry
$scriptDir/builds/schema-registrar.sh

echo "Running the NiFi flow"
curl 'http://sandbox.hortonworks.com:9090/nifi-api/flow/process-groups/90e748c3-015a-1000-f68d-292036f42e8f' -X PUT -H 'Content-Type: application/json' -H 'X-Requested-With: XMLHttpRequest' --data-binary '{"id":"90e748c3-015a-1000-f68d-292036f42e8f","state":"RUNNING"}' --compressed

# Build and deploy the Storm topology
$scriptDir/builds/storm-topology.sh

# Build and run the web application
$scriptDir/builds/web-application.sh

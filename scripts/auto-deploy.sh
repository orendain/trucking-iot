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

# Build and deploy the Storm topology
$scriptDir/builds/storm-topology.sh

# Build and run the web application
$scriptDir/builds/web-application.sh

#!/bin/bash

# Variables
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"

# Jump to the project directory for executing SBT commands
cd $projDir

# Schema Registry: Register the project schemas with the registry
echo "Registering schema with Schema Registry"
sbt schemaRegistrar/run

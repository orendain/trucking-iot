#!/bin/bash

# Jump to the project directory for executing SBT commands
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"
cd $projDir

echo "Registering schema with the Schema Registry service"
sbt schemaRegistrar/run

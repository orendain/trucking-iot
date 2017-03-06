#!/bin/bash

# Variables
projVer="0.3.2"
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"

# Jump to the project directory for executing SBT commands
cd $projDir

echo "Building and deploying the Storm topology"
sbt stormTopology/assembly
storm jar $projDir/trucking-storm-topology/target/scala-2.11/trucking-storm-topology-assembly-$projVer.jar com.orendainx.hortonworks.trucking.topology.topologies.KafkaToKafka

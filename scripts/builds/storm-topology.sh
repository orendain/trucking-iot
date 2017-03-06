#!/bin/bash

# Jump to the project directory for executing SBT commands
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"
cd $projDir

projVer=$(cat version.sbt | grep '".*"' -o)

echo "Building and deploying the Storm topology"
sbt stormTopology/assembly
storm jar $projDir/trucking-storm-topology/target/scala-2.11/trucking-storm-topology-assembly-$projVer.jar com.orendainx.hortonworks.trucking.storm.topologies.KafkaToKafka

#!/bin/bash

# Jump to the project directory for executing SBT commands
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"
cd $projDir

projVer=$(cat version.sbt | grep '".*"' -o | sed 's/"//g')

echo "Building and deploying the Storm topology"

# This is the Scala version of the topology
sbt stormTopology/assembly
storm jar $projDir/trucking-storm-topology/target/scala-2.11/trucking-storm-topology-assembly-$projVer.jar com.orendainx.trucking.storm.topologies.KafkaToKafka

# Valid, built-in options for the topology (last argument) above are:
#
# com.orendainx.trucking.storm.topologies.NiFiToNiFi
# com.orendainx.trucking.storm.topologies.NiFiToNiFiWithSchema
# com.orendainx.trucking.storm.topologies.KafkaToKafka
# com.orendainx.trucking.storm.topologies.KafkaToKafkaWithSchema

# This is the Java version of the topology
#sbt stormTopologyJava/assembly
#storm jar $projDir/trucking-storm-topology-java/target/scala-2.11/trucking-storm-topology-java-assembly-$projVer.jar com.orendainx.trucking.storm.java.topologies.KafkaToKafka


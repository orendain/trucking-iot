#!/bin/bash

# Variables
projVer="0.3.2"
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
nifiLibs=($(find / -path "/usr/*/nifi/lib"))

# Jump to the project directory for executing SBT commands
cd $projDir

# NiFI: Compile the trucking-nifi-bundle project, generating a nar file to copy to the NiFi lib directory.
sbt nifiBundle/compile
cp $projDir/trucking-nifi-bundle/nifi-trucking-nar/target/nifi-trucking-nar-${projVer}.nar ${nifiLibs[0]}

# Schema Registry: Register the project schemas with the registry
sbt schemaRegistrar/run

# Storm: Build and deploy the Storm topology
sbt topology/assembly
storm jar $projDir/trucking-topology/target/scala-2.11/trucking-topology-assembly-${projVer}.jar com.orendainx.hortonworks.trucking.topology.TruckingTopology

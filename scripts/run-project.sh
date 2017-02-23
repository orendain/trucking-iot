#!/bin/bash

# Variables
projVer="0.3.2"
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
nifiLibDir=$(find / -type d -path "/usr/*/nifi/lib" -print -quit)
nifiSh=$(find / -type f -wholename "/usr/*/nifi.sh" -print -quit)

# Jump to the project directory for executing SBT commands
cd $projDir

# NiFI: Compile the trucking-nifi-bundle project, generating a nar file to copy to the NiFi lib directory.
echo "Building and installing a NiFi nar to NiFi.  NiFi will be restarted"
sbt nifiBundle/compile
cp -f $projDir/trucking-nifi-bundle/nifi-trucking-nar/target/nifi-trucking-nar-$projVer.nar $nifiLibDir
$nifiSh restart

# Schema Registry: Register the project schemas with the registry
echo "Registering schema with Schema Registry"
sbt schemaRegistrar/run

# Storm: Build and deploy the Storm topology
echo "Building and deploying a Storm topology"
sbt topology/assembly
storm jar $projDir/trucking-topology/target/scala-2.11/trucking-topology-assembly-$projVer.jar com.orendainx.hortonworks.trucking.topology.topologies.NiFiToNiFi

#!/bin/bash

PROJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd $PROJ_DIR && sbt topology/assembly
storm jar $PROJ_DIR/trucking-topology/target/scala-2.11/trucking-topology-assembly-0.3.2.jar com.orendainx.hortonworks.trucking.topology.TruckingTopology

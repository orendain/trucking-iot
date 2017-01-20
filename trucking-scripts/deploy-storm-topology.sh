#!/bin/bash

sbt topology/assembly
storm jar ../trucking-topology/target/scala-2.11/trucking-topology-assembly-0.3.2.jar com.hortonworks.orendainx.trucking.topology.TruckingTopology

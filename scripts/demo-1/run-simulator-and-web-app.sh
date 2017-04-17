#!/usr/bin/env bash

projectDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )"/.. && pwd)"
cd $projectDir

# Start simulator
sbt simulator/run-main\ com.orendainx.hortonworks.trucking.simulator.simulators.EnrichToKafkaSimulator

# Start web applcation
sbt webApplicationBackend/run

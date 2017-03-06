#!/bin/bash

# Variables
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"

# Jump to the project directory for executing SBT commands
cd $projDir

echo "Building and running the web application"
sbt webApplicationBackend/run

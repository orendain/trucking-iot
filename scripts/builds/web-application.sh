#!/bin/bash

# Variables
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd)"

# Jump to the project directory for executing SBT commands
cd $projDir

# Run the web application
echo "Building and running the web application"
sbt webApplicationBackend/run

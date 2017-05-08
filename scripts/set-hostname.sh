#!/usr/bin/env bash

projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

if [ $# -eq 0 ]
  then
    echo "Pass in the hostname to set"
else
  for dir in "scripts" "trucking-schema-registrar" "trucking-simulator" "trucking-nifi-templates" "trucking-storm-topology" "trucking-storm-topology-java" "trucking-web-application"
  do
    find $projDir/$dir -type f -exec sed -i '' -e "s/sandbox.hortonworks.com/$1/g" {} \;
  done
fi

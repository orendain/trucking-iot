#!/usr/bin/env bash

projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

if [ $# -eq 0 ]
  then
    echo "Pass in the hostname to set"
else
  for dir in "scripts" "trucking-schema-registrar/src" "trucking-simulator/src" "trucking-nifi-templates" "trucking-storm-topology/src" "trucking-storm-topology-java/src" "trucking-web-application/backend/conf" "trucking-web-application/frontend/src"
  do
    find $projDir/$dir -type f -exec sed -i -e "s/sandbox-hdf.hortonworks.com/$1/g" {} \;
  done
fi

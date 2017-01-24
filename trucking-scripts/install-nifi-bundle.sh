#!/bin/bash

PROJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd $PROJ_DIR && sbt nifiBundle/compile
cp $PROJ_DIR/trucking-nifi-bundle/nifi-trucking-nar/target/nifi-trucking-nar-0.3.2.nar /usr/hdf/current/nifi/lib/

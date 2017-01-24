#!/bin/bash

#sbt nifiBundle/compile
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cp $DIR/trucking-nifi-bundle/nifi-trucking-nar/target/nifi-trucking-nar-0.3.2.nar /usr/hdf/current/nifi/lib/

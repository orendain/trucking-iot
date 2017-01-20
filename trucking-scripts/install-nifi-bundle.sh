#!/bin/bash

sbt nifiBundle/compile
cp ../trucking-nifi-bundle/nifi-trucking-nar/target/nifi-trucking-nar-0.3.2.nar /usr/hdf/current/nifi/lib/

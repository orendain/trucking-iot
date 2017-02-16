#!/bin/bash

# Find the registry webservice binary and restart the service
registry=$(find / -type f -wholename '/usr/hd*/registry' -print -quit)
echo "Found Registry binaries at $registry"

$registry stop
$registry clean
$registry start

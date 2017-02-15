#!/bin/bash

# Find the registry webservice binary and start the web service
registryArr=($(find / -type f -name "registry"))
registry=${registryArr[*]: -1}
echo "Found registry binary at $registry"
$registry stop
$registry clean
$registry start

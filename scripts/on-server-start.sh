#!/bin/bash

# Find the registry webservice binary and start the web service
registryArr=($(find / -type f -name "registry"))
registry=${registryArr[-1]}
$registry stop
$registry clean
$registry start

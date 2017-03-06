#!/bin/bash

echo "Restarting the registry service"
registry=$(find / -type f -wholename '/usr/hd*/registry' -print -quit)
$registry stop
$registry clean
$registry start

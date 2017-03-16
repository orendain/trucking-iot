#!/bin/bash

echo "Restarting the registry service"
registry=$(find / -type f -wholename '/usr/hd*/registry' -print -quit 2> /dev/null)
$registry stop
$registry clean
$registry start

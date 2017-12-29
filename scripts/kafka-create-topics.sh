#!/bin/bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

# Start via Ambari
echo "Starting the Kafka service"
curl -u admin:admin -H "X-Requested-By: ambari" -X PUT -d '{"RequestInfo": {"context" :"Start Kafka"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://sandbox-hdf.hortonworks.com:8080/api/v1/clusters/Sandbox/services/KAFKA | python $scriptDir/wait-until-done.py

# Start via local package
#$(find / -type f -wholename '/usr/hd*/kafka' -print -quit 2> /dev/null) start

kafkaTopicsSh=$(find / -type f -wholename '/usr/hd*/kafka-topics.sh' -print -quit 2> /dev/null)

#echo "Deleting existing Kafka topics"
$kafkaTopicsSh --zookeeper sandbox-hdf.hortonworks.com:2181 --delete --topic trucking_data_truck 2> /dev/null
$kafkaTopicsSh --zookeeper sandbox-hdf.hortonworks.com:2181 --delete --topic trucking_data_traffic 2> /dev/null
$kafkaTopicsSh --zookeeper sandbox-hdf.hortonworks.com:2181 --delete --topic trucking_data_joined 2> /dev/null
$kafkaTopicsSh --zookeeper sandbox-hdf.hortonworks.com:2181 --delete --topic trucking_data_driverstats 2> /dev/null

echo "Creating Kafka topics"
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic trucking_data_truck
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic trucking_data_traffic
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic trucking_data_joined
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic trucking_data_driverstats

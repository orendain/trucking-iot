#!/bin/bash


# Make sure Kafka is running
$(find / -type f -wholename '/usr/hd*/kafka' -print -quit 2> /dev/null) start

echo "Deleting existing Kafka topics"
kafkaTopicsSh=$(find / -type f -wholename '/usr/hd*/kafka-topics.sh' -print -quit 2> /dev/null)
$kafkaTopicsSh --zookeeper sandbox.hortonworks.com:2181 --delete --topic trucking_data_truck 2> /dev/null
$kafkaTopicsSh --zookeeper sandbox.hortonworks.com:2181 --delete --topic trucking_data_traffic 2> /dev/null
$kafkaTopicsSh --zookeeper sandbox.hortonworks.com:2181 --delete --topic trucking_data_joined 2> /dev/null
$kafkaTopicsSh --zookeeper sandbox.hortonworks.com:2181 --delete --topic trucking_data_driverstats 2> /dev/null

echo "Creating Kafka topics"
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_truck
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_traffic
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_joined
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_driverstats

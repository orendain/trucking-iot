#!/bin/bash

# Variables
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

# Install SBT if missing
echo "Checking for SBT, installing if missing"
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
yum -y install sbt

# Install Maven if missing
echo "Checking for Maven, installing if missing"
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
yum install -y apache-maven

# Use kafka-topics.sh to create necessary topics
echo "Starting Kafka and creating Kafka topics"
$(find / -type f -wholename '/usr/hd*/kafka' -print -quit) start
kafkaTopicsSh=$(find / -type f -wholename '/usr/hd*/kafka-topics.sh' -print -quit)
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_truck
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_traffic
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_joined
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_driverstats

# Move NiFi template into proper location
echo "Importing NiFi template and restart NiFi.  Existing flow is renamed to flow.xml.gz.bak, and NiFi"
mv /var/lib/nifi/conf/flow.xml.gz /var/lib/nifi/conf/flow.xml.gz.bak
cp -f $projDir/trucking-nifi-templates/flow.xml.gz /var/lib/nifi/conf
$(find / -type f -wholename '/usr/hd*/nifi.sh' -print -quit) restart

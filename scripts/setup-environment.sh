#!/bin/bash

#
# Note: This script assumes that Ambari and NiFi and are up and running at this point.
# It also assumes an Ambari username/pass of admin/admin and that it's running on port 8080 with hostname "sandbox.hortonworks.com"
#

# Variables
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

echo "Setting nifi.remote.input.socket.port to correct value via Ambari"
/var/lib/ambari-server/resources/scripts/configs.py admin admin 8080 http set sandbox.hortonworks.com Sandbox nifi-properties nifi.remote.input.socket.port 15000

echo "Stopping NiFi via Ambari"
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Stop NIFi"}, "ServiceInfo": {"state": "INSTALLED"}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/NIFI

echo "Checking for SBT, installing if missing"
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
yum -y install sbt

echo "Checking for Maven, installing if missing"
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
yum install -y apache-maven

echo "Starting Kafka and creating Kafka topics"
$(find / -type f -wholename '/usr/hd*/kafka' -print -quit 2> /dev/null) start
kafkaTopicsSh=$(find / -type f -wholename '/usr/hd*/kafka-topics.sh' -print -quit 2> /dev/null)
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_truck
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_traffic
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_joined
$kafkaTopicsSh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_driverstats

echo "Importing NiFi flow.  Existing flow is renamed to flow.xml.gz.bak"
mv /var/lib/nifi/conf/flow.xml.gz /var/lib/nifi/conf/flow.xml.gz.bak
cp -f $projDir/trucking-nifi-templates/flows/nifi-to-nifi-with-schema.xml.gz /var/lib/nifi/conf/flow.xml.gz

# Valid, built-in, flows that can be used are:
#
# nifi-to-nifi.xml.gz
# nifi-to-nifi-with-schema.xml.gz
# kafka-to-kafka.xml.gz
# kafka-to-kafka-with-schema.xml.gz

# Start NiFi via Ambari
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Start NIFi"}, "ServiceInfo": {"state": "STARTED"}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/NIFI

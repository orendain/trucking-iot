#!/bin/bash

# Variables
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

# Stop NiFi via Ambari
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Stop NIFi"}, "ServiceInfo": {"state": "INSTALLED"}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/NIFI

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
echo "Importing NiFi template.  Existing flow is renamed to flow.xml.gz.bak"
mv /var/lib/nifi/conf/flow.xml.gz /var/lib/nifi/conf/flow.xml.gz.bak
cp -f $projDir/trucking-nifi-templates/flows/nifi-to-nifi.xml.gz /var/lib/nifi/conf/flow.xml.gz

# Set nifi.remote.input.socket.port via Ambari
/var/lib/ambari-server/resources/scripts/configs.py admin admin 8080 http set sandbox.hortonworks.com Sandbox nifi-properties nifi.remote.input.socket.port 15000

# Start NiFi via Ambari
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Start NIFi"}, "ServiceInfo": {"state": "STARTED"}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/NIFI

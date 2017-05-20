#!/bin/bash

#
# Note: This script assumes that Ambari and NiFi and are up and running at this point.
# It also assumes an Ambari username/pass of admin/admin and that it's running on port 8080 with hostname "sandbox-hdf.hortonworks.com"
#

echo "Setting nifi.remote.input.socket.port to correct value via Ambari"
/var/lib/ambari-server/resources/scripts/configs.py admin admin 8080 http set sandbox-hdf.hortonworks.com Sandbox nifi-properties nifi.remote.input.socket.port 15000

echo "Setting delete.topic.enable to true via Ambari"
/var/lib/ambari-server/resources/scripts/configs.py admin admin 8080 http set sandbox-hdf.hortonworks.com Sandbox kafka-broker delete.topic.enable true

# Below works in HDP 2.6, in place of above
#/var/lib/ambari-server/resources/scripts/configs.py -u admin -p admin --action=set --host=sandbox-hdf.hortonworks.com --cluster=Sandbox --config-type=kafka-broker -k delete.topic.enable -v true

echo "Restarting Kafka via Ambari"
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Stop Kafka"}, "ServiceInfo": {"state": "INSTALLED"}}' http://sandbox-hdf.hortonworks.com:8080/api/v1/clusters/Sandbox/services/KAFKA
sleep 10
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Start Kafka"}, "ServiceInfo": {"state": "STARTED"}}' http://sandbox-hdf.hortonworks.com:8080/api/v1/clusters/Sandbox/services/KAFKA

echo "Checking for SBT, installing if missing"
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
yum -y install sbt-0.13.13.1-1

echo "Checking for Maven, installing if missing"
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
yum install -y apache-maven

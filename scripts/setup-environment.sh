#!/bin/bash

# Start Kafka for topic creation (Sandbox only, due to default username/pass)
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo":{"context":"_PARSE_.START.KAFKA","operation_level":{"level":"SERVICE","cluster_name":"Sandbox","service_name":"KAFKA"}},"Body":{"ServiceInfo":{"state":"STARTED"}}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services
#curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo":{"context":"_PARSE_.START.ALL_SERVICES","operation_level":{"level":"CLUSTER","cluster_name":"Sandbox"}},"Body":{"ServiceInfo":{"state":"STARTED"}}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services

# Install SBT if not installed
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
yum -y install sbt

# Install Maven if not installed
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
yum install -y apache-maven

# Find "kafka-topics.sh" and create the necessary topics
kafkaArr=($(find / -type f -name "kafka-topics.sh"))
${kafkaArr[0]} --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 1 --topic trucking.data.truckandtraffic

# Move NiFi template into proper location
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

nifiSh=($(find / -type f -name "nifi.sh"))
$nifiSh stop
cp -f $projDir/trucking-nifi-templates/flow.xml.gz /var/lib/nifi/conf
$nifiSh start

#!/bin/bash

# Variables
PROJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# Make sure all services are up and running (do this first, as it takes a while for things to come up)
# TODO: Really only need Kafka up for this script.
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo":{"context":"_PARSE_.START.ALL_SERVICES","operation_level":{"level":"CLUSTER","cluster_name":"Sandbox"}},"Body":{"ServiceInfo":{"state":"STARTED"}}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services

# Install SBT if not installed
hash sbt 2>/dev/null || {
  curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
  yum -y install sbt
}

# Install Maven if not installed
hash mvn 2>/dev/null || {
  cd
  wget http://apache.mirrors.pair.com/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
  tar zxf apache-maven-3.3.9-bin.tar.gz
  mv apache-maven-3.3.9 /opt/apache-maven-3.3.9

  # Write Maven into profile and source it
  cat >/etc/profile.d/maven.sh <<EOL
  export M2_HOME=/opt/apache-maven-3.3.9
  export PATH=${M2_HOME}/bin:${PATH}
  EOL
  source /etc/profile.d/maven.sh
}

# Create necessary Kafka topics
/usr/hdf/current/kafka-broker/bin/kafka-topics.sh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 1 --topic trucking.data.truckandtraffic

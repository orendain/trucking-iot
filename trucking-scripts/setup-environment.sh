#!/bin/bash

# Variables
PROJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

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

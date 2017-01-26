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

  # Write Maven profile and source it
  cat >/etc/profile.d/maven.sh <<EOL
  export M2_HOME=/opt/apache-maven-3.3.9
  export PATH=${M2_HOME}/bin:${PATH}
  EOL
  source /etc/profile.d/maven.sh
}

# Locally install a compatible version of the Schema Registry library
cd
git clone https://github.com/orendain/registry.git
cd registry
mvn install -DskipTests

# Install Schema Registry
rpm -Uvh $PROJ_DIR/trucking-scripts/resources/registry_2_1_0_0_164-0.0.1.2.1.0.0-164.el6.noarch.rpm
cp /usr/hdf/2.1.0.0-164/etc/registry/conf.dist/* /usr/hdf/2.1.0.0-164/registry/conf/

# Replace instances of port 9090 to 8090 in config
perl -pi -e 's/9090/8080/g' /usr/hdf/2.1.0.0-164/registry/conf/registry-dev.yaml

# Install Schema Registry nar file
cp $PROJ_DIR/trucking-scripts/resources/nifi-registry-nar-0.0.1-SNAPSHOT.nar /usr/hdf/current/nifi/lib

# Create necessary Kafka topics
/usr/hdf/current/kafka-broker/bin/kafka-topics.sh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partition 1 --topic trucking.data.truckandtraffic

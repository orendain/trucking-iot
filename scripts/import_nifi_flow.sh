#!/bin/bash

# Root project directory
projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

echo "Stopping NiFi via Ambari"
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Stop NIFi"}, "ServiceInfo": {"state": "INSTALLED"}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/NIFI

sleep 10

echo "Importing NiFi flow.  Existing flow is backed up to flow.xml.gz.bak"
mv /var/lib/nifi/conf/flow.xml.gz /var/lib/nifi/conf/flow.xml.gz.bak
cp -f $projDir/trucking-nifi-templates/flows/nifi-to-nifi-with-schema.xml.gz /var/lib/nifi/conf/flow.xml.gz

# Built-in flows that can be used are:
#
# nifi-to-nifi.xml.gz
# nifi-to-nifi-with-schema.xml.gz
# kafka-to-kafka.xml.gz
# kafka-to-kafka-with-schema.xml.gz

echo "Starting NiFi via Ambari"
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Start NIFi"}, "ServiceInfo": {"state": "STARTED"}}' http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/NIFI

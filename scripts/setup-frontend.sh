#!/usr/bin/env bash

projDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
propFile="/etc/trucking-iot/conf/web-application.properties"
#propFile="/Users/eorendain/Documents/trucking/trucking-iot/scripts/test.properties"

function get_property
{
  grep "^$1=" "$propFile" | cut -d'=' -f2
}

# Set backend properties
kafkaBootstrapServer=$(get_property trucking-web-application.backend.kafka.bootstrap-servers)
echo "Kafka bootstrap server set to: $kafkaBootstrapServer"
sed -i "s/kafkabroker1.kafka-app-2.root.hwx.site:9092/$kafkaBootstrapServer/g" /trucking-iot/trucking-web-application/backend/conf/application.conf

# Set frontend properties via container-id
containerId=$(get_property trucking-web-application.frontend.container-uri)
containerUri=$(echo $containerId | sed -e 's/container/ctr/g' -e 's/_/-/g')
echo "Container uri set to: $containerUri"
sed -i "s/localhost:15100/$containerUri/g" /trucking-iot/trucking-web-application/frontend/src/main/scala/com/orendainx/hortonworks/trucking/webapplication/WebSocketService.scala

# Set frontend properties
#websocketUri=$(get_property trucking-web-application.frontend.kafka.websocket-uri)
#echo "Websocket uri set to: $websocketUri"
#sed -i "s/localhost:15100/$websocketUri/g" /trucking-iot/trucking-web-application/frontend/src/main/scala/com/orendainx/hortonworks/trucking/webapplication/WebSocketService.scala

# Enter base directory
#cd $projDir
cd "/trucking-iot"

# Clean up pre-compiled frontend files
sbt "project webApplicationFrontend" "clean"

# Run project
sbt "project webApplicationBackend" "run"

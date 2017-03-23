# Trucking Scripts

A collection of scripts to help automate the setup, deployment and project building process.

-   `auto-deploy.sh` - Run-once deployment script for auto deploying this reference application.  This uses the other scripts listed below as necessary.
-   `registry-restart.sh` - Finds the Schema Registry service and (re)starts it.
-   `setup-environment.sh` - Initial environment setup, only needs to be run once when the project is downloaded.
-   `create-kafka-topics.sh` - Create Kafka topics necessary for application, deleting existing ones.
-   `import-nifi-flow.sh` - Import application NiFi flow into NiFi, restarting NiFi.
-   `build/nifi-bundle.sh` - Build and install the trucking-nifi-bundle subproject, generating a NiFi nar for use.
-   `build/schema-registrar.sh` - Build and run the trucking-schema-registrar subproject, registering schema with the registry
-   `build/storm-topology.sh` - Build and deploy the trucking-storm-topology subproject, deploying a Storm topology
-   `build/web-appication.sh` - Build and run the web application visualize the processed data.

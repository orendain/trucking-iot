# Trucking IoT

The Trucking IoT project is a modern real-time streaming application serving as a reference framework for developing a big data pipeline, complete with a broad range of use cases and powerful reusable core components.

Modern applications can ingest data and leverage analytics in real-time.  These analytics are based on machine learning models typically built using historical big data.  This reference application provides examples of connecting data-in-motion analytics to your application based on Big Data.

From IoT sensor data collection, to flow management, real-time stream processing and analytics, through to machine learning and prediction, this reference project aims to demonstrate the power of open source solutions.

## Outline

-   [Prerequisites](#prerequisites)
-   [Quick, How Do I Use It?!](#quick-how-do-i-use-it)
-   [Setup on existing HDF/HDP](#setup-on-existing-hdf-hdp)

## Prerequisites

-   Any HDF/HDP deployment.  For a ready-to-go environment, the [Hortonworks Sandbox](#) is recommended.
-   For integration with Schema Registry, download and run the setup script located at: <https://github.com/orendain/schema-registry-setup>

## Quick, How Do I Use It?!

// TODO

## Setup on existing HDF/HDP

1.  On your sandbox/cluster, download this project.
```
git clone https://github.com/orendain/trucking-iot.git
```

2.  Run the included automated deployment script.
```
cd trucking-iot
scripts/auto-deploy.sh
```

3.  On your local machine, also download this project.
```
git clone https://github.com/orendain/trucking-iot.git
```

4.  Run the included web application deployment script.
```
cd trucking-iot
scripts/builds/web-application.sh
```

5.  On your local machine, open a browser and navigate to the web application (by default, runs on port 1234): <http://localhost:1234>

## Commands

Schema Registrar

This subproject registers data type schema with the registry service

If changes are made to this project, you can apply the changes by running this project via sbt with:

```
sbt schemaRegistrar/run
```

# Trucking IoT

The Trucking IoT project is a modern real-time streaming application serving as a reference framework for developing a big data pipeline, complete with a broad range of use cases and powerful reusable core components.

Modern applications can ingest data and leverage analytics in real-time.  These analytics are based on machine learning models typically built using historical big data.  This reference application provides examples of connecting data-in-motion analytics to your application based on Big Data.

From IoT sensor data collection, to flow management, real-time stream processing and analytics, through to machine learning and prediction, this reference project aims to demonstrate the power of open source solutions.

## Outline

-   [Prerequisites](#prerequisites)
-   [Quick, How Do I Use It?!](#quick-how-do-i-use-it)
-   [Setup on existing HDF/HDP](#setup-on-existing-hdf-hdp)
-   [How It Works](#how-it-works)

## Prerequisites

-   An instance of the [Hortonworks HDF Sandbox](#).
-   (or, alternatively) Your own Ambari-powered cluster with ZooKeeper, NiFi, Storm and Kafka
-   For integration with Schema Registry, download and run the single-script setup located at: <https://github.com/orendain/schema-registry-setup>

## Quick, How Do I Use It?!

For an instance of the HDF Sandbox preloaded with this reference application, run one of the following Docker commands below.

For a Nifi -> Storm -> Nifi -> Kafka -> Web Application pipeline, with integration with Hortonworks Schema Registry:
```
```

For a Nifi -> Kafka -> Storm -> Kafka -> Web Application pipeline:
```
```

## Setup on existing HDF/HDP

> Note: If you're **not** on the HDF Sandbox, you'll need to replace the default cluster hostname "sandbox.hortonworks.com" in the following files, as well as check the port endpoints:
>
> trucking-schema-registrar/src/main/resources/application.conf
>
> trucking-storm-topology/src/main/resources/application.conf
>
> trucking-web-application/backend/conf/application.conf
>
> /scripts/*.sh

1.  On your sandbox/cluster, download this project.
```
git clone https://github.com/orendain/trucking-iot.git
```

2.  Run the included automated deployment script.  Read a comicbook, this may take a few minutes.

> Note: By default the application deploys a Nifi -> Storm -> Nifi -> Kafka -> Web Application pipeline, with integration with Hortonworks Schema Registry.
>
> To use a different pre-built pipeline, open/edit `scripts/setup-environment.sh` and `scripts/builds/storm-topology.sh` before running the commands below.
```
cd trucking-iot
scripts/auto-deploy.sh
```

3.  On your local machine, open a browser and navigate to the web application: <http://sandbox.hortonworks.com:25001>

4.  Optionally check out the NiFi Flow and Storm UI. 

## How it Works

For an indepth look at the different components, check out the [Trucking IoT Wiki](https://github.com/orendain/trucking-iot/wiki).  For any questions or requests for more documentation, feel free to open an issue or fork this repo and contribute!

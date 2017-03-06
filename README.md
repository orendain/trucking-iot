# Trucking IoT

TODO: Expand this readme.

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

1.  On the cluster, download this project.
```
git clone https://github.com/orendain/trucking-iot.git
```

2.  Run the included auto deploy script.
```
trucking-iot/scripts/auto-deploy.sh
```


## Download project and set up environment

Serverside (e.g. HDF node) download this project:


Next, run the included script to set up the environment:

```

```

## Run the

, then run the on-server-start script, and finally run the project

```
cd trucking-iot
./scripts/setup-environment.sh
./scripts/on-server-restart.sh
./scripts/run-project.sh
```



## Commands

Schema Registrar

This subproject registers data type schema with the registry service

If changes are made to this project, you can apply the changes by running this project via sbt with:

```
sbt schemaRegistrar/run
```

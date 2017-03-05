# Trucking IoT

TODO: Expand this readme.

## Outline

- [Requirement: Schema Registry](#requirement-schema-registry)
- [Creating the Sandbox](#creating-the-sandbox)


## Requirement: Schema Registry

As a reference application for a registry-enabled application, one requirement is a Schema Registry.  Download and run the setup script located at: https://github.com/orendain/schema-registry-setup


## Download project and set up environment

Download the project:
```
git clone https://github.com/orendain/trucking-iot.git
```

Next, run the included script to set up the environment:

```
trucking-iot/scripts/setup-environment.sh
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

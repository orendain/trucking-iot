# Trucking Scripts

A collection of scripts to help automate the environment setup and project building process.

- `setup-environment.sh` - Initial environment setup, only needs to be run once when the project is downloaded.
- `on-server-restart.sh` - Restarts necessary services that aren't brought up on a server restart.
- `run-project.sh` - Run the project, rebuilding changed components if necessary. 

### Notes
Ports used:
- 8090 - Schema Registry Webservice
- 8091 - NiFi Jetty WebSockets
- 8765 - NiFi Remote/SiteToSite
- 4557 - NiFi DistributedMapCacheClientService

###TODO: Things to scriptify:
- Create Storm View in Ambari if necessary
- Disable NiFi SSL if necessary

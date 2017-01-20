# trucking-nifi-bundle

First, clone and build the necessary library [trucking-simulator](https://github.com/orendain/trucking-simulator) (instructions in that readme).

Next, to build this NiFi bundle:
```
git clone https://github.com/orendain/trucking-nifi-bundle
cd trucking-nifi-bundle
mvn package
```

A `nar` file is built and saved to `./nifi-trucking-nar/target/nifi-trucking-nar-0.3.1.nar`.

Take this file and upload it to your NiFi library.  Upon a NiFi restart, you should see a `GetTruckingData` processor included in your NiFi instance.

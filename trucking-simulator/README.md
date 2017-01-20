# trucking-simulator

First, clone and build the necessary library [trucking-shared](https://github.com/orendain/trucking-shared) (instructions in that readme).

Next, to build this library and publish it locally:
```
git clone https://github.com/orendain/trucking-simulator
cd trucking-simulator
sbt publishLocal && sbt publish
```

To demo the simulator:
```
sbt run
```

By default, generated data is published to `/tmp/trucking-simulator/data.txt`.

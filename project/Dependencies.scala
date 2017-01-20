import sbt._

object Dependencies {

  // Scala libraries
  val config = "com.typesafe" % "config" % "1.3.1"
  val betterFiles = "com.github.pathikrit" %% "better-files" % "2.16.0"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.4"

  // Logging backends
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.8"

  // Akka libraries
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.4.14"

  // Apache libraries
  val nifiStormSpout = "org.apache.nifi" % "nifi-storm-spout" % "1.1.0"
  val stormCore = "org.apache.storm" % "storm-core" % "1.0.2" % "provided"
  val schemaRegistrySerdes = ("com.hortonworks.registries" % "schema-registry-serdes" % "0.1.0-SNAPSHOT")
  //val schemaRegistrySerdes = ("com.hortonworks.registries" % "schema-registry-serdes" % "0.0.1.2.2.0.0-5" from "http://nexus-private.hortonworks.com/nexus/content/groups/public/com/hortonworks/registries/schema-registry-serdes/0.0.1.2.2.0.0-5/schema-registry-serdes-0.0.1.2.2.0.0-5.jar")
    .exclude("org.slf4j", "log4j-over-slf4j")
    .exclude("commons-beanutils", "commons-beanutils-core")
    //.exclude("org.slf4j", "log4j-over-slf4j")
    //.exclude("commons-beanutils", "commons-beanutils-core")
    //.exclude("commons-collections", "commons-collections")
    //.exclude("commons-beanutils", "commons-beanutils")
  val schemaRegistryClient = ("com.hortonworks.registries" % "schema-registry-client" % "0.0.1.2.2.0.0-5" from "http://nexus-private.hortonworks.com/nexus/content/groups/public/com/hortonworks/registries/schema-registry-client/0.0.1.2.2.0.0-5/schema-registry-client-0.0.1.2.2.0.0-5.jar")
      .exclude("org.slf4j", "log4j-over-slf4j")
      .exclude("commons-beanutils", "commons-beanutils-core")
  val schemaRegistryCommon = ("com.hortonworks.registries" % "schema-registry-common" % "0.0.1.2.2.0.0-5" from "http://nexus-private.hortonworks.com/nexus/content/groups/public/com/hortonworks/registries/schema-registry-common/0.0.1.2.2.0.0-5/schema-registry-common-0.0.1.2.2.0.0-5.jar")
  val glass = "org.glassfish.jersey.core" % "jersey-client" % "2.25"
  val schemaRegistryCore = ("com.hortonworks.registries" % "schema-registry-core" % "0.0.1.2.2.0.0-5" from "http://nexus-private.hortonworks.com/nexus/content/groups/public/com/hortonworks/registries/schema-registry-core/0.0.1.2.2.0.0-5/schema-registry-core-0.0.1.2.2.0.0-5.jar")
    .exclude("org.slf4j", "slf4j-api")
    .exclude("org.slf4j", "log4j-over-slf4j")
    .exclude("commons-beanutils", "commons-beanutils-core")

  // Projects
  val simulatorDeps = Seq(akkaActor, config, betterFiles, scalaLogging, logback)
  val enrichmentDeps = Seq(scalaCsv, config, betterFiles)
  val schemaRegistrarDeps = Seq(schemaRegistrySerdes, config, scalaLogging, logback)
  val topologyDeps = Seq(nifiStormSpout, stormCore, glass, schemaRegistrySerdes, config, betterFiles, scalaLogging)
  //val topologyDeps = Seq(nifiStormSpout, stormCore, glass, schemaRegistrySerdes, schemaRegistryClient, schemaRegistryCommon, schemaRegistryCore, config, betterFiles, scalaLogging)
}

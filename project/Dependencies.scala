import sbt._

object Dependencies {

  /*
   * Dependency declarations for the different subprojects
   */
  lazy val simulatorDeps = Seq(akkaActor, config, betterFiles, scalaLogging, logback, kafkaClients)
  lazy val enrichmentDeps = Seq(scalaCsv, config, betterFiles)
  lazy val schemaRegistrarDeps = Seq(schemaRegistrySerdes, javaxXml, config, scalaLogging, logback)
  lazy val stormTopologyDeps = Seq(stormCoreProvided, stormNifi, stormKafkaClient, kafka, schemaRegistrySerdes, config, betterFiles, scalaLogging)
  lazy val stormTopologyJavaDeps = Seq(stormCoreProvided, stormKafka, stormKafkaClient, kafkaClients, config)
  lazy val webApplicationBackendDeps = Seq(scalaJsScripts, compass, bootstrap, akkaStreamKafka) ++ angular2Deps
  lazy val webApplicationFrontendDeps = Seq(leaflet, shocon)


  /*
   * Individual library dependencies
   * (library version changes are made in this section)
   */

  // Scala libraries
  lazy val config = "com.typesafe" % "config" % "1.3.1"
  lazy val betterFiles = "com.github.pathikrit" %% "better-files" % "3.4.0"
  lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.5"
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.8"


  // Logging-related dependencies
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"


  // Storm-related dependencies
  lazy val stormCoreProvided = "org.apache.storm" % "storm-core" % "1.1.0" % "provided"
  //lazy val stormCoreProvided = ("org.apache.storm" % "storm-core" % "1.1.0")
    //.exclude("org.apache.hadoop", "hadoop-auth") // vs storm-core/scheme-registry, but only when storm-core is not marked as provided
    //.exclude("ring-cors", "ring-cors") // vs storm-core/nifi-storm-spout, but only when storm-core is not marked as provided
  lazy val stormKafkaClient = "org.apache.storm" % "storm-kafka-client" % "1.1.0"
  lazy val stormKafka = "org.apache.storm" % "storm-kafka" % "1.1.0"
  lazy val stormNifi = "org.apache.nifi" % "nifi-storm-spout" % "1.2.0"
    //.exclude("ring-cors", "ring-cors") // vs storm-core/nifi-storm-spout, but only when storm-core is not marked as provided


  // See: http://stackoverflow.com/questions/33054294/kafkaspout-throws-noclassdeffounderror-for-log4j
  lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % "0.10.2.1"
  lazy val kafka = ("org.apache.kafka" %% "kafka" % "0.10.2.1")
    .exclude("org.apache.zookeeper", "zookeeper")
    .exclude("org.slf4j", "slf4j-log4j12")


  lazy val schemaRegistrySerdes = ("com.hortonworks.registries" % "schema-registry-serdes" % "0.3.0.3.0.1.1-5")
    .exclude("commons-beanutils", "commons-beanutils") // vs itself - commons-beanutils/commons-beanutils-core
    .exclude("commons-collections", "commons-collections") // vs itself - commons-beanutils/commons-beanutils-core
    .exclude("org.springframework", "spring-aop") // vs itself - aopalliance/intercept/ConstructorInvocation.class
  //  .exclude("org.apache.hadoop", "hadoop-auth") // vs storm-core/scheme-registry, but only when storm-core is not marked as provided
  lazy val javaxXml = "javax.xml.bind" % "jaxb-api" % "2.3.0"


  // Web application backend dependencies
  lazy val ngVersion = "2.4.3"
  lazy val scalaJsScripts = "com.vmunier" %% "scalajs-scripts" % "1.0.0"
  lazy val compass = "org.webjars.bower" % "compass-mixins" % "0.12.10"
  lazy val bootstrap = "org.webjars.bower" % "bootstrap-sass" % "3.3.6"
  lazy val angular2Deps = Seq(
    "org.webjars.npm" % "angular__common" % ngVersion,
    "org.webjars.npm" % "angular__compiler" % ngVersion,
    "org.webjars.npm" % "angular__core" % ngVersion,
    "org.webjars.npm" % "angular__http" % ngVersion,
    "org.webjars.npm" % "angular__forms" % ngVersion,
    "org.webjars.npm" % "angular__platform-browser-dynamic" % ngVersion,
    "org.webjars.npm" % "angular__platform-browser" % ngVersion,
    "org.webjars.npm" % "angular__router" % "3.4.3",
    "org.webjars.npm" % "systemjs" % "0.19.41",
    "org.webjars.npm" % "rxjs" % "5.0.3",
    "org.webjars.npm" % "reflect-metadata" % "0.1.9",
    "org.webjars.npm" % "zone.js" % "0.6.26",
    "org.webjars.npm" % "core-js" % "2.4.1",
    "org.webjars.npm" % "symbol-observable" % "1.0.4")

  lazy val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "0.14"


  // Web application frontend dependencies
  lazy val leaflet = "com.github.fancellu.scalajs-leaflet" % "scalajs-leaflet_sjs0.6_2.11" % "v0.1"
  lazy val shocon = "eu.unicredit" % "shocon_sjs0.6_2.11" % "0.1.1"
}

import sbt._

object Dependencies {

  // Scala libraries
  lazy val config = "com.typesafe" % "config" % "1.3.1"
  lazy val betterFiles = "com.github.pathikrit" %% "better-files" % "2.16.0"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.4"

  // Logging backends
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.1.8"

  // Akka libraries
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.4.14"

  // Apache libraries
  lazy val nifiStormSpout = "org.apache.nifi" % "nifi-storm-spout" % "1.1.0"
  lazy val stormCore = "org.apache.storm" % "storm-core" % "1.0.2"
  lazy val stormCoreProvided = "org.apache.storm" % "storm-core" % "1.0.2" % "provided"
  lazy val stormKafka = "org.apache.storm" % "storm-kafka" % "1.0.2"

  lazy val kafka = ("org.apache.kafka" %% "kafka" % "0.10.2.0")
    .exclude("org.apache.zookeeper", "zookeeper")
    .exclude("org.slf4j", "slf4j-log4j12")
  lazy val kafkaProducer = "org.apache.kafka" % "kafka-clients" % "0.10.2.0"

  lazy val stormHbase = ("org.apache.storm" % "storm-hbase" % "1.0.2")
    .exclude("tomcat", "jasper-compiler") // vs itself - org.mortbay.jetty/jsp-2.1/jars/jsp-2.1-6.1.14.jar
    .exclude("tomcat", "jasper-runtime") // vs itself - org.mortbay.jetty/jsp-2.1/jars/jsp-2.1-6.1.14.jar
    .exclude("javax.servlet", "servlet-api") // vs itself - org.mortbay.jetty/servlet-api-2.5/jars/servlet-api-2.5-6.1.14.jar
    .exclude("javax.servlet", "jsp-api") // vs itself - org.mortbay.jetty/servlet-api-2.5/jars/servlet-api-2.5-6.1.14.jar

  lazy val schemaRegistrySerdes = ("com.hortonworks.registries" % "schema-registry-serdes" % "0.0.1.3.0.0.0-55")
    .exclude("org.slf4j", "log4j-over-slf4j")
    .exclude("javax.servlet", "servlet-api") // vs stormHBase - org.mortbay.jetty/servlet-api-2.5/jars/servlet-api-2.5-6.1.14.jar
    .exclude("commons-beanutils", "commons-beanutils-core")
    .exclude("org.mortbay.jetty", "jsp-api-2.1") // vs stormHBase - org.mortbay.jetty/jsp-api-2.1/jars/jsp-api-2.1-6.1.14.jar


  lazy val ngVersion = "2.4.3"
  val angular2Deps = Seq(
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

  //val playDeps = Seq(filters, cache, ws)
  val scalaJsScripts = "com.vmunier" %% "scalajs-scripts" % "1.0.0"
  val webjars = "org.webjars" %% "webjars-play" % "2.5.0"


  // Web dependencies
  val compass = "org.webjars.bower" % "compass-mixins" % "0.12.10"
  val bootstrap = "org.webjars.bower" % "bootstrap-sass" % "3.3.6"
  val leaflet = "com.github.fancellu.scalajs-leaflet" % "scalajs-leaflet_sjs0.6_2.11" % "v0.1"


  // Projects
  val simulatorDeps = Seq(akkaActor, config, betterFiles, scalaLogging, logback, kafkaProducer)
  val enrichmentDeps = Seq(scalaCsv, config, betterFiles)
  val schemaRegistrarDeps = Seq(schemaRegistrySerdes, config, scalaLogging, logback)
  val topologyDeps = Seq(nifiStormSpout, stormCoreProvided, kafka, stormKafka, schemaRegistrySerdes, config, betterFiles, scalaLogging)

  val webApplicationBackendDeps = angular2Deps ++ Seq(scalaJsScripts, webjars, compass, bootstrap, stormCore)
  val webApplicationFrontendDeps = Seq(leaflet)
}

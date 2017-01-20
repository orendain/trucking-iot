
lazy val commonSettings = Seq(
  version := "0.3.2",
  scalaVersion := "2.11.8",
  description := """Trucking IoT application.""",
  homepage := Some(url("https://github.com/orendain/trucking-iot")),
  organization := "com.orendainx.hortonworks",
  organizationHomepage := Some(url("https://github.com/orendain/trucking-iot")),
  licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))),
  promptTheme := ScalapenosTheme
)

lazy val truckingIot = (project in file(".")).
  settings(
    commonSettings
  )

lazy val common = (project in file("trucking-common")).
  settings(
    commonSettings,
    name := "trucking-common"
  )

lazy val simulator = (project in file("trucking-simulator"))
  .dependsOn(common)
  .settings(
    commonSettings,
    name := "trucking-simulator",
    libraryDependencies ++= Dependencies.simulatorDeps
  )

lazy val enrichment = (project in file("trucking-enrichment"))
  .dependsOn(common)
  .settings(
    commonSettings,
    name := "trucking-enrichment",
    libraryDependencies ++= Dependencies.enrichmentDeps
  )

lazy val schemaRegistrar = (project in file("trucking-schema-registrar"))
  .dependsOn(common)
  .settings(
    commonSettings,
    name := "trucking-schema-registrar",
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Dependencies.schemaRegistrarDeps
  )

lazy val topology = (project in file("trucking-topology"))
  .dependsOn(common)
  .settings(
    commonSettings,
    name := "trucking-topology",
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Dependencies.topologyDeps,

    assemblyMergeStrategy in assembly := {
      case PathList("org", "apache", "commons", "collections", xs @ _*)      => MergeStrategy.first
      case PathList("org", "slf4j", xs @ _*)      => MergeStrategy.first
      case PathList("org", "apache", "http", xs @ _*)      => MergeStrategy.first
      case PathList("org", "apache", "commons", xs @ _*)      => MergeStrategy.first
      case PathList("org", "jvnet", "hk2", xs @ _*)      => MergeStrategy.last
      case PathList("org", "glassfish", xs @ _*)      => MergeStrategy.last
      case PathList("javassist", xs @ _*)      => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },

    scalacOptions ++= Seq("-feature")
  )


lazy val execScript = taskKey[Unit]("Execute the shell script")

lazy val nifiBundle = (project in file("trucking-nifi-bundle"))
  .dependsOn(simulator)
  .settings(
    commonSettings,
    execScript := { Process("mvn clean package", baseDirectory.value) ! } ,
    (`compile` in Compile) <<= (compile in Compile).dependsOn(execScript)
  )







lazy val ngVersion = "2.4.3"
lazy val webAppBackend = (project in file("trucking-web-app/backend"))
  .settings(
    commonSettings,
    name := "trucking-web-app-backend",
  scalaJSProjects := Seq(webAppFrontend),


  unmanagedResources in Assets ++= Seq(
    baseDirectory.value / "../frontend/target/scala-2.11/trucking-web-app-frontend-sjsx.js",
    baseDirectory.value / "../frontend/target/scala-2.11/trucking-web-app-frontend-fastop.js"
  ),
  unmanagedResourceDirectories in Assets += baseDirectory.value / "../frontend/src/main/resources",

  pipelineStages in Assets := Seq(scalaJSPipeline),
  //pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
  //compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (map, Compile)),
  resolvers += Resolver.mavenLocal,
  libraryDependencies ++= Seq(
    filters,
    cache,
    ws,
    "com.vmunier" %% "scalajs-scripts" % "1.0.0",
    "org.webjars" %% "webjars-play" % "2.5.0",

    //angular2 dependencies
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
    "org.webjars.npm" % "symbol-observable" % "1.0.4",

    "org.webjars.bower" % "compass-mixins" % "0.12.10",
    "org.webjars.bower" % "bootstrap-sass" % "3.3.6",


    "com.orendainx.hortonworks" %% "trucking-topology" % "0.3.2",
    "org.apache.storm" % "storm-core" % "1.0.2"
  ),

  scalacOptions += "-Yresolve-term-conflict:package",

  promptTheme := ScalapenosTheme,
  shellPrompt := (state ⇒ promptTheme.value.render(state))
).enablePlugins(PlayScala)


lazy val webAppFrontend = (project in file("trucking-web-app/frontend"))
  .settings(
    commonSettings,
  name := "trucking-web-app-frontend",
  //persistLauncher := true,
  //persistLauncher in Test := false,
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies ++= Seq(
    //"org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.orendainx.hortonworks" %% "trucking-common" % "0.3.2",
    "com.github.fancellu.scalajs-leaflet" % "scalajs-leaflet_sjs0.6_2.11" % "v0.1",
    "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M6"
  ),
  jsDependencies ++= Seq(
    "org.webjars.npm" % "leaflet" % "1.0.2" / "leaflet.js" commonJSName "Leaflet"
  ),
  //ngBootstrap := Some("Frontend") //qualified name (including packages) of Scala class called NAME_OF_THE_MODULE_TO_BOOTSTRAP
  ngBootstrap := Some("com.orendainx.hortonworks.trucking.webapp.AppModule") //qualified name (including packages) of Scala class called NAME_OF_THE_MODULE_TO_BOOTSTRAP
).enablePlugins(ScalaJSPlugin, ScalaJSWeb, Angulate2Plugin)

// loads the server project at sbt startup
//onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

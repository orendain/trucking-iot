
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

lazy val common = (project in file("trucking-common"))
    .aggregate(commonJVM, commonJS)
lazy val commonCross = crossProject.in(file("trucking-common"))
  .settings(
    commonSettings,
    name := "trucking-common"
  )
lazy val commonJVM = commonCross.jvm
lazy val commonJS = commonCross.js

lazy val simulator = (project in file("trucking-simulator"))
  .dependsOn(commonJVM)
  .settings(
    isSnapshot := true,
    commonSettings,
    name := "trucking-simulator",
    libraryDependencies ++= Dependencies.simulatorDeps
  )

lazy val enrichment = (project in file("trucking-enrichment"))
  .dependsOn(commonJVM)
  .settings(
    commonSettings,
    name := "trucking-enrichment",
    libraryDependencies ++= Dependencies.enrichmentDeps
  )

lazy val schemaRegistrar = (project in file("trucking-schema-registrar"))
  .dependsOn(commonJVM)
  .settings(
    commonSettings,
    name := "trucking-schema-registrar",
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Dependencies.schemaRegistrarDeps
  )

lazy val topology = (project in file("trucking-topology"))
  .dependsOn(commonJVM)
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
    execScript := {
      (publishM2 in Compile in commonJVM).value
      (publishM2 in Compile in simulator).value
      Process("mvn clean package", baseDirectory.value) !
    } ,
    (`compile` in Compile) <<= (compile in Compile).dependsOn(execScript)
  )








lazy val webAppBackend = (project in file("trucking-web-app/backend"))
  .dependsOn(topology)
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
    compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
    resolvers += Resolver.mavenLocal, // For topology > schema-registry-serdes
    libraryDependencies ++= (Seq(filters, cache, ws) ++ Dependencies.webAppBackendDeps),
    scalacOptions += "-Yresolve-term-conflict:package",
    shellPrompt := (state ⇒ promptTheme.value.render(state)) // Override Play's Sbt prompt
  ).enablePlugins(PlayScala)

lazy val webAppFrontend = (project in file("trucking-web-app/frontend"))
  .dependsOn(commonJS)
  .settings(
    commonSettings,
    name := "trucking-web-app-frontend",
    resolvers += "jitpack" at "https://jitpack.io", // For scalajs-leaflet
    libraryDependencies ++= Seq("io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M6") ++ Dependencies.webAppFrontendDeps,
    jsDependencies ++= Seq("org.webjars.npm" % "leaflet" % "1.0.2" / "leaflet.js" commonJSName "Leaflet"),
    ngBootstrap := Some("com.orendainx.hortonworks.trucking.webapp.AppModule")
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb, Angulate2Plugin)

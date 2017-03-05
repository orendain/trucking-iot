/*
 * Definition of common settings for all subprojects
 */
lazy val commonSettings = Seq(
  version := "0.3.2",
  scalaVersion := "2.11.8",
  description := """Trucking IoT application.""",
  organization := "com.orendainx.hortonworks",
  homepage := Some(url("https://github.com/orendain/trucking-iot")),
  organizationHomepage := Some(url("https://github.com/orendain/trucking-iot")),
  licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))),
  promptTheme := ScalapenosTheme,
  autoAPIMappings := true
)



/*
 * Root project definition
 */
lazy val truckingIot = (project in file("."))
  .settings(commonSettings)



/*
 * Subproject definition for trucking-common
 */
lazy val common = (project in file("trucking-common")).aggregate(commonJVM, commonJS)
lazy val commonCross = crossProject.in(file("trucking-common"))
  .settings(
    commonSettings,
    name := "trucking-common",
    isSnapshot := true // I forget exactly why this was necessary
  )
lazy val commonJVM = commonCross.jvm
lazy val commonJS = commonCross.js


/*
 * Subproject definition for trucking-simulator
 */
lazy val simulator = (project in file("trucking-simulator"))
  .dependsOn(commonJVM)
  .settings(
    commonSettings,
    name := "trucking-simulator",
    libraryDependencies ++= Dependencies.simulatorDeps,
    isSnapshot := true,
  )



/*
 * Subproject definition for trucking-enrichment
 */
lazy val enrichment = (project in file("trucking-enrichment"))
  .dependsOn(commonJVM)
  .settings(
    commonSettings,
    name := "trucking-enrichment",
    libraryDependencies ++= Dependencies.enrichmentDeps,
    isSnapshot := true
  )



/*
 * Subproject definition for trucking-schema-registrar
 */
lazy val schemaRegistrar = (project in file("trucking-schema-registrar"))
  .dependsOn(commonJVM)
  .settings(
    commonSettings,
    name := "trucking-schema-registrar",
    resolvers += Resolver.mavenLocal,
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.schemaRegistrarDeps
  )



/*
 * Subproject definition for trucking-storm-topology
 */
lazy val stormTopology = (project in file("trucking-storm-topology"))
  .dependsOn(commonJVM)
  .settings(
    commonSettings,
    name := "trucking-storm-topology",
    resolvers += Resolver.mavenLocal,
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.topologyDeps,

    // TODO: Temporary, change once schema registry release updated
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "ws", "rs", "core", "MultivaluedMap.class")      => MergeStrategy.last // com.sun.jersey/jersey-core vs javax.ws.rs/javax.ws.rs-api
      case PathList("javax", "ws", "rs", "core", xs @ _) if xs.startsWith("Response")      => MergeStrategy.last // com.sun.jersey/jersey-core vs javax.ws.rs/javax.ws.rs-api
      case PathList("javax", "ws", "rs", "core", xs @ _*)      => MergeStrategy.first // com.sun.jersey/jersey-core vs javax.ws.rs/javax.ws.rs-api
      case PathList("javax", "ws", xs @ _*)      => MergeStrategy.first // com.sun.jersey/jersey-core vs javax.ws.rs/javax.ws.rs-api
      case PathList("javax", "el", xs @ _*)      => MergeStrategy.first // javax.servlet.jsp vs org.mortbay.jetty
      case PathList("javax", "servlet", xs @ _*)      => MergeStrategy.first // javax.servlet.jsp vs org.mortbay.jetty
      case PathList("org", "apache", "commons", "collections", xs @ _*)      => MergeStrategy.first
      //case PathList("org", "slf4j", xs @ _*)      => MergeStrategy.first
      case PathList("org", "apache", "http", xs @ _*)      => MergeStrategy.first
      case PathList("org", "apache", "commons", xs @ _*)      => MergeStrategy.first
      case PathList("org", "jvnet", "hk2", xs @ _*)      => MergeStrategy.last
      case PathList("org", "glassfish", xs @ _*)      => MergeStrategy.last
      case PathList("javassist", xs @ _*)      => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },

    scalacOptions ++= Seq("-feature", "-Yresolve-term-conflict:package")
)



/*
 * Subproject definition for trucking-nifi-bundle
 */
lazy val execScript = taskKey[Unit]("Execute the shell script")
lazy val nifiBundle = (project in file("trucking-nifi-bundle"))
  .dependsOn(simulator, enrichment)
  .settings(
    commonSettings,
    execScript := {
      (publishM2 in Compile in commonJVM).value
      (publishM2 in Compile in simulator).value
      (publishM2 in Compile in enrichment).value
      Process("mvn clean package", baseDirectory.value) !
    },
    (`compile` in Compile) := (compile in Compile).dependsOn(execScript).value
  )



/*
 * Subproject definition for trucking-web-application/backend
 */
lazy val webApplicationBackend = (project in file("trucking-web-application/backend"))
  .dependsOn(stormTopology)
  .settings(
    commonSettings,
    name := "trucking-web-application-backend",
    scalaJSProjects := Seq(webApplicationFrontend),

    unmanagedResources in Assets ++= Seq(
      baseDirectory.value / "../frontend/target/scala-2.11/trucking-web-app-frontend-sjsx.js",
      baseDirectory.value / "../frontend/target/scala-2.11/trucking-web-app-frontend-fastop.js"
    ),
    unmanagedResourceDirectories in Assets += baseDirectory.value / "../frontend/src/main/resources",

    pipelineStages in Assets := Seq(scalaJSPipeline),
    //pipelineStages := Seq(digest, gzip),
    compile in Compile := (compile in Compile).dependsOn(scalaJSPipeline).value,
    resolvers += Resolver.mavenLocal, // For topology > schema-registry-serdes
    libraryDependencies ++= (Seq(filters, cache, ws) ++ Dependencies.webApplicationBackendDeps),
    scalacOptions += "-Yresolve-term-conflict:package",
    shellPrompt := (state ⇒ promptTheme.value.render(state)), // Override Play's Sbt prompt
    PlayKeys.devSettings := Seq("play.server.http.port" -> "1234")
  ).enablePlugins(PlayScala)



/*
 * Subproject definition for trucking-web-application/frontend
 */
lazy val webApplicationFrontend = (project in file("trucking-web-application/frontend"))
  .dependsOn(commonJS)
  .settings(
    commonSettings,
    name := "trucking-web-applicationfrontend",
    resolvers += "jitpack" at "https://jitpack.io", // For scalajs-leaflet
    libraryDependencies ++= Seq("io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M6") ++ Dependencies.webApplicationFrontendDeps,
    jsDependencies ++= Seq("org.webjars.npm" % "leaflet" % "1.0.2" / "leaflet.js" commonJSName "Leaflet"),
    ngBootstrap := Some("com.orendainx.hortonworks.trucking.webapp.AppModule")
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb, Angulate2Plugin)

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
  autoAPIMappings := true // TODO: I forget exactly why this was necessary
)



/*
 * Root project definition
 */
lazy val truckingIot = (project in file("."))
  .settings(commonSettings)



/*
 * Subproject definition for trucking-commons
 */
lazy val commons = (project in file("trucking-commons")).aggregate(commonsJVM, commonsJS)
lazy val commonsJVM = commonsCross.jvm
lazy val commonsJS = commonsCross.js
lazy val commonsCross = crossProject.in(file("trucking-commons"))
  .settings(
    commonSettings,
    name := "trucking-commons",
    isSnapshot := true // TODO: I forget exactly why this was necessary
  )



/*
 * Subproject definition for trucking-simulator
 */
lazy val simulator = (project in file("trucking-simulator"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-simulator",
    libraryDependencies ++= Dependencies.simulatorDeps,
    isSnapshot := true
  )



/*
 * Subproject definition for trucking-enrichment
 */
lazy val enrichment = (project in file("trucking-enrichment"))
  .dependsOn(commonsJVM)
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
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-schema-registrar",
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.schemaRegistrarDeps
  )



/*
 * Subproject definition for trucking-nifi-bundle
 *
 * Because this subproject is built with Maven instead of Sbt, we set execScript to publish all dependencies to the local
 * M2 repository so those libraries can be accessible by the subproject during Maven's build process.
 */
lazy val execScript = taskKey[Unit]("Execute the shell script")
lazy val nifiBundle = (project in file("trucking-nifi-bundle"))
  .dependsOn(commonsJVM, simulator, enrichment)
  .settings(
    commonSettings,
    execScript := {
      (publishM2 in Compile in commonsJVM).value
      (publishM2 in Compile in simulator).value
      (publishM2 in Compile in enrichment).value
      Process("mvn clean package", baseDirectory.value) !
    },
    (`compile` in Compile) := (compile in Compile).dependsOn(execScript).value,
    (Keys.`package` in Compile) := (Keys.`package` in Compile).dependsOn(execScript).value
  )



/*
 * Subproject definition for trucking-storm-topology
 */
lazy val stormTopology = (project in file("trucking-storm-topology"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-storm-topology",
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.stormTopologyDeps,

    // TODO: Temporary, change once schema registry release updated
    assemblyMergeStrategy in assembly := {
      /*case PathList("javax", "ws", "rs", "core", "MultivaluedMap.class")      => MergeStrategy.last // com.sun.jersey/jersey-core vs javax.ws.rs/javax.ws.rs-api
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
      */
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },

    scalacOptions ++= Seq("-feature", "-Yresolve-term-conflict:package")
)



/*
 * Subproject definition for trucking-web-application/backend
 *
 * The project is build on top of the Play Framework and depends on the subproject webApplicationFrontend,
 * developed using ScalaJS.
 */
lazy val webApplicationBackend = (project in file("trucking-web-application/backend"))
  .settings(
    commonSettings,
    name := "trucking-web-application-backend",

    // Link to ScalaJS subproject
    scalaJSProjects := Seq(webApplicationFrontend),
    compile in Compile := (compile in Compile).dependsOn(scalaJSPipeline).value,
    pipelineStages in Assets := Seq(scalaJSPipeline), // TODO: add digest/gzip ?

    // Move some frontend static resources into the subproject's assets directory
    unmanagedResourceDirectories in Assets += baseDirectory.value / "../frontend/src/main/resources",
    unmanagedResources in Assets ++= Seq(
      baseDirectory.value / "../frontend/target/scala-2.11/trucking-web-application-frontend-sjsx.js",
      baseDirectory.value / "../frontend/target/scala-2.11/trucking-web-application-frontend-fastop.js"
    ),

    libraryDependencies ++= Dependencies.webApplicationBackendDeps ++ Seq(filters, cache, ws),
    PlayKeys.devSettings := Seq("play.server.http.port" -> "15001"), // Custom port when deployed using Sbt's run command
    shellPrompt := (state ⇒ promptTheme.value.render(state)), // Override Play's default Sbt prompt
    scalacOptions += "-Yresolve-term-conflict:package"
  ).enablePlugins(PlayScala)



/*
 * Subproject definition for trucking-web-application/frontend
 *
 * Build using ScalaJS, leverages the Angular2 framework, using ScalaJS facades provided by the ScalaJS Angulate2 plugin
 */
lazy val webApplicationFrontend = (project in file("trucking-web-application/frontend"))
  .dependsOn(commonsJS)
  .settings(
    commonSettings,
    name := "trucking-web-application-frontend",
    resolvers += "jitpack" at "https://jitpack.io", // For scalajs-leaflet
    libraryDependencies ++= Dependencies.webApplicationFrontendDeps,
    jsDependencies ++= Seq("org.webjars.npm" % "leaflet" % "1.0.2" / "leaflet.js" commonJSName "Leaflet"),
    ngBootstrap := Some("com.orendainx.hortonworks.trucking.webapplication.AppModule")
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb, Angulate2Plugin)

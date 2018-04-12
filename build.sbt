/*
 * Definition of common settings for all subprojects
 */
lazy val commonSettings = Seq(
  version := "0.5.4",
  isSnapshot := false,
  //scalaVersion := "2.11.8",
  scalaVersion := "2.12.4",
  description := """Trucking IoT application.""",
  sonatypeProfileName := "com.orendainx",
  organization := "com.orendainx.trucking",
  homepage := Some(url("https://github.com/orendain/trucking-iot")),
  organizationHomepage := Some(url("https://github.com/orendain/trucking-iot")),
  licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))),
  promptTheme := ScalapenosTheme,
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
  autoAPIMappings := true,
  pomIncludeRepository := { _ => false },
  scmInfo := Some(ScmInfo(url("https://github.com/orendain/trucking-iot"), "scm:git@github.com/orendain/trucking-iot.git")),
  developers := List(Developer(
    id    = "orendain",
    name  = "Edgar Orendain",
    email = "edgar@orendainx.com",
    url   = url("http://www.orendainx.com")
  )),
  publishTo := Some(
    if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
    else Opts.resolver.sonatypeStaging
  )
  //publishMavenStyle := true
)



/*
 * Root project definition
 */
lazy val truckingIot = (project in file("."))
  .settings(commonSettings)



/*
 * Subproject definition for commons
 */
lazy val commons = (project in file("commons")).aggregate(commonsJVM, commonsJS)
lazy val commonsJVM = commonsCross.jvm
lazy val commonsJS = commonsCross.js
lazy val commonsCross = crossProject.in(file("commons"))
  .settings(
    commonSettings,
    name := "trucking-commons"
  )



/*
 * Subproject definition for enrichment
 */
lazy val enrichment = (project in file("enrichment"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-enrichment",
    libraryDependencies ++= Dependencies.enrichmentDeps
  )



/*
 * Subproject definition for simulator
 */
lazy val simulator = (project in file("simulator"))
  .dependsOn(commonsJVM, enrichment)
  .settings(
    commonSettings,
    name := "trucking-simulator",
    libraryDependencies ++= Dependencies.simulatorDeps,
    mainClass := Some("com.orendainx.trucking.simulator.simulators.EnrichToKafkaSimulator")
  )



/*
 * Subproject definition for schema-registrar
 */
lazy val schemaRegistrar = (project in file("schema-registrar"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-schema-registrar",
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.schemaRegistrarDeps
  )



/*
 * Subproject definition for nifi-bundle
 *
 * Because this subproject is built with Maven instead of Sbt, we set execScript to publish all dependencies to the local
 * M2 repository so those libraries can be accessible by the subproject during Maven's build process.
 */
lazy val packageScript = taskKey[Unit]("Package this project using Maven via shell script")
lazy val nifiBundle = (project in file("nifi-bundle"))
  .dependsOn(commonsJVM, simulator, enrichment)
  .settings(
    commonSettings,
    packageScript := {
      //(publishM2 in Compile in commonsJVM).value
      //(publishM2 in Compile in simulator).value
      //(publishM2 in Compile in enrichment).value
      scala.sys.process.Process("mvn clean package", baseDirectory.value) !
    },
    (Keys.`package` in Compile) := (Keys.`package` in Compile).dependsOn(packageScript).value
  )



/*
 * Subproject definition for storm-topology
 */
lazy val stormTopology = (project in file("storm-topology"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-storm-topology",
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.stormTopologyDeps,
    scalacOptions ++= Seq("-feature", "-Yresolve-term-conflict:package")
  )



/*
 * Subproject definition for storm-topology-java
 */
lazy val stormTopologyJava = (project in file("storm-topology-java"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-storm-topology-java",
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Dependencies.stormTopologyJavaDeps,
    autoScalaLibrary := false,
    mainClass in (Compile, run) := Some("com.orendainx.trucking.storm.java.topologies.KafkaToKafka"),
    assemblyMergeStrategy in assembly := {
      case PathList(x) if x.endsWith("clj") => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )




/*
 * Subproject definition for web-application/backend
 *
 * The project is build on top of the Play Framework and depends on the subproject webApplicationFrontend,
 * developed using ScalaJS.
 */
/*
lazy val webApplicationBackend = (project in file("web-application/backend"))
  .settings(
    commonSettings,
    name := "web-application-backend",

    // Link to ScalaJS subproject
    scalaJSProjects := Seq(webApplicationFrontend),
    compile in Compile := (compile in Compile).dependsOn(scalaJSPipeline).value,
    pipelineStages in Assets := Seq(scalaJSPipeline), // TODO: add digest/gzip ?

    // Move some frontend static resources into the subproject's assets directory
    unmanagedResourceDirectories in Assets += baseDirectory.value / "../frontend/src/main/resources",
    unmanagedResources in Assets ++= Seq(
      baseDirectory.value / "../frontend/target/scala-2.11/web-application-frontend-sjsx.js",
      baseDirectory.value / "../frontend/target/scala-2.11/web-application-frontend-fastop.js"
    ),

    libraryDependencies ++= Dependencies.webApplicationBackendDeps ++ Seq(filters, cache, ws),
    PlayKeys.devSettings := Seq("play.server.http.port" -> "15100"), // Custom port when deployed using Sbt's run command
    shellPrompt := (state ⇒ promptTheme.value.render(state)), // Override Play's default Sbt prompt
    scalacOptions += "-Yresolve-term-conflict:package"

    // For use with sbt-assembly
    //mainClass in assembly := Some("play.core.server.ProdServerStart"),
    //fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
  ).enablePlugins(PlayScala)
*/


/*
 * Subproject definition for web-application/frontend
 *
 * Build using ScalaJS, leverages the Angular2 framework, using ScalaJS facades provided by the ScalaJS Angulate2 plugin
 */
/*
lazy val webApplicationFrontend = (project in file("web-application/frontend"))
  .dependsOn(commonsJS)
  .settings(
    commonSettings,
    name := "web-application-frontend",
    resolvers += "jitpack" at "https://jitpack.io", // For scalajs-leaflet
    libraryDependencies ++= Dependencies.webApplicationFrontendDeps,
    jsDependencies ++= Seq("org.webjars.npm" % "leaflet" % "1.0.2" / "leaflet.js" commonJSName "Leaflet"),
    ngBootstrap := Some("com.orendainx.trucking.webapplication.AppModule")//,
    //resourceDirectory in Compile := baseDirectory.value / "../backend/conf",
    //unmanagedResourceDirectories in Compile += baseDirectory.value / "../backend/conf"
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb, Angulate2Plugin)
*/
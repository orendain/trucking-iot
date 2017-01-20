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

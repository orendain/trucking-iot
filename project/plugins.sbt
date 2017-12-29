logLevel := Level.Info

// For finding dependency issues
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

// For publishing JARs, used by trucking-topology
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

// Sbt and Git prompt
addSbtPlugin("com.scalapenos" % "sbt-prompt" % "1.0.2")

// GPG and publishing
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

// ScalaJS plugin
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.21")

/*
// Web application plugins

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.18")

// ScalaJS plugins
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")

// Angulate2
addSbtPlugin("de.surfice" % "sbt-angulate2" % "0.1.0-RC1")

// Web plugins
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.8")
*/

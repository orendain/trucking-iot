logLevel := Level.Info

// For finding dependency issues
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// For publishing JARs, used by trucking-topology
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")

// ScalaJS plugins
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.16")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.3")

// Angulate2
addSbtPlugin("de.surfice" % "sbt-angulate2" % "0.0.5")

// Web plugins
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.8")
//addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

// Sbt and Git prompt
addSbtPlugin("com.scalapenos" % "sbt-prompt" % "1.0.0")

// GPG and publishing
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

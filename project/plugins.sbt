logLevel := Level.Warn

// For finding dependency issues
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// For publishing JARs, used by trucking-topology
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

addSbtPlugin("com.scalapenos" % "sbt-prompt" % "1.0.0")
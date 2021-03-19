addSbtPlugin("de.heikoseeberger" % "sbt-header" % "4.1.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

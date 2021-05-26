
val googleCloudVersion = "1.113.15"
val monixVersion = "2.3.3"
val scalaTestVersion = "3.0.4"

lazy val root = (project in file(".")).
  enablePlugins(GitVersioning).
  settings(
    name := "sbt-gcs",
    organization := "com.github.saint1991",
    organizationName := "saint1991",
    description := "GCS Plugin for sbt",
    sbtPlugin := true,
    scalaVersion := "2.12.4",
    sbtVersion := "1.1.0",
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-storage" % googleCloudVersion,
      "com.typesafe.akka" %% "akka-actor" % "2.6.1",
      "com.typesafe.akka" %% "akka-stream" % "2.6.1",
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.5" % Test,
      "io.monix" %% "monix-reactive" % monixVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  ).
  settings(
    publishMavenStyle := false,
    startYear := Some(2018),
    licenses += ("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0")),
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None
  )

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
}
scriptedBufferLog := false

import sbt.Keys._



name := "bwhc-authentication"
organization in ThisBuild := "de.bwhc"
scalaVersion in ThisBuild := "2.13.8"
version in ThisBuild := "1.0-SNAPSHOT"


scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-feature",
  "-deprecation"
)


libraryDependencies in ThisBuild ++= Seq(
  guice,
  "org.scalatest"          %% "scalatest"          % "3.1.1" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "com.typesafe.play"      %% "play"               % "2.8.1",
  "de.bwhc"                %% "utils"              % "1.0-SNAPSHOT",
  "de.bwhc"                %% "user-service-api"   % "1.0-SNAPSHOT"
)


lazy val root = project
  .in(file("."))
  .settings(
    publish / skip := true
  )
  .aggregate(
    api,
    fake_session_manager,
    session_manager_impl
  )


lazy val api = project
  .settings(
    name := "authentication-api",
  )


lazy val fake_session_manager = project
  .settings(
    name := "fake-session-manager",
  )
  .dependsOn(api)


lazy val session_manager_impl = project
  .settings(
    name := "session-manager-impl",
  )
  .dependsOn(api)

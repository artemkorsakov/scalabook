import Dependencies.Version.Scala

ThisBuild / organization      := "ru.gitflic.artemkorsakov"
ThisBuild / version           := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion      := Scala
ThisBuild / semanticdbEnabled := true

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-explain",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Ykind-projector"
  )

lazy val root = (project in file("."))
  .aggregate(examples)
  .settings(
    name           := "scalabook",
    publish / skip := true
  )

lazy val examples = (project in file("examples"))
  .settings(name := "scalabook-examples")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Dependencies.examples,
    libraryDependencies ++= Dependencies.examplesTests
  )

lazy val commonSettings =
  Seq(
    Compile / console / scalacOptions --= Seq(
      "-Wunused:_",
      "-Xfatal-warnings"
    ),
    Test / console / scalacOptions :=
      (Compile / console / scalacOptions).value
  )

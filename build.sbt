import Dependencies.Version._

ThisBuild / organization := "ru.gitflic.artemkorsakov"
ThisBuild / version      := "0.0.0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := Scala

lazy val examples = (project in file("examples"))
  .settings(
    name := "scalabook-examples",
    libraryDependencies ++= Dependencies.examples,
    libraryDependencies ++= Dependencies.examplesTests
  )

lazy val root = (project in file("."))
  .aggregate(examples)
  .settings(
    name           := "scalabook",
    publish / skip := true
  )

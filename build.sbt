import Dependencies.Version._

ThisBuild / organization := "ru.gitflic.artemkorsakov"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := Scala

lazy val examples = (project in file("examples"))
  .settings(
    name := "scalabook-examples",
    libraryDependencies ++= Dependencies.examples
  )

lazy val root = (project in file("."))
  .aggregate(examples)
  .settings(
    name := "scalabook"
  )

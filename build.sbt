import Dependencies.Version._

ThisBuild / organization := "ru.gitflic.artemkorsakov"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := Scala3

lazy val examples = (project in file("examples"))
  .settings(
    name := "examples",
    libraryDependencies ++= Dependencies.examples
  )

lazy val root = (project in file("."))
  .aggregate(examples)
  .settings(
    name := "scalabook"
  )

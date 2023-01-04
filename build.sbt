import Dependencies.Version._

inThisBuild(
  List(
    organization      := "ru.gitflic.artemkorsakov",
    version           := "0.0.1-SNAPSHOT",
    scalaVersion      := Scala,
    semanticdbEnabled := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )
)

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

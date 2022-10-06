import Dependencies.Version._
import sbt._

object Dependencies {

  object Version {
    val Scala = "3.1.3"

    val scalaz = "7.3.6"

    val munit = "0.7.29"
  }

  val examples: Seq[ModuleID] = Seq(
    "org.scalaz" %% "scalaz-core" % scalaz,
    "org.scalaz" %% "scalaz-effect" % scalaz,
    "org.scalaz" %% "scalaz-iteratee" % scalaz
  )

  val examplesTests: Seq[ModuleID] = Seq(
    "org.scalameta" %% "munit" % munit,
    "org.scalameta" %% "munit-scalacheck" % munit,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalaz
  ).map(_ % Test)

}

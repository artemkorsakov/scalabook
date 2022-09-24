import Dependencies.Version.munit
import sbt._

object Dependencies {

  object Version {
    val Scala3 = "3.1.3"

    val munit = "0.7.29"
  }

  val examples: Seq[ModuleID] = Seq(
    "org.scalameta" %% "munit" % munit % Test,
    "org.scalameta" %% "munit-scalacheck" % munit % Test
  )

}

import Dependencies.Version._
import sbt._

object Dependencies {

  object Version {
    val Scala = "3.2.1"

    val cats       = "2.9.0"
    val catsEffect = "3.4.1"
    val http4s     = "0.23.12"
    val scalaz     = "7.3.6"

    val munit           = "0.7.29"
    val munitCatsEffect = "1.0.7"
  }

  val examples: Seq[ModuleID] = Seq(
    "org.http4s"    %% "http4s-blaze-server" % http4s,
    "org.http4s"    %% "http4s-dsl"          % http4s,
    "org.scalaz"    %% "scalaz-core"         % scalaz,
    "org.scalaz"    %% "scalaz-effect"       % scalaz,
    "org.scalaz"    %% "scalaz-iteratee"     % scalaz,
    "org.typelevel" %% "cats-core"           % cats,
    "org.typelevel" %% "cats-effect"         % catsEffect,
    "org.typelevel" %% "cats-free"           % cats
  )

  val examplesTests: Seq[ModuleID] =
    Seq(
      "org.scalameta" %% "munit"                     % munit,
      "org.scalameta" %% "munit-scalacheck"          % munit,
      "org.scalaz"    %% "scalaz-scalacheck-binding" % scalaz,
      "org.typelevel" %% "cats-testkit"              % cats,
      "org.typelevel" %% "munit-cats-effect-3"       % munitCatsEffect
    ).map(_ % Test)

}

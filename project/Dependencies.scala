import sbt._

object Dependencies {

  object Version {
    val Scala = "3.2.1"

    val cats        = "2.9.0"
    val catsEffect  = "3.5-639ac01"
    val http4s      = "0.23.16"
    val http4sBlaze = "0.23.12"
    val refined     = "0.10.1"
    val scalaz      = "7.3.7"

    val munit           = "0.7.29"
    val munitCatsEffect = "1.0.7"
  }

  private val refinedStack: Seq[ModuleID] = Seq(
    "eu.timepit" %% "refined"      % Version.refined,
    "eu.timepit" %% "refined-cats" % Version.refined
  )

  private val http4sStack: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-blaze-server" % Version.http4sBlaze,
    "org.http4s" %% "http4s-dsl"          % Version.http4s
  )

  private val scalazStack: Seq[ModuleID] = Seq(
    "org.scalaz" %% "scalaz-core"     % Version.scalaz,
    "org.scalaz" %% "scalaz-effect"   % Version.scalaz,
    "org.scalaz" %% "scalaz-iteratee" % Version.scalaz
  )

  private val catsStack: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core"   % Version.cats,
    "org.typelevel" %% "cats-effect" % Version.catsEffect,
    "org.typelevel" %% "cats-free"   % Version.cats
  )

  val examples: Seq[ModuleID] =
    refinedStack ++ http4sStack ++ scalazStack ++ catsStack

  val examplesTests: Seq[ModuleID] =
    Seq(
      "org.scalameta" %% "munit"                     % Version.munit,
      "org.scalameta" %% "munit-scalacheck"          % Version.munit,
      "org.scalaz"    %% "scalaz-scalacheck-binding" % Version.scalaz,
      "org.typelevel" %% "cats-testkit"              % Version.cats,
      "org.typelevel" %% "munit-cats-effect-3"       % Version.munitCatsEffect
    ).map(_ % Test)

}

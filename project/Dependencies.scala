import sbt._

object Dependencies {
  val Scala = "3.2.2"

  object cats {
    private val version = "2.9.0"
    val core            = "org.typelevel" %% "cats-core"    % version
    val effect          = "org.typelevel" %% "cats-effect"  % "3.5-639ac01"
    val free            = "org.typelevel" %% "cats-free"    % version
    val testkit         = "org.typelevel" %% "cats-testkit" % version
  }

  object http4s {
    val blaze = "org.http4s" %% "http4s-blaze-server" % "0.23.13"
    val dsl   = "org.http4s" %% "http4s-dsl"          % "0.23.16"
  }

  object iron {
    private val version = "2.0.0"
    val iron            = "io.github.iltotore" %% "iron"      % version
    val cats            = "io.github.iltotore" %% "iron-cats" % version
  }
  object munit {
    private val version = "0.7.29"
    val core            = "org.scalameta" %% "munit"               % version
    val catsEffect3     = "org.typelevel" %% "munit-cats-effect-3" % "1.0.7"
    val scalacheck      = "org.scalameta" %% "munit-scalacheck"    % version
  }

  object refined {
    private val version = "0.10.1"
    val core            = "eu.timepit" %% "refined"      % version
    val cats            = "eu.timepit" %% "refined-cats" % version
  }

  object scalaz {
    private val version = "7.3.7"
    val core            = "org.scalaz" %% "scalaz-core"               % version
    val effect          = "org.scalaz" %% "scalaz-effect"             % version
    val iteratee        = "org.scalaz" %% "scalaz-iteratee"           % version
    val scalacheck      = "org.scalaz" %% "scalaz-scalacheck-binding" % version
  }
}

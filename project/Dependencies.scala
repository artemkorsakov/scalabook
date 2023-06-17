import sbt.*

object Dependencies {
  val Scala = "3.3.0"

  object cats {
    private val version = "2.9.0"
    val core            = "org.typelevel" %% "cats-core"    % version
    val effect          = "org.typelevel" %% "cats-effect"  % "3.5-639ac01"
    val free            = "org.typelevel" %% "cats-free"    % version
    val testkit         = "org.typelevel" %% "cats-testkit" % version
  }

  object http4s {
    val blaze = "org.http4s" %% "http4s-blaze-server" % "0.23.14"
    val dsl   = "org.http4s" %% "http4s-dsl"          % "0.23.18"
  }

  object iron {
    private val version = "2.1.0"
    val iron            = "io.github.iltotore" %% "iron"      % version
    val cats            = "io.github.iltotore" %% "iron-cats" % version
  }

  object laika {
    private val version = "0.19.2"
    val core            = "org.planet42" %% "laika-core"    % version
    val io              = "org.planet42" %% "laika-io"      % version
    val pdf             = "org.planet42" %% "laika-pdf"     % version
    val preview         = "org.planet42" %% "laika-preview" % version
  }

  object munit {
    private val version = "0.7.29"
    val core            = "org.scalameta" %% "munit"               % version
    val catsEffect3     = "org.typelevel" %% "munit-cats-effect-3" % "1.0.7"
    val scalacheck      = "org.scalameta" %% "munit-scalacheck"    % version
  }

  object refined {
    private val version = "0.10.3"
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

  object zio {
    val core = "dev.zio" %% "zio" % "2.0.14"
  }

  val exampleLibs: Seq[ModuleID] =
    Seq(
      cats.core,
      cats.effect,
      cats.free,
      http4s.blaze,
      http4s.dsl,
      iron.iron,
      iron.cats,
      refined.cats,
      refined.core,
      scalaz.core,
      scalaz.effect,
      scalaz.iteratee,
      zio.core
    )

  val exampleTestLibs: Seq[ModuleID] =
    Seq(
      cats.testkit,
      munit.core,
      munit.catsEffect3,
      munit.scalacheck,
      scalaz.scalacheck
    ).map(_ % Test)

  val siteLibs: Seq[ModuleID] =
    Seq(
      laika.core,
      laika.io,
      laika.pdf,
      laika.preview
    )
}

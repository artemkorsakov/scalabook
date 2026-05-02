import sbt.*

// @formatter:off
object Dependencies {
  val Scala = "3.8.3"

  object algebra {
    private val version       = "2.13.0"

    val core = "org.typelevel" %% "algebra" % version
  }

  object cats {
    private val version       = "2.13.0"

    val core    = "org.typelevel" %% "cats-core"    % version
    val free    = "org.typelevel" %% "cats-free"    % version
    val kernel  = "org.typelevel" %% "cats-kernel"  % version
    val testkit = "org.typelevel" %% "cats-testkit" % version
  }

  object catsEffect {
    private val version = "3.7.0"

    val core   = "org.typelevel" %% "cats-effect"        % version
    val kernel = "org.typelevel" %% "cats-effect-kernel" % version
    val std    = "org.typelevel" %% "cats-effect-std"    % version
  }

  object fs2 {
    private val version = "3.13.0"

    val core = "co.fs2" %% "fs2-core" % version
  }

  object http4s {
    private val version = "0.23.34"

    val blaze  = "org.http4s" %% "http4s-blaze-server" % "0.23.17"
    val core   = "org.http4s" %% "http4s-core"         % version
    val dsl    = "org.http4s" %% "http4s-dsl"          % version
    val server = "org.http4s" %% "http4s-server"       % version
  }

  object iron {
    private val version = "3.3.1"

    val cats       = "io.github.iltotore" %% "iron-cats"       % version
    val core       = "io.github.iltotore" %% "iron"            % version
    val scalacheck = "io.github.iltotore" %% "iron-scalacheck" % version
  }

  object laika {
    private val version = "1.3.2"

    val core    = "org.typelevel" %% "laika-core"    % version
    val io      = "org.typelevel" %% "laika-io"      % version
    val preview = "org.typelevel" %% "laika-preview" % version
  }

  object log4cats {
    private val version = "2.8.0"

    val core  = "org.typelevel" %% "log4cats-core"  % version
    val noop  = "org.typelevel" %% "log4cats-noop"  % version
    val slf4j = "org.typelevel" %% "log4cats-slf4j" % version
  }

  object logback {
    private val version = "1.5.32"

    val logback = "ch.qos.logback" % "logback-classic" % version
  }

  // https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Manual.md
  object metrics {
    val dropwizardCore = "io.dropwizard.metrics" % "metrics-core"    % "4.2.38"
    val metrics4Scala  = "nl.grons"              %% "metrics4-scala" % "4.3.7"
  }

  object refined {
    private val version = "0.11.3"

    val core = "eu.timepit" %% "refined"      % version
    val cats = "eu.timepit" %% "refined-cats" % version
  }

  object scalaz {
    private val version = "7.3.8"

    val core       = "org.scalaz" %% "scalaz-core"               % version
    val effect     = "org.scalaz" %% "scalaz-effect"             % version
    val iteratee   = "org.scalaz" %% "scalaz-iteratee"           % version
    val scalacheck = "org.scalaz" %% "scalaz-scalacheck-binding" % version
  }

  object spire {
    val core = "org.typelevel" %% "spire"  % "0.18.0"
  }

  object sttp {
    val client = "com.softwaremill.sttp.client4" %% "core" % "4.0.23"
    val model  = "com.softwaremill.sttp.model" %% "core" % "1.7.17"
  }

  object weaver {
    private val version = "0.8.4"

    val cats       = "com.disneystreaming" %% "weaver-cats"       % version
    val scalacheck = "com.disneystreaming" %% "weaver-scalacheck" % version
  }

  val exampleLibs: Seq[ModuleID] =
    Seq(
      algebra.core,
      cats.core,
      cats.kernel,
      catsEffect.core,
      catsEffect.kernel,
      catsEffect.std,
      iron.core,
      log4cats.core,
      log4cats.noop,
      log4cats.slf4j,
      scalaz.core,
      spire.core
    )

  val exampleTestLibs: Seq[ModuleID] =
    Seq(
      cats.testkit,
      catsEffect.kernel,
      iron.cats,
      iron.scalacheck,
      scalaz.scalacheck,
      weaver.cats,
      weaver.scalacheck
    ).map(_ % Test)

  val benchLibs: Seq[ModuleID] =
    Seq(
      cats.core,
      metrics.dropwizardCore,
      metrics.metrics4Scala,
      spire.core,
    )

  val siteLibs: Seq[ModuleID] =
    Seq(
      cats.core,
      catsEffect.core,
      catsEffect.kernel,
      http4s.server,
      laika.core,
      laika.io,
      laika.preview,
      sttp.client,
      sttp.model,
    )
}
// @formatter:on
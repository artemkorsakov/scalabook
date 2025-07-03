import sbt.*

// @formatter:off
object Dependencies {
  val Scala = "3.7.0" // Не забыть в sonar-project.properties обновить папку

  object fs2 {
    val core = "co.fs2" %% "fs2-core" % "3.12.0"
  }

  object http4s {
    private val version = "0.23.30"
    private val org     = "org.http4s"
    val blaze           = org %% "http4s-blaze-server" % "0.23.17"
    val core            = org %% "http4s-core"         % version
    val dsl             = org %% "http4s-dsl"          % version
    val server          = org %% "http4s-server"       % version
  }

  object iron {
    private val version = "3.0.0"
    private val org     = "io.github.iltotore"
    val iron            = org %% "iron"      % version
    val cats            = org %% "iron-cats" % version
    val scalacheck      = org %% "iron-scalacheck" % version
  }

  // https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Manual.md
  object metrics {
    val dropwizardCore = "io.dropwizard.metrics" % "metrics-core"    % "4.2.30"
    val metrics4Scala  = "nl.grons"              %% "metrics4-scala" % "4.3.3"
  }

  object logback {
    val logback = "ch.qos.logback" % "logback-classic" % "1.4.11"
  }

  object refined {
    private val version = "0.11.0"
    val core            = "eu.timepit" %% "refined"      % version
    val cats            = "eu.timepit" %% "refined-cats" % version
  }

  object scalaz {
    private val version = "7.3.8"
    private val org     = "org.scalaz"
    val core            = org %% "scalaz-core"               % version
    val effect          = org %% "scalaz-effect"             % version
    val iteratee        = org %% "scalaz-iteratee"           % version
    val scalacheck      = org %% "scalaz-scalacheck-binding" % version
  }

  object sttp {
    val client = "com.softwaremill.sttp.client4" %% "core" % "4.0.0-RC1"
    val model = "com.softwaremill.sttp.model" %% "core" % "1.7.11"
  }

  object typelevel {
    private val catsVersion       = "2.13.0"
    private val catsEffectVersion = "3.7-4972921"
    private val laikaVersion      = "1.3.2"
    private val org               = "org.typelevel"

    val algebra          = org %% "algebra"            % catsVersion
    val catsCore         = org %% "cats-core"          % catsVersion
    val catsEffect       = org %% "cats-effect"        % catsEffectVersion
    val catsEffectKernel = org %% "cats-effect-kernel" % catsEffectVersion
    val catsEffectStd    = org %% "cats-effect-std"    % catsEffectVersion
    val catsFree         = org %% "cats-free"          % catsVersion
    val catsKernel       = org %% "cats-kernel"        % catsVersion
    val catsTestkit      = org %% "cats-testkit"       % catsVersion
    val laikaCore        = org %% "laika-core"         % laikaVersion
    val laikaIo          = org %% "laika-io"           % laikaVersion
    val laikaPreview     = org %% "laika-preview"      % laikaVersion
    val spire            = org %% "spire"              % "0.18.0"
  }

  object weaver {
    private val version = "0.8.4"
    val cats            = "com.disneystreaming" %% "weaver-cats"       % version
    val scalacheck      = "com.disneystreaming" %% "weaver-scalacheck" % version
  }

  object zio {
    private val version = "2.1.17"
    val core        = "dev.zio" %% "zio"             % version
    val stacktracer = "dev.zio" %% "zio-stacktracer" % version
  }

  val exampleLibs: Seq[ModuleID] =
    Seq(
      fs2.core,
      http4s.blaze,
      http4s.core,
      http4s.dsl,
      http4s.server,
      iron.iron,
      iron.cats,
      scalaz.core,
      typelevel.algebra,
      typelevel.catsCore,
      typelevel.catsEffect,
      typelevel.catsEffectKernel,
      typelevel.catsEffectStd,
      typelevel.catsKernel,
      typelevel.spire,
      zio.core,
      zio.stacktracer
    )

  val exampleTestLibs: Seq[ModuleID] =
    Seq(
      iron.scalacheck,
      scalaz.scalacheck,
      typelevel.catsEffectKernel,
      typelevel.catsTestkit,
      weaver.cats,
      weaver.scalacheck
    ).map(_ % Test)

  val benchLibs: Seq[ModuleID] =
    Seq(
      metrics.dropwizardCore,
      metrics.metrics4Scala,
      typelevel.catsCore,
      typelevel.spire
    )

  val siteLibs: Seq[ModuleID] =
    Seq(
      http4s.server,
      sttp.client,
      sttp.model,
      typelevel.catsCore,
      typelevel.catsEffect,
      typelevel.catsEffectKernel,
      typelevel.laikaCore,
      typelevel.laikaIo,
      typelevel.laikaPreview,
    )
}
// @formatter:on
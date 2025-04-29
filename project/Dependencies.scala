import sbt.*

// @formatter:off
object Dependencies {
  val Scala = "3.6.4"

  object fs2 {
    val core = "co.fs2" %% "fs2-core" % "3.12.0"
  }

  object http4s {
    private val version = "0.23.30"
    val blaze           = "org.http4s" %% "http4s-blaze-server" % "0.23.17"
    val core            = "org.http4s" %% "http4s-core"         % version
    val dsl             = "org.http4s" %% "http4s-dsl"          % version
    val server          = "org.http4s" %% "http4s-server"       % version
  }

  object iron {
    private val version = "3.0.0"
    val iron            = "io.github.iltotore" %% "iron"      % version
    val cats            = "io.github.iltotore" %% "iron-cats" % version
    val scalacheck      = "io.github.iltotore" %% "iron-scalacheck" % version
  }

  // https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Manual.md
  object metrics {
    val dropwizardCore = "io.dropwizard.metrics" % "metrics-core"    % "4.2.30"
    val metrics4Scala  = "nl.grons"              %% "metrics4-scala" % "4.3.3"
  }

  object laika {
    private val version = "1.3.2"
    val core            = "org.typelevel" %% "laika-core"    % version
    val io              = "org.typelevel" %% "laika-io"      % version
    val preview         = "org.typelevel" %% "laika-preview" % version
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
    val core            = "org.scalaz" %% "scalaz-core"               % version
    val effect          = "org.scalaz" %% "scalaz-effect"             % version
    val iteratee        = "org.scalaz" %% "scalaz-iteratee"           % version
    val scalacheck      = "org.scalaz" %% "scalaz-scalacheck-binding" % version
  }

  object typelevel {
    private val catsVersion       = "2.13.0"
    private val catsEffectVersion = "3.7-4972921"

    val algebra          = "org.typelevel" %% "algebra"            % catsVersion
    val catsCore         = "org.typelevel" %% "cats-core"          % catsVersion
    val catsEffect       = "org.typelevel" %% "cats-effect"        % catsEffectVersion
    val catsEffectKernel = "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion
    val catsEffectStd    = "org.typelevel" %% "cats-effect-std"    % catsEffectVersion
    val catsFree         = "org.typelevel" %% "cats-free"          % catsVersion
    val catsKernel       = "org.typelevel" %% "cats-kernel"        % catsVersion
    val catsTestkit      = "org.typelevel" %% "cats-testkit"       % catsVersion
    val spire            = "org.typelevel" %% "spire"              % "0.18.0"
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
      laika.core,
      laika.io,
      laika.preview,
      typelevel.catsCore,
      typelevel.catsEffect,
      typelevel.catsEffectKernel
    )
}
// @formatter:on
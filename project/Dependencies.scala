import sbt.*

// @formatter:off
object Dependencies {
  val Scala = "3.3.1"

  object cats {
    private val version       = "2.10.0"
    private val effectVersion = "3.6-0142603"

    val core         = "org.typelevel" %% "cats-core"          % version
    val effect       = "org.typelevel" %% "cats-effect"        % effectVersion
    val effectKernel = "org.typelevel" %% "cats-effect-kernel" % effectVersion
    val effectStd    = "org.typelevel" %% "cats-effect-std"    % effectVersion
    val free         = "org.typelevel" %% "cats-free"          % version
    val kernel       = "org.typelevel" %% "cats-kernel"        % version
    val testkit      = "org.typelevel" %% "cats-testkit"       % version
  }

  object fs2 {
    val core = "co.fs2" %% "fs2-core" % "3.7.0"
  }

  object http4s {
    private val version = "0.23.23"
    val blaze           = "org.http4s" %% "http4s-blaze-server" % "0.23.15"
    val core            = "org.http4s" %% "http4s-core"         % version
    val dsl             = "org.http4s" %% "http4s-dsl"          % version
    val server          = "org.http4s" %% "http4s-server"       % version
  }

  object iron {
    private val version = "2.3.0"
    val iron            = "io.github.iltotore" %% "iron"      % version
    val cats            = "io.github.iltotore" %% "iron-cats" % version
  }

  object laika {
    private val version = "0.19.5"
    val core            = "org.planet42" %% "laika-core"    % version
    val io              = "org.planet42" %% "laika-io"      % version
    val pdf             = "org.planet42" %% "laika-pdf"     % version
    val preview         = "org.planet42" %% "laika-preview" % version
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
    private val version = "7.3.7"
    val core            = "org.scalaz" %% "scalaz-core"               % version
    val effect          = "org.scalaz" %% "scalaz-effect"             % version
    val iteratee        = "org.scalaz" %% "scalaz-iteratee"           % version
    val scalacheck      = "org.scalaz" %% "scalaz-scalacheck-binding" % version
  }

  object spire {
    val spire = "org.typelevel" %% "spire" % "0.18.0"
  }

  object weaver {
    private val version = "0.8.3"
    val cats            = "com.disneystreaming" %% "weaver-cats"       % version
    val scalacheck      = "com.disneystreaming" %% "weaver-scalacheck" % version
  }

  object zio {
    val core        = "dev.zio" %% "zio"             % "2.0.18"
    val stacktracer = "dev.zio" %% "zio-stacktracer" % "2.0.18"
  }

  val exampleLibs: Seq[ModuleID] =
    Seq(
      cats.core,
      cats.effect,
      cats.effectKernel,
      cats.effectStd,
      cats.kernel,
      fs2.core,
      http4s.blaze,
      http4s.core,
      http4s.dsl,
      http4s.server,
      iron.iron,
      iron.cats,
      scalaz.core,
      spire.spire,
      zio.core,
      zio.stacktracer
    )

  val exampleTestLibs: Seq[ModuleID] =
    Seq(
      cats.effectKernel,
      cats.testkit,
      scalaz.scalacheck,
      weaver.cats,
      weaver.scalacheck
    ).map(_ % Test)

  val siteLibs: Seq[ModuleID] =
    Seq(
      cats.core,
      cats.effect,
      cats.effectKernel,
      http4s.server,
      laika.core,
      laika.io,
      laika.preview
    )
}
// @formatter:on
import sbt.*

object Dependencies {
  val Scala = "3.3.1"

  object cats {
    private val version       = "2.10.0"
    private val effectVersion = "3.6-0142603"

    val core         = "org.typelevel" %% "cats-core"          % version
    val effect       = "org.typelevel" %% "cats-effect"        % effectVersion
    val effectKernel = "org.typelevel" %% "cats-effect-kernel" % effectVersion
    val free         = "org.typelevel" %% "cats-free"          % version
    val testkit      = "org.typelevel" %% "cats-testkit"       % version
  }

  object http4s {
    val blaze = "org.http4s" %% "http4s-blaze-server" % "0.23.15"
    val dsl   = "org.http4s" %% "http4s-dsl"          % "0.23.23"
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
    val core = "dev.zio" %% "zio" % "2.0.18"
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
      spire.spire,
      zio.core
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
      laika.core,
      laika.io,
      laika.pdf,
      laika.preview,
      logback.logback
    )
}

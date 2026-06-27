import sbt.*

// @formatter:off
object Dependencies {
  val Scala = "3.8.3"

  object cats {
    private val version       = "2.13.0"

    val algebra   = "org.typelevel" %% "algebra"        % version
    val alleycats = "org.typelevel" %% "alleycats-core" % version
    val core      = "org.typelevel" %% "cats-core"      % version
    val free      = "org.typelevel" %% "cats-free"      % version
    val kernel    = "org.typelevel" %% "cats-kernel"    % version
    val laws      = "org.typelevel" %% "cats-laws"      % version
    val testkit   = "org.typelevel" %% "cats-testkit"   % version
  }

  object catsEffect {
    private val version = "3.7.0"

    val core   = "org.typelevel" %% "cats-effect"        % version
    val kernel = "org.typelevel" %% "cats-effect-kernel" % version
    val std    = "org.typelevel" %% "cats-effect-std"    % version
  }

  object http4s {
    private val version = "0.23.34"

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

  object sttp {
    val client = "com.softwaremill.sttp.client4" %% "core" % "4.0.25"
    val model  = "com.softwaremill.sttp.model" %% "core" % "1.7.17"
  }

  object weaver {
    private val version = "0.8.4"

    val cats       = "com.disneystreaming" %% "weaver-cats"       % version
    val scalacheck = "com.disneystreaming" %% "weaver-scalacheck" % version
  }

  val exampleLibs: Seq[ModuleID] =
    Seq(
      cats.core,
      catsEffect.core,
      catsEffect.kernel,
      catsEffect.std,
      log4cats.core,
      log4cats.slf4j
    )

  val exampleTestLibs: Seq[ModuleID] =
    Seq(
      cats.laws,
      cats.testkit,
      catsEffect.kernel,
      iron.cats,
      iron.scalacheck,
      weaver.cats,
      weaver.scalacheck
    ).map(_ % Test)

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
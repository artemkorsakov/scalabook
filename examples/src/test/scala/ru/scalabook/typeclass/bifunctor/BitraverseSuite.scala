package ru.scalabook.typeclass.bifunctor

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import ru.scalabook.typeclass.common.*

class BitraverseSuite extends ScalaCheckSuite, BitraverseLaw:
  property("Bitraverse[Either] должен удовлетворять законам Bitraverse") {
    forAll {
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        checkBitraverseLaw[
          Either,
          Id,
          Option,
          String,
          Char,
          Int,
          String,
          Boolean
        ](far, fla, faa)
    }
  }

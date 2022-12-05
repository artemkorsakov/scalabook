package typeclass.bifunctor

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Traverse.given
import typeclass.monad.{Traverse, TraverseLaw}

class BitraverseSuite extends ScalaCheckSuite, BitraverseLaw:
  property("Bitraverse[Either] должен удовлетворять законам Bitraverse") {
    forAll { (far: Either[Int, Char], fla: Either[String, Int], faa: Either[Int, Int]) =>
      checkBitraverseLaw[Either, Id, Option, String, Char, Int, String, Boolean](far, fla, faa)
    }
  }

package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Apply.{apply, map, given}

class ApplySuite extends ScalaCheckSuite:
  private val f: Int => String = _.toString
  private val g: String => Boolean = _.startsWith("1")

  property("idApply должен удовлетворять законам Apply") {
    forAll { (x: Int) =>
      checkApply[Id, Int, String, Boolean](Id(x), f, g, Id(f), Id(g))
    }
  }

  private def checkApply[F[_], A, B, C](fa: F[A], ab: A => B, bc: B => C, fab: F[A => B], fbc: F[B => C])(using
      Apply[F]
  ): Unit =
    assertEquals(
      apply(fbc)(apply(fab)(fa)),
      apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)
    )
    // checkFunctor(fa, ab, bc)

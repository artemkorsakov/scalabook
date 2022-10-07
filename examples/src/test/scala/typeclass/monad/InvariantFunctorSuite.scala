package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.InvariantFunctor.{xmap, given}

class InvariantFunctorSuite extends ScalaCheckSuite:
  private val f1: Int => String = _.toString
  private val g1: String => Int = _.toInt
  private val f2: String => Boolean = _.startsWith("1")
  private val g2: Boolean => String = _.toString

  property("idFunctor должен удовлетворять законам инвариантного функтора") {
    forAll { (x: Int) =>
      checkInvariantFunctor(Id(x), f1, g1, f2, g2)
    }
  }

  private def checkInvariantFunctor[F[_], A, B, C](fa: F[A], f1: A => B, g1: B => A, f2: B => C, g2: C => B)(using
      InvariantFunctor[F]
  ): Unit =
    assertEquals(xmap(fa, identity, identity), fa, "check identity")
    assertEquals(xmap(xmap(fa, f1, g1), f2, g2), xmap(fa, f2 compose f1, g1 compose g2), "check composition")

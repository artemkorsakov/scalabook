package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Functor.{map, given}
import typeclass.monad.FunctorSuite.checkFunctor

class FunctorSuite extends ScalaCheckSuite:
  private val f: Int => String = _.toString
  private val g: String => Boolean = _.startsWith("1")

  property("idFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      assertEquals(map(Id(x), _ * 2), Id(x * 2))
      checkFunctor(Id(x), f, g)
    }
  }

  property("optionFunctor должен удовлетворять законам функтора") {
    forAll { (maybeInt: Option[Int]) =>
      checkFunctor(maybeInt, f, g)
    }
  }

  property("listFunctor должен удовлетворять законам функтора") {
    forAll { (list: List[Int]) =>
      checkFunctor(list, f, g)
    }
  }

  property("eitherFunctor должен удовлетворять законам функтора") {
    forAll { (either: Either[String, Int]) =>
      checkFunctor(either, f, g)
    }
  }

  property("writerFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      val writer = Writer(() => ("state", x))
      checkFunctor(writer, f, g)
    }
  }

  property("stateFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      val state = State(s => (s, x))
      checkFunctor(state, f, g)
    }
  }

object FunctorSuite extends Assertions:
  def checkFunctor[F[_], A, B, C](fa: F[A], f: A => B, g: B => C)(using Functor[F]): Unit =
    assertEquals(map(fa, identity), fa, "check identity")
    assertEquals(map(map(fa, f), g), map(fa, f.andThen(g)), "check composition")

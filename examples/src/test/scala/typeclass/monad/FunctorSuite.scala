package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Functor.{map, given}
import typeclass.monad.FunctorSuite.{checkFunctor, checkStateFunctor}

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
      val state = State[String, Int](s => (s, x))
      checkStateFunctor(state, f, g)
    }
  }

  property("nestedFunctor должен удовлетворять законам функтора") {
    forAll { (maybeInt: Option[Int]) =>
      val nested = Nested[Id, Option, Int](Id(maybeInt))
      checkFunctor[[X] =>> Nested[Id, Option, X], Int, String, Boolean](nested, f, g)
    }
  }

  property("ioFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      checkFunctor(IO(() => x), f, g)
    }
  }

object FunctorSuite extends Assertions:
  def checkFunctor[F[_], A, B, C](fa: F[A], f: A => B, g: B => C)(using Functor[F]): Unit =
    assertEquals(map(fa, identity), fa, "check identity")
    assertEquals(map(map(fa, f), g), map(fa, f.andThen(g)), "check composition")

  def checkStateFunctor[A, B, C](fa: State[String, A], f: A => B, g: B => C)(using
      Functor[[x] =>> State[String, x]]
  ): Unit =
    assertEquals(map(fa, identity).run("state"), fa.run("state"), "check identity")
    assertEquals(map(map(fa, f), g).run("state"), map(fa, f.andThen(g)).run("state"), "check composition")

package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.FunctorSuite.{checkFunctor, checkStateFunctor}
import typeclass.monad.Monad.{flatMap, map, unit, given}

class MonadSuite extends ScalaCheckSuite:
  private val f: Int => String = _.toString
  private val g: String => Boolean = _.startsWith("1")
  private val h: String => Boolean = _.startsWith("11")

  property("idMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonad[Id, Int, String, Boolean](x, Id(x), i => Id(f(i)), s => Id(g(s)))
    }
  }

  property("optionMonad должен удовлетворять законам монады") {
    forAll { (x: Int, fa: Option[Int], fb: Option[String], fc: Option[Boolean]) =>
      checkMonad[Option, Int, String, Boolean](
        x,
        fa,
        i => if i % 2 == 0 then Some(f(i)) else fb,
        s => if g(s) then Some(h(s)) else fc
      )
    }
  }

  property("listMonad должен удовлетворять законам монады") {
    forAll { (x: Int, fa: List[Int], fb: List[String], fc: List[Boolean]) =>
      checkMonad[List, Int, String, Boolean](
        x,
        fa,
        i => if i % 2 == 0 then List(f(i)) else fb,
        s => if g(s) then List(h(s)) else fc
      )
    }
  }

  property("eitherMonad должен удовлетворять законам монады") {
    forAll { (x: Int, fa: Either[String, Int], fb: Either[String, String], fc: Either[String, Boolean]) =>
      checkMonad[[x] =>> Either[String, x], Int, String, Boolean](
        x,
        fa,
        i => if i % 2 == 0 then Right(f(i)) else fb,
        s => if g(s) then Right(h(s)) else fc
      )
    }
  }

  property("writerMonad должен удовлетворять законам монады") {
    forAll { (x: Int, a: Int, b: String, c: Boolean) =>
      val writerA = Writer(() => ("state", a))
      val writerB = Writer(() => ("state", b))
      val writerC = Writer(() => ("state", c))

      checkMonad[[x] =>> Writer[String, x], Int, String, Boolean](
        x,
        writerA,
        i => if i % 2 == 0 then Writer(() => ("state", f(i))) else writerB,
        s => if g(s) then Writer(() => ("state", h(s))) else writerC
      )
    }
  }

  property("stateMonad должен удовлетворять законам монады") {
    forAll { (x: Int, a: Int, b: String, c: Boolean) =>
      val stateA = State[String, Int](s => (s, a))
      val stateB = State[String, String](s => (s, b))
      val stateC = State[String, Boolean](s => (s, c))

      checkStateMonad[Int, String, Boolean](
        x,
        stateA,
        i => if i % 2 == 0 then State(s => (s, f(i))) else stateB,
        s => if g(s) then State(s => (s, h(s))) else stateC
      )
    }
  }

  property("ioMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonad[IO, Int, String, Boolean](x, IO(() => x), i => IO(() => f(i)), s => IO(() => g(s)))
    }
  }

  private def checkMonad[F[_], A, B, C](x: A, fa: F[A], f: A => F[B], g: B => F[C])(using Monad[F]): Unit =
    assertEquals(flatMap(unit(x), f), f(x))
    assertEquals(flatMap(fa, unit _), fa)
    assertEquals(
      flatMap(flatMap(fa, f), g),
      flatMap(fa, x => flatMap(f(x), g))
    )

  private def checkStateMonad[A, B, C](x: A, fa: State[String, A], f: A => State[String, B], g: B => State[String, C])(
      using Monad[[x] =>> State[String, x]]
  ): Unit =
    assertEquals(flatMap(unit(x), f).run("state"), f(x).run("state"))
    assertEquals(flatMap(fa, unit _).run("state"), fa.run("state"))
    assertEquals(
      flatMap(flatMap(fa, f), g).run("state"),
      flatMap(fa, x => flatMap(f(x), g)).run("state")
    )

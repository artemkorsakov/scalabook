package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Monad.given

class MonadSuite extends ScalaCheckSuite, MonadLaw:
  private val f: Int => String = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply
  private val h: String => Boolean = _.startsWith("11")

  property("idMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[Id, Int, String, Boolean](x, Id(x), i => Id(f(i)), s => Id(g(s)))
    }
  }

  property("optionMonad должен удовлетворять законам монады") {
    forAll { (x: Int, fa: Option[Int], fb: Option[String], fc: Option[Boolean]) =>
      checkMonadLaw[Option, Int, String, Boolean](
        x,
        fa,
        i => if i % 2 == 0 then Some(f(i)) else fb,
        s => if g(s) then Some(h(s)) else fc
      )
    }
  }

  property("listMonad должен удовлетворять законам монады") {
    forAll { (x: Int, fa: List[Int], fb: List[String], fc: List[Boolean]) =>
      checkMonadLaw[List, Int, String, Boolean](
        x,
        fa,
        i => if i % 2 == 0 then List(f(i)) else fb,
        s => if g(s) then List(h(s)) else fc
      )
    }
  }

  property("eitherMonad должен удовлетворять законам монады") {
    forAll { (x: Int, fa: Either[String, Int], fb: Either[String, String], fc: Either[String, Boolean]) =>
      checkMonadLaw[[X] =>> Either[String, X], Int, String, Boolean](
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

      checkMonadLaw[[X] =>> Writer[String, X], Int, String, Boolean](
        x,
        writerA,
        i => if i % 2 == 0 then Writer(() => ("state", f(i))) else writerB,
        s => if g(s) then Writer(() => ("state", h(s))) else writerC
      )
    }
  }

  property("stateMonad должен удовлетворять законам монады") {
    forAll { (x: Int, a: Int, b: String, c: Boolean, s: String) =>
      val stateA = State[String, Int](s => (s, a))
      val stateB = State[String, String](s => (s, b))
      val stateC = State[String, Boolean](s => (s, c))

      checkMonadLaw[[X] =>> State[String, X], Int, String, Boolean](
        x,
        stateA,
        i => if i % 2 == 0 then State(s => (s, f(i))) else stateB,
        s => if g(s) then State(s => (s, h(s))) else stateC,
        _.run(s)._2
      )
    }
  }

  property("ioMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[IO, Int, String, Boolean](x, IO(() => x), i => IO(() => f(i)), s => IO(() => g(s)))
    }
  }

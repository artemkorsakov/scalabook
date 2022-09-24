package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Functor.{map, given}

class FunctorSuite extends ScalaCheckSuite:
  private val f: Int => String = _.toString
  private val g: String => Boolean = _.startsWith("1")

  property("'Обертка' является функтором") {
    forAll { (x: Int) =>
      assertEquals(map(Id(x), _ * 2), Id(x * 2))
      checkFunctor(Id(x), f, g)
    }
  }

  property("Option является функтором") {
    forAll { (maybeInt: Option[Int]) =>
      checkFunctor(maybeInt, f, g)
    }
  }

  property("List является функтором") {
    forAll { (list: List[Int]) =>
      checkFunctor(list, f, g)
    }
  }

  property("Either является функтором") {
    forAll { (either: Either[String, Int]) =>
      checkFunctor(either, f, g)
    }
  }

  property("Writer является функтором") {
    forAll { (x: Int) =>
      val writer = Writer(() => ("state", x))
      assertEquals(
        map(writer, identity).run(),
        writer.run(),
        "check identity"
      )
      assertEquals(
        map(map(writer, f), g).run(),
        map(writer, f.andThen(g)).run(),
        "check composition"
      )
    }
  }

  property("State является функтором") {
    forAll { (x: Int) =>
      val s = "state"
      val state = State(s => (s, x))
      assertEquals(
        map(state, identity).run(s),
        state.run(s),
        "check identity"
      )
      assertEquals(
        map(map(state, f), g).run(s),
        map(state, f.andThen(g)).run(s),
        "check composition"
      )
    }
  }

  private def checkFunctor[F[_], A, B, C](fa: F[A], f: A => B, g: B => C)(using Functor[F]): Unit =
    assertEquals(map(fa, identity), fa, "check identity")
    assertEquals(map(map(fa, f), g), map(fa, f.andThen(g)), "check composition")

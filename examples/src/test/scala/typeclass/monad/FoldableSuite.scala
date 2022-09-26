package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Foldable.{foldRight, given}

class FoldableSuite extends ScalaCheckSuite:
  property("idFoldable должен 'сворачиваться'") {
    forAll { (x: Int) =>
      assertEquals(foldRight(Id(x))(100)(_ + _), 100 + x)
    }
  }

  property("optionFoldable должен 'сворачиваться'") {
    forAll { (maybeInt: Option[Int]) =>
      val expected = maybeInt match
        case Some(a) => a + 100
        case None    => 100
      assertEquals(foldRight(maybeInt)(100)(_ + _), expected)
    }
  }

  property("listFoldable должен 'сворачиваться'") {
    forAll { (list: List[Int]) =>
      assertEquals(foldRight(list)(100)(_ + _), list.sum + 100)
    }
  }

  property("tuple2Foldable должен 'сворачиваться'") {
    forAll { (x: Int, y: Int) =>
      val actual = foldRight[[X] =>> (X, X), Int, Int]((x, y))(100)(_ + _)
      assertEquals(actual, x + y + 100)
    }
  }

  property("tuple3Foldable должен 'сворачиваться'") {
    forAll { (x: Int, y: Int, z: Int) =>
      val actual = foldRight[[X] =>> (X, X, X), Int, Int]((x, y, z))(100)(_ + _)
      assertEquals(actual, x + y + z + 100)
    }
  }

  property("eitherFoldable должен 'сворачиваться'") {
    forAll { (either: Either[String, Int]) =>
      val expected = either match
        case Right(a) => a + 100
        case _        => 100
      assertEquals(foldRight(either)(100)(_ + _), expected)
    }
  }

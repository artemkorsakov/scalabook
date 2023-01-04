package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Foldable.given
import typeclass.monoid.Monoid.given

class FoldableSuite extends ScalaCheckSuite, FoldableLaw:
  property("idFoldable должен 'сворачиваться'") {
    forAll { (x: Int) =>
      assertEquals(Foldable[Id].foldRight(Id(x))(100)(_ + _), 100 + x)
      checkFoldableLaw[Id, Int](Id(x))
    }
  }

  property("optionFoldable должен 'сворачиваться'") {
    forAll { (maybeInt: Option[Int]) =>
      val expected = maybeInt match
        case Some(a) => a + 100
        case None    => 100
      assertEquals(Foldable[Option].foldRight(maybeInt)(100)(_ + _), expected)
      checkFoldableLaw[Option, Int](maybeInt)
    }
  }

  property("listFoldable должен 'сворачиваться'") {
    forAll { (list: List[Int]) =>
      assertEquals(Foldable[List].foldRight(list)(100)(_ + _), list.sum + 100)
      checkFoldableLaw[List, Int](list)
    }
  }

  property("tuple2Foldable должен 'сворачиваться'") {
    forAll { (x: Int, y: Int) =>
      val actual = Foldable[[X] =>> (X, X)].foldRight[Int]((x, y))(100)(_ + _)
      assertEquals(actual, x + y + 100)
      checkFoldableLaw[[X] =>> (X, X), Int]((x, x))
    }
  }

  property("tuple3Foldable должен 'сворачиваться'") {
    forAll { (x: Int, y: Int, z: Int) =>
      val actual =
        Foldable[[X] =>> (X, X, X)].foldRight[Int]((x, y, z))(100)(_ + _)
      assertEquals(actual, x + y + z + 100)
      checkFoldableLaw[[X] =>> (X, X, X), Int]((x, x, x))
    }
  }

  property("eitherFoldable должен 'сворачиваться'") {
    forAll { (either: Either[String, Int]) =>
      val expected = either match
        case Right(a) => a + 100
        case _        => 100
      assertEquals(
        Foldable[[X] =>> Either[String, X]].foldRight(either)(100)(_ + _),
        expected
      )
      checkFoldableLaw[[X] =>> Either[String, X], Int](either)
    }
  }

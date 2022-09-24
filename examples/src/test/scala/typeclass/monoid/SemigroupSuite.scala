package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monoid.Semigroup
import typeclass.monoid.Semigroup.{*, given}

class SemigroupSuite extends ScalaCheckSuite:
  property("Int образуют полугруппу относительно сложения") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkAssociativity(x, y, z)(using sumSemigroupInstance)
    }
  }

  property("Int образуют полугруппу относительно умножения") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkAssociativity(x, y, z)(using productSemigroupInstance)
    }
  }

  property("Строки образуют полугруппу относительно конкатенации") {
    forAll { (x: String, y: String, z: String) =>
      checkAssociativity(x, y, z)
    }
  }

  property("List образует полугруппу относительно операции объединения") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkAssociativity(x, y, z)
    }
  }

  property("Кортеж от двух и более полугрупп также является полугруппой") {
    forAll { (x0: String, y0: String, z0: String, x1: List[Int], y1: List[Int], z1: List[Int]) =>
      assertEquals(combine((x0, x1), (y0, y1)), (s"$x0$y0", x1 ++ y1))
      checkAssociativity((x0, x1), (y0, y1), (z0, z1))
    }
  }

  private def checkAssociativity[A](x: A, y: A, z: A)(using Semigroup[A]): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)))

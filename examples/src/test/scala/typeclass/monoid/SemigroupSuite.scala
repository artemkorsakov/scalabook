package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monoid.Semigroup
import typeclass.monoid.Semigroup.{combine, given}

class SemigroupSuite extends ScalaCheckSuite:
  property("sumSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkAssociativity(x, y, z)(using sumSemigroupInstance)
    }
  }

  property("productSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkAssociativity(x, y, z)(using productSemigroupInstance)
    }
  }

  property("stringSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: String, y: String, z: String) =>
      checkAssociativity(x, y, z)
    }
  }

  property("listSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkAssociativity(x, y, z)
    }
  }

  property("nestedSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x0: String, y0: String, z0: String, x1: List[Int], y1: List[Int], z1: List[Int]) =>
      assertEquals(combine((x0, x1), (y0, y1)), (s"$x0$y0", x1 ++ y1))
      checkAssociativity((x0, x1), (y0, y1), (z0, z1))
    }
  }

  private def checkAssociativity[A](x: A, y: A, z: A)(using Semigroup[A]): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)), "Associativity")

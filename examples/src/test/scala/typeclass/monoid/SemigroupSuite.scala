package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.laws.SemigroupLaw
import typeclass.monoid.Semigroup
import typeclass.monoid.Semigroup.{combine, given}

class SemigroupSuite extends ScalaCheckSuite, SemigroupLaw:
  property("sumSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkSemigroupLaw(x, y, z)(using sumSemigroupInstance)
    }
  }

  property("productSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkSemigroupLaw(x, y, z)(using productSemigroupInstance)
    }
  }

  property("stringSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: String, y: String, z: String) =>
      checkSemigroupLaw(x, y, z)
    }
  }

  property("listSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkSemigroupLaw(x, y, z)
    }
  }

  property("nestedSemigroupInstance должен удовлетворять законам полугруппы") {
    forAll { (x0: String, y0: String, z0: String, x1: List[Int], y1: List[Int], z1: List[Int]) =>
      assertEquals(combine((x0, x1), (y0, y1)), (s"$x0$y0", x1 ++ y1))
      checkSemigroupLaw((x0, x1), (y0, y1), (z0, z1))
    }
  }

package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monoid.Monoid.{combine, empty, given}

class MonoidSuite extends ScalaCheckSuite:
  property("sumMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMonoid(x, y, z)(using sumMonoidInstance)
    }
  }

  property("productMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMonoid(x, y, z)(using productMonoidInstance)
    }
  }

  property("stringMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: String, y: String, z: String) =>
      checkMonoid(x, y, z)
    }
  }

  property("listMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkMonoid(x, y, z)
    }
  }

  property("nestedMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x0: String, y0: String, z0: String, x1: List[Int], y1: List[Int], z1: List[Int]) =>
      assertEquals(combine((x0, x1), (y0, y1)), (s"$x0$y0", x1 ++ y1))
      checkMonoid((x0, x1), (y0, y1), (z0, z1))
    }
  }

  private def checkMonoid[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)), "associativity")
    assertEquals(combine(x, empty), x, "right identity")
    assertEquals(combine(empty, x), x, "left identity")

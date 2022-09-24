package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monoid.Monoid.{combine, empty, given}

class MonoidSuite extends ScalaCheckSuite:
  property("Int являются моноидами относительно сложения (`0` является identity элементом)") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMonoid(x, y, z)(using sumMonoidInstance)
    }
  }

  property("Int являются моноидами относительно умножения (`1` является identity элементом)") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMonoid(x, y, z)(using productMonoidInstance)
    }
  }

  property("Строки образуют моноид относительно конкатенации (пустая строка является identity элементом)") {
    forAll { (x: String, y: String, z: String) =>
      checkMonoid(x, y, z)
    }
  }

  property(
    "List образует моноид относительно операции объединения (пустая последовательность является identity элементом)"
  ) {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkMonoid(x, y, z)
    }
  }

  property("Кортеж от двух и более моноидов также является моноидом") {
    forAll { (x0: String, y0: String, z0: String, x1: List[Int], y1: List[Int], z1: List[Int]) =>
      assertEquals(combine((x0, x1), (y0, y1)), (s"$x0$y0", x1 ++ y1))
      checkMonoid((x0, x1), (y0, y1), (z0, z1))
    }
  }

  private def checkMonoid[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)), "associativity")
    assertEquals(combine(x, empty), x, "right identity")
    assertEquals(combine(empty, x), x, "left identity")

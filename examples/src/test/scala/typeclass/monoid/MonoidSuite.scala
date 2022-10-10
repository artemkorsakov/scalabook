package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monoid.Monoid.{combine, given}

class MonoidSuite extends ScalaCheckSuite, MonoidLaw:
  property("sumMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMonoidLaw(x, y, z)(using sumMonoidInstance)
    }
  }

  property("productMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMonoidLaw(x, y, z)(using productMonoidInstance)
    }
  }

  property("stringMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: String, y: String, z: String) =>
      checkMonoidLaw(x, y, z)
    }
  }

  property("listMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkMonoidLaw(x, y, z)
    }
  }

  property("nestedMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x0: String, y0: String, z0: String, x1: List[Int], y1: List[Int], z1: List[Int]) =>
      assertEquals(combine((x0, x1), (y0, y1)), (s"$x0$y0", x1 ++ y1))
      checkMonoidLaw((x0, x1), (y0, y1), (z0, z1))
    }
  }

  property("optionMonoidInstance должен удовлетворять законам моноида") {
    forAll { (x: Option[String], y: Option[String], z: Option[String]) =>
      checkMonoidLaw[Option[String]](x, y, z)
    }
  }

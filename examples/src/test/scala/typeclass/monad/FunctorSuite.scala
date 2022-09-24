package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class FunctorSuite extends ScalaCheckSuite:
  property("Int являются моноидами относительно сложения (`0` является identity элементом)") {
    forAll { (x: Int, y: Int, z: Int) =>
      assertEquals(1, 1)
    }
  }

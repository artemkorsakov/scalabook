package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.MonadPlus.given

class MonadPlusSuite extends ScalaCheckSuite, MonadPlusLaw:
  property("listMonadPlusInstance должен удовлетворять законам MonadPlus") {
    forAll {
      (
          i: Int,
          x: List[Int],
          y: List[Int],
          z: List[Int],
          fta: List[Option[Int]]
      ) =>
        checkMonadPlusLaw(i, x, y, z)
        assertEquals(
          MonadPlus[List].filter(x)(_ % 2 == 0),
          x.filter(_ % 2 == 0)
        )
        assertEquals(MonadPlus[List].unite(fta), fta.flatten)
    }
  }

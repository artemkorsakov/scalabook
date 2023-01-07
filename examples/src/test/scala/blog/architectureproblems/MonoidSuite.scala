package blog.architectureproblems

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import org.scalacheck.Gen
import blog.architectureproblems.Monoid.doTheMonoidLawsHold

class MonoidSuite extends ScalaCheckSuite:
  private val smallNumber  = Gen.choose[Int](-100, 100)
  private val smallNumbers =
    for
      x <- smallNumber
      y <- smallNumber
      z <- smallNumber
    yield (x, y, z)

  property("Множество чисел образует моноид относительно умножения с единичным элементом равным 1") {
    given Monoid[Int] with
      override def e: Int = 1
      extension (x: Int) override def |+|(y: Int): Int = x * y

    forAll(smallNumbers) { (x, y, z) =>
      assert(doTheMonoidLawsHold(x, y, z))
    }
  }

  property("Множество чисел НЕ образует моноид относительно умножения с единичным элементом равным 0") {
    given Monoid[Int] with
      override def e: Int = 0
      extension (x: Int) override def |+|(y: Int): Int = x * y

    exists(smallNumbers) { (x, y, z) =>
      assertEquals(doTheMonoidLawsHold(x, y, z), false)
    }
  }

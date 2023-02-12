package blog.architectureproblems

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import org.scalacheck.Gen
import blog.architectureproblems.Semigroup.doTheSemigroupLawsHold

class SemigroupSuite extends ScalaCheckSuite:
  private val smallNumber = Gen.choose[Int](-100, 100)
  private val smallNumbers =
    for
      x <- smallNumber
      y <- smallNumber
      z <- smallNumber
    yield (x, y, z)

  property("Множество чисел образует полугруппу относительно умножения") {
    given Semigroup[Int] with
      extension (x: Int) override def |+|(y: Int): Int = x * y

    forAll(smallNumbers) { (x, y, z) =>
      assert(doTheSemigroupLawsHold(x, y, z))
    }
  }

  property("Множество чисел НЕ образует полугруппу относительно вычитания") {
    given Semigroup[Int] with
      extension (x: Int) override def |+|(y: Int): Int = x - y

    exists(smallNumbers) { (x, y, z) =>
      assertEquals(doTheSemigroupLawsHold(x, y, z), false)
    }
  }

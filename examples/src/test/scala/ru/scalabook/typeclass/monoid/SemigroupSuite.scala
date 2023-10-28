package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import org.scalacheck.{Arbitrary, Gen}
import ru.scalabook.typeclass.monoid.Semigroup.given

class SemigroupSuite extends ScalaCheckSuite, SemigroupLaw:
  opaque type SmallInt = Int
  given Arbitrary[SmallInt] = Arbitrary[SmallInt](Gen.choose(-10, 100))

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
    forAll {
      (
          x0: String,
          y0: String,
          z0: String,
          x1: List[Int],
          y1: List[Int],
          z1: List[Int]
      ) =>
        assertEquals(
          Semigroup[(String, List[Int])].combine((x0, x1), (y0, y1)),
          (s"$x0$y0", x1 ++ y1)
        )
        checkSemigroupLaw((x0, x1), (y0, y1), (z0, z1))
    }
  }

  property("combineN должен складывать k раз") {
    forAll { (x: Int, k: SmallInt, acc: Int) =>
      val actual   = sumSemigroupInstance.combineN(x, k, acc)
      val expected = if k <= 0 then acc else x * k + acc
      assertEquals(actual, expected)
    }
  }

package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import org.scalacheck.{Arbitrary, Gen}
import ru.scalabook.typeclass.monoid.MultiplicativeSemigroup.given

class MultiplicativeSemigroupSuite
    extends ScalaCheckSuite,
      MultiplicativeSemigroupLaw:
  opaque type SmallInt = Int
  given Arbitrary[SmallInt] = Arbitrary[SmallInt](Gen.choose(-10, 100))

  property(
    "MultiplicativeSemigroup[Int] должен удовлетворять законам полугруппы"
  ) {
    forAll { (x: Int, y: Int, z: Int) =>
      checkMultiplicativeSemigroupLaw(x, y, z)
    }
  }

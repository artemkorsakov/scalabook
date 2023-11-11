package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Generators.smallNumbersGen
import weaver.*
import weaver.scalacheck.*

object EuclideanRingSuite extends SimpleIOSuite, Checkers, EuclideanRingLaw:
  test(
    "Int is a Euclidean ring with unity with addition, 0, multiplication and 1"
  ):
    forall(smallNumbersGen): (x: Int, y: Int, z: Int) =>
      whenSuccess(checkEuclideanRingLaw(x, y, z)): _ =>
        success

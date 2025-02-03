package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.monoid.MonoidLaws.*
import ru.scalabook.typeclass.common.Generators.smallNumbersGen
import weaver.*
import weaver.scalacheck.*

object EuclideanRingSuite extends SimpleIOSuite, Checkers:
  test(
    "Int is a Euclidean ring with unity with addition, 0, multiplication and 1"
  ):
    forall(smallNumbersGen): (x: Int, y: Int, z: Int) =>
      expect(checkEuclideanRing(x, y, z).isValid)

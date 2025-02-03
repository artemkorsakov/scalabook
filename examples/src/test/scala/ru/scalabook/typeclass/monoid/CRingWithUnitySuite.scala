package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.monoid.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object CRingWithUnitySuite extends SimpleIOSuite, Checkers:
  test(
    "Int is a commutative ring with unity with addition, 0, multiplication and 1"
  ):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkCRingWithUnity(x, y, z).isValid)

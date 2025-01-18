package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.TypeLaws.*
import weaver.*
import weaver.scalacheck.*

object RingWithUnitySuite extends SimpleIOSuite, Checkers:
  test("Int is a ring with unity with addition, 0, multiplication and 1"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkRingWithUnity(x, y, z).isValid)

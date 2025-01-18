package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.TypeLaws.*
import ru.scalabook.typeclass.common.Generators.smallNumbersGen
import weaver.*
import weaver.scalacheck.*

object GCDRingSuite extends SimpleIOSuite, Checkers:
  test("Int is a GCD ring with unity with addition, 0, multiplication and 1"):
    forall(smallNumbersGen): (x: Int, y: Int, z: Int) =>
      expect(checkGCDRing(x, y, z).isValid)

package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.TypeLaws.*
import weaver.*
import weaver.scalacheck.*

object AbGroupSuite extends SimpleIOSuite, Checkers:
  test("Int is a Abelian group with addition"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkAbGroup(x, y, z).isValid)

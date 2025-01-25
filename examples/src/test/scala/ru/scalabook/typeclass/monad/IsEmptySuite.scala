package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.MonadLaws.*
import weaver.*
import weaver.scalacheck.*

object IsEmptySuite extends SimpleIOSuite, Checkers:
  test("listIsEmptyInstance must satisfy the IsEmpty laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      expect(checkIsEmptyLawLaw[List, Int](x, y, z).isValid)

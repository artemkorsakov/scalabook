package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object IsEmptySuite extends SimpleIOSuite, Checkers:
  test("listIsEmptyInstance must satisfy the IsEmpty laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      expect(checkIsEmptyLawLaw[List, Int](x, y, z).isValid)

package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object PlusEmptySuite extends SimpleIOSuite, Checkers:
  test("listPlusEmptyInstance must satisfy the PlusEmpty laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      expect(checkPlusEmptyLaw(x, y, z).isValid)

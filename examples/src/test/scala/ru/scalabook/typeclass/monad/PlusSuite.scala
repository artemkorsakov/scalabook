package ru.scalabook.typeclass.monad

import MonadLaws.*
import weaver.*
import weaver.scalacheck.*

object PlusSuite extends SimpleIOSuite, Checkers:
  test("listPlusInstance must satisfy the Plus laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      expect(checkPlusLaw(x, y, z).isValid)

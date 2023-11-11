package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

object IsEmptySuite extends SimpleIOSuite, Checkers, IsEmptyLaw:
  test("listIsEmptyInstance must satisfy the IsEmpty laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      whenSuccess(checkIsEmptyLawLaw[List, Int](x, y, z)): _ =>
        success

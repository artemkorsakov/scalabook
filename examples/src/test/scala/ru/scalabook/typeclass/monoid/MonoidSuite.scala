package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object MonoidSuite extends SimpleIOSuite, Checkers, MonoidLaw:
  test("Non-negative numbers are a monoid with addition and zero"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkMonoidLaw(x, y, z)): _ =>
        success

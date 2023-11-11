package ru.scalabook.typeclass.monoid

import ru.scalabook.algorithms.numbers.RationalNumber
import ru.scalabook.typeclass.common.Generators.given
import weaver.*
import weaver.scalacheck.*

object FieldSuite extends SimpleIOSuite, Checkers, FieldLaw:
  given cats.Show[RationalNumber] = cats.Show.fromToString

  test(
    "RationalNumber is a field with unity with addition, 0, multiplication and 1"
  ):
    forall: (x: RationalNumber, y: RationalNumber, z: RationalNumber) =>
      whenSuccess(checkFieldLaw(x, y, z)): _ =>
        success

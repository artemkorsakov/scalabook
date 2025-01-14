package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Laws.*
import spire.math.Rational
import ru.scalabook.typeclass.common.Generators.given
import weaver.*
import weaver.scalacheck.*

object FieldSuite extends SimpleIOSuite, Checkers:
  given cats.Show[Rational] = cats.Show.fromToString

  test(
    "RationalNumber is a field with unity with addition, 0, multiplication and 1"
  ):
    forall: (x: Rational, y: Rational, z: Rational) =>
      expect(checkField(x, y, z).isValid)

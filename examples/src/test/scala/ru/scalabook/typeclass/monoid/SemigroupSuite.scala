package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.cats.given
import ru.scalabook.typeclass.common.Generators.given
import ru.scalabook.typeclass.common.PositiveInt
import ru.scalabook.typeclass.monoid.Semigroup.given
import weaver.*
import weaver.scalacheck.*

object SemigroupSuite extends SimpleIOSuite, Checkers, SemigroupLaw:
  test("Natural numbers is a semigroup with addiction"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkSemigroupLaw(x, y, z)): _ =>
        success

  test("combineN should combine n times"):
    forall: (x: Int, n: PositiveInt) =>
      expect.eql(x * n, Semigroup[Int].combineN(x, n))

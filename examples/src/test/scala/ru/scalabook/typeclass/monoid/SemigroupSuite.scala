package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.constraint.all.Greater
import io.github.iltotore.iron.scalacheck.numeric.given
import ru.scalabook.typeclass.monoid.Semigroup.given
import weaver.*
import weaver.scalacheck.*

object SemigroupSuite extends SimpleIOSuite, Checkers, SemigroupLaw:
  test("Natural numbers is a semigroup with addiction"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkSemigroupLaw(x, y, z)): _ =>
        success

  test("combineN should combine n times"):
    forall: (x: Int, n: Int :| Greater[0]) =>
      expect.eql(x * n, Semigroup[Int].combineN(x, n))

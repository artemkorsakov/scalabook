package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.constraint.numeric.*
import io.github.iltotore.iron.scalacheck.numeric.given
import ru.scalabook.typeclass.common.Laws.checkMultiplicativeAssociativity
import weaver.*
import weaver.scalacheck.*

object MultiplicativeSemigroupSuite extends SimpleIOSuite, Checkers:
  test("Natural numbers is a semigroup with multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkMultiplicativeAssociativity(x, y, z).isValid)

  test("pow should multiply n times"):
    forall: (x: Int :| Interval.Open[1, 5], n: Int :| Interval.Open[1, 5]) =>
      expect.eql(
        math.pow(x, n).toInt,
        MultiplicativeSemigroup[Int].pow(x, n.refineUnsafe[Greater[0]])
      )

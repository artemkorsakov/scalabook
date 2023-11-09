package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.constraint.all.Greater
import ru.scalabook.typeclass.common.Generators.{smallInt, smallPosInt}
import weaver.*
import weaver.scalacheck.*

object MultiplicativeSemigroupSuite
    extends SimpleIOSuite,
      Checkers,
      MultiplicativeSemigroupLaw:
  test("Natural numbers is a semigroup with multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkMultiplicativeSemigroupLaw(x, y, z)): _ =>
        success

  test("pow should multiply n times"):
    forall(smallInt.flatMap(x => smallPosInt.map(n => (x, n)))):
      (x: Int, n: Int :| Greater[0]) =>
        expect.eql(math.pow(x, n).toInt, MultiplicativeSemigroup[Int].pow(x, n))

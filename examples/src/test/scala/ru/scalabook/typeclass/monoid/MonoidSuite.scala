package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.constraint.all.GreaterEqual
import io.github.iltotore.iron.scalacheck.numeric.given
import weaver.*
import weaver.scalacheck.*

object MonoidSuite extends SimpleIOSuite, Checkers, MonoidLaw:
  test("Non-negative numbers are a monoid with addition and zero"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkMonoidLaw(x, y, z)): _ =>
        success

  test("combineNonNegN should combine n (>=0) times"):
    forall: (x: Int, n: Int :| GreaterEqual[0]) =>
      expect.eql(x * n, Monoid[Int].combineNonNegN(x, n))

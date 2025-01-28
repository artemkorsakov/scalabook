package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Applicator
import ru.scalabook.typeclass.common.MonadLaws.*
import ru.scalabook.typeclass.monad.Divisible.given
import ru.scalabook.typeclass.monoid.Monoid
import ru.scalabook.typeclass.monoid.Monoid.booleanAnd
import weaver.*
import weaver.scalacheck.*

object DivisibleSuite extends SimpleIOSuite, Checkers:
  def delta[A]: A => (A, A) = a => (a, a)

  test("The unary function must satisfy the laws of Divisible"):
    forall: (x: List[Char]) =>
      type F = [X] =>> X => Boolean
      val fa1: F[List[Char]] = _.length % 2 == 0
      val fa2: F[List[Char]] = _.contains('a')
      val fa3: F[List[Char]] = _.nonEmpty
      val fc: F[Int]         = _ % 2 == 0

      val f: List[Char] => String = _.mkString
      val g: String => Int        = _.length

      given givenApplicatorF: Applicator[F] with
        override def run[A](a: A): F[A] => Boolean = _.apply(a)

      expect(
        checkDivisibleLaw[F, List[Char], String, Int](fa1, fa2, fa3, fc, x)(
          using
          functionDivisible[Boolean],
          givenApplicatorF,
          f,
          g
        ).isValid
      )
end DivisibleSuite

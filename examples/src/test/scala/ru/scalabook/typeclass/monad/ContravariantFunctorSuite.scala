package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Applicator
import ru.scalabook.typeclass.common.MonadLaws.*
import ru.scalabook.typeclass.monad.ContravariantFunctor.given
import weaver.*
import weaver.scalacheck.*

object ContravariantFunctorSuite extends SimpleIOSuite, Checkers:
  test("The unary function must satisfy the laws of contravariant functor"):
    forall: (x: List[Char]) =>
      type F = [X] =>> X => Boolean
      val fa: F[List[Char]] = _.length % 2 == 0
      val fc: F[Int]        = _ % 2 == 0

      val f: List[Char] => String = _.mkString
      val g: String => Int        = _.length

      given givenApplicatorF: Applicator[F] with
        override def run[A](a: A): F[A] => Boolean = _.apply(a)

      expect(
        checkContravariantFunctorLaw[F, List[Char], String, Int](fa, fc, x)(
          using
          functionContravariantFunctor,
          givenApplicatorF,
          f,
          g
        ).isValid
      )
end ContravariantFunctorSuite

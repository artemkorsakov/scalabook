package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

object ContravariantFunctorSuite
    extends SimpleIOSuite,
      Checkers,
      ContravariantFunctorLaw:
  test("The unary function must satisfy the laws of contravariant functor"):
    forall: (x: List[Char], y: Int) =>
      val f: List[Char] => String = _.mkString
      val g: String => Int        = _.length
      val fc: Int => Boolean      = _ % 2 == 0

      type F = [X] =>> X => Boolean
      whenSuccess(
        check(
          ContravariantFunctor[F].cmap[Int, Int](fc)(identity).apply(y) ==
            fc.apply(y),
          "check identity"
        ) combine
          check(
            ContravariantFunctor[F]
              .cmap[List[Char], String](
                ContravariantFunctor[F].cmap[String, Int](fc)(g)
              )(f)
              .apply(x) ==
              ContravariantFunctor[F]
                .cmap[List[Char], Int](fc)(a => g(f(a)))
                .apply(x),
            "check composition"
          )
      ): _ =>
        success
end ContravariantFunctorSuite

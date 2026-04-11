package ru.scalabook.typeclass.monad

import cats.Show
import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object MonadPlusSuite extends SimpleIOSuite, Checkers:
  test("listMonadPlusInstance must satisfy the MonadPlus laws"):
    forall:
      (
          a: Int,
          b: String,
          c: Boolean,
          fa1: List[Int],
          fa2: List[Int],
          fa3: List[Int]
      ) =>
        expect.all(
          checkMonadPlusLaw[List, Int, String, Boolean](
            a,
            b,
            c,
            a :: fa1,
            a :: fa2,
            a :: fa3
          ).isValid
        )

  test("listMonadPlusInstance must satisfy the filter laws"):
    forall: (fa1: List[Int]) =>
      expect.all(checkMonadPlusFilter(fa1).isValid)

  test("listMonadPlusInstance must satisfy the unite laws"):
    forall: (fga: List[Option[Int]]) =>
      expect.all(checkMonadPlusUnite(fga).isValid)

  private def checkMonadPlusFilter(fa: List[Int])(using
      mp: MonadPlus[List]
  ): ValidatedNel[String, Unit] =
    import mp.*
    Either.cond[String, Unit](
      filter[Int](fa)(_ % 2 == 0) == fa.filter(_ % 2 == 0),
      (),
      "Не соблюдается закон: filter(fa)(p) == fa.filter(p)"
    ).toValidatedNel

  private def checkMonadPlusUnite(fga: List[Option[Int]])(using
      mp: MonadPlus[List]
  ): ValidatedNel[String, Unit] =
    import mp.*
    Either.cond[String, Unit](
      unite(fga) == fga.flatten,
      (),
      "Не соблюдается закон: unite(fga) == fga.flatten"
    ).toValidatedNel
end MonadPlusSuite

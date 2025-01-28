package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.MonadLaws.*
import cats.Id
import cats.syntax.either.*
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object FoldableSuite extends SimpleIOSuite, Checkers:
  test("idFoldable must satisfy the Foldable laws"):
    forall: (x: Int) =>
      expect(
        (
          checkFoldableLaw[Id, Int](Id(x)) combine
            Either.cond[String, Unit](
              Foldable[Id].foldRight(Id(x))(100)(_ + _) == 100 + x,
              (),
              "Не соблюдается: Foldable[Id].foldRight(Id(x))(100)(_ + _) == 100 + x"
            ).toValidatedNel
        ).isValid
      )

package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object FoldableSuite extends SimpleIOSuite, Checkers, FoldableLaw:
  test("idFoldable must satisfy the Foldable laws"):
    forall: (x: Int) =>
      whenSuccess(
        checkFoldableLaw[Id, Int](Id(x)) combine
          check(Foldable[Id].foldRight(Id(x))(100)(_ + _) == 100 + x)
      ): _ =>
        success

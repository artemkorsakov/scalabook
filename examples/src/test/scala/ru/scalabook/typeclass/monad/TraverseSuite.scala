package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Instancies.given
import MonadLaws.*
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object TraverseSuite extends SimpleIOSuite, Checkers:
  test("optionTraverse must satisfy the laws of Traverse"):
    forall: (fa: Option[Int], fga: Option[List[Int]]) =>
      expect(
        checkTraverseLaw[Option, List, List, Int, String, Boolean](fa, fga).isValid
      )

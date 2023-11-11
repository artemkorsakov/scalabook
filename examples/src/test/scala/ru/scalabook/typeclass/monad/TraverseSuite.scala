package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object TraverseSuite extends SimpleIOSuite, Checkers, TraverseLaw:
  test("Traverse[List].sequence check"):
    forall: (x: Option[Int]) =>
      whenSuccess(
        check(Traverse[List].sequence[Option, Int](List(x)) == x.map(List(_)))
      ): _ =>
        success

  test("optionTraverse должен удовлетворять законам Traverse"):
    forall: (x: Option[Int]) =>
      whenSuccess(
        checkTraverseLaw[Option, List, List, Int, String, Boolean](x)
      ): _ =>
        success

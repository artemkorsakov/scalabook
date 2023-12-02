package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object MonadPlusSuite extends SimpleIOSuite, Checkers, MonadPlusLaw:
  test("listMonadPlusInstance must satisfy the MonadPlus laws"):
    forall:
      (
          i: Int,
          x: List[Int],
          y: List[Int],
          z: List[Int],
          fta: List[Option[Int]]
      ) =>
        whenSuccess(
          checkMonadPlusLaw[List, Int, String, Boolean](i, x, y, z) combine
            check(
              MonadPlus[List].filter(x)(_ % 2 == 0) ==
                x.filter(_ % 2 == 0)
            ) combine
            check(MonadPlus[List].unite(fta) == fta.flatten)
        ): _ =>
          success
end MonadPlusSuite

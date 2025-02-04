package ru.scalabook.typeclass.bifunctor

import ru.scalabook.typeclass.bifunctor.BifunctorLaws.*
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object BifoldableSuite extends SimpleIOSuite, Checkers:
  test("Bifoldable[Either] must satisfy the laws of Bifoldable"):
    forall:
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        expect(
          checkBifoldableLaw[Either, String, Char, Int](far, fla, faa).isValid
        )
end BifoldableSuite

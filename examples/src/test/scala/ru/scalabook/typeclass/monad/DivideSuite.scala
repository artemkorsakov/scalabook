package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

object DivideSuite extends SimpleIOSuite, Checkers, DivideLaw:
  test("The unary function must satisfy the laws of Divide"):
    forall: (x: Int) =>
      val fa1: Int => Int = _ + 1
      val fa2: Int => Int = _ - 1
      val fa3: Int => Int = _ * 2

      whenSuccess(
        check(
          (Divide[[X] =>> X => Int]
            .divide[Int, Int, Int](
              Divide[[X] =>> X => Int].divide[Int, Int, Int](fa1, fa2)(delta),
              fa3
            )(delta))(
            x
          ) ==
            (Divide[[X] =>> X => Int]
              .divide[Int, Int, Int](
                fa1,
                Divide[[X] =>> X => Int].divide[Int, Int, Int](fa2, fa3)(delta)
              )(delta))(
              x
            ),
          "composition"
        )
      ): _ =>
        success

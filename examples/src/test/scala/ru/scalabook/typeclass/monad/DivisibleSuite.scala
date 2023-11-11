package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

object DivisibleSuite extends SimpleIOSuite, Checkers, DivisibleLaw:
  test("The unary function must satisfy the laws of Divisible"):
    forall: (x: Int) =>
      val fa1: Int => Int = _ + 1
      val fa2: Int => Int = _ - 1
      val fa3: Int => Int = _ * 2

      whenSuccess(
        check(
          (Divisible[[X] =>> X => Int]
            .divide[Int, Int, Int](
              Divisible[[X] =>> X => Int]
                .divide[Int, Int, Int](fa1, fa2)(delta),
              fa3
            )(delta))(
            x
          ) ==
            (Divisible[[X] =>> X => Int]
              .divide[Int, Int, Int](
                fa1,
                Divisible[[X] =>> X => Int]
                  .divide[Int, Int, Int](fa2, fa3)(delta)
              )(delta))(
              x
            ),
          "composition"
        ) combine
          check(
            Divisible[[X] =>> X => Int].divide[Int, Int, Int](
              fa1,
              Divisible[[X] =>> X => Int].conquer[Int]
            )(delta)(x) ==
              fa1(x),
            "rightIdentity"
          ) combine
          check(
            Divisible[[X] =>> X => Int].divide[Int, Int, Int](
              Divisible[[X] =>> X => Int].conquer[Int],
              fa1
            )(delta)(x) ==
              fa1(x),
            "leftIdentity"
          )
      ): _ =>
        success

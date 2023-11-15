package ru.scalabook.libs.cats.effect

import cats.effect.IO
import weaver.SimpleIOSuite

object ExampleSuite extends SimpleIOSuite:
  test("make sure IO computes the right result"):
    IO.pure(1).map(_ + 2) flatMap { result =>
      IO(expect.eql(result, 3))
    }

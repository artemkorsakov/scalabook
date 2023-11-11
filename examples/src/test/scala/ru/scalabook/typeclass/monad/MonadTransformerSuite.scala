package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.{IO, Laws, OptionT}
import weaver.*
import weaver.scalacheck.*

object MonadTransformerSuite extends SimpleIOSuite, Checkers, Laws:
  test("optionTMonadTransformer should cast Option inside the monad"):
    forall: (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Option[Int]] =
        MonadTransformer[OptionT, IO].lift[Int](monadA).run
      whenSuccess(check(monadIdA.run().contains(x))): _ =>
        success

package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.{IO, OldLaws, OptionT}
import weaver.*
import weaver.scalacheck.*

object MonadTransformerSuite extends SimpleIOSuite, Checkers, OldLaws:
  test("optionTMonadTransformer should cast Option inside the monad"):
    forall: (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Option[Int]] =
        MonadTransformer[OptionT, IO].lift[Int](monadA).run
      whenSuccess(check(monadIdA.run().contains(x))): _ =>
        success

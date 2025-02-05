package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.{IO, OptionT}
import weaver.*
import weaver.scalacheck.*

object MonadTransformerSuite extends SimpleIOSuite, Checkers:
  test("optionTMonadTransformer should cast Option inside the monad"):
    forall: (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Option[Int]] =
        MonadTransformer[OptionT, IO].lift[Int](monadA).run
      expect(
        Either.cond[String, Unit](
          monadIdA.run().contains(x),
          (),
          "Не соблюдается трансформация"
        ).isRight
      )

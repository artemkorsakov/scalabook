package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.common.*

class MonadTransformerSuite extends ScalaCheckSuite:
  property("optionTMonadTransformer должен прокидывать Option внутрь монады") {
    forAll { (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Option[Int]] =
        MonadTransformer[OptionT, IO].lift[Int](monadA).run
      assertEquals(monadIdA.run(), Some(x))
    }
  }

package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.{IO, Id, IdT, OptionT}
import typeclass.monad.MonadTransformer.{lift, given}

class MonadTransformerSuite extends ScalaCheckSuite:
  property("idtMonadTransformer должен 'прокидывать внутрь монады' Id") {
    forAll { (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Id[Int]] = lift[IdT, IO, Int](monadA).run
      assertEquals(monadIdA.run(), Id(x))
    }
  }

  property("optionTMonadTransformer должен 'прокидывать внутрь монады' Option") {
    forAll { (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Option[Int]] = lift[OptionT, IO, Int](monadA).run
      assertEquals(monadIdA.run(), Some(x))
    }
  }

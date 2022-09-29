package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.{IO, Id, IdT}
import typeclass.monad.MonadTransformer.{lift, given}

class MonadTransformerSuite extends ScalaCheckSuite:
  property("idtMonadTransformer должен 'прокидывать внутрь монады' Id") {
    forAll { (x: Int) =>
      val monadA = IO(() => x)
      val monadIdA: IO[Id[Int]] = lift[IO, IdT, Int](monadA).runIdT
      assertEquals(monadIdA.run(), Id(x))
    }
  }

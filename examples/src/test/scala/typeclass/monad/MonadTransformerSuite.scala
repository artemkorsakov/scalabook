package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.MonadTransformer.given

class MonadTransformerSuite extends ScalaCheckSuite:
  property("idtMonadTransformer должен прокидывать Id внутрь монады") {
    forAll { (x: Int) =>
      val monadA                = IO(() => x)
      val monadIdA: IO[Id[Int]] =
        MonadTransformer[IdT, IO].lift[Int](monadA).run
      assertEquals(monadIdA.run(), Id(x))
    }
  }

  property("optionTMonadTransformer должен прокидывать Option внутрь монады") {
    forAll { (x: Int) =>
      val monadA                    = IO(() => x)
      val monadIdA: IO[Option[Int]] =
        MonadTransformer[OptionT, IO].lift[Int](monadA).run
      assertEquals(monadIdA.run(), Some(x))
    }
  }

  property("writerTMonadTransformer должен прокидывать Writer внутрь монады") {
    forAll { (x: Int) =>
      val monadA                      = IO(() => x)
      val monadIdA: IO[(String, Int)] =
        MonadTransformer[[Y[_], X] =>> WriterT[Y, String, X], IO]
          .lift[Int](monadA)
          .run()
      assertEquals(monadIdA.run(), ("", x))
    }
  }

  property("stateTMonadTransformer должен прокидывать State внутрь монады") {
    forAll { (x: Int) =>
      val monadA                      = IO(() => x)
      val monadIdA: IO[(String, Int)] =
        MonadTransformer[[Y[_], X] =>> StateT[Y, String, X], IO]
          .lift[Int](monadA)
          .run("state")
      assertEquals(monadIdA.run(), ("state", x))
    }
  }

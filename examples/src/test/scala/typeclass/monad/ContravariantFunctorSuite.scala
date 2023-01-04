package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.ContravariantFunctor.predicateContravariantFunctor

class ContravariantFunctorSuite extends ScalaCheckSuite:
  property(
    "Унарная функция должна удовлетворять законам контравариантного функтора"
  ) {
    forAll { (x: List[Char], y: Int) =>
      val f: List[Char] => String = _.mkString
      val g: String => Int        = _.length
      val fc: Int => Boolean      = _ % 2 == 0

      type F = [X] =>> X => Boolean
      assertEquals(
        ContravariantFunctor[F].cmap[Int, Int](fc)(identity).apply(y),
        fc.apply(y),
        "check identity"
      )
      assertEquals(
        ContravariantFunctor[F]
          .cmap[List[Char], String](
            ContravariantFunctor[F].cmap[String, Int](fc)(g)
          )(f)
          .apply(x),
        ContravariantFunctor[F]
          .cmap[List[Char], Int](fc)(a => g(f(a)))
          .apply(x),
        "check composition"
      )
    }
  }

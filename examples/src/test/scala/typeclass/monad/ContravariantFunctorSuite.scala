package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.ContravariantFunctor.{cmap, predicateContravariantFunctor}

class ContravariantFunctorSuite extends ScalaCheckSuite:
  property("Унарная функция должна удовлетворять законам контравариантного функтора") {
    forAll { (x: List[Char], y: Int) =>
      val f: List[Char] => String = _.mkString
      val g: String => Int = _.length
      val fc: Int => Boolean = _ % 2 == 0

      assertEquals(cmap[[X] =>> X => Boolean, Int, Int](fc)(identity).apply(y), fc.apply(y), "check identity")
      assertEquals(
        cmap[[X] =>> X => Boolean, List[Char], String](cmap[[X] =>> X => Boolean, String, Int](fc)(g))(f).apply(x),
        cmap[[X] =>> X => Boolean, List[Char], Int](fc)(a => g(f(a))).apply(x),
        "check composition"
      )
    }
  }

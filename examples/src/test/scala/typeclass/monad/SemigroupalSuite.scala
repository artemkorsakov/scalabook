package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import typeclass.common.Id

class SemigroupalSuite extends ScalaCheckSuite, SemigroupalLaw:
  property("Semigroupal[Id] должен удовлетворять законам Semigroupal") {
    forAll { (a: Int, b: String, c: Boolean) =>
      checkSemigroupalLawWithRunner[Id, Int, String, Boolean](
        Id(a),
        Id(b),
        Id(c)
      )
    }
  }

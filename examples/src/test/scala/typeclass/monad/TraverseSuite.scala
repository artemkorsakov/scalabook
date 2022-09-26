package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Traverse.{traverse, given}

class TraverseSuite extends ScalaCheckSuite:
  property("idTraverse должен менять местами 'обертку'") {
    forAll { (x: Int) =>
      val f: Int => List[Char] = i => i.toString.toList
      val actual = traverse[Id, List, Int, Char](Id(x), f)
      val expected = x.toString.toList.map(Id(_))
      assertEquals(actual, expected)
    }
  }

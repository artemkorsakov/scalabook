package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.monad.Traverse.given

class TraverseSuite extends ScalaCheckSuite, TraverseLaw:
  property("Проверка sequence") {
    forAll { (x: Option[Int]) =>
      assertEquals(
        Traverse[List].sequence[Option, Int](List(x)),
        x.map(List(_))
      )
    }
  }

  property("optionTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Option[Int]) =>
      checkTraverseLaw[Option, List, List, Int, String, Boolean](x)
    }
  }

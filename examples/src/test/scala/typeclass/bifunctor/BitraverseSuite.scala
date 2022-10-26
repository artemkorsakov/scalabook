package typeclass.bifunctor

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Traverse.{traverse, given}
import typeclass.monad.{Traverse, TraverseLaw}

class BitraverseSuite extends ScalaCheckSuite, TraverseLaw:
  property("Проверка sequence") {
    forAll { (x: Option[Int]) =>
      assertEquals(summon[Traverse[Id]].sequence[Option, Int](Id(x)), x.map(Id(_)))
    }
  }

  property("idTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Int) =>
      checkTraverseLaw[Id, Option, List, Int, String, Boolean](Id(x))
    }
  }

  property("tuple2Traverse должен удовлетворять законам Traverse") {
    forAll { (x: Int) =>
      checkTraverseLaw[[X] =>> (X, X), Option, List, Int, String, Boolean]((x, x))
    }
  }

  property("tuple3Traverse должен удовлетворять законам Traverse") {
    forAll { (x: Int) =>
      checkTraverseLaw[[X] =>> (X, X, X), Option, List, Int, String, Boolean]((x, x, x))
    }
  }

  property("optionTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Option[Int]) =>
      checkTraverseLaw[Option, Id, List, Int, String, Boolean](x)
    }
  }

  property("listTraverse должен удовлетворять законам Traverse") {
    forAll { (x: List[Int]) =>
      checkTraverseLaw[List, Id, Option, Int, String, Boolean](x)
    }
  }

  property("treeTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Int, list: List[Int]) =>
      val tree = Tree(x, list.map(a => Tree(a, Nil)))
      checkTraverseLaw[Tree, Id, Option, Int, String, Boolean](tree)
    }
  }

  property("mapTraverse должен удовлетворять законам Traverse") {
    forAll { (map: Map[String, Int]) =>
      checkTraverseLaw[[X] =>> Map[String, X], Id, Option, Int, String, Boolean](map)
    }
  }

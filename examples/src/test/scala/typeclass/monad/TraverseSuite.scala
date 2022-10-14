package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Traverse.{traverse, given}

class TraverseSuite extends ScalaCheckSuite, TraverseLaw:
  private val f: Int => Id[String] = i => Id(i.toString)

  property("idTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Int) =>
      checkTraverseLaw[Id, Int, String, Boolean](Id(x))
    }
  }

  property("tuple2Traverse должен удовлетворять законам Traverse") {
    forAll { (x: Int) =>
      val actual = traverse[[X] =>> (X, X), Id, Int, String]((x, x), f)
      assertEquals(actual, Id(x.toString, x.toString))
    }
  }

  property("tuple3Traverse должен удовлетворять законам Traverse") {
    forAll { (x: Int) =>
      val actual = traverse[[X] =>> (X, X, X), Id, Int, String]((x, x, x), f)
      assertEquals(actual, Id(x.toString, x.toString, x.toString))
    }
  }

  property("optionTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Option[Int]) =>
      val actual = traverse[Option, Id, Int, String](x, f)
      assertEquals(actual, Id(x.map(_.toString)))
    }
  }

  property("listTraverse должен удовлетворять законам Traverse") {
    forAll { (x: List[Int]) =>
      val actual = traverse[List, Id, Int, String](x, f)
      assertEquals(actual, Id(x.map(_.toString)))
    }
  }

  property("treeTraverse должен удовлетворять законам Traverse") {
    forAll { (x: Int, list: List[Int]) =>
      val tree = Tree(x, list.map(a => Tree(a, Nil)))
      val actual = traverse[Tree, Id, Int, String](tree, f)
      val expected = Tree(x.toString, list.map(a => Tree(a.toString, Nil)))
      assertEquals(actual, Id(expected))
    }
  }

  property("mapTraverse должен удовлетворять законам Traverse") {
    forAll { (map: Map[String, Int]) =>
      val actual = traverse[[X] =>> Map[String, X], Id, Int, String](map, f)
      val expected = map.view.mapValues(_.toString).toMap
      assertEquals(actual, Id(expected))
    }
  }

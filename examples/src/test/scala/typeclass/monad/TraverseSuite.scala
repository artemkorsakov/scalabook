package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Traverse.{traverse, given}

class TraverseSuite extends ScalaCheckSuite:
  private val f: Int => Id[String] = i => Id(i.toString)

  property("idTraverse должен менять местами 'обертку'") {
    forAll { (x: Int) =>
      val f: Int => List[Char] = i => i.toString.toList
      val actual = traverse[Id, List, Int, Char](Id(x), f)
      val expected = x.toString.toList.map(Id(_))
      assertEquals(actual, expected)
    }
  }

  property("tuple2Traverse должен менять местами 'обертку'") {
    forAll { (x: Int) =>
      val actual = traverse[[X] =>> (X, X), Id, Int, String]((x, x), f)
      assertEquals(actual, Id(x.toString, x.toString))
    }
  }

  property("tuple3Traverse должен менять местами 'обертку'") {
    forAll { (x: Int) =>
      val actual = traverse[[X] =>> (X, X, X), Id, Int, String]((x, x, x), f)
      assertEquals(actual, Id(x.toString, x.toString, x.toString))
    }
  }

  property("optionTraverse должен менять местами 'обертку'") {
    forAll { (x: Option[Int]) =>
      val actual = traverse[Option, Id, Int, String](x, f)
      assertEquals(actual, Id(x.map(_.toString)))
    }
  }

  property("listTraverse должен менять местами 'обертку'") {
    forAll { (x: List[Int]) =>
      val actual = traverse[List, Id, Int, String](x, f)
      assertEquals(actual, Id(x.map(_.toString)))
    }
  }

  property("treeTraverse должен менять местами 'обертку'") {
    forAll { (x: Int, list: List[Int]) =>
      val tree = Tree(x, list.map(a => Tree(a, Nil)))
      val actual = traverse[Tree, Id, Int, String](tree, f)
      val expected = Tree(x.toString, list.map(a => Tree(a.toString, Nil)))
      assertEquals(actual, Id(expected))
    }
  }

  property("mapTraverse должен менять местами 'обертку'") {
    forAll { (map: Map[String, Int]) =>
      val actual = traverse[[X] =>> Map[String, X], Id, Int, String](map, f)
      val expected = map.view.mapValues(_.toString).toMap
      assertEquals(actual, Id(expected))
    }
  }

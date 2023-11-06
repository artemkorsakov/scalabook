package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.monoid.Semigroup.given
import weaver.*
import weaver.scalacheck.*

object SemigroupSuite extends SimpleIOSuite, Checkers, SemigroupLaw:
  test("Natural numbers is a semigroup with addiction"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkSemigroupLaw(x, y, z)): _ =>
        success

//  test("combineN должен складывать k раз") {
//    forall { (x: Int) =>
//      val actual = Semigroup[Int].combineN(x, 10)
//      assertEquals(actual, x * 10)
//    }
//  }

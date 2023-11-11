package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Generators.smallNumbersGen
import weaver.*
import weaver.scalacheck.*

object EuclideanRingSuite extends SimpleIOSuite, Checkers, EuclideanRingLaw:
  test("EuclideanRing[Int] должен удовлетворять законам евклидова кольца"):
    forall(smallNumbersGen): (x: Int, y: Int, z: Int) =>
      whenSuccess( checkEuclideanRingLaw(x, y, z)): _ =>
        success

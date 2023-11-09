package ru.scalabook.typeclass.common

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*
import org.scalacheck.{Arbitrary, Gen}

object Generators:
  val smallInt: Gen[Int]                  = Gen.choose(1, 5)
  val smallPosInt: Gen[Int :| Greater[0]] = smallInt.map(i => i.refine)

  given posIntArb: Arbitrary[Int :| Greater[0]] = Arbitrary(
    Gen.posNum[Int].map(i => i.refine)
  )

  given nonNegIntArg: Arbitrary[Int :| GreaterEqual[0]] = Arbitrary(
    Gen.frequency((1, Gen.const(0)), (4, Gen.posNum[Int])).map(i => i.refine)
  )

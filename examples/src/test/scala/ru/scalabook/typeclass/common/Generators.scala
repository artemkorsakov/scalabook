package ru.scalabook.typeclass.common

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*
import org.scalacheck.{Arbitrary, Gen}

object Generators:
  given posIntArb: Arbitrary[Int :| Greater[0]] = Arbitrary(
    Gen.posNum[Int].map(i => i.refine)
  )

  given nonNegIntArg: Arbitrary[Int :| GreaterEqual[0]] = Arbitrary(
    Gen.frequency((1, Gen.const(0)), (4, Gen.posNum[Int])).map(i => i.refine)
  )

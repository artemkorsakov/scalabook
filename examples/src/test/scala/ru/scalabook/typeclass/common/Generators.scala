package ru.scalabook.typeclass.common

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*
import org.scalacheck.{Arbitrary, Gen}
import ru.scalabook.typeclass.common.PositiveInt

object Generators:
  given Arbitrary[PositiveInt] = Arbitrary(Gen.posNum[Int].map(i => i.refine))

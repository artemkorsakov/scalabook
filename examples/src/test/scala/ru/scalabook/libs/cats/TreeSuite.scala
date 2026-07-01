package ru.scalabook.libs.cats

import cats.kernel.laws.discipline.SemigroupTests
import cats.laws.discipline.FunctorTests
import org.scalacheck.{Arbitrary, Gen}
import weaver.*
import weaver.discipline.*

object TreeSuite extends FunSuite with Discipline:
  given arbTree[A: Arbitrary]: Arbitrary[Tree[A]] =
    Arbitrary(Gen.oneOf(
      Gen.const(Leaf),
      Arbitrary.arbitrary[A].map(e => Node(e, Leaf, Leaf))
    ))

  checkAll("Tree.FunctorLaws", FunctorTests[Tree].functor[Int, Int, String])

  checkAll("Tree[Int].SemigroupLaws", SemigroupTests[Tree[Int]].semigroup)

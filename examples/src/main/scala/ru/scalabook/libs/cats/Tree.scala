package ru.scalabook.libs.cats

import cats.*

sealed trait Tree[+A]
case object Leaf                                        extends Tree[Nothing]
case class Node[A](p: A, left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree:
  given functorTree: Functor[Tree] = new Functor[Tree]:
    def map[A, B](tree: Tree[A])(f: A => B): Tree[B] =
      tree match
        case Leaf                 => Leaf
        case Node(p, left, right) => Node(f(p), map(left)(f), map(right)(f))

  given semigroupTree[A: Semigroup]: Semigroup[Tree[A]] =
    new Semigroup[Tree[A]]:
      def combine(x: Tree[A], y: Tree[A]): Tree[A] = (x, y) match
        case (Leaf, _)                                          => Leaf
        case (_, Leaf)                                          => Leaf
        case (Node(xp, xLeft, xRight), Node(yp, yLeft, yRight)) =>
          Node(
            Semigroup[A].combine(xp, yp),
            combine(xLeft, yLeft),
            combine(xRight, yRight)
          )

  given eqTree[A: Eq]: Eq[Tree[A]] = Eq.fromUniversalEquals

  given showTree[A: Show]: Show[Tree[A]] =
    Show.fromToString
end Tree

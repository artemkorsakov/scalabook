package ru.scalabook.algorithms.heap

import scala.math.Ordered.orderingToOrdered

enum SkewHeap[+A]:
  import SkewHeap.*

  case Leaf
  private case Branch(min: A, left: SkewHeap[A], right: SkewHeap[A])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  def insert[B >: A: Ordering](x: B): SkewHeap[B] =
    merge(apply(x), this)

  def remove[B >: A: Ordering]: Option[SkewHeap[B]] = this match
    case Branch(_, left, right) => Some(merge(left, right))
    case _                      => None

end SkewHeap

object SkewHeap:
  def empty[A]: SkewHeap[A] = Leaf

  def apply[A: Ordering](x: A): SkewHeap[A] =
    Branch(x, Leaf, Leaf)

  def merge[A: Ordering](x: SkewHeap[A], y: SkewHeap[A]): SkewHeap[A] =
    (x, y) match
      case (_, Leaf)                                => x
      case (Leaf, _)                                => y
      case (Branch(x1, l1, r1), Branch(x2, l2, r2)) =>
        if x1 < x2 then
          Branch(x1, merge(Branch(x2, l2, r2), r1), l1)
        else Branch(x2, merge(Branch(x1, l1, r1), r2), l2)

end SkewHeap

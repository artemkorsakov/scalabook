package ru.scalabook.algorithms.heap

import scala.math.Ordered.orderingToOrdered

enum SkewHeap[+A]:
  import SkewHeap.*

  private case EmptySkewHeap

  private case NonEmptySkewHeap(
      min: A,
      left: SkewHeap[A],
      right: SkewHeap[A]
  )

  lazy val isEmpty: Boolean = this match
    case EmptySkewHeap => true
    case _             => false

  def insert[B >: A: Ordering](x: B): SkewHeap[B] =
    merge(make(x), this)

  def remove[B >: A: Ordering]: Option[SkewHeap[B]] = this match
    case NonEmptySkewHeap(_, left, right) => Some(merge(left, right))
    case _                                => None

end SkewHeap

object SkewHeap:
  def empty[A]: SkewHeap[A] = EmptySkewHeap

  def make[A: Ordering](x: A): SkewHeap[A] =
    NonEmptySkewHeap(x, EmptySkewHeap, EmptySkewHeap)

  def merge[A: Ordering](x: SkewHeap[A], y: SkewHeap[A]): SkewHeap[A] =
    (x, y) match
      case (_, EmptySkewHeap) => x
      case (EmptySkewHeap, _) => y
      case (NonEmptySkewHeap(x1, l1, r1), NonEmptySkewHeap(x2, l2, r2)) =>
        if x1 < x2 then
          NonEmptySkewHeap(x1, merge(NonEmptySkewHeap(x2, l2, r2), r1), l1)
        else NonEmptySkewHeap(x2, merge(NonEmptySkewHeap(x1, l1, r1), r2), l2)

end SkewHeap

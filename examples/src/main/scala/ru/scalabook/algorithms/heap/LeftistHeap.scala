package ru.scalabook.algorithms.heap

import scala.math.Ordered.orderingToOrdered

enum LeftistHeap[+A]:
  import LeftistHeap.*

  private case EmptyLeftistHeap

  private case NonEmptyLeftistHeap(
      min: A,
      left: LeftistHeap[A],
      right: LeftistHeap[A],
      override val rank: Int
  )

  lazy val isEmpty: Boolean = this match
    case EmptyLeftistHeap => true
    case _                => false

  val rank: Int = this match
    case neh: NonEmptyLeftistHeap[A] => neh.rank
    case _                           => 0

  lazy val minOption: Option[A] = this match
    case neh: NonEmptyLeftistHeap[A] => Some(neh.min)
    case _                           => None

  def insert[B >: A: Ordering](x: B): LeftistHeap[B] = this match
    case NonEmptyLeftistHeap(min, left, right, _) =>
      if left.rank > right.rank then bubble(min, left, right.insert(x))
      else bubble(min, left.insert(x), right)
    case _ => make(x)

  def remove[B >: A: Ordering]: Option[LeftistHeap[B]] = this match
    case NonEmptyLeftistHeap(_, left, right, _) => Some(merge(left, right))
    case _                                      => None
end LeftistHeap

object LeftistHeap:
  def empty[A]: LeftistHeap[A] = EmptyLeftistHeap

  def make[A: Ordering](x: A): LeftistHeap[A] =
    make(x, EmptyLeftistHeap, EmptyLeftistHeap)

  private def make[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    NonEmptyLeftistHeap(x, l, r, r.rank + 1)

  private def bubble[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    (l, r) match
      case (NonEmptyLeftistHeap(y, lt, rt, _), _) if x > y =>
        make(y, make(x, lt, rt), r)
      case (_, NonEmptyLeftistHeap(z, lt, rt, _)) if x > z =>
        make(z, l, make(x, lt, rt))
      case (_, _) => make(x, l, r)

  def merge[A: Ordering](x: LeftistHeap[A], y: LeftistHeap[A]): LeftistHeap[A] =
    (x, y) match
      case (_, EmptyLeftistHeap) => x
      case (EmptyLeftistHeap, _) => y
      case (
            NonEmptyLeftistHeap(xx, xl, xr, _),
            NonEmptyLeftistHeap(yy, yl, yr, _)
          ) =>
        if xx < yy then swap(xx, xl, merge(xr, y))
        else swap(yy, yl, merge(yr, x))

  private def swap[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    if l.rank < r.rank then make(x, r, l)
    else make(x, l, r)

end LeftistHeap

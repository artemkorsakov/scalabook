package ru.scalabook.algorithms.heap

import scala.math.Ordered.orderingToOrdered

enum LeftistHeap[+A]:
  import LeftistHeap.*

  case Leaf

  private case Branch(
      min: A,
      left: LeftistHeap[A],
      right: LeftistHeap[A],
      override val rank: Int
  )

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  val rank: Int = this match
    case neh: Branch[A] => neh.rank
    case _              => 0

  lazy val minOption: Option[A] = this match
    case neh: Branch[A] => Some(neh.min)
    case _              => None

  def insert[B >: A: Ordering](x: B): LeftistHeap[B] = this match
    case Branch(min, left, right, _) =>
      if left.rank > right.rank then bubble(min, left, right.insert(x))
      else bubble(min, left.insert(x), right)
    case _ => LeftistHeap(x)

  def remove[B >: A: Ordering]: Option[LeftistHeap[B]] = this match
    case Branch(_, left, right, _) => Some(merge(left, right))
    case _                         => None
end LeftistHeap

object LeftistHeap:
  def empty[A]: LeftistHeap[A] = Leaf

  def apply[A: Ordering](x: A): LeftistHeap[A] =
    LeftistHeap(x, Leaf, Leaf)

  private def apply[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    Branch(x, l, r, r.rank + 1)

  private def bubble[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    (l, r) match
      case (Branch(y, lt, rt, _), _) if x > y =>
        LeftistHeap(y, LeftistHeap(x, lt, rt), r)
      case (_, Branch(z, lt, rt, _)) if x > z =>
        LeftistHeap(z, l, LeftistHeap(x, lt, rt))
      case (_, _) => LeftistHeap(x, l, r)

  def merge[A: Ordering](x: LeftistHeap[A], y: LeftistHeap[A]): LeftistHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (
            Branch(xx, xl, xr, _),
            Branch(yy, yl, yr, _)
          ) =>
        if xx < yy then swap(xx, xl, merge(xr, y))
        else swap(yy, yl, merge(yr, x))

  private def swap[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    if l.rank < r.rank then LeftistHeap(x, r, l)
    else LeftistHeap(x, l, r)

end LeftistHeap

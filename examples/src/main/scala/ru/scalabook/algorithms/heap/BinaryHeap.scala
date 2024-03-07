package ru.scalabook.algorithms.heap

import scala.math.Ordered.orderingToOrdered

enum BinaryHeap[+A]:
  import BinaryHeap.*

  private case EmptyHeap

  private case NonEmptyHeap(
      min: A,
      left: BinaryHeap[A],
      right: BinaryHeap[A],
      override val size: Int,
      override val height: Int
  )

  lazy val isEmpty: Boolean = this match
    case EmptyHeap => true
    case _         => false

  val size: Int = this match
    case neh: NonEmptyHeap[A] => neh.size
    case _                    => 0

  val height: Int = this match
    case neh: NonEmptyHeap[A] => neh.height
    case _                    => 0

  lazy val minOption: Option[A] = this match
    case neh: NonEmptyHeap[A] => Some(neh.min)
    case _                    => None

  def insert[B >: A: Ordering](x: B): BinaryHeap[B] = this match
    case EmptyHeap => make(x)
    case NonEmptyHeap(min, left, right, size, height) =>
      if left.size < left.height * left.height - 1 then
        bubbleUp(min, left.insert(x), right)
      else if right.size < right.height * right.height - 1 then
        bubbleUp(min, left, right.insert(x))
      else if right.height < left.height then
        bubbleUp(min, left, right.insert(x))
      else
        bubbleUp(min, left.insert(x), right)

  def remove[B >: A: Ordering]: Option[BinaryHeap[B]] =
    def floatLeft(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      l match
        case NonEmptyHeap(y, lt, rt, _, _) => make(y, make(x, lt, rt), r)
        case _                             => make(x, l, r)

    def floatRight(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      r match
        case NonEmptyHeap(y, lt, rt, _, _) => make(y, l, make(x, lt, rt))
        case _                             => make(x, l, r)

    def mergeChildren(l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      (l, r) match
        case (EmptyHeap, EmptyHeap) => EmptyHeap
        case (EmptyHeap, NonEmptyHeap(rmin, rleft, rright, rsize, rheight)) =>
          floatRight(rmin, l, mergeChildren(rleft, rright))
        case (NonEmptyHeap(lmin, lleft, lright, lsize, lheight), EmptyHeap) =>
          floatLeft(lmin, mergeChildren(lleft, lright), r)
        case (
              NonEmptyHeap(lmin, lleft, lright, lsize, lheight),
              NonEmptyHeap(rmin, rleft, rright, rsize, rheight)
            ) =>
          if lsize < lheight * lheight - 1 then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else if rsize < rheight * rheight - 1 then
            floatRight(rmin, l, mergeChildren(rleft, rright))
          else if rheight < lheight then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else floatRight(rmin, l, mergeChildren(rleft, rright))

    def bubbleRootDown(h: BinaryHeap[A]): BinaryHeap[A] = h match
      case NonEmptyHeap(min, left, right, _, _) =>
        bubbleDown(min, left, right)
      case _ => EmptyHeap

    this match
      case NonEmptyHeap(_, left, right, _, _) =>
        Some(bubbleRootDown(mergeChildren(left, right)))
      case _ => None
  end remove
end BinaryHeap

object BinaryHeap:
  def empty[A]: BinaryHeap[A] = EmptyHeap

  def make[A: Ordering](x: A): BinaryHeap[A] =
    make(x, EmptyHeap, EmptyHeap)

  private def make[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] =
    NonEmptyHeap(x, l, r, l.size + r.size + 1, math.max(l.height, r.height) + 1)

  private def bubbleUp[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (NonEmptyHeap(y, lt, rt, _, _), _) if x > y =>
      make(y, make(x, lt, rt), r)
    case (_, NonEmptyHeap(z, lt, rt, _, _)) if x > z =>
      make(z, l, make(x, lt, rt))
    case (_, _) => make(x, l, r)

  private def bubbleDown[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (NonEmptyHeap(y, _, _, _, _), NonEmptyHeap(z, lt, rt, _, _))
        if z < y && z < x =>
      make(z, l, bubbleDown(x, lt, rt))
    case (NonEmptyHeap(y, lt, rt, _, _), _) if x > y =>
      make(y, bubbleDown(x, lt, rt), r)
    case (_, _) => make(x, l, r)
end BinaryHeap

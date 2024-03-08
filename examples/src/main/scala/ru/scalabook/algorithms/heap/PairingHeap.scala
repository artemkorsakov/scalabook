package ru.scalabook.algorithms.heap

import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

enum PairingHeap[+A]:

  import PairingHeap.*

  private case EmptyPairingHeap

  private case NonEmptyPairingHeap(min: A, subtrees: List[PairingHeap[A]])

  lazy val isEmpty: Boolean = this match
    case EmptyPairingHeap => true
    case _                => false

  def insert[B >: A: Ordering](x: B): PairingHeap[B] =
    merge(make(x), this)

  def remove[B >: A: Ordering]: Option[PairingHeap[B]] = this match
    case NonEmptyPairingHeap(_, subtrees) => Some(pairing(subtrees))
    case _                                => None

end PairingHeap

object PairingHeap:
  def empty[A]: PairingHeap[A] = EmptyPairingHeap

  def make[A: Ordering](x: A): PairingHeap[A] =
    NonEmptyPairingHeap(x, List.empty)

  private def merge[A: Ordering](
      x: PairingHeap[A],
      y: PairingHeap[A]
  ): PairingHeap[A] =
    (x, y) match
      case (_, EmptyPairingHeap) => x
      case (EmptyPairingHeap, _) => y
      case (NonEmptyPairingHeap(x1, subs1), NonEmptyPairingHeap(x2, subs2)) =>
        if x1 < x2 then
          NonEmptyPairingHeap(x1, NonEmptyPairingHeap(x2, subs2) :: subs1)
        else
          NonEmptyPairingHeap(x2, NonEmptyPairingHeap(x1, subs1) :: subs2)

  @tailrec
  private def pairing[A: Ordering](subtrees: List[PairingHeap[A]])
      : PairingHeap[A] =
    subtrees match
      case Nil              => EmptyPairingHeap
      case hd :: Nil        => hd
      case h1 :: h2 :: tail => pairing(merge(h1, h2) :: tail)

end PairingHeap

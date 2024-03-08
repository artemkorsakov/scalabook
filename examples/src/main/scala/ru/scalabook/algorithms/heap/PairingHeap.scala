package ru.scalabook.algorithms.heap

import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

enum PairingHeap[+A]:

  import PairingHeap.*

  case Leaf
  private case Branch(min: A, subtrees: List[PairingHeap[A]])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  def insert[B >: A: Ordering](x: B): PairingHeap[B] =
    merge(PairingHeap(x), this)

  def remove[B >: A: Ordering]: Option[PairingHeap[B]] = this match
    case Branch(_, subtrees) => Some(pairing(subtrees))
    case _                   => None

end PairingHeap

object PairingHeap:
  def empty[A]: PairingHeap[A] = Leaf

  def apply[A: Ordering](x: A): PairingHeap[A] =
    Branch(x, List.empty)

  private def merge[A: Ordering](
      x: PairingHeap[A],
      y: PairingHeap[A]
  ): PairingHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (Branch(x1, subs1), Branch(x2, subs2)) =>
        if x1 < x2 then
          Branch(x1, Branch(x2, subs2) :: subs1)
        else
          Branch(x2, Branch(x1, subs1) :: subs2)

  @tailrec
  private def pairing[A: Ordering](subtrees: List[PairingHeap[A]])
      : PairingHeap[A] =
    subtrees match
      case Nil              => Leaf
      case hd :: Nil        => hd
      case h1 :: h2 :: tail => pairing(merge(h1, h2) :: tail)

end PairingHeap

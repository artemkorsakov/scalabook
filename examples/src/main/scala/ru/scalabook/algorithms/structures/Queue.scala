package ru.scalabook.algorithms.structures

final case class Queue[+A](in: List[A], out: List[A]):
  def frontOption: Option[A] = dequeue.map(_._1)

  def rearOption: Option[Queue[A]] = dequeue.map(_._2)

  lazy val isEmpty: Boolean = in.isEmpty && out.isEmpty

  def enqueue[B >: A](x: B): Queue[B] = Queue(x :: in, out)

  def dequeue: Option[(A, Queue[A])] = out match
    case hd :: tl => Some((hd, Queue(in, tl)))
    case Nil      => in.reverse match
        case hd :: tl => Some((hd, Queue(Nil, tl)))
        case Nil      => None

  override lazy val toString: String = (out ::: in.reverse).mkString(
    "Queue(",
    ",",
    ")"
  )
end Queue

object Queue:
  def empty[A]: Queue[A] = Queue(Nil, Nil)

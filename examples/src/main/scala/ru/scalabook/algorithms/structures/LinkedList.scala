package ru.scalabook.algorithms.structures

enum LinkedList[+A]:
  case Nil
  case Cons(head: A, tail: LinkedList[A])

  lazy val headOption: Option[A] = this match
    case Nil        => None
    case Cons(h, _) => Some(h)

  lazy val tailOption: Option[LinkedList[A]] = this match
    case Nil           => None
    case Cons(_, tail) => Some(tail)

  lazy val isEmpty: Boolean = this match
    case Nil => true
    case _   => false

  lazy val length: Int = this match
    case Nil           => 0
    case Cons(_, tail) => 1 + tail.length

  def prepend[B >: A](b: B): LinkedList[B] = Cons(b, this)

  def append[B >: A](b: B): LinkedList[B] = this match
    case Nil              => Cons(b, Nil)
    case Cons(head, tail) => Cons(head, tail.append(b))

  def get(i: Int): Option[A] =
    if i < 0 then None
    else if i == 0 then headOption
    else tailOption.flatMap(_.get(i - 1))

  def contains[B >: A](x: B): Boolean = this match
    case Nil                        => false
    case Cons(head, _) if head == x => true
    case Cons(_, tail)              => tail.contains(x)

  def concat[B >: A](list: LinkedList[B]): LinkedList[B] = this match
    case Nil              => list
    case Cons(head, tail) => Cons(head, tail.concat(list))

  def filter(p: A => Boolean): LinkedList[A] = this match
    case Nil                         => Nil
    case Cons(head, tail) if p(head) => Cons(head, tail.filter(p))
    case Cons(_, tail)               => tail.filter(p)

  def map[B](f: A => B): LinkedList[B] = this match
    case Nil              => Nil
    case Cons(head, tail) => Cons(f(head), tail.map(f))

  def fold[B](init: B)(op: (B, A) => B): B = this match
    case Nil              => init
    case Cons(head, tail) => tail.fold(op(init, head))(op)

end LinkedList

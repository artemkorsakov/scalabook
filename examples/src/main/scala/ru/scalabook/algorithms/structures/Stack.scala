package ru.scalabook.algorithms.structures

enum Stack[+A]:
  case EmptyStack
  case NonEmptyStack(value: A, tail: Stack[A])

  lazy val headOption: Option[A] = this match
    case EmptyStack          => None
    case NonEmptyStack(h, _) => Some(h)

  lazy val tailOption: Option[Stack[A]] = this match
    case EmptyStack             => None
    case NonEmptyStack(_, tail) => Some(tail)

  lazy val isEmpty: Boolean = this match
    case EmptyStack          => true
    case NonEmptyStack(_, _) => false

  lazy val length: Int = this match
    case EmptyStack             => 0
    case NonEmptyStack(_, tail) => 1 + tail.length

  def push[B >: A](value: B): Stack[B] = NonEmptyStack(value, this)

  def pop(): Option[(A, Stack[A])] = this match
    case EmptyStack                 => None
    case NonEmptyStack(value, tail) => Some((value, tail))

  def peek(): Option[(A, Stack[A])] = this match
    case EmptyStack              => None
    case NonEmptyStack(value, _) => Some((value, this))

end Stack

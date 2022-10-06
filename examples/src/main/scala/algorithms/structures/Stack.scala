package algorithms.structures

enum Stack[+A]:
  case EmptyStack
  case NonEmptyStack(value: A, tail: Stack[A])

  lazy val isEmpty: Boolean = this match
    case EmptyStack          => true
    case NonEmptyStack(_, _) => false

object Stack:
  def empty[A]: Stack[A] = EmptyStack

  extension [A](stack: Stack[A])
    def push[B <: A](value: B): Stack[A] = NonEmptyStack(value, stack)

    def peek(): Option[(A, Stack[A])] = stack match
      case EmptyStack              => None
      case NonEmptyStack(value, _) => Some((value, stack))

    def pop(): Option[(A, Stack[A])] = stack match
      case EmptyStack                 => None
      case NonEmptyStack(value, tail) => Some((value, tail))

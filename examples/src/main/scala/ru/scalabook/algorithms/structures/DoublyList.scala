package ru.scalabook.algorithms.structures

enum DoublyList[+A]:
  case Nil
  case DoublyListCons(left: DoublyList[A], value: A, right: DoublyList[A])

  lazy val length: Int = this match
    case Nil                            => 0
    case DoublyListCons(left, _, right) => left.length + 1 + right.length

object DoublyList:
  def prepend[A](list: DoublyList[A], a: A): DoublyList[A] = list match
    case Nil                                => DoublyListCons(Nil, a, Nil)
    case DoublyListCons(left, value, right) =>
      DoublyListCons(prepend(left, a), value, right)

  def append[A](list: DoublyList[A], a: A): DoublyList[A] = list match
    case Nil                                => DoublyListCons(Nil, a, Nil)
    case DoublyListCons(left, value, right) =>
      DoublyListCons(left, value, append(right, a))

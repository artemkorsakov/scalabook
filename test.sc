enum List[+A]:
  case Nil
  case Cons(head: A, tail: List[A])

object List:
  def apply[A](as: A*): List[A] =
    if as.isEmpty then Nil
    else Cons(as.head, apply(as.tail*))

import List.*

val ex1: List[Double] = Nil
// ex1: List[Double] = Nil
val ex2: List[Int] = Cons(1, Nil)
// ex2: List[Int] = Cons(head = 1, tail = Nil)
val ex3: List[String] = Cons("a", Cons("b", Nil))
// ex3: List[String] = Cons(head = "a", tail = Cons(head = "b", tail = Nil))
val ex4: List[String] = List.apply("a", "b")
// ex4: List[String] = Cons(head = "a", tail = Cons(head = "b", tail = Nil))

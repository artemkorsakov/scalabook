trait Monoid[A]:
  def empty: A
  def compose(a1: A, a2: A): A

final case class NonEmptyList[A](head: A, tail: List[A])

object MonoidWithException extends Monoid[NonEmptyList[Int]]:
  val empty: NonEmptyList[Int]  = ???
  val empty1: NonEmptyList[Int] = throw new Exception("")
  var empty2: NonEmptyList[Int] = _
  val empty3: NonEmptyList[Int] = null
  def compose(l1: NonEmptyList[Int], l2: NonEmptyList[Int]): NonEmptyList[Int] =
    NonEmptyList(head = l1.head, tail = l1.tail ++ (l2.head :: l2.tail))

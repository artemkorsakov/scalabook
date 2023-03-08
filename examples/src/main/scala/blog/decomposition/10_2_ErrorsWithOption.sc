trait Monoid[A]:
  val empty: Option[A]
  def compose(a1: A, a2: A): A

final case class NonEmptyList[A](head: A, tail: List[A])

object MonoidWithOption extends Monoid[NonEmptyList[Int]]:
  val empty: Option[NonEmptyList[Int]] = None
  def compose(l1: NonEmptyList[Int], l2: NonEmptyList[Int]): NonEmptyList[Int] =
    NonEmptyList(head = l1.head, tail = l1.tail ++ (l2.head :: l2.tail))


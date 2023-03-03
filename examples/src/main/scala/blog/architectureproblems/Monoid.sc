trait Monoid[A]:
  def e: A
  extension (x: A) def |+|(y: A): A

given listMonoid[T]: Monoid[List[T]] with
  override def e: List[T] = List.empty
  extension (x: List[T]) override def |+|(y: List[T]): List[T] = y ++ x

List(1, 2, 3) |+| List(4, 5, 6)

final case class NonEmptyList[A](head: A, tail: List[A]):
  def ++(that: NonEmptyList[A]): NonEmptyList[A] =
    NonEmptyList(head, tail ++ (that.head :: that.tail))

given monoidWithException[T]: Monoid[NonEmptyList[T]] with
  override def e: NonEmptyList[T] =
    throw new IllegalArgumentException("...")
  extension (x: NonEmptyList[T])
    override def |+|(y: NonEmptyList[T]): NonEmptyList[T] = x ++ y

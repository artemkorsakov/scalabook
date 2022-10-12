package typeclass.monad

trait Semigroup[A]:
  def combine(x: A, y: A): A

object Semigroup:
  given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y

  given productSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x * y

  given stringSemigroupInstance: Semigroup[String] = (x: String, y: String) => x + y

  given listSemigroupInstance[T]: Semigroup[List[T]] =
    (x: List[T], y: List[T]) => x ++ y

  given nestedSemigroupInstance[A, B](using aSemigroup: Semigroup[A], bSemigroup: Semigroup[B]): Semigroup[(A, B)] =
    (x: (A, B), y: (A, B)) => (aSemigroup.combine(x._1, y._1), bSemigroup.combine(x._2, y._2))

  def combine[A](x: A, y: A)(using s: Semigroup[A]): A = s.combine(x, y)

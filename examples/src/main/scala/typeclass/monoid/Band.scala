package typeclass.monoid

trait Band[A] extends Semigroup[A]

object Band:
  given setBandInstance[A]: Band[Set[A]] =
    (x: Set[A], y: Set[A]) => x ++ y

  def combine[A](x: A, y: A)(using b: Band[A]): A = b.combine(x, y)

package typeclass.monoid

trait Band[A] extends Semigroup[A]

object Band:
  def apply[A: Band]: Band[A] = summon[Band[A]]

  given setBandInstance[A]: Band[Set[A]] =
    (x: Set[A], y: Set[A]) => x ++ y

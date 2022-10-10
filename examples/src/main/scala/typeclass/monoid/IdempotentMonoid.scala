package typeclass.monoid

trait IdempotentMonoid[A] extends Monoid[A], Band[A]

object IdempotentMonoid:
  given setIdempotentMonoidInstance[A]: IdempotentMonoid[Set[A]] with
    override def empty: Set[A] = Set.empty[A]

    override def combine(x: Set[A], y: Set[A]): Set[A] = x ++ y

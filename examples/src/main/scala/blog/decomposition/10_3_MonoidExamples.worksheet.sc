trait Semigroup[A]:
  def compose(a1: A, a2: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

def foldSemigroup[A](list: List[A], empty: A)(using m: Semigroup[A]): A =
  list match
    case Nil      => empty
    case h :: Nil => h
    case h :: t   => m.compose(h, foldSemigroup(t, empty))

def foldMonoid[A](list: List[A])(using m: Monoid[A]): A =
  foldSemigroup(list, m.empty)
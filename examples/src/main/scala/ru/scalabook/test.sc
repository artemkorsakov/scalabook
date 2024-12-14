trait Semigroup[A]:
  def combine(x: A, y: A): A

object Semigroup:
  def apply[A: Semigroup]: Semigroup[A] = summon[Semigroup[A]]

  given Semigroup[Int] = (x: Int, y: Int) => x + y


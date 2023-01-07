package blog.architectureproblems

trait Monoid[A] extends Semigroup[A]:
  def e: A

object Monoid:
  def doTheMonoidLawsHold[A: Monoid](x: A, y: A, z: A): Boolean =
    val s = summon[Monoid[A]]
    import s.*
    Semigroup.doTheSemigroupLawsHold[A](x, y, z) &&
    ((e |+| x) == x) && ((x |+| e) == x)

package blog.architectureproblems

trait Semigroup[A]:
  extension (x: A)
    def |+|(y: A): A

object Semigroup:
  def doTheSemigroupLawsHold[A: Semigroup](x: A, y: A, z: A): Boolean =
    val s = summon[Semigroup[A]]
    import s.|+|
    ((x |+| y) |+| z) == (x |+| (y |+| z))

package typeclass.arrow

trait Strong[=>:[_, _]] extends Profunctor[=>:]:
  def first[A, B, C](fa: A =>: B): (A, C) =>: (B, C)

  def second[A, B, C](fa: A =>: B): (C, A) =>: (C, B) =
    dimap[(A, C), (B, C), (C, A), (C, B)](first(fa))(_.swap)(_.swap)

object Strong:
  def apply[=>:[_, _]: Strong]: Strong[=>:] = summon[Strong[=>:]]

  given Strong[Function1] with
    override def mapfst[A, B, C](fab: A => B)(f: C => A): C => B = f andThen fab

    override def mapsnd[A, B, C](fab: A => B)(f: B => C): A => C = fab andThen f

    override def first[A, B, C](fa: A => B): ((A, C)) => (B, C) = (a, c) => (fa(a), c)

    override def second[A, B, C](fa: A => B): ((C, A)) => (C, B) = (c, a) => (c, fa(a))

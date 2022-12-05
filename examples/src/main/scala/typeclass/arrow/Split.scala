package typeclass.arrow

trait Split[=>:[_, _]] extends Compose[=>:]:
  def split[A, B, C, D](f: A =>: B, g: C =>: D): (A, C) =>: (B, D)

object Split:
  def apply[=>:[_, _]: Strong]: Strong[=>:] = summon[Strong[=>:]]
  
  given Split[Function1] with
    override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f

    override def split[A, B, C, D](f: A => B, g: C => D): ((A, C)) => (B, D) =
      (a, c) => (f(a), g(c))

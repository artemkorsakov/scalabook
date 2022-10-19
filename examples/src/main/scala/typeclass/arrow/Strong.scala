package typeclass.arrow

trait Strong[=>:[_, _]] extends Profunctor[=>:]:
  def first[A, B, C](fa: A =>: B): (A, C) =>: (B, C)

  def second[A, B, C](fa: A =>: B): (C, A) =>: (C, B) =
    dimap[(A, C), (B, C), (C, A), (C, B)](first(fa))(_.swap)(_.swap)

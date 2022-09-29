package typeclass.common

final case class OptionT[A, M[_]](run: M[Option[A]])

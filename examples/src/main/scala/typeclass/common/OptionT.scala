package typeclass.common

final case class OptionT[M[_], A](run: M[Option[A]])

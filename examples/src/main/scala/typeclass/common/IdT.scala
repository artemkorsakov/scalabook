package typeclass.common

final case class IdT[A, F[_]](run: F[Id[A]])

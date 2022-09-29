package typeclass.common

final case class IdT[A, F[_]](runIdT: F[Id[A]])

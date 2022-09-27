package typeclass.common

final case class Nested[F[_], G[_], A](value: F[G[A]])

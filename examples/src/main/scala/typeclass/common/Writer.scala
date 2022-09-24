package typeclass.common

final case class Writer[W, A](run: () => (W, A))

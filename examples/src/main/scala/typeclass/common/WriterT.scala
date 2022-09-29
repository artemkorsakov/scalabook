package typeclass.common

final case class WriterT[M[_], W, A](run: () => M[(W, A)])

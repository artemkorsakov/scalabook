package typeclass.common

final case class StateT[M[_], S, A](run: S => M[(S, A)])

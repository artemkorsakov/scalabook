package typeclass.common

final case class State[S, +A](run: S => (S, A))

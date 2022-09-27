package typeclass.common

final case class IO[R](run: () => R):
  override def equals(that: Any): Boolean = that match
    case IO(thatRun) => this.run() == thatRun()
    case _           => false

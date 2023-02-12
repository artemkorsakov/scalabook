package typeclass.common

final case class Writer[W, A](run: () => (W, A)):
  override def equals(that: Any): Boolean = that match
    case Writer(thatRun) =>
      this.run() == thatRun()
    case _ =>
      false

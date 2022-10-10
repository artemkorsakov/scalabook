package typeclass.monoid

trait IdempotentMonoidLaw extends MonoidLaw, BandLaw:
  def checkIdempotentMonoidLaw[A](x: A, y: A, z: A)(using IdempotentMonoid[A]): Unit =
    checkMonoidLaw(x, y, z)
    checkBandLaw(x, y, z)

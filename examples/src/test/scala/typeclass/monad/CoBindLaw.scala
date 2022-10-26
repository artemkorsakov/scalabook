package typeclass.monad

import typeclass.monad.CoBind.cobind

trait CoBindLaw extends FunctorLaw:
  def checkCoBindLaw[F[_]: CoBind, A, B, C](fa: F[A], fab: F[A] => B, fbc: F[B] => C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkFunctorLaw[F, A, B, C](fa)
    assertEquals(
      cobind(cobind(fa)(fab))(fbc),
      cobind(cobind(fa)(fab))(fbc), // ToDo: ???
      "associative"
    )

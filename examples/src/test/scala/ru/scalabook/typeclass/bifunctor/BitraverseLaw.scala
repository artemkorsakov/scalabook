package ru.scalabook.typeclass.bifunctor

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monad.{Applicative, Traverse, TraverseLaw}
import ru.scalabook.typeclass.monoid.Monoid

trait BitraverseLaw extends TraverseLaw, BifoldableLaw, BifunctorLaw:
  def checkBitraverseLaw[F[_, _], G[_]: Applicative, H[
      _
  ]: Applicative, L, R, A, B, C](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using
      m: Monoid[Vector[A]]
  )(using
      bi: Bitraverse[F]
  ): ValidatedNel[String, Unit] =
    given t1: Traverse[[X] =>> F[X, R]] = bi.leftTraverse
    given t2: Traverse[[X] =>> F[L, X]] = bi.rightTraverse
    given t3: Traverse[[X] =>> F[X, X]] = bi.uTraverse

    checkBifoldableLaw[F, L, R, A](far, fla, faa) combine
      checkBifunctorLaw[F, L, R, A, B, C](far, fla, faa) combine
      checkTraverseLaw[[X] =>> F[X, R], G, H, A, B, C](far) combine
      checkTraverseLaw[[X] =>> F[L, X], G, H, A, B, C](fla) combine
      checkTraverseLaw[[X] =>> F[X, X], G, H, A, B, C](faa)
  end checkBitraverseLaw
end BitraverseLaw

package typeclass.bifunctor

import typeclass.common.Id
import typeclass.monad.Applicative.unit
import typeclass.monad.Functor.map
import typeclass.monad.Traverse.traverse
import typeclass.monad.{Applicative, FunctorLaw, Traverse, TraverseLaw}
import typeclass.monoid.Monoid

trait BitraverseLaw extends TraverseLaw, BifoldableLaw, BifunctorLaw:
  def checkBitraverseLaw[F[_, _], G[_]: Applicative, H[_]: Applicative, L, R, A, B, C](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using Monoid[Vector[A]])(using
      bi: Bitraverse[F]
  ): Unit =
    checkBifoldableLaw[F, L, R, A](far, fla, faa)
    checkBifunctorLaw[F, L, R, A, B, C](far, fla, faa)
    // checkTraverseLaw[F[_] : Traverse, G, H, A, B, C](fa)

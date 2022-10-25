package typeclass.monad

import typeclass.common.Runner1
import typeclass.common.Runner1.run
import typeclass.monad.ContravariantFunctor.cmap

trait ContravariantFunctorLaw extends InvariantFunctorLaw:
  def checkContravariantFunctorLaw[F[_]: ContravariantFunctor: Runner1, A, B, C](
      fa: F[A]
  )(using f: B => A, g: C => B): Unit =
    assertEquals(run(cmap[F, A, A](fa)(x => x)), run(fa), "identity")
    assertEquals(run(cmap(cmap(fa)(f))(g)), run(cmap(fa)(f compose g)), "composition")

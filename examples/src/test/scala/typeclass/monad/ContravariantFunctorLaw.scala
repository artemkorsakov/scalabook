package typeclass.monad

import typeclass.common.Runner1

trait ContravariantFunctorLaw extends InvariantFunctorLaw:
  def checkContravariantFunctorLaw[F[
      _
  ]: ContravariantFunctor: Runner1, A, B, C](
      fa: F[A]
  )(using fba: B => A, fcb: C => B, fab: A => B, fbc: B => C): Unit =
    checkInvariantFunctorLawWithRunner[F, A, B, C](fa: F[A])
    assertEquals(
      Runner1[F].run(ContravariantFunctor[F].cmap[A, A](fa)(x => x)),
      Runner1[F].run(fa),
      "identity"
    )
    assertEquals(
      Runner1[F].run(
        ContravariantFunctor[F].cmap(ContravariantFunctor[F].cmap(fa)(fba))(fcb)
      ),
      Runner1[F].run(ContravariantFunctor[F].cmap(fa)(fba compose fcb)),
      "composition"
    )

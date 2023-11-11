package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.Runner1

trait ContravariantFunctorLaw extends InvariantFunctorLaw:
  def checkContravariantFunctorLaw[F[
      _
  ]: ContravariantFunctor: Runner1, A, B, C](
      fa: F[A]
  )(using
      fba: B => A,
      fcb: C => B,
      fab: A => B,
      fbc: B => C
  ): ValidatedNel[String, Unit] =
    check(
      Runner1[F].run(
        ContravariantFunctor[F].xmap(fa)(identity, identity)
      ) == Runner1[F].run(fa),
      "identity: xmap(ma)(identity, identity) == ma"
    ) combine
      check(
        Runner1[F].run(
          ContravariantFunctor[F]
            .xmap(ContravariantFunctor[F].xmap(fa)(fab, fba))(fbc, fcb)
        ) ==
          Runner1[F].run(
            ContravariantFunctor[F].xmap(fa)(fbc compose fab, fba compose fcb)
          ),
        "composition: xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)"
      ) combine
      check(
        Runner1[F].run(ContravariantFunctor[F].cmap[A, A](fa)(x => x)) ==
          Runner1[F].run(fa),
        "identity"
      ) combine
      check(
        Runner1[F].run(
          ContravariantFunctor[F]
            .cmap(ContravariantFunctor[F].cmap(fa)(fba))(fcb)
        ) ==
          Runner1[F].run(ContravariantFunctor[F].cmap(fa)(fba compose fcb)),
        "composition"
      )

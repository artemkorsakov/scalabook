package ru.scalabook.typeclass.bifunctor

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.common.BasicLaws
import ru.scalabook.typeclass.monad.*
import ru.scalabook.typeclass.monoid.Monoid
import ru.scalabook.typeclass.monad.MonadLaws.checkFunctorLaw
import ru.scalabook.typeclass.monad.MonadLaws.checkFoldableLaw

object BifunctorLaws extends BasicLaws:
  def checkBitraverseLaw[
      F[_, _],
      G[_]: Applicative,
      H[_]: Applicative,
      L,
      R,
      A,
      B,
      C
  ](
      fab: F[A, B],
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using
      m: Monoid[Vector[A]]
  )(using
      bi: Bitraverse[F]
  ): ValidatedNel[String, Unit] =
    given t1: Traverse[[X] =>> F[X, R]] = bi.leftTraverse

    given t2: Traverse[[X] =>> F[L, X]] = bi.rightTraverse

    given t3: Traverse[[X] =>> F[X, X]] = bi.uTraverse

    checkBifoldableLaw[F, L, R, A](far, fla, faa) combine
      checkBifunctorLaw[F, L, R, A, B, C](fab, far, fla, faa)
  end checkBitraverseLaw

  def checkBifoldableLaw[F[_, _], L, R, A](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using
      bi: Bifoldable[F]
  )(using mv: Monoid[Vector[A]]): ValidatedNel[String, Unit] =
    checkFoldableLaw[[X] =>> F[X, R], A](far)(using
      bi.leftFoldable[R],
      mv
    ) combine
      checkFoldableLaw[[X] =>> F[L, X], A](fla)(using
        bi.rightFoldable[L],
        mv
      ) combine
      checkFoldableLaw[[X] =>> F[X, X], A](faa)(using bi.uFoldable, mv)

  def checkBifunctorLaw[F[_, _], L, R, A, B, C](
      fab: F[A, B],
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using
      bi: Bifunctor[F]
  ): ValidatedNel[String, Unit] =
    checkBimapIdentityLaw[F, A, B](fab) combine
      checkBimapCompositionLaw[F, A, B, B, C, A, B](fab) combine
      checkFunctorLaw[[X] =>> F[X, R], A, B, C](far)(using
        bi.leftFunctor[R],
        f,
        fReverse,
        g,
        gReverse
      ) combine
      checkFunctorLaw[[X] =>> F[L, X], A, B, C](fla)(using
        bi.rightFunctor[L],
        f,
        fReverse,
        g,
        gReverse
      ) combine
      checkFunctorLaw[[X] =>> F[X, X], A, B, C](faa)(using
        bi.uFunctor,
        f,
        fReverse,
        g,
        gReverse
      )

  private def checkBimapIdentityLaw[F[_, _], A, B](fab: F[A, B])(using
      bi: Bifunctor[F]
  ): ValidatedNel[String, Unit] =
    import bi.*
    Either.cond[String, Unit](
      bimap(fab)(identity, identity) == fab,
      (),
      "Не соблюдается тождественность: bimap(fab)(identity, identity) == fab"
    ).toValidatedNel

  private def checkBimapCompositionLaw[F[_, _], A, B, C, D, E, G](fab: F[A, B])(
      using
      bi: Bifunctor[F],
      f1: A => C,
      g1: B => D,
      f2: C => E,
      g2: D => G
  ): ValidatedNel[String, Unit] =
    import bi.*
    Either.cond[String, Unit](
      bimap(bimap(fab)(f1, g1))(f2, g2) ==
        bimap(fab)(f2.compose(f1), g2.compose(g1)),
      (),
      "Не соблюдается композиция: bimap(bimap(fab)(f1, g1))(f2, g2) == bimap(fab)(f2.compose(f1), g2.compose(g1))"
    ).toValidatedNel
end BifunctorLaws

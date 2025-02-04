package ru.scalabook.typeclass.common

import cats.Id
import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.bifunctor.*
import ru.scalabook.typeclass.monad.*
import ru.scalabook.typeclass.monoid.Monoid

trait OldLaws:
  protected def check(law: Boolean): ValidatedNel[String, Unit] =
    check(law, "")

  protected def check(
      law: Boolean,
      message: => String
  ): ValidatedNel[String, Unit] =
    Either
      .cond[String, Unit](law, (), message)
      .toValidatedNel

  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    check(
      InvariantFunctor[F].xmap(fa)(identity, identity) == fa,
      "identity: xmap(ma)(identity, identity) == ma"
    ) combine
      check(
        InvariantFunctor[F]
          .xmap(InvariantFunctor[F].xmap(fa)(f, fReverse))(g, gReverse) ==
          InvariantFunctor[F].xmap(fa)(g compose f, fReverse compose gReverse),
        "composition: xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)"
      )

  def checkFunctorLaw[F[_]: Functor, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkInvariantFunctorLaw[F, A, B, C](fa) combine
      check(
        Functor[F].map(fa)(identity) == fa,
        "identity: map(fa)(identity) == fa"
      ) combine
      check(
        Functor[F].map(Functor[F].map(fa)(f))(g) ==
          Functor[F].map(fa)(f.andThen(g)),
        "composition: fa.map(f).map(g) == fa.map(g(f(_)))"
      )

  def checkFoldableLaw[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    check(fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _)) combine
      check(fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _))

  def checkTraverseLaw[F[_]: Traverse, G[_]: Applicative, H[
      _
  ]: Applicative, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkFunctorLaw[F, A, B, C](fa) combine {
      val a2GB: A => G[B] = a => Applicative[G].unit(f(a))
      val a2HB: A => H[B] = a => Applicative[H].unit(f(a))
      val b2HC: B => H[C] = b => Applicative[H].unit(g(b))

      val optFb: G[F[B]] = Traverse[F].traverse(fa)(a2GB)
      val optListFc1: G[H[F[C]]] =
        Functor[G].map(optFb)(fb => Traverse[F].traverse(fb)(b2HC))
      val optListFc2: G[H[F[C]]] =
        Traverse[F].traverse[A](fa)[[X] =>> G[H[X]], C](a =>
          Functor[G].map(Applicative[G].unit(f(a)))(b2HC)
        )

      type GH[D] = (G[D], H[D])
      val t1: GH[F[B]] =
        (Traverse[F].traverse(fa)(a2GB), Traverse[F].traverse(fa)(a2HB))
      val t2: GH[F[B]] =
        Traverse[F].traverse[A](fa)[GH, B](a => (a2GB(a), a2HB(a)))

      check(
        Traverse[F].traverse(fa)(a => Id(f(a))) == fa.map(f),
        "Обход Id эквивалентен `Functor#map`"
      ) combine
        check(
          optListFc1 == optListFc2,
          "Два последовательно зависимых эффекта могут быть объединены в один, их композицию"
        ) combine
        check(
          Traverse[F].traverse[A](fa)[G, A](a => Applicative[G].unit(a)) ==
            Applicative[G].unit[F[A]](fa),
          "Обход с помощью функции unit аналогичен прямому применению функции unit"
        ) combine
        check(
          t1 == t2,
          "Два независимых эффекта могут быть объединены в один эффект, их произведение"
        )
    }

  def checkBifunctorLaw[F[_, _], L, R, A, B, C](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using
      bi: Bifunctor[F]
  ): ValidatedNel[String, Unit] =
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

  def checkBifoldableLaw[F[_, _], L, R, A](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using bi: Bifoldable[F])(using
      mv: Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    checkFoldableLaw[[X] =>> F[X, R], A](far)(using
      bi.leftFoldable[R],
      mv
    ) combine
      checkFoldableLaw[[X] =>> F[L, X], A](fla)(using
        bi.rightFoldable[L],
        mv
      ) combine
      checkFoldableLaw[[X] =>> F[X, X], A](faa)(using bi.uFoldable, mv)

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
end OldLaws

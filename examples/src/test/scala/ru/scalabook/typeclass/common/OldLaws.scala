package ru.scalabook.typeclass.common

import cats.Id
import cats.data.ValidatedNel
import cats.syntax.either.*
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

  protected def delta[A]: A => (A, A) = a => (a, a)

  def checkDivideLaw[F[_]: Divide: Runner1, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A]
  )(using
      fba: B => A,
      fcb: C => B,
      fab: A => B,
      fbc: B => C
  ): ValidatedNel[String, Unit] =
    checkContravariantFunctorLaw[F, A, B, C](fa1) combine
      check(
        Divide[F].divide(Divide[F].divide(fa1, fa2)(delta), fa3)(delta) ==
          Divide[F].divide(fa1, Divide[F].divide(fa2, fa3)(delta))(delta),
        "composition"
      )

  def checkDivisibleLaw[F[_]: Divisible: Runner1, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A]
  )(using
      fba: B => A,
      fcb: C => B,
      fab: A => B,
      fbc: B => C
  ): ValidatedNel[String, Unit] =
    checkDivideLaw[F, A, B, C](fa1, fa2, fa3) combine
      check(
        Divisible[F].divide(fa1, Divisible[F].conquer)(delta) ==
          fa1,
        "rightIdentity"
      ) combine
      check(
        Divisible[F].divide(Divisible[F].conquer, fa1)(delta) ==
          fa1,
        "leftIdentity"
      )

  def checkCoBindLaw[F[_]: CoBind, A, B, C](
      fa: F[A],
      fab: F[A] => B,
      fbc: F[B] => C
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkFunctorLaw[F, A, B, C](fa) combine
      check(
        CoBind[F].cobind(CoBind[F].cobind(fa)(fab))(fbc) ==
          CoBind[F].cobind(CoBind[F].cobind(fa)(fab))(fbc), // ToDo: ???
        "associative"
      )

  def checkCoMonadLaw[F[_]: CoMonad, A, B, C](
      fa: F[A],
      fab: F[A] => B,
      fbc: F[B] => C
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkCoBindLaw(fa, fab, fbc) combine
      check(
        CoMonad[F].coFlatMap(fa)(CoMonad[F].coUnit) ==
          fa,
        "left identity"
      ) combine
      check(
        CoMonad[F].coUnit(CoMonad[F].coFlatMap(fa)(fab)) ==
          fab(fa),
        "right identity"
      )
end OldLaws

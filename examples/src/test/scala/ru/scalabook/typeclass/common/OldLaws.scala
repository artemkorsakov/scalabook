package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monad.*
import ru.scalabook.typeclass.monoid.*

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

  protected def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    val s = summon[Semigroup[A]]
    check(
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      "associative: (x + y) + z == x + (y + z)"
    )

  def checkMonoidLaw[A: Monoid](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(
        Monoid[A].combine(x, Monoid[A].empty) == x,
        "right identity: x + e = x"
      ) combine
      check(
        Monoid[A].combine(Monoid[A].empty, x) == x,
        "left identity: e + x = x"
      )

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

  def checkSemigroupalLaw[F[_]: Semigroupal: Runner1, A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C]
  ): ValidatedNel[String, Unit] =
    val product1: (A, (B, C)) =
      Runner1[F].run(
        Semigroupal[F]
          .product[A, (B, C)](fa, Semigroupal[F].product[B, C](fb, fc))
      )
    val product2: ((A, B), C) =
      Runner1[F].run(
        Semigroupal[F]
          .product[(A, B), C](Semigroupal[F].product[A, B](fa, fb), fc)
      )
    val value1: (A, B, C) = (product1._1, product1._2._1, product1._2._2)
    val value2: (A, B, C) = (product2._1._1, product2._1._2, product2._2)
    check(value1 == value2, "associativity")
end OldLaws

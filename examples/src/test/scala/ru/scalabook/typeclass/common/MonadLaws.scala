package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monad.*
import cats.syntax.either.*

object MonadLaws:
  def checkSemigroupalLaw[F[_]: Runner1, A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C]
  )(using sg: Semigroupal[F], r: Runner1[F]): ValidatedNel[String, Unit] =
    val product1: (A, (B, C)) =
      r.run(sg.product[A, (B, C)](fa, sg.product[B, C](fb, fc)))
    val product2: ((A, B), C) =
      r.run(sg.product[(A, B), C](sg.product[A, B](fa, fb), fc))
    val value1: (A, B, C) = (product1._1, product1._2._1, product1._2._2)
    val value2: (A, B, C) = (product2._1._1, product2._1._2, product2._2)
    Either.cond[String, Unit](
      value1 == value2,
      (),
      "Не соблюдается ассоциативность: product(a, product(b, c)) == product(product(a, b), c)"
    ).toValidatedNel
  end checkSemigroupalLaw

  def checkFunctorLaw[F[_]: Functor, A, B, C](fa: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkInvariantFunctorLaw[F, A, B, C](fa) combine
      checkMapIdentityLaw[F, A](fa) combine
      checkMapCompositionLaw[F, A, B, C](fa)

  def checkInvariantApplicativeLaw[F[_]: InvariantApplicative, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkInvariantFunctorLaw[F, A, B, C](fa)

  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](fa: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkXMapIdentityLaw[F, A](fa) combine
      checkXMapCompositionLaw[F, A, B, C](fa)

  private def checkMapIdentityLaw[F[_], A](fa: F[A])(using
      fu: Functor[F]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fu.map(fa)(identity) == fa,
      (),
      "Не соблюдается тождественность: map(fa)(identity) == fa"
    ).toValidatedNel

  private def checkMapCompositionLaw[F[_], A, B, C](fa: F[A])(using
      fu: Functor[F],
      f: A => B,
      g: B => C
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fu.map(fu.map(fa)(f))(g) == fu.map(fa)(f.andThen(g)),
      (),
      "Не соблюдается композиция: fa.map(f).map(g) == fa.map(g(f(_)))"
    ).toValidatedNel

  private def checkXMapIdentityLaw[F[_], A](fa: F[A])(using
      inf: InvariantFunctor[F]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      inf.xmap(fa)(identity, identity) == fa,
      (),
      "Не соблюдается тождественность: xmap(ma)(identity, identity) == ma"
    ).toValidatedNel

  private def checkXMapCompositionLaw[F[_], A, B, C](fa: F[A])(using
      inf: InvariantFunctor[F],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      inf.xmap(inf.xmap(fa)(f, fReverse))(g, gReverse) ==
        inf.xmap(fa)(g compose f, fReverse compose gReverse),
      (),
      "Не соблюдается композиция: xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)"
    ).toValidatedNel
end MonadLaws

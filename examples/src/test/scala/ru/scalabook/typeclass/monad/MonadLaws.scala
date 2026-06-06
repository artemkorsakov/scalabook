package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.common.{BasicLaws, Runner1}
import ru.scalabook.typeclass.monoid.Monoid

object MonadLaws extends BasicLaws:
  def checkBimonadLaw[F[_]: Bimonad: Runner1, A, B, C](
      a: A,
      b: B,
      c: C,
      fa: F[A],
      fab: F[A] => B,
      fbc: F[B] => C
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkCoMonadLaw[F, A, B, C](fa, fab, fbc) combine
      checkMonadLaw[F, A, B, C](a, b, c)

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
      checkCoMonadLeftIdentity[F, A](fa) combine
      checkCoMonadRightIdentity[F, A, B](fa, fab)

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
      checkCobindAssociativity[F, A, B, C](fa, fab, fbc)

  def checkFoldableLaw[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    checkFoldLeftMatchesFoldMap(fa) combine
      checkFoldRightMatchesFoldMap(fa)

  def checkMonadLaw[F[_]: Runner1, A, B, C](a: A, b: B, c: C)(using
      monad: Monad[F],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    import monad.*
    val fa: F[A]       = unit(a)
    val fab: F[A => B] = unit(f)
    val afb: A => F[B] = a => unit(f(a))
    val bfc: B => F[C] = b => unit(g(b))
    checkApplicativeLaw[F, A, B, C](a, b, c) combine
      checkApplyFlatmap[F, A, B](fa, fab) combine
      checkFlatmapAssociativity[F, A, B, C](fa, afb, bfc) combine
      checkFlatmapLeftIdentity[F, A, B](a, afb) combine
      checkFlatmapRightIdentity[F, A](fa)

  def checkApplicativeLaw[F[_]: Runner1, A, B, C](a: A, b: B, c: C)(using
      appl: Applicative[F],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    import appl.*
    val fa: F[A]       = unit(a)
    val fb: F[B]       = unit(b)
    val fc: F[C]       = unit(c)
    val fab: F[A => B] = unit(f)
    val fbc: F[B => C] = unit(g)
    checkApplyLaw[F, A, B, C](fa, fb, fc, fab, fbc) combine
      checkApplyIdentity[F, A](fa) combine
      checkUnitMap[F, A, B](a, f) combine
      checkApplyUnit[F, A, B](fa, f) combine
      checkHomomorphism[F, A, B](a, f) combine
      checkInterchange[F, A, B](a, unit(f)) combine
      checkLeftIdentity[F, A](fa) combine
      checkRightIdentity[F, A](fa) combine
      checkAssociativity[F, A, B, C](fa, fb, fc) combine
      checkNaturality[F, A, B, C](fa, fb, f, g) combine
      checkApplicativeComposition[F, A, B, C](fa, fab, fbc)
  end checkApplicativeLaw

  def checkApplyLaw[F[_]: Runner1, A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C],
      fab: F[A => B],
      fbc: F[B => C]
  )(using
      ap: Apply[F],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkFunctorLaw[F, A, B, C](fa) combine
      checkSemigroupalLaw[F, A, B, C](fa, fb, fc) combine
      checkApplyComposition[F, A, B, C](fa, fab, fbc)
  end checkApplyLaw

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

  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](fa: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkXMapIdentityLaw[F, A](fa) combine
      checkXMapCompositionLaw[F, A, B, C](fa)

  private def checkCoMonadLeftIdentity[F[_], A](fa: F[A])(using
      com: CoMonad[F]
  ): ValidatedNel[String, Unit] =
    import com.*

    Either.cond[String, Unit](
      coFlatMap(fa)(coUnit) == fa,
      (),
      "Не соблюдается тождественность слева CoMonad: coFlatMap(fa)(coUnit) == fa"
    ).toValidatedNel

  private def checkCoMonadRightIdentity[F[_], A, B](
      fa: F[A],
      fab: F[A] => B
  )(using com: CoMonad[F]): ValidatedNel[String, Unit] =
    import com.*

    Either.cond[String, Unit](
      coUnit(coFlatMap(fa)(fab)) == fab(fa),
      (),
      "Не соблюдается тождественность справа CoMonad: coUnit(coFlatMap(fa)(fab)) == fab(fa)"
    ).toValidatedNel

  private def checkCobindAssociativity[F[_], A, B, C](
      fa: F[A],
      fab: F[A] => B,
      fbc: F[B] => C
  )(using cob: CoBind[F]): ValidatedNel[String, Unit] =
    import cob.*

    Either.cond[String, Unit](
      fa.cobind(fab).cobind(fbc) == fa.cobind(x => fbc(x.cobind(fab))),
      (),
      "Не соблюдается ассоциативность CoBind: fa.cobind(fab).cobind(fbc) == fa.cobind(x => fbc(x.cobind(fab)))"
    ).toValidatedNel

  private def checkFoldLeftMatchesFoldMap[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _),
      (),
      "`foldLeft` не соответствует `foldMap`, не соблюдается: `fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _)`"
    ).toValidatedNel

  private def checkFoldRightMatchesFoldMap[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _),
      (),
      "`foldRight` соответствует `foldMap`, не соблюдается: `fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _)`"
    ).toValidatedNel

  private def checkFlatmapLeftIdentity[F[_], A, B](a: A, afb: A => F[B])(using
      monad: Monad[F]
  ): ValidatedNel[String, Unit] =
    import monad.*
    Either.cond[String, Unit](
      unit(a).flatMap(afb) == afb(a),
      (),
      "Не соблюдается тождественность слева: unit(a).flatMap(afb) == afb(a)"
    ).toValidatedNel

  private def checkFlatmapRightIdentity[F[_], A](fa: F[A])(using
      monad: Monad[F]
  ): ValidatedNel[String, Unit] =
    import monad.*
    Either.cond[String, Unit](
      fa.flatMap(a => unit(a)) == fa,
      (),
      "Не соблюдается тождественность справа: fa.flatMap(a => unit(a)) == fa"
    ).toValidatedNel

  private def checkApplyFlatmap[F[_], A, B](fa: F[A], fab: F[A => B])(using
      bind: Bind[F]
  ): ValidatedNel[String, Unit] =
    import bind.*
    Either.cond[String, Unit](
      apply(fab)(fa) == fab.flatMap(a2b => fa.map(a2b)),
      (),
      "Не соблюдается соответствие apply и flatMap: apply(fab)(fa) == fab.flatMap(a2b => fa.map(a2b))"
    ).toValidatedNel

  private def checkFlatmapAssociativity[F[_], A, B, C](
      fa: F[A],
      f: A => F[B],
      g: B => F[C]
  )(using bind: Bind[F]): ValidatedNel[String, Unit] =
    import bind.*
    Either.cond[String, Unit](
      fa.flatMap(f).flatMap(g) == fa.flatMap { a => f(a).flatMap(g) },
      (),
      "Не соблюдается ассоциативность на flatMap: fa.flatMap(f).flatMap(g) == fa.flatMap { a => f(a).flatMap(g) }"
    ).toValidatedNel

  private def checkApplyIdentity[F[_], A](fa: F[A])(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      apply[A, A](unit(identity))(fa) == fa,
      (),
      "Не соблюдается тождественность: apply(unit(identity))(fa) == fa"
    ).toValidatedNel

  private def checkUnitMap[F[_], A, B](a: A, f: A => B)(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      unit(a).map(f) == unit(f(a)),
      (),
      "Не соблюдается закон: unit(x).map(f) == unit(f(x))"
    ).toValidatedNel

  private def checkApplyUnit[F[_], A, B](fa: F[A], f: A => B)(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      fa.map(f) == apply(unit(f))(fa),
      (),
      "Не соблюдается закон: fa.map(f) == apply(unit(f))(fa)"
    ).toValidatedNel

  private def checkHomomorphism[F[_], A, B](a: A, f: A => B)(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      apply(unit(f))(unit(a)) == unit(f(a)),
      (),
      "Не соблюдается гомоморфизм: apply(unit(f))(unit(x)) == unit(f(x))"
    ).toValidatedNel

  private def checkInterchange[F[_], A, B](a: A, fab: F[A => B])(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      apply(fab)(unit(a)) == apply(unit((f: A => B) => f(a)))(fab),
      (),
      "Не соблюдается обмен: apply(fab)(unit(a)) == apply(unit((f: A => B) => f(a)))(fab)"
    ).toValidatedNel

  private def checkLeftIdentity[F[_], A](fa: F[A])(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      unit(()).map2(fa)((_, a) => a) == fa,
      (),
      "Не соблюдается тождественность слева: unit(()).map2(fa)((_, a) => a) == fa"
    ).toValidatedNel

  private def checkRightIdentity[F[_], A](fa: F[A])(using
      ap: Applicative[F]
  ): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      fa.map2(unit(()))((a, _) => a) == fa,
      (),
      "Не соблюдается тождественность справа: fa.map2(unit(()))((a, _) => a) == fa"
    ).toValidatedNel

  private def checkAssociativity[F[_], A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C]
  )(using ap: Applicative[F]): ValidatedNel[String, Unit] =
    import ap.*
    def assoc(p: (A, (B, C))): ((A, B), C) = p match
      case (a, (b, c)) => ((a, b), c)
    Either.cond[String, Unit](
      product(product(fa, fb), fc) == product(fa, product(fb, fc)).map(assoc),
      (),
      "Не соблюдается ассоциативность: product(product(fa, fb), fc) == product(fa, product(fb, fc)).map(assoc)"
    ).toValidatedNel

  private def checkNaturality[F[_], A, B, C](
      fa: F[A],
      fb: F[B],
      f: A => B,
      g: B => C
  )(using ap: Applicative[F]): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      fa.map2(fb)((a, b) => (f(a), g(b))) == product(fa.map(f), fb.map(g)),
      (),
      "Не соблюдается естественность: fa.map2(fb)((a, b) => (f(a), g(b))) == fa.map(f).product(fb.map(g))"
    ).toValidatedNel

  private def checkApplicativeComposition[F[_], A, B, C](
      fa: F[A],
      fab: F[A => B],
      fbc: F[B => C]
  )(using ap: Applicative[F]): ValidatedNel[String, Unit] =
    import ap.*
    Either.cond[String, Unit](
      apply(fbc)(apply(fab)(fa)) ==
        apply(apply(
          apply(unit((g: B => C) => (f: A => B) => g compose f))(fbc)
        )(fab))(fa),
      (),
      "Не соблюдается композиция: apply(fbc)(apply(fab)(fa)) == apply(apply(apply(unit((g: B => C) => (f: A => B) => g compose f))(fbc))(fab))(fa)"
    ).toValidatedNel

  private def checkApplyComposition[F[_], A, B, C](
      fa: F[A],
      fab: F[A => B],
      fbc: F[B => C]
  )(using ap: Apply[F]): ValidatedNel[String, Unit] =
    import ap.apply
    Either.cond[String, Unit](
      apply(fbc)(apply(fab)(fa)) ==
        apply(
          apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab)
        )(fa),
      (),
      "Не соблюдается композиция: apply(fbc)(apply(fab)(fa)) == apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)"
    ).toValidatedNel

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

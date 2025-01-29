package ru.scalabook.typeclass.common

import cats.Id
import cats.data.ValidatedNel
import ru.scalabook.typeclass.monad.*
import cats.syntax.either.*
import ru.scalabook.typeclass.common.MonoidLaws.*
import ru.scalabook.typeclass.monoid.Monoid

object MonadLaws extends BasicLaws:
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

  def checkDivisibleLaw[F[_]: Divisible: Applicator, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A],
      fc: F[C],
      a: A
  )(using f: A => B, g: B => C): ValidatedNel[String, Unit] =
    checkDivideLaw[F, A, B, C](fa1, fa2, fa3, fc, a) combine
      checkDivisibleRightIdentity[F, A](fa1, a) combine
      checkDivisibleLeftIdentity[F, A](fa1, a)

  def checkDivideLaw[F[_]: Divide: Applicator, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A],
      fc: F[C],
      a: A
  )(using f: A => B, g: B => C): ValidatedNel[String, Unit] =
    checkContravariantFunctorLaw[F, A, B, C](fa1, fc, a) combine
      checkDivideComposition[F, A](fa1, fa2, fa3, a)

  def checkContravariantFunctorLaw[
      F[_]: ContravariantFunctor: Applicator,
      A,
      B,
      C
  ](
      fa: F[A],
      fc: F[C],
      a: A
  )(using f: A => B, g: B => C): ValidatedNel[String, Unit] =
    checkContravariantFunctorIdentity[F, A](fa, a) combine
      checkContravariantFunctorComposition[F, A, B, C](fc, a)

  def checkTraverseLaw[F[_], G[_], H[_], A, B, C](fa: F[A], fga: F[G[A]])(using
      trF: Traverse[F],
      apG: Applicative[G],
      apH: Applicative[H],
      m: Monoid[Vector[A]],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkFunctorLaw[F, A, B, C](fa) combine
      checkFoldableLaw[F, A](fa) combine
      checkTraverseId[F, A, B](fa) combine
      checkTraverseConsistent[F, G, H, A, B, C](fa) combine
      checkTraverseUnit[F, G, A](fa) combine
      checkTraverseCombine[F, G, H, A, B](fa) combine
      checkTraverseSequence[F, G, A](fga)

  def checkFoldableLaw[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    checkFoldLeftMatchesFoldMap(fa) combine
      checkFoldRightMatchesFoldMap(fa)

  def checkIsEmptyLawLaw[F[_]: IsEmpty, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  ): ValidatedNel[String, Unit] =
    checkPlusEmptyLaw(f1, f2, f3) combine
      checkIsEmptyEmpty combine
      checkIsEmptyPair(f1, f2)

  def checkMonadPlusLaw[F[_]: Runner1, A, B, C](
      a: A,
      b: B,
      c: C,
      f1: F[A],
      f2: F[A],
      f3: F[A]
  )(using
      mp: MonadPlus[F],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkMonadLaw[F, A, B, C](a, b, c) combine
      checkApplicativePlusLaw[F, A, B, C](a, b, c, f1, f2, f3) combine
      checkEmptyIsAPolymorphicValue[F, A](f1) combine
      checkShortCircuitsRight[F, A](f1) combine
      checkShortCircuitsThroughout[F, A](f1)
  end checkMonadPlusLaw

  def checkApplicativePlusLaw[F[_]: ApplicativePlus: Runner1, A, B, C](
      a: A,
      b: B,
      c: C,
      f1: F[A],
      f2: F[A],
      f3: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkApplicativeLaw[F, A, B, C](a, b, c) combine
      checkPlusEmptyLaw[F, A](f1, f2, f3)

  def checkPlusEmptyLaw[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using
      pl: PlusEmpty[F]
  ): ValidatedNel[String, Unit] =
    checkPlusLaw(f1, f2, f3) combine
      checkPlusEmptyIdentity(f1) combine
      checkMonoidLaw(f1, f2, f3)(using pl.monoid)

  def checkPlusLaw[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using
      pl: Plus[F]
  ): ValidatedNel[String, Unit] =
    checkPlusAssociativity(f1, f2, f3) combine
      super.checkAssociativity(f1, f2, f3)(using pl.semigroup)

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

  def checkBindLaw[F[_]: Bind: Runner1, A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C],
      fab: F[A => B],
      fbc: F[B => C],
      afb: A => F[B],
      bfc: B => F[C]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkApplyLaw[F, A, B, C](fa, fb, fc, fab, fbc) combine
      checkApplyFlatmap[F, A, B](fa, fab) combine
      checkFlatmapAssociativity[F, A, B, C](fa, afb, bfc)

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
      cobind(cobind(fa)(fab))(fbc) == cobind(cobind(fa)(fab))(fbc), // ToDo: ???
      (),
      "Не соблюдается ассоциативность CoBind: divide(fa, conquer)(delta) == fa, где delta: A => (A, A) = a => (a, a)"
    ).toValidatedNel

  private def checkDivisibleRightIdentity[F[_], A](fa: F[A], a: A)(using
      div: Divisible[F],
      app: Applicator[F]
  ): ValidatedNel[String, Unit] =
    import div.*
    import app.*
    def delta: A => (A, A) = a => (a, a)

    Either.cond[String, Unit](
      run(a)(divide(fa, conquer)(delta)) == run(a)(fa),
      (),
      "Не соблюдается Тождественность справа: divide(fa, conquer)(delta) == fa, где delta: A => (A, A) = a => (a, a)"
    ).toValidatedNel

  private def checkDivisibleLeftIdentity[F[_], A](fa: F[A], a: A)(using
      div: Divisible[F],
      app: Applicator[F]
  ): ValidatedNel[String, Unit] =
    import div.*
    import app.*
    def delta: A => (A, A) = a => (a, a)

    Either.cond[String, Unit](
      run(a)(divide(conquer, fa)(delta)) == run(a)(fa),
      (),
      "Не соблюдается Тождественность слева: divide(conquer, fa)(delta) == fa, где delta: A => (A, A) = a => (a, a)"
    ).toValidatedNel

  private def checkDivideComposition[F[_], A](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A],
      a: A
  )(using div: Divide[F], app: Applicator[F]): ValidatedNel[String, Unit] =
    import div.*
    import app.*
    def delta: A => (A, A) = a => (a, a)

    Either.cond[String, Unit](
      run(a)(divide(divide(fa1, fa2)(delta), fa3)(delta)) ==
        run(a)(divide(fa1, divide(fa2, fa3)(delta))(delta)),
      (),
      "Не соблюдается Композиция Divide: divide(divide(fa1, fa2)(delta), fa3)(delta) == divide(fa1, divide(fa2, fa3)(delta))(delta), где delta: A => (A, A) = a => (a, a)"
    ).toValidatedNel
  end checkDivideComposition

  private def checkContravariantFunctorIdentity[F[_], A](fa: F[A], a: A)(using
      cf: ContravariantFunctor[F],
      ap: Applicator[F]
  ): ValidatedNel[String, Unit] =
    import cf.*
    import ap.*
    Either.cond[String, Unit](
      run(a)(cmap[A, A](fa)(identity)) == run(a)(fa),
      (),
      "Не соблюдается Тождественность контравариантного функтора: cmap(fa)(id) == fa"
    ).toValidatedNel

  private def checkContravariantFunctorComposition[F[_], A, B, C](
      fc: F[C],
      a: A
  )(
      using
      cf: ContravariantFunctor[F],
      ap: Applicator[F],
      f: A => B,
      g: B => C
  ): ValidatedNel[String, Unit] =
    import cf.*
    import ap.*
    Either.cond[String, Unit](
      run(a)(cmap(cmap(fc)(g))(f)) == run(a)(cmap(fc)(g compose f)),
      (),
      "Не соблюдается Композиция контравариантного функтора: cmap(cmap(fc)(g))(f) == cmap(fc)(g(f(_)))"
    ).toValidatedNel

  private def checkTraverseId[F[_], A, B](fa: F[A])(using
      trF: Traverse[F],
      f: A => B
  ): ValidatedNel[String, Unit] =
    check(
      trF.traverse(fa)(a => Id(f(a))) == fa.map(f),
      "Обход Id эквивалентен `Functor#map`"
    )

  private def checkTraverseConsistent[F[_], G[_], H[_], A, B, C](fa: F[A])(using
      trF: Traverse[F],
      apG: Applicative[G],
      apH: Applicative[H],
      f: A => B,
      g: B => C
  ): ValidatedNel[String, Unit] =
    val a2GB: A => G[B] = a => apG.unit(f(a))
    val b2HC: B => H[C] = b => apH.unit(g(b))

    val optFb: G[F[B]] = trF.traverse(fa)(a2GB)
    val optListFc1: G[H[F[C]]] =
      apG.map(optFb)(fb => trF.traverse(fb)(b2HC))
    val optListFc2: G[H[F[C]]] =
      trF.traverse[A](fa)[[X] =>> G[H[X]], C](a =>
        apG.map(apG.unit(f(a)))(b2HC)
      )
    check(
      optListFc1 == optListFc2,
      "Два последовательно зависимых эффекта могут быть объединены в один, их композицию"
    )
  end checkTraverseConsistent

  private def checkTraverseSequence[F[_], G[_], A](fga: F[G[A]])(using
      trF: Traverse[F],
      apG: Applicative[G]
  ): ValidatedNel[String, Unit] =
    check(
      trF.traverse[G[A]](fga)(identity) == trF.sequence(fga),
      "Не соблюдается закон согласованности с sequence: sequence(fga) == traverse(fga)(identity)"
    )

  private def checkTraverseUnit[F[_], G[_], A](fa: F[A])(using
      trF: Traverse[F],
      apG: Applicative[G]
  ): ValidatedNel[String, Unit] =
    check(
      trF.traverse[A](fa)[G, A](a => apG.unit(a)) == apG.unit[F[A]](fa),
      "Обход с помощью функции unit аналогичен прямому применению функции unit"
    )

  private def checkTraverseCombine[F[_], G[_], H[_], A, B](fa: F[A])(using
      trF: Traverse[F],
      apG: Applicative[G],
      apH: Applicative[H],
      f: A => B
  ): ValidatedNel[String, Unit] =
    val a2GB: A => G[B] = a => apG.unit(f(a))
    val a2HB: A => H[B] = a => apH.unit(f(a))
    type GH[D] = (G[D], H[D])
    val t1: GH[F[B]] = (trF.traverse(fa)(a2GB), trF.traverse(fa)(a2HB))
    val t2: GH[F[B]] = trF.traverse[A](fa)[GH, B](a => (a2GB(a), a2HB(a)))
    check(
      t1 == t2,
      "Два независимых эффекта могут быть объединены в один эффект, их произведение"
    )

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

  private def checkIsEmptyEmpty[F[_], A](using
      is: IsEmpty[F]
  ): ValidatedNel[String, Unit] =
    import is.*
    Either.cond[String, Unit](
      isEmpty(empty[A]),
      (),
      "\"Пустое\" значение всегда должно быть распознано как \"пустое\": isEmpty(empty[A])"
    ).toValidatedNel

  private def checkIsEmptyPair[F[_], A](fa1: F[A], fa2: F[A])(using
      is: IsEmpty[F]
  ): ValidatedNel[String, Unit] =
    import is.*
    Either.cond[String, Unit](
      (isEmpty(fa1) && isEmpty(fa2)) == isEmpty(plus(fa1, fa2)),
      (),
      "Результат комбинирования двух значений является \"пустым\" только тогда, когда оба значения \"пусты\": " +
        "(isEmpty(fa1) && isEmpty(fa2)) == isEmpty(plus(fa1, fa2))"
    ).toValidatedNel

  private def checkEmptyIsAPolymorphicValue[F[_], A](fa: F[A])(using
      mp: MonadPlus[F]
  ): ValidatedNel[String, Unit] =
    import mp.*
    Either.cond[String, Unit](
      empty[A].map(_ => fa) == empty[A],
      (),
      "Не соблюдается Закон сохранения нейтрального элемента при `map`: empty[A].map(_ => fa) == empty[A]"
    ).toValidatedNel

  private def checkShortCircuitsRight[F[_], A](fa: F[A])(using
      mp: MonadPlus[F]
  ): ValidatedNel[String, Unit] =
    import mp.*
    Either.cond[String, Unit](
      empty[A].flatMap(_ => fa) == empty[A],
      (),
      "Не соблюдается Закон сохранения нейтрального элемента при `flatMap`: empty[A].flatMap(_ => fa) == empty[A]"
    ).toValidatedNel

  private def checkShortCircuitsThroughout[F[_], A](fa: F[A])(using
      mp: MonadPlus[F]
  ): ValidatedNel[String, Unit] =
    import mp.*
    Either.cond[String, Unit](
      fa.flatMap(_ => empty[A]) == empty[A],
      (),
      "Не соблюдается Закон замыкания нейтрального элемента: fa.flatMap(_ => empty[A]) == empty[A]"
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

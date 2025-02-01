package ru.scalabook.typeclass.common

import cats.syntax.either.*
import cats.data.ValidatedNel
import ru.scalabook.typeclass.arrow.*

object ArrowLaws extends BasicLaws:
  def checkArrowLaw[=>:[_, _]: Arrow: Runner2, A, B, C, D, E, F](
      ad: A =>: D,
      ab: A =>: B,
      aa: A =>: A,
      bc: B =>: C,
      be: B =>: E,
      cd: C =>: D,
      df: D =>: F,
      ef: E =>: F
  )(a: A, c: C, d: D, e: E)(using
      fcb: C => B,
      fcd: C => D,
      fba: B => A,
      fde: D => E,
      fef: E => F
  ): ValidatedNel[String, Unit] =
    checkStrongLaw[=>:, A, B, C, D, E, F](ad, ab)(a, c, d) combine
      checkSplitLaw[=>:, A, B, C, D, E, F](ab, bc, cd, ef, be, df)(
        a,
        c,
        e
      ) combine
      checkCategoryLaw[=>:, A, B, C, D](ab, bc, cd, aa)(a)

  def checkStrongLaw[=>:[_, _]: Strong: Runner2, A, B, C, D, E, F](
      gad: A =>: D,
      gab: A =>: B
  )(a: A, c: C, d: D)(using
      fcb: C => B,
      fcd: C => D,
      fba: B => A,
      fde: D => E,
      fef: E => F
  )(using PF: Strong[Function1]): ValidatedNel[String, Unit] =
    checkProfunctorLaw[=>:, A, B, C, D, E, F](gad)(a, c) combine
      check(
        Runner2[=>:].run((a, c))(
          Strong[=>:]
            .dimap(Strong[=>:].second(gad))(swapTuple[A, C])(swapTuple[C, D])
        ) ==
          Runner2[=>:].run((a, c))(Strong[=>:].first(gad)),
        "first' == dimap swap swap . second'"
      ) combine
      check(
        Runner2[=>:].run((c, a))(
          Strong[=>:]
            .dimap(Strong[=>:].first(gad))(swapTuple[C, A])(swapTuple[D, C])
        ) ==
          Runner2[=>:].run((c, a))(Strong[=>:].second(gad)),
        "second' == dimap swap swap . first'"
      ) combine
      check(
        Runner2[=>:].run((a, c))(Strong[=>:].mapfst[A, D, (A, C)](gad)(_._1)) ==
          Runner2[=>:].run((a, c))(
            Strong[=>:].mapsnd(Strong[=>:].first[A, D, C](gad))(_._1)
          ),
        "lmap fst == rmap fst . first'"
      ) combine
      check(
        Runner2[=>:].run((c, a))(Strong[=>:].mapfst[A, D, (C, A)](gad)(_._2)) ==
          Runner2[=>:].run((c, a))(
            Strong[=>:].mapsnd(Strong[=>:].second[A, D, C](gad))(_._2)
          ),
        "lmap snd == rmap snd . second'"
      ) combine
      check(
        Runner2[=>:].run((a, c))(
          Strong[=>:]
            .mapsnd(Strong[=>:].first[A, B, C](gab))(PF.second[C, D, B](fcd))
        ) ==
          Runner2[=>:].run((a, c))(
            Strong[=>:]
              .mapfst(Strong[=>:].first[A, B, D](gab))(PF.second[C, D, A](fcd))
          ),
        "lmap (second f) . first == rmap (second f) . first"
      ) combine
      check(
        Runner2[=>:].run((c, a))(
          Strong[=>:]
            .mapsnd(Strong[=>:].second[A, B, C](gab))(PF.first[C, D, B](fcd))
        ) ==
          Runner2[=>:].run((c, a))(
            Strong[=>:]
              .mapfst(Strong[=>:].second[A, B, D](gab))(PF.first[C, D, A](fcd))
          ),
        "lmap (first f) . second == rmap (first f) . second"
      ) combine
      check(
        Runner2[=>:].run(((a, c), d))(
          Strong[=>:].first[(A, C), (B, C), D](Strong[=>:].first[A, B, C](gab))
        ) ==
          Runner2[=>:].run(((a, c), d))(
            Strong[=>:]
              .dimap[(A, (C, D)), (B, (C, D)), ((A, C), D), ((B, C), D)](
                Strong[=>:].first[A, B, (C, D)](gab)
              )(
                assoc
              )(unassoc)
          ),
        "first' . first' == dimap assoc unassoc . first'"
      ) combine
      check(
        Runner2[=>:].run((d, (c, a)))(
          Strong[=>:].second[(C, A), (C, B), D](
            Strong[=>:].second[A, B, C](gab)
          )
        ) ==
          Runner2[=>:].run((d, (c, a)))(
            Strong[=>:]
              .dimap[((D, C), A), ((D, C), B), (D, (C, A)), (D, (C, B))](
                Strong[=>:].second[A, B, (D, C)](gab)
              )(
                unassoc
              )(assoc)
          ),
        "second' . second' == dimap unassoc assoc . second'"
      )

  private def swapTuple[X, Y]: ((X, Y)) => (Y, X) = _.swap

  private def assoc[A, B, C]: (((A, B), C)) => (A, (B, C)) =
    case ((a, b), c) => (a, (b, c))

  private def unassoc[A, B, C]: ((A, (B, C))) => ((A, B), C) =
    case (a, (b, c)) => ((a, b), c)

  def checkProfunctorLaw[=>:[_, _]: Profunctor: Runner2, A, B, C, D, E, F](
      gad: A =>: D
  )(a: A, c: C)(using
      fcb: C => B,
      fba: B => A,
      fde: D => E,
      fef: E => F
  ): ValidatedNel[String, Unit] =
    check(
      Runner2[=>:].run(a)(
        Profunctor[=>:].dimap(gad)((a: A) => a)((d: D) => d)
      ) ==
        Runner2[=>:].run(a)(gad),
      "identity"
    ) combine
      check(
        Runner2[=>:].run(c)(
          Profunctor[=>:].dimap(Profunctor[=>:].dimap(gad)(fba)(fde))(fcb)(fef)
        ) ==
          Runner2[=>:].run(c)(
            Profunctor[=>:].dimap(gad)(fba compose fcb)(fef compose fde)
          ),
        "composite"
      )

  def checkSplitLaw[=>:[_, _], A, B, C, D, E, F](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      ef: E =>: F,
      be: B =>: E,
      df: D =>: F
  )(a: A, c: C, e: E)(using
      spl: Split[=>:],
      cat: Category[=>:],
      rnn: Runner2[=>:]
  ): ValidatedNel[String, Unit] =
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd)(a)(using spl, rnn) combine
      checkSplitAssociativityLaw[=>:, A, B, C, D, E, F](ab, cd, ef)(
        a,
        c,
        e
      ) combine
      checkSplitIdentityLaw[=>:, A, C](a, c) combine
      checkSplitCompatibilityWithCompositionLaw[=>:, A, B, C, D, E, F](
        ab,
        cd,
        be,
        df
      )(a, c)

  def checkCategoryLaw[=>:[_, _]: Category: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A
  )(a: A): ValidatedNel[String, Unit] =
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd)(a) combine
      checkRightIdentityLaw[=>:, A](f1)(a) combine
      checkLeftIdentityLaw[=>:, A](f1)(a)

  def checkComposeLaw[=>:[_, _], A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D
  )(a: A)(using c: Compose[=>:], r: Runner2[=>:]): ValidatedNel[String, Unit] =
    import r.*, c.*
    Either.cond[String, Unit](
      run(a)(compose(cd, compose(bc, ab))) ==
        run(a)(compose(compose(cd, bc), ab)),
      (),
      "Не соблюдается ассоциативность: compose(cd, compose(bc, ab)) должно быть равно compose(compose(cd, bc), ab)"
    ).toValidatedNel

  private def checkSplitAssociativityLaw[=>:[_, _], A, B, C, D, E, F](
      ab: A =>: B,
      cd: C =>: D,
      ef: E =>: F
  )(a: A, c: C, e: E)(using
      s: Split[=>:],
      r: Runner2[=>:]
  ): ValidatedNel[String, Unit] =
    import r.*, s.*
    val first: ((A, C), E) =>: ((B, D), F)  = split(split(ab, cd), ef)
    val second: (A, (C, E)) =>: (B, (D, F)) = split(ab, split(cd, ef))
    val ((b1, d1), f1): ((B, D), F)         = run(((a, c), e))(first)
    val (b2, (d2, f2)): (B, (D, F))         = run((a, (c, e)))(second)
    Either.cond[String, Unit](
      b1 == b2 && d1 == d2 && f1 == f2,
      (),
      "Не соблюдается ассоциативность произведения: (f split g) split h == f split (g split h)"
    ).toValidatedNel

  private def checkSplitIdentityLaw[=>:[_, _], A, B](a: A, b: B)(using
      s: Split[=>:],
      c: Category[=>:],
      r: Runner2[=>:]
  ): ValidatedNel[String, Unit] =
    import r.*, s.*, c.*
    Either.cond[String, Unit](
      run((a, b))(split(id[A], id[B])) == (a, b),
      (),
      "Не соблюдается тождественность произведения: id_a split id_b = id_ab"
    ).toValidatedNel

  private def checkSplitCompatibilityWithCompositionLaw[=>:[
      _,
      _
  ], A, B, C, D, E, F](ab: A =>: B, cd: C =>: D, be: B =>: E, df: D =>: F)(
      a: A,
      c: C
  )(using s: Split[=>:], r: Runner2[=>:]): ValidatedNel[String, Unit] =
    import r.*, s.*
    val first: (A, C) =>: (E, F)  = compose(split(be, df), split(ab, cd))
    val second: (A, C) =>: (E, F) = split(compose(be, ab), compose(df, cd))
    Either.cond[String, Unit](
      run((a, c))(first) == run((a, c))(second),
      (),
      "Не соблюдается совместимость с композицией: (f1 split g1) compose (f2 split g2) == (f1 compose f2) split (g1 compose g2)"
    ).toValidatedNel

  private def checkRightIdentityLaw[=>:[_, _], A](f1: A =>: A)(a: A)(using
      c: Category[=>:],
      r: Runner2[=>:]
  ): ValidatedNel[String, Unit] =
    import r.*, c.*
    Either.cond[String, Unit](
      run(a)(compose(f1, id[A])) == run(a)(f1),
      (),
      "Не соблюдается тождественность справа: compose(f1, id[A]) == f1"
    ).toValidatedNel

  private def checkLeftIdentityLaw[=>:[_, _], A](f1: A =>: A)(a: A)(using
      c: Category[=>:],
      r: Runner2[=>:]
  ): ValidatedNel[String, Unit] =
    import r.*, c.*
    Either.cond[String, Unit](
      run(a)(compose(id[A], f1)) == run(a)(f1),
      (),
      "Не соблюдается тождественность справа: compose(id[A], f1) == f1"
    ).toValidatedNel
end ArrowLaws

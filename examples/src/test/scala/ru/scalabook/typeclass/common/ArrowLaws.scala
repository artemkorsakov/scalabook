package ru.scalabook.typeclass.common

import cats.syntax.either.*
import cats.data.ValidatedNel
import ru.scalabook.typeclass.arrow.*

object ArrowLaws extends BasicLaws:
  def checkArrowLaw[=>:[_, _]: Arrow: Runner2, A, B, C, D, E, F](
      gad: A =>: D,
      gab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A
  )(a: A, c: C, d: D)(using
      fcb: C => B,
      fcd: C => D,
      fba: B => A,
      fde: D => E,
      fef: E => F
  ): ValidatedNel[String, Unit] =
    checkStrongLaw[=>:, A, B, C, D, E, F](gad, gab)(a, c, d) combine
      checkSplitLaw[=>:, A, B, C, D](gab, bc, cd)(a) combine
      checkCategoryLaw[=>:, A, B, C, D](gab, bc, cd, f1)(a)

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

  def checkSplitLaw[=>:[_, _]: Split: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D
  )(a: A): ValidatedNel[String, Unit] =
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd)(a)

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

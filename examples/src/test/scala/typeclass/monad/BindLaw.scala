package typeclass.monad

import typeclass.common.Runner1

trait BindLaw extends ApplyLaw:
  def checkBindLaw[F[_]: Bind, A, B, C](
      fa: F[A],
      fab: F[A => B],
      fbc: F[B => C],
      afb: A => F[B],
      bfc: B => F[C]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkApplyLaw(fa, fab, fbc)
    assertEquals(
      fa.flatMap(afb).flatMap(bfc),
      fa.flatMap((a: A) => afb(a).flatMap(bfc)),
      "flatMap associativity"
    )
    assertEquals(
      fa.bind(afb).bind(bfc),
      fa.bind((a: A) => afb(a).bind(bfc)),
      "bind associativity"
    )
    assertEquals(
      Bind[F].apply(fab)(fa),
      fab.flatMap(a2b => fa.map(a2b)),
      "`ap` is consistent with `flatMap`"
    )
    assertEquals(
      Bind[F].apply(fab)(fa),
      fab.bind(a2b => fa.map(a2b)),
      "`ap` is consistent with `bind`"
    )

  def checkBindLawWithRunner[F[_]: Bind: Runner1, A, B, C](
      fa: F[A],
      fab: F[A => B],
      fbc: F[B => C],
      afb: A => F[B],
      bfc: B => F[C]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkApplyLawWithRunner(fa, fab, fbc)
    assertEquals(
      Runner1[F].run(fa.flatMap(afb).flatMap(bfc)),
      Runner1[F].run(fa.flatMap((a: A) => afb(a).flatMap(bfc))),
      "flatMap associativity"
    )
    assertEquals(
      Runner1[F].run(fa.bind(afb).bind(bfc)),
      Runner1[F].run(fa.bind((a: A) => afb(a).bind(bfc))),
      "bind associativity"
    )
    assertEquals(
      Runner1[F].run(Bind[F].apply(fab)(fa)),
      Runner1[F].run(fab.flatMap(a2b => fa.map(a2b))),
      "`ap` is consistent with `flatMap`"
    )
    assertEquals(
      Runner1[F].run(Bind[F].apply(fab)(fa)),
      Runner1[F].run(fab.bind(a2b => fa.map(a2b))),
      "`ap` is consistent with `bind`"
    )

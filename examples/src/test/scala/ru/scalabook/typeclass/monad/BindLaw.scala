package ru.scalabook.typeclass.monad

trait BindLaw extends ApplyLaw, munit.Assertions:
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
    checkApplyLaw(fa, fab, fbc): Unit
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

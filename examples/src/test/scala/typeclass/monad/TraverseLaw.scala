package typeclass.monad

import munit.Assertions
import typeclass.common.Id
import typeclass.monad.Applicative.given
import typeclass.monad.Functor.given
import typeclass.monoid.Monoid

trait TraverseLaw extends FunctorLaw:
  def checkTraverseLaw[F[_]: Traverse, G[_]: Applicative, H[
      _
  ]: Applicative, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    checkFunctorLaw[F, A, B, C](fa)

    val a2GB: A => G[B] = a => Applicative[G].unit(f(a))
    val a2HB: A => H[B] = a => Applicative[H].unit(f(a))
    val b2HC: B => H[C] = b => Applicative[H].unit(g(b))

    // Обход Id эквивалентен `Functor#map`
    assertEquals(Traverse[F].traverse(fa)(a => Id(f(a))).value, fa.map(f))

    // Два последовательно зависимых эффекта могут быть объединены в один, их композицию
    val optFb: G[F[B]]         = Traverse[F].traverse(fa)(a2GB)
    val optListFc1: G[H[F[C]]] =
      Functor[G].map(optFb)(fb => Traverse[F].traverse(fb)(b2HC))
    val optListFc2: G[H[F[C]]] =
      Traverse[F].traverse[A](fa)[[X] =>> G[H[X]], C](a =>
        Functor[G].map(Applicative[G].unit(f(a)))(b2HC)
      )
    assertEquals(optListFc1, optListFc2)

    // Обход с помощью функции unit аналогичен прямому применению функции unit
    assertEquals(
      Traverse[F].traverse[A](fa)[G, A](a => Applicative[G].unit(a)),
      Applicative[G].unit[F[A]](fa)
    )

    // Два независимых эффекта могут быть объединены в один эффект, их произведение
    type GH[D] = (G[D], H[D])
    val t1: GH[F[B]] =
      (Traverse[F].traverse(fa)(a2GB), Traverse[F].traverse(fa)(a2HB))
    val t2: GH[F[B]] =
      Traverse[F].traverse[A](fa)[GH, B](a => (a2GB(a), a2HB(a)))
    assertEquals(t1, t2)

package typeclass.monad

import munit.Assertions
import typeclass.common.Id
import typeclass.monad.Applicative.{unit, given}
import typeclass.monad.Functor.{map, given}
import typeclass.monoid.Monoid

trait TraverseLaw extends FunctorLaw:
  def checkTraverseLaw[F[_]: Traverse, G[_]: Applicative, H[_]: Applicative, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using Monoid[Vector[A]]): Unit =
    checkFunctorLaw[F, A, B, C](fa)

    val a2GB: A => G[B] = a => unit(f(a))
    val a2HB: A => H[B] = a => unit(f(a))
    val b2HC: B => H[C] = b => unit(g(b))

    // Обход Id эквивалентен `Functor#map`
    assertEquals(Traverse[F].traverse(fa)(a => Id(f(a))).value, fa.map(f))

    // Два последовательно зависимых эффекта могут быть объединены в один, их композицию
    val optFb: G[F[B]]         = Traverse[F].traverse(fa)(a2GB)
    val optListFc1: G[H[F[C]]] =
      map[G, F[B], H[F[C]]](optFb, fb => Traverse[F].traverse(fb)(b2HC))
    val optListFc2: G[H[F[C]]] =
      Traverse[F].traverse[A](fa)[[X] =>> G[H[X]], C](a => map[G, B, H[C]](unit(f(a)), b2HC))
    assertEquals(optListFc1, optListFc2)

    // Обход с помощью функции unit аналогичен прямому применению функции unit
    assertEquals(Traverse[F].traverse[A](fa)[G, A](a => unit(a)), unit[G, F[A]](fa))

    // Два независимых эффекта могут быть объединены в один эффект, их произведение
    type GH[A] = (G[A], H[A])
    val t1: GH[F[B]] = (Traverse[F].traverse(fa)(a2GB), Traverse[F].traverse(fa)(a2HB))
    val t2: GH[F[B]] = Traverse[F].traverse[A](fa)[GH, B](a => (a2GB(a), a2HB(a)))
    assertEquals(t1, t2)

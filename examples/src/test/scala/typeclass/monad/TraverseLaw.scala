package typeclass.monad

import munit.Assertions
import typeclass.common.Id
import typeclass.monad.Applicative.{unit, given}
import typeclass.monad.Functor.{map, given}
import typeclass.monad.Traverse.traverse
import typeclass.monoid.Monoid

trait TraverseLaw extends FoldableLaw, FunctorLaw:
  def checkTraverseLaw[F[_]: Traverse, G[_]: Applicative, H[_]: Applicative, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using Monoid[Vector[A]]): Unit =
    checkFoldableLaw[F, A](fa)
    checkFunctorLaw[F, A, B, C](fa)

    // Обход эффекта [[scalaz.Id]] эквивалентен `Functor#map`
    // Traversal through the [[scalaz.Id]] effect is equivalent to `Functor#map`
    assertEquals(traverse[F, Id, A, B](fa, a => Id(f(a))).value, fa.map(f))

    // Два последовательно зависимых эффекта могут быть объединены в один, их композицию
    // Two sequentially dependent effects can be fused into one, their composition
    val optFb: G[F[B]] = traverse[F, G, A, B](fa, a => unit(f(a)))
    val optListFc1: G[H[F[C]]] =
      map[G, F[B], H[F[C]]](optFb, fb => traverse[F, H, B, C](fb, b => unit(g(b))))
    val optListFc2: G[H[F[C]]] =
      traverse[F, [X] =>> G[H[X]], A, C](fa, a => map[G, B, H[C]](unit(f(a)), b => unit(g(b))))
    assertEquals(optListFc1, optListFc2)

/*
  /** Traversal with the `point` function is the same as applying the `point` function directly */
  def purity[G[_], A](fa: F[A])(implicit G: Applicative[G], GFA: Equal[G[F[A]]]): Boolean =
    GFA.equal(traverse[G, A, A](fa)(G.point[A](_)), G.point(fa))

  /**
 * @param nat A natural transformation from `M` to `N` for which these properties hold:
 *            `(a: A) => nat(Applicative[M].point[A](a)) === Applicative[N].point[A](a)`
 *            `(f: M[A => B], ma: M[A]) => nat(Applicative[M].ap(ma)(f)) === Applicative[N].ap(nat(ma))(nat(f))`
 */
  def naturality[N[_], M[_], A](nat: (M ~> N))
                               (fma: F[M[A]])
                               (implicit N: Applicative[N], M: Applicative[M], NFA: Equal[N[F[A]]]): Boolean = {
    val n1: N[F[A]] = nat[F[A]](sequence[M, A](fma))
    val n2: N[F[A]] = sequence[N, A](map(fma)(ma => nat(ma)))
    NFA.equal(n1, n2)
  }

  /** Two independent effects can be fused into a single effect, their product. */
  def parallelFusion[N[_], M[_], A, B](fa: F[A], amb: A => M[B], anb: A => N[B])
                                      (implicit N: Applicative[N], M: Applicative[M], MN: Equal[(M[F[B]], N[F[B]])]): Boolean = {
    type MN[A] = (M[A], N[A])
    val t1: MN[F[B]] = (traverse[M, A, B](fa)(amb), traverse[N, A, B](fa)(anb))
    val t2: MN[F[B]] = traverse[MN, A, B](fa)(a => (amb(a), anb(a)))(M product N)
    MN.equal(t1, t2)
  }
}
 */

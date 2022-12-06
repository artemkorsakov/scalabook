package typeclass.monad

import typeclass.common.*

trait Traverse[F[_]] extends Functor[F], Foldable[F]:
  self =>

  extension [A](fa: F[A])
    def traverse[G[_]: Applicative, B](f: A => G[B]): G[F[B]]

    override def map[B](f: A => B): F[B] = traverse(a => Id(f(a))).value

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      traverse(a => State[B, A]((b: B) => (f(a, b), a))).run(init)._1

  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    fga.traverse(ga => ga)

object Traverse:
  def apply[F[_]: Traverse]: Traverse[F] = summon[Traverse[F]]

  given Traverse[Id] with
    extension [A](fa: Id[A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Id[B]] =
        f(fa.value).map(b => Id(b))

  given Traverse[[X] =>> (X, X)] with
    extension [A](fa: (A, A))
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[(B, B)] =
        val func: G[B => B => (B, B)] = Applicative[G].unit(b1 => b2 => (b1, b2))
        Applicative[G].apply(Applicative[G].apply(func)(f(fa._1)))(f(fa._2))

  given Traverse[[X] =>> (X, X, X)] with
    extension [A](fa: (A, A, A))
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[(B, B, B)] =
        val func: G[B => B => B => (B, B, B)] = Applicative[G].unit(b1 => b2 => b3 => (b1, b2, b3))
        Applicative[G].apply(Applicative[G].apply(Applicative[G].apply(func)(f(fa._1)))(f(fa._2)))(f(fa._3))

  given Traverse[Option] with
    extension [A](fa: Option[A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Option[B]] =
        fa match
          case Some(a) => f(a).map(Some(_))
          case None    => Applicative[G].unit(None)

  given Traverse[List] with
    extension [A](fa: List[A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[List[B]] =
        fa.foldRight(Applicative[G].unit(List[B]()))((a, acc) => f(a).map2(acc)(_ :: _))

  given Traverse[Tree] with
    extension [A](ta: Tree[A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Tree[B]] =
        f(ta.head).map2(ta.tail.traverse(a => a.traverse(f)))(Tree(_, _))

  given mapTraverse[K]: Traverse[[X] =>> Map[K, X]] with
    extension [A](m: Map[K, A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Map[K, B]] =
        m.foldLeft(Applicative[G].unit(Map.empty[K, B])) { case (acc, (k, a)) =>
          acc.map2(f(a))((m, b) => m + (k -> b))
        }

package typeclass.monad

trait MonadPlus[F[_]] extends Monad[F] with ApplicativePlus[F]:
  def filter[A](fa: F[A])(f: A => Boolean): F[A] =
    fa.flatMap(a => if f(a) then unit(a) else empty[A])

  def unite[T[_], A](value: F[T[A]])(using T: Foldable[T]): F[A] =
    value.flatMap(ta => T.foldMap(ta)(a => unit(a))(using monoid[A]))

object MonadPlus:
  given MonadPlus[List] with
    override def unit[A](a: => A): List[A] = List(a)

    override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
      fab.flatMap { aToB => fa.map(aToB) }

    override def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2

    override def empty[A]: List[A] = List.empty[A]

    extension [A](fa: List[A]) override def flatMap[B](f: A => List[B]): List[B] = fa.flatMap(f)

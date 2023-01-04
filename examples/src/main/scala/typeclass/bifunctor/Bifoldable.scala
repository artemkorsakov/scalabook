package typeclass.bifunctor

import typeclass.common.*
import typeclass.monad.Foldable
import typeclass.monoid.Monoid
import typeclass.monoid.Monoid.*

trait Bifoldable[F[_, _]]:
  extension [A, B](fa: F[A, B])
    /** Аккумулирование всех `A` и `B`. */
    def bifoldMap[M](f: A => M)(g: B => M)(using ma: Monoid[M]): M

    /** Аккумулирование до `C`, начиная "справа". */
    def bifoldRight[C](z: => C)(f: (A, C) => C)(g: (B, C) => C): C =
      fa.bifoldMap[C => C](f.curried)(g.curried)(using endoMonoid[C])(z)

    /** Аккумулирование до `C`, начиная "слева". */
    def bifoldLeft[C](z: C)(f: (C, A) => C)(g: (C, B) => C): C =
      fa.bifoldMap[C => C](a => c => f(c, a))(b => c => g(c, b))(using
        dual(endoMonoid[C])
      )(z)

  /** Выделение Foldable из первого параметра. */
  def leftFoldable[R]: Foldable[[X] =>> F[X, R]] =
    new Foldable[[X] =>> F[X, R]]:
      extension [A](fa: F[A, R])
        override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
          fa.bifoldMap[B](f)(_ => mb.empty)

  /** Выделение Foldable из второго параметра. */
  def rightFoldable[L]: Foldable[[X] =>> F[L, X]] =
    new Foldable[[X] =>> F[L, X]]:
      extension [A](fa: F[L, A])
        override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
          fa.bifoldMap[B](_ => mb.empty)(f)

  /** Унифицирование Foldable на обоих параметрах. */
  def uFoldable: Foldable[[X] =>> F[X, X]] =
    new Foldable[[X] =>> F[X, X]]:
      extension [A](fa: F[A, A])
        override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
          fa.bifoldMap[B](f)(f)

object Bifoldable:
  def apply[F[_, _]: Bifoldable]: Bifoldable[F] = summon[Bifoldable[F]]

  given eitherBifoldable: Bifoldable[Either] with
    extension [A, B](fab: Either[A, B])
      def bifoldMap[M](f: A => M)(g: B => M)(using ma: Monoid[M]): M =
        fab match
          case Right(value) => g(value)
          case Left(value)  => f(value)

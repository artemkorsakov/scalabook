import ru.scalabook.typeclass.monoid.Semigroup

trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

trait Functor[F[_]] extends InvariantFunctor[F]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]

    override def xmap[B](f: A => B, g: B => A): F[B] = fa.map(f)

trait InvariantApplicative[F[_]] extends InvariantFunctor[F]:
  def xunit0[A](a: => A): F[A]

trait Semigroupal[F[_]]:
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]

trait Apply[F[_]] extends Functor[F], Semigroupal[F]:
  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    apply(fa.map(f.curried))(fb)

  override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    apply2(fa, fb)((_, _))

  def lift2[A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] =
    apply2(_, _)(f)

trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  def unit[A](a: => A): F[A]
  override def xunit0[A](a: => A): F[A] = unit(a)

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    fa.map2(fb)((a, b) => (a, b))

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)

trait Bind[F[_]] extends Apply[F]:
  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def bind[B](f: A => F[B]): F[B] = flatMap(f)

  def join[A](ffa: F[F[A]]): F[A] = flatMap(ffa)(a => a)

  override def apply[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    flatMap(fab)(x => fa.map(x))

  override def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    flatMap(fa)(a => fb.map(b => f(a, b)))

trait Monad[F[_]] extends Applicative[F], Bind[F]:
  extension [A](fa: F[A])
    override def map[B](f: A => B): F[B] =
      fa.flatMap(a => unit(f(a)))

trait Plus[F[_]]:
  def plus[A](fa1: F[A], fa2: => F[A]): F[A]

  def semigroup[A]: Semigroup[F[A]] = (f1: F[A], f2: F[A]) => plus(f1, f2)

(false && false) == false  
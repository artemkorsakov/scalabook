package typeclass.monad

import typeclass.common.*
import typeclass.monoid.Monoid

trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  def unit[A](a: => A): F[A]
  override final def xunit0[A](a: => A): F[A] = unit(a)

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)

object Applicative:
  given idApplicative: Applicative[Id] with
    override def unit[A](a: => A): Id[A] = Id(a)
    override def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = Id(fab.value(fa.value))

  given optionApplicative: Applicative[Option] with
    override def unit[A](a: => A): Option[A] = Some(a)

    override def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
      (fab, fa) match
        case (Some(aToB), Some(a)) => Some(aToB(a))
        case _                     => None

  given listApplicative: Applicative[List] with
    override def unit[A](a: => A): List[A] = List(a)

    override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
      fab.flatMap { aToB => fa.map(aToB) }

  given eitherApplicative[E]: Applicative[[x] =>> Either[E, x]] with
    override def unit[A](a: => A): Either[E, A] = Right(a)

    override def apply[A, B](fab: Either[E, A => B])(fa: Either[E, A]): Either[E, B] =
      (fab, fa) match
        case (Right(fx), Right(a)) => Right(fx(a))
        case (Left(l), _)          => Left(l)
        case (_, Left(l))          => Left(l)

  given writerApplicative[W](using monoid: Monoid[W]): Applicative[[x] =>> Writer[W, x]] with
    override def unit[A](a: => A): Writer[W, A] =
      Writer[W, A](() => (monoid.empty, a))

    override def apply[A, B](fab: Writer[W, A => B])(fa: Writer[W, A]): Writer[W, B] =
      Writer { () =>
        val (w0, aToB) = fab.run()
        val (w1, a) = fa.run()
        (monoid.combine(w0, w1), aToB(a))
      }

  given stateApplicative[S]: Applicative[[X] =>> State[S, X]] with
    override def unit[A](a: => A): State[S, A] =
      State[S, A](s => (s, a))

    override def apply[A, B](fab: State[S, A => B])(fa: State[S, A]): State[S, B] =
      State { s =>
        val (s0, aToB) = fab.run(s)
        val (s1, a) = fa.run(s0)
        (s1, aToB(a))
      }

  given nestedApplicative[F[_], G[_]](using
      applF: Applicative[F],
      applG: Applicative[G],
      functorF: Functor[F]
  ): Applicative[[X] =>> Nested[F, G, X]] with
    override def unit[A](a: => A): Nested[F, G, A] = Nested(applF.unit(applG.unit(a)))

    override def apply[A, B](fab: Nested[F, G, A => B])(fa: Nested[F, G, A]): Nested[F, G, B] =
      val curriedFuncs: G[A => B] => G[A] => G[B] = gaTob => ga => applG.apply(gaTob)(ga)
      val fgaToB: F[G[A => B]] = fab.value
      val fGaToGb: F[G[A] => G[B]] = functorF.map(fgaToB)(curriedFuncs)
      val fga: F[G[A]] = fa.value
      val fgb: F[G[B]] = applF.apply(fGaToGb)(fga)
      Nested(fgb)

  given Applicative[IO] with
    override def unit[A](a: => A): IO[A] = IO(() => a)

    override def apply[A, B](fab: IO[A => B])(fa: IO[A]): IO[B] = IO(() => fab.run()(fa.run()))

  given compositeApplicative[F[_]: Applicative, G[_]: Applicative]: Applicative[[X] =>> F[G[X]]] with
    override def unit[A](a: => A): F[G[A]] = summon[Applicative[F]].unit(summon[Applicative[G]].unit(a))

    override def apply[A, B](fab: F[G[A => B]])(fa: F[G[A]]): F[G[B]] =
      val applicativeF = summon[Applicative[F]]
      val applicativeG = summon[Applicative[G]]
      val tmp: F[G[A] => G[B]] = applicativeF.map(fab)(ga2b => applicativeG.apply(ga2b))
      applicativeF.apply(tmp)(fa)

  def unit[F[_], A](a: => A)(using applicative: Applicative[F]): F[A] = applicative.unit(a)

  def apply[F[_], A, B](fab: F[A => B])(fa: F[A])(using applicative: Applicative[F]): F[B] =
    applicative.apply(fab)(fa)

  def map[F[_], A, B](fa: F[A], f: A => B)(using applicative: Applicative[F]): F[B] =
    applicative.map(fa)(f)

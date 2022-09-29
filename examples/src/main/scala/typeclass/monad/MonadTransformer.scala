package typeclass.monad

import typeclass.common.*
import typeclass.monoid.Monoid

trait MonadTransformer[T[_[_], _], M[_]](using mMonad: Monad[M], tMonad: Monad[[X] =>> T[M, X]]):
  def lift[A](ma: M[A]): T[M, A]

object MonadTransformer:
  given idtMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> IdT[M, X]] with
    override def unit[A](a: => A): IdT[M, A] =
      IdT[M, A](outerMonad.unit(Id(a)))
    extension [A](fa: IdT[M, A])
      override def flatMap[B](f: A => IdT[M, B]): IdT[M, B] =
        IdT[M, B](fa.run.flatMap(ida => f(ida.value).run))

  given idtMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[IdT, M] with
    override def lift[A](ma: M[A]): IdT[M, A] =
      IdT[M, A](ma.map(a => Id(a)))

  given optionTMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> OptionT[M, X]] with
    override def unit[A](a: => A): OptionT[M, A] =
      OptionT[M, A](outerMonad.unit(Some(a)))

    extension [A](fa: OptionT[M, A])
      override def flatMap[B](f: A => OptionT[M, B]): OptionT[M, B] =
        OptionT(
          fa.run.flatMap {
            case Some(value) => f(value).run
            case None        => outerMonad.unit(None)
          }
        )

  given optionTMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[OptionT, M] with
    override def lift[A](ma: M[A]): OptionT[M, A] =
      OptionT[M, A](ma.map(a => Some(a)))

  given writerMonoidInstance[W, A](using monoidW: Monoid[W], monoidA: Monoid[A]): Monoid[Writer[W, A]] with
    val empty: Writer[W, A] = Writer[W, A](() => (monoidW.empty, monoidA.empty))
    def combine(x: Writer[W, A], y: Writer[W, A]): Writer[W, A] =
      val (w0, a0) = x.run()
      val (w1, a1) = y.run()
      Writer(() => (monoidW.combine(w0, w1), monoidA.combine(a0, a1)))

  given writerTMonad[M[_], W](using outerMonad: Monad[M], outerMonoid: Monoid[W]): Monad[[X] =>> WriterT[M, W, X]] with
    override def unit[A](a: => A): WriterT[M, W, A] =
      WriterT[M, W, A](() => outerMonad.unit((outerMonoid.empty, a)))

    extension [A](fa: WriterT[M, W, A])
      override def flatMap[B](f: A => WriterT[M, W, B]): WriterT[M, W, B] =
        WriterT[M, W, B] { () =>
          outerMonad.flatMap(fa.run()) { case (wa, a) =>
            f(a).run().map { case (wb, b) => (outerMonoid.combine(wa, wb), b) }
          }
        }

  given writerTMonadTransformer[M[_], W](using
      outerMonad: Monad[M],
      outerMonoid: Monoid[W]
  ): MonadTransformer[({ type E[Y[_], X] = WriterT[Y, W, X] })#E, M] with
    override def lift[A](ma: M[A]): WriterT[M, W, A] =
      WriterT[M, W, A](() => ma.map(a => (outerMonoid.empty, a)))

  /*
implicit def stateTMonadInstance[S, M[_]](implicit
      f: Functor[M],
      m: Monad[M]
  ) = new Monad[({ type E[X] = StateT[S, M, X] })#E] {
    override def pure[A](a: A): StateT[S, M, A] = StateT[S, M, A] { s =>
      m.pure((s, a))
    }

    override def flatMap[A, B](a: StateT[S, M, A])(
        fx: A => StateT[S, M, B]
    ): StateT[S, M, B] = StateT[S, M, B] { s =>
      m.flatMap(a.runStateT(s)) { case (s1, a) => fx(a).runStateT(s1) }
    }
  }

implicit def stateTMonadTransformerInstance[S, M[_]](implicit
      f: Functor[M],
      m: Monad[M]
  ) =
    new MonadTransformer[M, ({ type E[X, Y[_]] = StateT[S, Y, X] })#E] {
      override def lift[A](a: M[A]): StateT[S, M, A] = StateT[S, M, A] { s =>
        f.fmap(a)(a => (s, a))
      }
    }
   */

  def lift[T[_[_], _], M[_], A](ma: M[A])(using monadTransformer: MonadTransformer[T, M]): T[M, A] =
    monadTransformer.lift(ma)

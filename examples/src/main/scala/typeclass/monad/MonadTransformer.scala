package typeclass.monad

import typeclass.common.{Id, IdT, OptionT}

trait MonadTransformer[M[_], T[_, _[_]]](using mMonad: Monad[M], tMonad: Monad[[X] =>> T[X, M]]):
  def lift[A](ma: M[A]): T[A, M]

object MonadTransformer:
  given idtMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> IdT[X, M]] with
    override def unit[A](a: => A): IdT[A, M] =
      IdT[A, M](outerMonad.unit(Id(a)))
    extension [A](fa: IdT[A, M])
      override def flatMap[B](f: A => IdT[B, M]): IdT[B, M] =
        IdT[B, M](fa.run.flatMap(ida => f(ida.value).run))

  given idtMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[M, IdT] with
    override def lift[A](ma: M[A]): IdT[A, M] =
      IdT[A, M](ma.map(a => Id(a)))

  given optionTMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> OptionT[X, M]] with
    override def unit[A](a: => A): OptionT[A, M] =
      OptionT[A, M](outerMonad.unit(Some(a)))

    extension [A](fa: OptionT[A, M])
      override def flatMap[B](f: A => OptionT[B, M]): OptionT[B, M] =
        val runMaybeT: M[Option[B]] = fa.run.flatMap {
          case Some(value) => f(value).run
          case None        => outerMonad.unit(None)
        }
        OptionT(runMaybeT)

  given optionTMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[M, OptionT] with
    override def lift[A](ma: M[A]): OptionT[A, M] =
      OptionT[A, M](ma.map(a => Some(a)))

  def lift[M[_], T[_, _[_]], A](ma: M[A])(using monadTransformer: MonadTransformer[M, T]): T[A, M] =
    monadTransformer.lift(ma)

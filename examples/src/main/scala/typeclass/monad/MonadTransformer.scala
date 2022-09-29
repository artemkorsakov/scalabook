package typeclass.monad

import typeclass.common.{Id, IdT, OptionT}

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
        val runMaybeT: M[Option[B]] = fa.run.flatMap {
          case Some(value) => f(value).run
          case None        => outerMonad.unit(None)
        }
        OptionT(runMaybeT)

  given optionTMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[OptionT, M] with
    override def lift[A](ma: M[A]): OptionT[M, A] =
      OptionT[M, A](ma.map(a => Some(a)))

  def lift[T[_[_], _], M[_], A](ma: M[A])(using monadTransformer: MonadTransformer[T, M]): T[M, A] =
    monadTransformer.lift(ma)

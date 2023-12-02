package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.*

trait MonadTransformer[T[_[_], _], M[_]](using
    mMonad: Monad[M],
    tMonad: Monad[[X] =>> T[M, X]]
):
  def lift[A](ma: M[A]): T[M, A]

object MonadTransformer:
  def apply[T[_[_], _], M[_]](using
      MonadTransformer[T, M]
  ): MonadTransformer[T, M] =
    summon[MonadTransformer[T, M]]

  given optionTMonad[M[_]](using
      outerMonad: Monad[M]
  ): Monad[[X] =>> OptionT[M, X]] with
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

  given optionTMonadTransformer[M[_]](using
      outerMonad: Monad[M]
  ): MonadTransformer[OptionT, M] with
    override def lift[A](ma: M[A]): OptionT[M, A] =
      OptionT[M, A](ma.map(Some(_)))
end MonadTransformer

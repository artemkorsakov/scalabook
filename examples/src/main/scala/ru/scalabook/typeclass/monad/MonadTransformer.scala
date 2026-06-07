package ru.scalabook.typeclass.monad

trait MonadTransformer[T[_[_], _], M[_]](using
    mMonad: Monad[M],
    tMonad: Monad[[X] =>> T[M, X]]
):
  def lift[A](ma: M[A]): T[M, A]

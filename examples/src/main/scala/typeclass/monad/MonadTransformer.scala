package typeclass.monad

import typeclass.common.{Id, IdT}

trait MonadTransformer[M[_], T[_, _[_]]](using mMonad: Monad[M], tMonad: Monad[[X] =>> T[X, M]]):
  def lift[A](ma: M[A]): T[A, M]

object MonadTransformer:
  given idtMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> IdT[X, M]] with
    override def unit[A](a: => A): IdT[A, M] =
      IdT[A, M](outerMonad.unit(Id(a)))
    extension [A](fa: IdT[A, M])
      override def flatMap[B](f: A => IdT[B, M]): IdT[B, M] =
        IdT[B, M](outerMonad.flatMap(fa.runIdT)(ida => f(ida.value).runIdT))

  given idtMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[M, IdT] with
    override def lift[A](a: M[A]): IdT[A, M] =
      IdT[A, M](outerMonad.map(a)(a => Id(a)))

  def lift[M[_], T[_, _[_]], A](ma: M[A])(using monadTransformer: MonadTransformer[M, T]): T[A, M] =
    monadTransformer.lift(ma)

package ru.scalabook.typeclass.monad

trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

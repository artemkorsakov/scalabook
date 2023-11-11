package ru.scalabook.typeclass.monad

trait Bimonad[F[_]] extends Monad[F], CoMonad[F]

package ru.scalabook.typeclass.monad

import cats.Id

trait Semigroupal[F[_]]:
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]

object Semigroupal:
  def apply[F[_]: Semigroupal]: Semigroupal[F] = summon[Semigroupal[F]]

  given Semigroupal[Id] with
    override def product[A, B](fa: Id[A], fb: Id[B]): Id[(A, B)] = Id((fa, fb))

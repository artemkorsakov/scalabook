package ru.scalabook.typeclass.monad

import cats.Id

trait Functor[F[_]] extends InvariantFunctor[F]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]

    override def xmap[B](f: A => B, g: B => A): F[B] = fa.map(f)

  def lift[A, B](f: A => B): F[A] => F[B] = _.map(f)

  def mapply[A, B](a: A)(f: F[A => B]): F[B] = map(f)((ff: A => B) => ff(a))

  def fproduct[A, B](fa: F[A])(f: A => B): F[(A, B)] = map(fa)(a => (a, f(a)))

object Functor:
  def apply[F[_]: Functor]: Functor[F] = summon[Functor[F]]

  given idFunctor: Functor[Id] with
    extension [A](as: Id[A]) override def map[B](f: A => B): Id[B] = Id(f(as))

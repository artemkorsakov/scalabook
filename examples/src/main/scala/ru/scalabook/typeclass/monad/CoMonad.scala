package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Env

trait CoMonad[F[_]] extends CoBind[F]:
  /** Также известно как `coPoint` и `coPure` */
  def coUnit[A](fa: F[A]): A

  final def coPoint[A](p: F[A]): A = coUnit(p)

  final def coPure[A](p: F[A]): A = coUnit(p)

object CoMonad:
  def apply[F[_]: CoMonad]: CoMonad[F] = summon[CoMonad[F]]

  given CoMonad[Id] with
    override def coUnit[A](fa: Id[A]): A = fa

    extension [A](as: Id[A])
      override def map[B](f: A => B): Id[B] = Id(f(as))

      override def cobind[B](f: Id[A] => B): Id[B] = Id(f(as))

  given envCoMonad[R]: CoMonad[[X] =>> Env[X, R]] with
    override def coUnit[A](fa: Env[A, R]): A = fa.a

    extension [A](as: Env[A, R])
      override def map[B](f: A => B): Env[B, R] =
        val Env(a, r) = as
        Env(f(a), r)

      override def cobind[B](f: Env[A, R] => B): Env[B, R] =
        val Env(_, r) = as
        Env(f(as), r)
end CoMonad

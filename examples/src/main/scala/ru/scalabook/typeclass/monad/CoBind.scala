package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Env

trait CoBind[F[_]] extends Functor[F]:
  extension [A](fa: F[A])
    /** Также известно как `extend` и `coFlatMap` */
    def cobind[B](f: F[A] => B): F[B]

    def extend[B](f: F[A] => B): F[B] = fa.cobind(f)

    def coFlatMap[B](f: F[A] => B): F[B] = fa.cobind(f)

    /** Также известно как `duplicate` */
    def cojoin: F[F[A]] = fa.cobind(fa => fa)

object CoBind:
  def apply[F[_]: CoBind]: CoBind[F] = summon[CoBind[F]]

  given CoBind[Id] with
    extension [A](as: Id[A])
      override def map[B](f: A => B): Id[B] = Id(f(as))

      override def cobind[B](f: Id[A] => B): Id[B] = Id(f(as))

  given envCoBind[R]: CoBind[[X] =>> Env[X, R]] with
    extension [A](as: Env[A, R])
      override def map[B](f: A => B): Env[B, R] =
        val Env(a, r) = as
        Env(f(a), r)

      override def cobind[B](f: Env[A, R] => B): Env[B, R] =
        val Env(_, r) = as
        Env(f(as), r)

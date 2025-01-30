package ru.scalabook.typeclass.monad

import cats.Id

trait Bimonad[F[_]] extends Monad[F], CoMonad[F]

object Bimonad:
  given Bimonad[Id] with
    def unit[A](a: => A): Id[A] = Id(a)

    def coUnit[A](fa: Id[A]): A = fa

    extension [A](as: Id[A])
      override def map[B](f: A => B): Id[B] = Id(f(as))

      def flatMap[B](f: A => Id[B]): Id[B] = f(as)

      def cobind[B](f: Id[A] => B): Id[B] = Id(f(as))

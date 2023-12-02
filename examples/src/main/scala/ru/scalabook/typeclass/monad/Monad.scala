package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.IO

trait Monad[F[_]] extends Applicative[F], Bind[F]:
  extension [A](fa: F[A])
    override def map[B](f: A => B): F[B] =
      fa.flatMap(a => unit(f(a)))

object Monad:
  def apply[F[_]: Monad]: Monad[F] = summon[Monad[F]]

  given Monad[Id] with
    override def unit[A](a: => A): Id[A] = Id(a)
    extension [A](fa: Id[A])
      override def flatMap[B](f: A => Id[B]): Id[B] = f(fa)

  given Monad[Option] with
    override def unit[A](a: => A): Option[A] = Some(a)

    extension [A](fa: Option[A])
      override def flatMap[B](f: A => Option[B]): Option[B] =
        fa match
          case Some(a) => f(a)
          case None    => None

  given Monad[List] with
    override def unit[A](a: => A): List[A] = List(a)

    extension [A](fa: List[A])
      override def flatMap[B](f: A => List[B]): List[B] = fa.flatMap(f)

  given Monad[IO] with
    override def unit[A](a: => A): IO[A] = IO(() => a)

    extension [A](fa: IO[A])
      override def flatMap[B](f: A => IO[B]): IO[B] = f(fa.run())
end Monad

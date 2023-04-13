package fp.freemonad

import fp.freemonad.Free.FlatMap

trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

sealed trait Free[S[+_], A]:
  self =>

  def flatMap[B](f: A => Free[S, B]): Free[S, B] =
    self match
      case FlatMap(a, g) => FlatMap(a, x => g(x).flatMap(f))
      case x             => FlatMap(x, f)

  def map[B](f: A => B): Free[S, B] =
    flatMap(a => Done(f(a)))

  @annotation.tailrec
  final def resume(using S: Functor[S]): Either[S[Free[S, A]], A] =
    this match
      case Done(a) => Right(a)
      case More(k) => Left(k)
      case a FlatMap f =>
        a match
          case Done(a)     => f(a).resume
          case More(k)     => Left(S.map(k)(_ flatMap f))
          case b FlatMap g => b.flatMap(x => g(x).flatMap(f)).resume

  def zip[B](b: Free[S, B])(using S: Functor[S]): Free[S, (A, B)] =
    (resume, b.resume) match
      case (Left(a), Left(b)) =>
        More(S.map(a)(x => More(S.map(b)(y => x zip y))))
      case (Left(a), Right(b)) =>
        More(S.map(a)(x => x zip Done(b)))
      case (Right(a), Left(b)) =>
        More(S.map(b)(y => Done(a) zip y))
      case (Right(a), Right(b)) =>
        Done((a, b))

object Free:
  private final case class FlatMap[S[+_], A, B](
      a: Free[S, A],
      f: A => Free[S, B]
  ) extends Free[S, B]

final case class Done[S[+_], A](a: A)             extends Free[S, A]
final case class More[S[+_], A](k: S[Free[S, A]]) extends Free[S, A]

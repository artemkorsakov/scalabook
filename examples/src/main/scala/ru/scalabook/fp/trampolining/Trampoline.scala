package ru.scalabook.fp.trampolining

import ru.scalabook.fp.*

sealed trait Trampoline[+A]:
  self =>

  @annotation.tailrec
  final def resume: Either[() => Trampoline[A], A] =
    self match
      case Done(v) => Right(v)
      case More(k) => Left(k)
      case FlatMap(a, f) =>
        a match
          case Done(v) => f(v).resume
          case More(k) => Left(() => k().flatMap(f))
          case FlatMap(b, g) =>
            b.flatMap(x => g(x).flatMap(f)).resume

  @annotation.tailrec
  final def runT: A =
    resume match
      case Right(a) => a
      case Left(k)  => k().runT

  def flatMap[B](f: A => Trampoline[B]): Trampoline[B] =
    self match
      case FlatMap(a, g) => FlatMap(a, x => g(x).flatMap(f))
      case x             => FlatMap(x, f)

  def map[B](f: A => B): Trampoline[B] =
    flatMap(a => Done(f(a)))

  def zip[B](b: Trampoline[B]): Trampoline[(A, B)] =
    (self.resume, b.resume) match
      case (Right(a), Right(b)) =>
        Done((a, b))
      case (Left(a), Left(b)) =>
        More(() => a() zip b())
      case (Left(a), Right(b)) =>
        More(() => a() zip Done(b))
      case (Right(a), Left(b)) =>
        More(() => Done(a) zip b())
end Trampoline

object Trampoline:
  given conv[A]: Conversion[A, Trampoline[A]] with
    def apply(a: A): Trampoline[A] = More(() => Done(a))

case class Done[+A](result: A)              extends Trampoline[A]
case class More[+A](k: () => Trampoline[A]) extends Trampoline[A]
case class FlatMap[A, +B](sub: Trampoline[A], k: A => Trampoline[B])
    extends Trampoline[B]

case class State[S, +A](runS: S => Trampoline[(A, S)]):
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State[S, B] { s =>
      More { () =>
        runS(s).flatMap { (a, s1) => More(() => f(a).runS(s1)) }
      }
    }

  def map[B](f: A => B): State[S, B] =
    State[S, B] { s =>
      runS(s).map { (a, s1) => (f(a), s1) }
    }

object State:
  def getState[S]: State[S, S] =
    State(s => Done((s, s)))

  def setState[S](s: S): State[S, Unit] =
    State(_ => Done[(Unit, S)](((), s)))

  def pureState[S, A](a: A): State[S, A] =
    State(s => Done((a, s)))

  def zipIndex[A](as: List[A]): List[(Int, A)] =
    as.foldLeft(pureState[Int, List[(Int, A)]](List())) { (acc, a) =>
      for
        xs <- acc
        n  <- getState
        _  <- setState(n + 1)
      yield (n, a) :: xs
    }.runS(0)
      .runT
      ._1
      .reverse
end State

@main def runZipIndex(): Unit =
  val list = State.zipIndex[String](List.fill(100000)(""))
  println("zipped list = " + list.head)

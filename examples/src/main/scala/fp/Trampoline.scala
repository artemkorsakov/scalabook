package fp

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
          case More(k) => Left(() => FlatMap(k(), f))
          case FlatMap(b, g) =>
            FlatMap(b, x => FlatMap(g(x), f)).resume

  @annotation.tailrec
  final def runT: A =
    resume match
      case Right(a) => a
      case Left(k)  => k().runT

  def flatMap[B](f: A => Trampoline[B]): More[B] =
    More[B](() => f(runT))

case class Done[+A](result: A)              extends Trampoline[A]
case class More[+A](k: () => Trampoline[A]) extends Trampoline[A]
case class FlatMap[A, +B](sub: Trampoline[A], k: A => Trampoline[B])
    extends Trampoline[B]

def even[A](ns: List[A]): Trampoline[Boolean] =
  ns match
    case Nil     => Done(true)
    case _ :: xs => More(() => odd(xs))

def odd[A](ns: List[A]): Trampoline[Boolean] =
  ns match
    case Nil     => Done(false)
    case _ :: xs => More(() => even(xs))

@main def runEvenList(): Unit =
  val list = List.fill(100000)(0)
  println(s"list is even = ${even(list).runT}")

case class State[S, +A](runS: S => Trampoline[(A, S)]):
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State[S, B] { s =>
      More { () =>
        runS(s).flatMap { (a, s1) => More(() => f(a).runS(s1)) }
      }
    }

//object State:
//  def getState[S]: State[S, S] =
//    State(s => (s, s))
//
//  def setState[S](s: S): State[S, Unit] =
//    State(_ => ((), s))
//
//  def pureState[S, A](a: A): State[S, A] =
//    State(s => (a, s))
//
//  def zipIndex[A](as: List[A]): List[(Int, A)] =
//    as.foldLeft(pureState[Int, List[(Int, A)]](List())) { (acc, a) =>
//      for
//        xs <- acc
//        n <- getState
//        _ <- setState(n + 1)
//      yield (n, a) :: xs
//    }.runS(0)
//      ._1
//      .reverse
//
//@main def runZipIndex(): Unit =
//  import TrampoliningWithStackOverflowException.State.zipIndex
//  val list = zipIndex[String](List.fill(10000)("el"))
//  println("zipped list = " + list)

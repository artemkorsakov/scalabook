package fp

case class State[S, +A](runS: S => (A, S)):
  def map[B](f: A => B): State[S, B] =
    State[S, B] { s =>
      val (a, s1) = runS(s)
      (f(a), s1)
    }

  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State[S, B] { s =>
      val (a, s1) = runS(s)
      f(a).runS(s1)
    }

object TrampoliningStackOverflowError:
  val f: Int => Int = (i: Int) => i

  val g: Int => Int =
    List.fill(10000)(f).foldLeft(f)((a, b) => x => b(a(x)))

@main def runStackOverflowError(): Unit =
  import TrampoliningStackOverflowError.*
  val gFortyTwo = g(42)
  println("run(g(42)) = " + gFortyTwo)

object TrampoliningInTailCalls:
  import scala.util.control.TailCalls.*

  val f: Int => TailRec[Int] = (i: Int) => done(i)

  val g: Int => TailRec[Int] =
    List.fill(10000)(f).foldLeft(f)((a, b) => x => tailcall(a(x).flatMap(b)))

@main def runTrampoliningInTailCalls(): Unit =
  import TrampoliningInTailCalls.*
  val gFortyTwo = g(42)
  println("run(g(42)) = " + gFortyTwo.result)

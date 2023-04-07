package fp

def even[A](ns: List[A]): Boolean =
  ns match
    case Nil     => true
    case _ :: xs => odd(xs)

def odd[A](ns: List[A]): Boolean =
  ns match
    case Nil     => false
    case _ :: xs => even(xs)

object TrampoliningInTailCalls:
  import scala.util.control.TailCalls.*

  val f: Int => TailRec[Int] = (i: Int) => done(i)

  val g: Int => TailRec[Int] =
    List.fill(10000)(f).foldLeft(f)((a, b) => x => tailcall(a(x).flatMap(b)))

@main def runTrampoliningInTailCalls(): Unit =
  import TrampoliningInTailCalls.*
  val gFortyTwo = g(42)
  println("run(g(42)) = " + gFortyTwo.result)

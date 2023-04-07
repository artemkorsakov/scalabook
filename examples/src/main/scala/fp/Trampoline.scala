package fp

sealed trait Trampoline[+A]:
  self =>

  @annotation.tailrec
  final def runT: A =
    self match
      case More(k) => k().runT
      case Done(v) => v

case class More[+A](k: () => Trampoline[A]) extends Trampoline[A]
case class Done[+A](result: A)              extends Trampoline[A]

def even[A](ns: List[A]): Trampoline[Boolean] =
  ns match
    case Nil     => Done(true)
    case _ :: xs => More(() => odd(xs))

def odd[A](ns: List[A]): Trampoline[Boolean] =
  ns match
    case Nil     => Done(false)
    case _ :: xs => More(() => even(xs))

@main def runTrampoline(): Unit =
  val list = List.fill(100000)(0)
  println(s"list is even = ${even(list).runT}")

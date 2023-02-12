package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.common.Id

class FreeSuite extends ScalaCheckSuite:
  sealed trait Operation[A]
  final case class Start(a: Int)       extends Operation[Int]
  final case class Add(a: Int, b: Int) extends Operation[Int]

  object OperationToId extends Natural[Operation, Id]:
    final def transform[A](a: Operation[A]): Id[A] =
      a match
        case Start(a)  => Id(a)
        case Add(a, b) => Id(a + b)

  def start(a: Int): Free[Operation, Int]       = Free.liftF(Start(a))
  def add(a: Int, b: Int): Free[Operation, Int] = Free.liftF(Add(a, b))

  property("Check free monad") {
    forAll { (a: Int, b: Int, c: Int) =>
      lazy val program = for {
        a1 <- start(a)
        a2 <- add(a1, b)
        a3 <- add(a2, c)
      } yield a3
      val result = Free.foldF(program)(OperationToId)
      assertEquals(result, Id(a + b + c))
    }
  }

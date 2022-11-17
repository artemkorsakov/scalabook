package typeclass.monoid

trait Group[A] extends Monoid[A]:
  extension (a: A) 
    def inverse: A

object Group:
  given Group[Int] with
    override val empty                           = 0
    override def combine(x: Int, y: Int): Int    = x + y
    extension (a: Int) override def inverse: Int = -a

  extension [A](a: A)(using g: Group[A]) def inverse: A = g.inverse(a)

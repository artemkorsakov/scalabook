package typeclass.monoid

trait Group[A] extends Monoid[A]:
  extension (a: A) def inverse: A

object Group:
  def apply[A: Group]: Group[A] = summon[Group[A]]

  given Group[Int] with
    override val empty                           = 0
    override def combine(x: Int, y: Int): Int    = x + y
    extension (a: Int) override def inverse: Int = -a

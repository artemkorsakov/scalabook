package typeclass.monoid

trait Monoid[A] extends Semigroup[A]:
  def empty: A

object Monoid:
  given sumMonoidInstance: Monoid[Int] with
    val empty = 0
    def combine(x: Int, y: Int): Int = x + y

  given productMonoidInstance: Monoid[Int] with
    val empty = 1
    def combine(x: Int, y: Int): Int = x * y

  given stringMonoidInstance: Monoid[String] with
    val empty = ""
    def combine(x: String, y: String): String = x + y

  given listMonoidInstance[T]: Monoid[List[T]] with
    val empty = List.empty[T]
    def combine(x: List[T], y: List[T]): List[T] = x ++ y

  given nestedMonoidInstance[A, B](using aMonoid: Monoid[A], bMonoid: Monoid[B]): Monoid[(A, B)] with
    lazy val empty: (A, B) = (aMonoid.empty, bMonoid.empty)
    def combine(x: (A, B), y: (A, B)): (A, B) = (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))

  def combine[A](x: A, y: A)(using m: Monoid[A]): A = m.combine(x, y)

  def empty[A](using m: Monoid[A]): A = m.empty

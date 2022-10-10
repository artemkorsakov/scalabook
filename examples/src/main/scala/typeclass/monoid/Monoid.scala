package typeclass.monoid

import typeclass.common.Writer

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

  given writerMonoid[W, A](using monoidW: Monoid[W], monoidA: Monoid[A]): Monoid[Writer[W, A]] with
    val empty: Writer[W, A] = Writer[W, A](() => (monoidW.empty, monoidA.empty))

    def combine(x: Writer[W, A], y: Writer[W, A]): Writer[W, A] =
      val (w0, a0) = x.run()
      val (w1, a1) = y.run()
      Writer(() => (monoidW.combine(w0, w1), monoidA.combine(a0, a1)))

  given optionMonoidInstance[A: Semigroup]: Monoid[Option[A]] with
    val empty: Option[A] = None

    def combine(x: Option[A], y: Option[A]): Option[A] =
      (x, y) match
        case (Some(a1), Some(a2)) => Some(summon[Semigroup[A]].combine(a1, a2))
        case (Some(_), None)      => x
        case (None, Some(_))      => y
        case (None, None)         => None

  def combine[A](x: A, y: A)(using m: Monoid[A]): A = m.combine(x, y)

  def empty[A](using m: Monoid[A]): A = m.empty

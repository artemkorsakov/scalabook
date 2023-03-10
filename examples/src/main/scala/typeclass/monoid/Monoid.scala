package typeclass.monoid

import typeclass.common.Writer

trait Monoid[A] extends Semigroup[A]:
  def empty: A

object Monoid:
  def apply[A: Monoid]: Monoid[A] = summon[Monoid[A]]

  given sumMonoidInstance: Monoid[Int] with
    val empty                        = 0
    def combine(x: Int, y: Int): Int = x + y

  given productMonoidInstance: Monoid[Int] with
    val empty                        = 1
    def combine(x: Int, y: Int): Int = x * y

  given stringMonoidInstance: Monoid[String] with
    val empty                                 = ""
    def combine(x: String, y: String): String = x + y

  given listMonoidInstance[T]: Monoid[List[T]] with
    val empty                                    = List.empty[T]
    def combine(x: List[T], y: List[T]): List[T] = x ++ y

  given nestedMonoidInstance[A, B](using
      aMonoid: Monoid[A],
      bMonoid: Monoid[B]
  ): Monoid[(A, B)] with
    lazy val empty: (A, B) = (aMonoid.empty, bMonoid.empty)
    def combine(x: (A, B), y: (A, B)): (A, B) =
      (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))

  given writerMonoid[W, A](using
      monoidW: Monoid[W],
      monoidA: Monoid[A]
  ): Monoid[Writer[W, A]] with
    val empty: Writer[W, A] = Writer[W, A](() => (monoidW.empty, monoidA.empty))

    def combine(x: Writer[W, A], y: Writer[W, A]): Writer[W, A] =
      val (w0, a0) = x.run()
      val (w1, a1) = y.run()
      Writer(() => (monoidW.combine(w0, w1), monoidA.combine(a0, a1)))

  given optionMonoidInstance[A: Semigroup]: Monoid[Option[A]] with
    val empty: Option[A] = None

    def combine(x: Option[A], y: Option[A]): Option[A] =
      (x, y) match
        case (Some(a1), Some(a2)) => Some(Semigroup[A].combine(a1, a2))
        case (Some(_), None)      => x
        case (None, Some(_))      => y
        case (None, None)         => None

  given vectorMonoidInstance[T]: Monoid[Vector[T]] with
    val empty                                          = Vector.empty[T]
    def combine(x: Vector[T], y: Vector[T]): Vector[T] = x ++ y

  // Можно получить двойник любого моноида, просто перевернув `combine`.
  def dual[A](m: Monoid[A]): Monoid[A] = new:
    def combine(x: A, y: A): A = m.combine(y, x)
    val empty: A               = m.empty

  def endoMonoid[A]: Monoid[A => A] = new:
    def combine(f: A => A, g: A => A): A => A = f andThen g
    val empty: A => A                         = identity

package ru.scalabook.typeclass.monoid

trait Monoid[A] extends Semigroup[A]:
  def empty: A

object Monoid:
  def apply[A: Monoid]: Monoid[A] = summon[Monoid[A]]

  // Числа относительно сложения с 0
  given additionMonoid: Monoid[Int] with
    val empty = 0

    def combine(x: Int, y: Int): Int = x + y

  // Числа относительно умножения с 1
  given multiplicationMonoid: Monoid[Int] with
    val empty = 1

    def combine(x: Int, y: Int): Int = x * y

  // Строки относительно конкатенации с пустой строкой
  given concatenationMonoid: Monoid[String] with
    val empty = ""

    def combine(x: String, y: String): String = x + y

  // Последовательность относительно операции объединения
  given listMonoid[T]: Monoid[List[T]] with
    val empty = List.empty[T]

    def combine(x: List[T], y: List[T]): List[T] = x ++ y

  // Кортеж от двух и более моноидов
  given nestedMonoid[A, B](using
      aMonoid: Monoid[A],
      bMonoid: Monoid[B]
  ): Monoid[(A, B)] with
    val empty: (A, B) = (aMonoid.empty, bMonoid.empty)

    def combine(x: (A, B), y: (A, B)): (A, B) =
      (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))

  // `Option` является моноидом, если его параметр типа - полугруппа, например:
  given optionMonoidInstance[A: Semigroup]: Monoid[Option[A]] with
    val empty: Option[A] = None

    def combine(x: Option[A], y: Option[A]): Option[A] =
      (x, y) match
        case (Some(a1), Some(a2)) => Some(summon[Semigroup[A]].combine(a1, a2))
        case (Some(_), None)      => x
        case (None, Some(_))      => y
        case (None, None)         => None

  // Множество - `Boolean`, операция - логическое И (`&&`), пустой элемент - `true`
  given booleanAnd: Monoid[Boolean] with
    val empty: Boolean = true

    def combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2

  // Множество - `Boolean`, операция - логическое Или (`||`), пустой элемент - `false`
  given booleanOr: Monoid[Boolean] with
    val empty: Boolean = false

    def combine(a1: Boolean, a2: Boolean): Boolean = a1 || a2

  // Можно получить двойник любого моноида, просто перевернув `combine`.
  def dual[A](m: Monoid[A]): Monoid[A] = new:
    def combine(x: A, y: A): A = m.combine(y, x)
    val empty: A               = m.empty

  def endoMonoid[A]: Monoid[A => A] = new:
    def combine(f: A => A, g: A => A): A => A = f andThen g
    val empty: A => A                         = identity
end Monoid

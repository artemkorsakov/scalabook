import cats.data.ValidatedNel
import cats.syntax.either.*

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

object Monoid:
  // Числа относительно сложения с 0
  given additionMonoid: Monoid[Int] with
    val empty                        = 0
    def combine(x: Int, y: Int): Int = x + y

  // Числа относительно умножения с 1
  given multiplicationMonoid: Monoid[Int] with
    val empty                        = 1
    def combine(x: Int, y: Int): Int = x * y

  // Строки относительно конкатенации с пустой строкой
  given concatenationMonoid: Monoid[String] with
    val empty                                 = ""
    def combine(x: String, y: String): String = x + y

  // Последовательность относительно операции объединения
  given listMonoid[T]: Monoid[List[T]] with
    val empty                                    = List.empty[T]
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
    val empty: Boolean                             = true
    def combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2

  // Множество - `Boolean`, операция - логическое Или (`||`), пустой элемент - `false`
  given booleanOr: Monoid[Boolean] with
    val empty: Boolean                             = false
    def combine(a1: Boolean, a2: Boolean): Boolean = a1 || a2
end Monoid

object MonoidLaws:
  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  def checkRightIdentity[A](x: A)(using
      m: Monoid[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      m.combine(x, m.empty) == x,
      (),
      "Не соблюдается тождественность справа: x + e = x"
    ).toValidatedNel

  def checkLeftIdentity[A](x: A)(using
      m: Monoid[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      m.combine(m.empty, x) == x,
      (),
      "Не соблюдается тождественность слева: e + x = x"
    ).toValidatedNel
end MonoidLaws

import Monoid.given
import MonoidLaws.*

checkAssociativity(1, 2, 3)(using additionMonoid)
// (1 + 2) + 3 = 1 + (2 + 3) = 6
checkAssociativity(1, 2, 3)(using multiplicationMonoid)
// (1 * 2) * 3 = 1 * (2 * 3) = 6
checkAssociativity("first", "second", "third")(using concatenationMonoid)
// ("first" + "second") + "third" = "first" + ("second" + "third") = "firstsecondthird"
checkAssociativity(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))(using
  listMonoid
)
// (List(1, 2, 3) ++ List(4, 5, 6)) ++ List(7, 8, 9) =
// List(1, 2, 3, 4, 5, 6, 7, 8, 9) =
// List(1, 2, 3) ++ (List(4, 5, 6) ++ List(7, 8, 9))

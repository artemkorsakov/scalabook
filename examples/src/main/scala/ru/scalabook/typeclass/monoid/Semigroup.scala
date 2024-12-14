package ru.scalabook.typeclass.monoid

trait Semigroup[A]:
  def combine(x: A, y: A): A

object Semigroup:
  // Натуральные числа N образуют полугруппу относительно сложения
  given additionSemigroup: Semigroup[Int] = (x: Int, y: Int) => x + y

  // Натуральные числа N образуют полугруппу относительно умножения
  given multiplicationSemigroup: Semigroup[Int] = (x: Int, y: Int) => x * y

  // Строки образуют полугруппу относительно конкатенации
  given concatenationSemigroup: Semigroup[String] =
    (x: String, y: String) => x + y

  // Последовательность образует полугруппу относительно операции объединения
  given listSemigroup[T]: Semigroup[List[T]] =
    (x: List[T], y: List[T]) => x ++ y

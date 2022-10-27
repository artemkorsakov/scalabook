# Bifunctor

`Bifunctor` - тип, порождающий два несвязанных [функтора](../monad/functor).


## Описание

```scala
trait Bifunctor[F[_, _]]:
  /** `map` over both type parameters. */
  def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D]

  /** Extract the Functor on the first param. */
  def leftFunctor[R]: Functor[[X] =>> F[X, R]] =
    new Functor[[X] =>> F[X, R]]:
      extension [A](fax: F[A, R]) override def map[B](f: A => B): F[B, R] = leftMap(fax)(f)

  def leftMap[A, B, C](fab: F[A, B])(f: A => C): F[C, B] =
    bimap(fab)(f, identity)

  /** Extract the Functor on the second param. */
  def rightFunctor[L]: Functor[[X] =>> F[L, X]] =
    new Functor[[X] =>> F[L, X]]:
      extension [A](fax: F[L, A]) override def map[B](f: A => B): F[L, B] = rightMap(fax)(f)

  def rightMap[A, B, D](fab: F[A, B])(g: B => D): F[A, D] =
    bimap(fab)(identity, g)

  /** Unify the functor over both params. */
  def uFunctor: Functor[[X] =>> F[X, X]] =
    new Functor[[X] =>> F[X, X]]:
      extension [A](fax: F[A, A]) override def map[B](f: A => B): F[B, B] = umap(fax)(f)

  def umap[A, B](faa: F[A, A])(f: A => B): F[B, B] =
    bimap(faa)(f, f)
```

## Примеры

### [Either](../../fp/handling-errors)

```scala
given Bifunctor[Either] with
  override def bimap[A, B, C, D](fab: Either[A, B])(f: A => C, g: B => D): Either[C, D] =
    fab match
      case Right(value) => Right(g(value))
      case Left(value)  => Left(f(value))
```

### [Writer](../../fp/writer) - функциональный журнал

```scala
case class Writer[W, A](run: () => (W, A))

given Bifunctor[Writer] with
  override def bimap[A, B, C, D](fab: Writer[A, B])(f: A => C, g: B => D): Writer[C, D] =
    Writer { () =>
      val (a, b) = fab.run()
      (f(a), g(b))
    }
```


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fbifunctor%2FBifunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fbifunctor%2FBifunctorSuite.scala&plain=1)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

Bifunctor[Tuple2].bimap(("asdf", 1))(_.toUpperCase, _ + 1)                               // ("ASDF",2)
("asdf", 1).bimap(_.length, _ + 1)                                                       // (4,2)

// Для суммированных типов, какая функция применяется, зависит от того, какое значение присутствует:
Bifunctor[Either].bimap(Left("asdf") : Either[String,Int])(_.toUpperCase, _ + 1)         // Left("ASDF")
Bifunctor[Either].bimap(Right(1): Either[String,Int])(_.toUpperCase, _ + 1)              // Right(2)

//
// leftMap / rightMap
//

// Существуют функции для отображения только «правого» или «левого» значения:
Bifunctor[Tuple2].leftMap(("asdf", 1))(_.substring(1))                                   // ("sdf" -> 1)
Bifunctor[Tuple2].rightMap(("asdf", 1))(_ + 3)                                           // ("asdf" -> 4)

// Они идут с таким синтаксисом
1.success[String].rightMap(_ + 10)                                                       // Success(11)
("a", 1).rightMap(_ + 10)                                                                // ("a" -> 11)

// и еще более причудливым
1.success[String] :-> (_ + 1)                                                            // Success(2)

// С левой стороны вывод типа может быть плохим, так что мы вынуждены явно указывать типы в функции, которую мы оставилиMap.
val strlen: String => Int = _.length
(strlen <-: ("asdf", 1))                                                                 // (4 -> 1)
(((_:String).length) <-: ("asdf", 1))                                                    // (4 -> 1)

strlen <-: ("asdf", 1) :-> (_ + 1)                                                       // (4 -> 2)

//
// Functor extraction
//

// Мы можем получить либо левый, либо правый базовые функторы.
val leftF = Bifunctor[\/].leftFunctor[String]
leftF.map("asdf".right[Int])(_ + 1)                                                      // "asdf".right[Int]
leftF.map(1.left)(_ + 1)                                                                 // 2.left[String]

val rightF = Bifunctor[\/].rightFunctor[String]
rightF.map("asdf".left[Int])(_ + 1)                                                      // "asdf".left[Int]
rightF.map(1.right)(_ + 1)                                                               // 2.right[String]

//
// Ufunctor
//

// Если у нас есть F[A,A] (вместо F[A,B] с разными A и B) мы можем извлечь "унифицированный функтор", который является функтором,
Bifunctor[Tuple2].uFunctor.map((2,3))(_ * 3)                                             // (6 -> 9)

// или пропустить шаг извлечения единого функтора методом umap.
Bifunctor[Tuple2].umap((2,3))(_ * 3)                                                     // (6 -> 9)
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Bifunctor.html)

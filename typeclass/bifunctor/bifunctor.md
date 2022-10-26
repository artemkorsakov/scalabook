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

val len: String => Int = _.length
Functor[Option].map(Some("adsf"))(len)             // Some(4)
Functor[Option].map(None)(len)                     // None
Functor[List].map(List("qwer", "adsfg"))(len)      // List(4, 5)
// или через вызов метода на типе
List(1, 2, 3) map {_ + 1}                          // List(2, 3, 4)
List(1, 2, 3) ∘ {_ + 1}                            // List(2, 3, 4)

// В ScalaZ есть метод fpair, дублирующий значение в функторе 
List(1, 2, 3).fpair                                // List((1,1), (2,2), (3,3))

// Используя Functor можно «поднять» функцию для работы с типом Functor. Пример на Functor[Option]:
val lenOption: Option[String] => Option[Int] = Functor[Option].lift(len)
lenOption(Some("abcd"))                            // Some(4)
Functor[List].lift {(_: Int) * 2} (List(1, 2, 3))  // List(2, 4, 6)

// В ScalaZ есть методы strength, позволяющие "прокидывать" значение, создавая коллекцию tuple-ов
List(1,2,3).strengthL("a")                         // List("a" -> 1, "a" -> 2, "a" -> 3)
List(1,2,3).strengthR("a")                         // List(1 -> "a", 2 -> "a", 3 -> "a")

// Functor предоставляет функцию fproduct, которая сопоставляет значение с результатом применения функции к этому значению.
List("a", "aa", "b", "ccccc").fproduct(len)        // List((a,1), (aa,2), (b,1), (ccccc,5))

// Метод void «аннулирует» функтор, заменяя любой F[A] на F[Unit]
Functor[Option].void(Some(1))                      // Some(())

// Также в ScalaZ есть метод "принудительно" выставляющий заданное значение
List(1, 2, 3) >| "x"                               // List(x, x, x)
List(1, 2, 3) as "x"                               // List(x, x, x)

// Компоновка функторов
val listOpt = Functor[List] compose Functor[Option]
listOpt.map(List(Some(1), None, Some(3)))(_ + 1)   // List(Some(2), None, Some(4))
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Bifunctor.html)

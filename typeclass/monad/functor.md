# Functor

Функтор — это преобразование из категории `A` в категорию `B`. 
Такие преобразования часто изображаются стрелкой: `A -> B` (или через метод `map`).

Функтор создает новые экземпляры классов типов, добавляя функцию в цепочку преобразований.

Функтор (F) расширяет [инвариантный функтор](invariant-functor) и должен следовать следующим законам 
(помимо законов инвариантного функтора):
- Identity (тождественность): Если определен метод идентификации `identity` такой, что: `identity(a) == a`, 
  тогда `map(fa)(identity) == fa`. Другими словами: если мы отобразим функцию `identity` на функтор, 
  функтор, который мы получим, должен быть таким же, как исходный функтор.
- Composition (композиция): Если определены два метода `f` и `g`, тогда `fa.map(f).map(g) == fa.map(g(f(_)))`.
  Другими словами: композиция двух функций и последующее отображение результирующей функции на функтор 
  должно быть таким же, как сначала отображение одной функции на функтор, а затем отображение другой.

  
## Описание

```scala
trait Functor[F[_]] extends InvariantFunctor[F]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]

    override def xmap[B](f: A => B, g: B => A): F[B] = fa.map(f)

  def lift[A, B](f: A => B): F[A] => F[B] = _.map(f)

  def mapply[A, B](a: A)(f: F[A => B]): F[B] = map(f)((ff: A => B) => ff(a))

  def fproduct[A, B](fa: F[A])(f: A => B): F[(A, B)] = map(fa)(a => (a, f(a)))
```

Как видно из примера выше, функтор позволяет определить дополнительные операции:
- "поднимает" функцию `A => B` до функции преобразования функторов `F[A] => F[B]`
- применяет функтор от функции преобразования из `A` в `B` (`F[A => B]`) к элементу типа `A` и получает функтор от `B`
- по функтору от `A` и функции преобразования из `A` в `B` позволяет получать функтор от кортежа `(A, B)` 


## Примеры

### "Обертка" является функтором

```scala
case class Id[A](value: A)

given idFunctor: Functor[Id] with
  extension [A](as: Id[A]) 
    override def map[B](f: A => B): Id[B] = Id(f(as.value))
```

### [Option](../../docs/scala/fp/functional-error-handling)

```scala
given optionFunctor: Functor[Option] with
  extension [A](optA: Option[A])
    override def map[B](f: A => B): Option[B] =
      optA match
        case Some(a) => Some(f(a))
        case None    => None
```

### [Последовательность](../../docs/scala/collections)

```scala
given listFunctor: Functor[List] with
  extension [A](as: List[A]) 
    override def map[B](f: A => B): List[B] = as.map(f)
```

### [Either](../../fp/handling-errors)

```scala
given eitherFunctor[E]: Functor[[x] =>> Either[E, x]] with
  extension [A](fa: Either[E, A])
    override def map[B](f: A => B): Either[E, B] =
      fa match
        case Right(a) => Right(f(a))
        case Left(e)  => Left(e)
```

### [Writer](../../fp/writer) - функциональный журнал

```scala
case class Writer[W, A](run: () => (W, A))

given writerFunctor[W]: Functor[[x] =>> Writer[W, x]] with
  extension [A](fa: Writer[W, A])
    override def map[B](f: A => B): Writer[W, B] =
      val (w, a) = fa.run()
      Writer[W, B](() => (w, f(a)))
```

### [State](../../fp/state) - функциональное состояние

```scala
case class State[S, +A](run: S => (S, A))

given stateFunctor[S]: Functor[[x] =>> State[S, x]] with
  extension [A](fa: State[S, A])
    override def map[B](f: A => B): State[S, B] =
      State[S, B] { s =>
        val (s1, a) = fa.run(s)
        (s1, f(a))
      }
```

### Nested - два вложенных функтора образуют новый функтор

```scala
final case class Nested[F[_], G[_], A](value: F[G[A]])

given nestedFunctor[F[_], G[_]](using functorF: Functor[F], functorG: Functor[G]): Functor[[X] =>> Nested[F, G, X]]
  with
  extension [A](fga: Nested[F, G, A])
    override def map[B](f: A => B): Nested[F, G, B] =
      Nested[F, G, B] {
        functorF.map(fga.value)(ga => functorG.map(ga)(f))
      }
```

### IO

```scala
final case class IO[R](run: () => R)

given ioFunctor: Functor[IO] with
  extension [A](as: IO[A]) override def map[B](f: A => B): IO[B] = IO { () => f(as.run()) }
```

### [Бинарное дерево](../../algorithms/trees/binary-tree)

```scala
given Functor[BinaryTree] with
  extension [A](as: BinaryTree[A])
    override def map[B](f: A => B): BinaryTree[B] = as match
      case Leaf                   => Leaf
      case Branch(a, left, right) => Branch(f(a), left.map(f), right.map(f))
```


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

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

## Реализация в Cats

```scala
import cats.*
import cats.implicits.*

val list1 = List(1, 2, 3)
val list2 = Functor[List].map(list1)(_ * 2)  // List(2, 4, 6)

val func = (x: Int) => x + 1
val liftedFunc = Functor[Option].lift(func)
liftedFunc(Option(1))                        // Some(2)
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FFunctor.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FFunctorSuite.scala)
- [Algebird](https://twitter.github.io/algebird/typeclasses/functor.html)
- [Cats](https://typelevel.org/cats/typeclasses/functor.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Functor.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Functor.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#definition-of-a-functor)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Functor.html)
- [Tour of Scala](https://tourofscala.com/scala/functor)

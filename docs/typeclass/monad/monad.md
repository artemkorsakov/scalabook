# Monad

Монада (_monad_) - это [`Applicative`](https://scalabook.gitflic.space/docs/typeclass/monad/applicative), 
который также поддерживает [`Bind`](https://scalabook.gitflic.space/docs/typeclass/monad/bind), 
ограниченный законами монады.

Монады являются естественным расширением аппликативных функторов, они обеспечивают решение следующей проблемы: 
если у нас есть значение с контекстом, `m a`, как нам применить его к функции, 
которая принимает нормальное значение `a` и возвращает значение с контекстом.

Для `Monad` должны соблюдаться следующие законы (+ все законы родителей: `Applicative`, `Bind`, `Apply`, `Functor` и т.д.):

- leftIdentity - первый закон монад гласит, что если мы берем значение, 
  помещаем его в контекст по умолчанию с помощью `unit` и затем передаем его функции с помощью `flatMap`, 
  это то же самое, что просто взять значение и применить к нему функцию: `unit(x).flatMap(f) == f(x)`
- rightIdentity - второй закон гласит, что если у нас есть монадическое значение, и мы используем `flatMap`, 
  чтобы передать его для `unit`, результатом будет исходное монадическое значение: `fa.flatMap(unit _) == fa`
- ассоциативность (наследуется от `Bind`): последний закон монад гласит, что когда у нас есть цепочка приложений 
  монадических функций с `flatMap`, не должно иметь значения, как они вложены: `fa.flatMap(f).flatMap(g) == fa.flatMap { a => f(a).flatMap(g) }`

  
## Описание

```scala
trait Monad[F[_]] extends Applicative[F], Bind[F]:
  extension [A](fa: F[A])
    override def map[B](f: A => B): F[B] =
      fa.flatMap(a => unit(f(a)))
```

## Примеры

### "Обертка"

```scala
case class Id[A](value: A)

given idMonad: Monad[Id] with
  override def unit[A](a: => A): Id[A] = Id(a)

  extension [A](fa: Id[A]) override def flatMap[B](f: A => Id[B]): Id[B] = f(fa.value)
```

### [Option](https://scalabook.gitflic.space/docs/scala/fp/functional-error-handling)

```scala
given optionMonad: Monad[Option] with
  override def unit[A](a: => A): Option[A] = Some(a)

  extension [A](fa: Option[A])
    override def flatMap[B](f: A => Option[B]): Option[B] =
      fa match
        case Some(a) => f(a)
        case None    => None
```

### [Последовательность](https://scalabook.gitflic.space/docs/scala/collections)

```scala
given listMonad: Monad[List] with
  override def unit[A](a: => A): List[A] = List(a)

  extension [A](fa: List[A]) override def flatMap[B](f: A => List[B]): List[B] = fa.flatMap(f)
```

### [Either](https://scalabook.gitflic.space/docs/fp/handling-errors)

```scala
given eitherMonad[E]: Monad[[x] =>> Either[E, x]] with
  override def unit[A](a: => A): Either[E, A] = Right(a)

  extension [A](fa: Either[E, A])
    override def flatMap[B](f: A => Either[E, B]): Either[E, B] =
      fa match
        case Right(a) => f(a)
        case Left(e)  => Left(e)
```

### [Writer](https://scalabook.gitflic.space/docs/fp/writer) - функциональный журнал

```scala
case class Writer[W, A](run: () => (W, A))

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

given writerMonad[W](using monoid: Monoid[W]): Monad[[x] =>> Writer[W, x]] with
  override def unit[A](a: => A): Writer[W, A] =
    Writer[W, A](() => (monoid.empty, a))

  extension [A](fa: Writer[W, A])
    override def flatMap[B](f: A => Writer[W, B]): Writer[W, B] =
      Writer[W, B] { () =>
        val (w1, a) = fa.run()
        val (w2, b) = f(a).run()
        (monoid.combine(w1, w2), b)
      }
```

### [State](https://scalabook.gitflic.space/docs/fp/state) - функциональное состояние

```scala
case class State[S, +A](run: S => (S, A))

given stateMonad[S]: Monad[[x] =>> State[S, x]] with
  override def unit[A](a: => A): State[S, A] =
    State[S, A](s => (s, a))

  extension [A](fa: State[S, A])
    override def flatMap[B](f: A => State[S, B]): State[S, B] =
      State[S, B] { s =>
        val (s1, a) = fa.run(s)
        f(a).run(s1)
      }
```

### IO

```scala
final case class IO[R](run: () => R)

given ioMonad: Monad[IO] with
  override def unit[A](a: => A): IO[A] = IO(() => a)

  extension [A](fa: IO[A]) override def flatMap[B](f: A => IO[B]): IO[B] = f(fa.run())
```


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

// ... Все операции родителей

for { n <- List(1, 2); ch <- List('a', 'b') } yield (n, ch)     // List((1,a), (1,b), (2,a), (2,b))
(for { a <- (_: Int) * 2; b <- (_: Int) + 10 } yield a + b)(3)  // 19
List(1, 2) filterM { x => List(true, false) }                   // List(List(1, 2), List(1), List(2), List())
```


## Реализация в Cats

```scala
import cats.Monad
import cats.instances.option.*
import cats.instances.list.*

val opt1 = Monad[Option].pure(3)                                     // Some(3)
val opt2 = Monad[Option].flatMap(opt1)(a => Some(a + 2))             // Some(5)
val opt3 = Monad[Option].map(opt2)(a => 100 * a)                     // Some(500)

val list1 = Monad[List].pure(3)                                      // List(3)
val list2 = Monad[List].flatMap(List(1, 2, 3))(a => List(a, a * 10)) // List(1, 10, 2, 20, 3, 30)
val list3 = Monad[List].map(list2)(a => a + 123)                     // List(124, 133, 125, 143, 126, 153)

1.pure[Option]                                                       // Some(1)
1.pure[List]                                                         // List(1)
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FMonad.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FMonadSuite.scala)
- [Algebird](https://twitter.github.io/algebird/typeclasses/monad.html)
- [Cats](https://typelevel.org/cats/typeclasses/monad.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Monad.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#sec:monads)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Monad.html)
- [Tour of Scala](https://tourofscala.com/scala/monad)

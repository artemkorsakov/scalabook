# Монада

## Формальное определение

Монада (_monad_) - это [`Applicative`](applicative.md), 
который также поддерживает [`Bind`](bind.md), 
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


## Определение в виде кода на Scala

```dotty
trait Monad[F[_]] extends Applicative[F], Bind[F]:
  extension [A](fa: F[A])
    override def map[B](f: A => B): F[B] =
      fa.flatMap(a => unit(f(a)))
```

### Виды монад

#### Monad с `flatMap` и `unit`

Монада может быть определена с помощью `flatMap` и `unit`.
В этом случае `map` и `map2` будут определяться так:

```dotty
trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def map[B](f: A => B): F[B] =
      flatMap(a => unit(f(a)))

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      fa.flatMap(a => fb.map(b => f(a, b)))
```

#### Monad с `compose` и `unit`

Монада может быть определена с помощью `compose` и `unit`.
В этом случае `flatMap` (и через неё остальные операции) будет определяться так:

```dotty
trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C]

  extension [A](fa: F[A])
    def flatMapViaCompose[B](f: A => F[B]): F[B] =
      compose[Unit, A, B](_ => fa, f)(())
```

В этом случае законы Монады примут вид:

- identities:
    - `compose(f, unit) == f`
    - `compose(unit, f) == f`
- associativity:
    - `compose(compose(f, g), h) == compose(f, compose(g, h))`

#### Monad с `map`, `join` и `unit`

Монада может быть определена с помощью `map`, `join` (другое имя - `flatten`) и `unit`.
В этом случае `flatMap` и `compose` могут быть определены так:

```dotty
trait Functor[F[_]]:
  extension [A](fa: F[A]) 
    def map[B](f: A => B): F[B]

trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension[A] (ffa: F[F[A]])
    def join: F[A]

  extension [A](fa: F[A])
    def flatMapViaJoinAndMap[B](f: A => F[B]): F[B] =
      fa.map(f).join
 
  def composeViaJoinAndMap[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => f(a).map(g).join      
```

### Комбинаторы монад

Монада определяет некоторые комбинаторы:

```dotty
def sequence[A](fas: List[F[A]]): F[List[A]] =
  traverse(fas)(identity)

def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
  as.foldRight(unit(List.empty[B]))((a, acc) => f(a).map2(acc)(_ :: _))

def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
  sequence(List.fill(n)(fa))

// Kleisli arrows
def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
  a => f(a).flatMap(g)  
```

## Примеры

### "Обертка"

```dotty
case class Id[A](value: A)

given idMonad: Monad[Id] with
  override def unit[A](a: => A): Id[A] = Id(a)

  extension [A](fa: Id[A]) override def flatMap[B](f: A => Id[B]): Id[B] = f(fa.value)
```

### Option

```dotty
given optionMonad: Monad[Option] with
  override def unit[A](a: => A): Option[A] = Some(a)

  extension [A](fa: Option[A])
    override def flatMap[B](f: A => Option[B]): Option[B] =
      fa match
        case Some(a) => f(a)
        case None    => None
```

### Последовательность

```dotty
given listMonad: Monad[List] with
  override def unit[A](a: => A): List[A] = List(a)

  extension [A](fa: List[A]) override def flatMap[B](f: A => List[B]): List[B] = fa.flatMap(f)
```

### Either

```dotty
given eitherMonad[E]: Monad[[x] =>> Either[E, x]] with
  override def unit[A](a: => A): Either[E, A] = Right(a)

  extension [A](fa: Either[E, A])
    override def flatMap[B](f: A => Either[E, B]): Either[E, B] =
      fa match
        case Right(a) => f(a)
        case Left(e)  => Left(e)
```

### Writer - функциональный журнал

```dotty
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

### State - функциональное состояние

```dotty
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

```dotty
final case class IO[R](run: () => R)

given ioMonad: Monad[IO] with
  override def unit[A](a: => A): IO[A] = IO(() => a)

  extension [A](fa: IO[A]) override def flatMap[B](f: A => IO[B]): IO[B] = f(fa.run())
```

## Реализация

### Реализация в Cats

```dotty
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

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// ... Все операции родителей

for { n <- List(1, 2); ch <- List('a', 'b') } yield (n, ch)     // List((1,a), (1,b), (2,a), (2,b))
(for { a <- (_: Int) * 2; b <- (_: Int) + 10 } yield a + b)(3)  // 19
List(1, 2) filterM { x => List(true, false) }                   // List(List(1, 2), List(1), List(2), List())
```

---

**Ссылки:**

- [Монада - wikipedia](https://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D0%BD%D0%B0%D0%B4%D0%B0_(%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5))
- [A Monads Approach for Beginners, in Scala - Rock the JVM](https://www.youtube.com/watch?v=d-dy1x33moA&list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
- [Algebird](https://twitter.github.io/algebird/typeclasses/monad.html)
- [Cats](https://typelevel.org/cats/typeclasses/monad.html)
- [Functional Programming in Scala, Second Edition, Chapter 11](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/Monad.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad.html)
- [Monads are fractals](https://eed3si9n.com/monads-are-fractals)
- [Monads are Elephants Part 1](https://james-iry.blogspot.com/2007/09/monads-are-elephants-part-1.html)
- [Monads are Elephants Part 2](https://james-iry.blogspot.com/2007/10/monads-are-elephants-part-2.html)
- [Monads are Elephants Part 3](https://james-iry.blogspot.com/2007/10/monads-are-elephants-part-3.html)
- [Monads are Elephants Part 4](https://james-iry.blogspot.com/2007/11/monads-are-elephants-part-4.html)
- [ObserveFunctorMonad](https://blog.tmorris.net/posts/observefunctormonad/index.html)
- [Of Algebirds, Monoids, Monads, and other Bestiary for Large-Scale Data Analytics](https://www.michael-noll.com/blog/2013/12/02/twitter-algebird-monoid-monad-for-large-scala-data-analytics/#monads)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#sec:monads)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Monad.html)
- [SKB – Scala Monad](https://leobenkel.com/2021/01/skb-scala-monad)
- [Strong Type Systems](https://blog.tmorris.net/posts/strong-type-systems/index.html)
- [Tour of Scala](https://tourofscala.com/scala/monad)
- [When you hear ‘Monad’, think ‘Chainable’](https://jamesward.com/2017/10/02/when-you-hear-monad-think-chainable)

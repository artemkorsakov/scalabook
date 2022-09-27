# Functor

Функтор — это преобразование из категории `A` в категорию `B`. 
Такие преобразования часто изображаются стрелкой: `A -> B` (или через метод `map`).

Функтор (F) в теории категорий должен следовать нескольким правилам:
- Все элементы `A` должны иметь результат в `B` 
- Identity (тождественность): Если определен метод идентификации `id` такой, что: `id(a) == a`, 
тогда `id(F) == F.map(id)`. 
- Composition (композиция): Если определены два метода `f` и `g`, тогда `F.map(f).map(g) == F.map(g(f(_)))`.


### Примеры функторов

##### Описание функтора

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]
```

##### "Обертка" является функтором

```scala
case class Id[A](value: A)

given idFunctor: Functor[Id] with
  extension [A](as: Id[A]) 
    override def map[B](f: A => B): Id[B] = Id(f(as.value))
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
given optionFunctor: Functor[Option] with
  extension [A](optA: Option[A])
    override def map[B](f: A => B): Option[B] =
      optA match
        case Some(a) => Some(f(a))
        case None    => None
```

##### [Последовательность](../../scala/collections)

```scala
given listFunctor: Functor[List] with
  extension [A](as: List[A]) 
    override def map[B](f: A => B): List[B] = as.map(f)
```

##### [Either](../../fp/handling-errors)

```scala
given eitherFunctor[E]: Functor[[x] =>> Either[E, x]] with
  extension [A](fa: Either[E, A])
    override def map[B](f: A => B): Either[E, B] =
      fa match
        case Right(a) => Right(f(a))
        case Left(e)  => Left(e)
```

##### Writer

```scala
case class Writer[W, A](run: () => (W, A))

given writerFunctor[W]: Functor[[x] =>> Writer[W, x]] with
  extension [A](fa: Writer[W, A])
    override def map[B](f: A => B): Writer[W, B] =
      val (w, a) = fa.run()
      Writer[W, B](() => (w, f(a)))
```

##### [State](../../fp/state) - функциональное состояние

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

##### Nested - два вложенных функтора образуют новый функтор

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

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FFunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FFunctorSuite.scala)


### Реализации функторов в различных библиотеках


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/functor)
- [Algebird](https://twitter.github.io/algebird/typeclasses/functor.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)

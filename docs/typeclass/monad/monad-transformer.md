{%
showPrev = true
prevPage = bimonad.html
showNext = true
nextPage = free-monads.html
%}

# Monad Transformer

**Monad Transformer** (или **трансформер монад**) — это конструкция в функциональном программировании, 
позволяющая комбинировать несколько монад в одну, сохраняя их индивидуальные свойства. 
Это особенно полезно, когда требуется работать с вложенными контекстами, 
например, `Option[Future[A]]` или `Either[Error, Option[A]]`.

## Основные Monad Transformers в Scala

- **`OptionT[F[_], A]`**:
    - Комбинирует монаду `F` с [`Option`](../../fp/handling-errors.md).
    - Полезен для работы с типами вида `F[Option[A]]`.
- **`EitherT[F[_], E, A]`**:
    - Комбинирует монаду `F` с [`Either[E, A]`](../../fp/handling-errors.md).
    - Полезен для работы с типами вида `F[Either[E, A]]`.
- **`ReaderT[F[_], R, A]`** (также известный как `Kleisli`):
    - Комбинирует монаду `F` с `Reader` (функцией `R => A`).
    - Полезен для работы с типами вида `R => F[A]`.
- **`WriterT[F[_], L, A]`**:
    - Комбинирует монаду `F` с [`Writer` (значением, сопровождаемым логом)](../../fp/writer.md).
    - Полезен для работы с типами вида `F[(L, A)]`.
- **`StateT[F[_], S, A]`**:
    - Комбинирует монаду `F` с [`State` (вычислениями, зависящими от состояния)](../../fp/state.md).
    - Полезен для работы с типами вида `S => F[(S, A)]`.

### Краткие примеры

- **Асинхронные вычисления с опциональным результатом**:
    - У вас есть асинхронная операция, которая возвращает `Future[Option[A]]`.
    - С помощью `OptionT` вы можете работать с этим типом как с монадой, не разворачивая вложенные контексты вручную.
- **Обработка ошибок в асинхронных вычислениях**:
    - У вас есть асинхронная операция, которая возвращает `Future[Either[Error, A]]`.
    - С помощью `EitherT` вы можете обрабатывать ошибки и асинхронные вычисления в одном контексте.
- **Работа с состоянием и логами**:
    - У вас есть вычисление, которое зависит от состояния (`State`) и сопровождается логами (`Writer`).
    - С помощью `StateT` и `WriterT` вы можете комбинировать эти контексты для удобной работы.

## Формальное определение

Monad Transformer — это тип `T[F[_], A]`, где:

- `F[_]` — это базовая монада (например, `Option`, `Future`, `Either`).
- `T` — это трансформер, который добавляет дополнительный слой контекста 
  (например, `OptionT` для `Option`, `EitherT` для `Either`).

Monad Transformer предоставляет методы для работы с вложенными монадами, 
такие как `map`, `flatMap`, и `pure`, но в контексте комбинированной монады.

## Код

```dotty
trait MonadTransformer[T[_[_], _], M[_]: Monad as mMonad](using
    tMonad: Monad[[X] =>> T[M, X]]
):
  def lift[A](ma: M[A]): T[M, A]
```

## Упражнения

### Упражнение 2.17.1: Реализация Monad для OptionT (трансформер монады Option)

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

`OptionT[M, A]` — это монадный трансформер, 
который добавляет эффект `Option` (возможное отсутствие значения) к внутренней монаде `M`. 
Это позволяет работать с эффектами `Option` внутри контекста другой монады (например, `IO`, `Future`, `Either`).

Структура: `OptionT[M, A] = M[Option[A]]` — внешний слой — это эффект `M`, внутренний — `Option`.

Операции:

- `unit`: поднимает значение в `OptionT`, создавая `M[Some(a)]`
- `flatMap`: последовательно применяет функцию, обрабатывая `None` как короткое замыкание

Реализуйте экземпляр `Monad` для `OptionT[M, *]`, предполагая, что `M` является монадой.

```dotty
import ru.scalabook.typeclasses.Monad

final case class OptionT[M[_], A](run: M[Option[A]])

given optionTMonad[M[_]: Monad as outerMonad]: Monad[[X] =>> OptionT[M, X]]
with
  override def unit[A](a: => A): OptionT[M, A] = ???

  extension [A](fa: OptionT[M, A])
    override def flatMap[B](f: A => OptionT[M, B]): OptionT[M, B] = ???
```

**Примеры:**

```dotty
import Monad.given

// Предположим, у нас есть IO как монада
type OptionIO[A] = OptionT[IO, A]

// Создание значения
val opt1: OptionIO[Int] = unit(42)
opt1.run  // IO(Some(42))

// flatMap с Some
val opt2 = opt1.flatMap(x => unit(x * 2))
opt2.run  // IO(Some(84))

// flatMap с завёрнутым None
val none: OptionIO[Int] = OptionT(IO(None))
val result = none.flatMap(x => unit(x * 2))
result.run  // IO(None)

// Цепочка преобразований
val computation = for
  a <- unit(5)
  b <- unit(10)
  c <- unit(15)
yield a + b + c
computation.run  // IO(Some(30))
```

@:note(🔍 Показать решение)

`unit` создаёт `OptionT[M, A]`, оборачивая `Some(a)` в монаду `M` с помощью `outerMonad.unit(Some(a))`. 
В `flatMap` мы запускаем текущее вычисление `fa.run`, получая `M[Option[A]]`. 
С помощью `flatMap` внутренней монады обрабатываем `Option`:

- `Some(value)` — применяем `f` к значению и запускаем результат
- `None` — возвращаем `None`, поднятый в `M` через `outerMonad.unit(None)`

```dotty
given optionTMonad[M[_]: Monad as outerMonad]: Monad[[X] =>> OptionT[M, X]]
with
  override def unit[A](a: => A): OptionT[M, A] =
    OptionT[M, A](outerMonad.unit(Some(a)))

  extension [A](fa: OptionT[M, A])
    override def flatMap[B](f: A => OptionT[M, B]): OptionT[M, B] =
      OptionT(
        fa.run.flatMap {
          case Some(value) => f(value).run
          case None        => outerMonad.unit(None)
        }
      )
```

@:@

### Упражнение 2.17.2: Реализация MonadTransformer для OptionT

`MonadTransformer` — это класс типов для трансформеров монад, 
который предоставляет операцию `lift` для поднятия вычислений из внутренней монады `M` в трансформер `T[M, *]`.

Для `OptionT` операция `lift` поднимает значение `M[A]` в `OptionT[M, A]`.

Реализуйте экземпляр `MonadTransformer` для `OptionT`.

```dotty
given optionTMonadTransformer[M[_]: Monad]: MonadTransformer[OptionT, M] with
  override def lift[A](ma: M[A]): OptionT[M, A] = ???
```

**Примеры:**

```dotty
import Monad.given

val io: IO[Int] = IO(42)
val optT: OptionT[IO, Int] = lift(io)
optT.run  // IO(Some(42))

// Поднятие пустого значения
val emptyIO: IO[Unit] = IO.unit
val emptyOptT = lift(emptyIO)
emptyOptT.run  // IO(Some(()))

// Использование в for-комплексных выражениях
val result = for
  a <- lift(IO(10))
  b <- unit(20)
yield a + b
result.run  // IO(Some(30))
```

@:note(🔍 Показать решение)

`lift` преобразует `M[A]` в `OptionT[M, A]` с помощью `map` внутренней монады: 
применяем `Some(_)` к значению внутри `ma`, поднимая его в `Option`. 
При этом внешний эффект `M` сохраняется, а `Option` добавляется как дополнительный слой.

```dotty
given optionTMonadTransformer[M[_]: Monad]: MonadTransformer[OptionT, M] with
  override def lift[A](ma: M[A]): OptionT[M, A] =
    OptionT[M, A](ma.map(Some(_)))
```

@:@

### Упражнение 2.17.3: Реализация Monad для WriterT (трансформер монады Writer)

`WriterT[M, W, A]` — это монадный трансформер, который добавляет эффект `Writer` (накопление лога типа `W`) 
к внутренней монаде `M`. Требуется, чтобы `W` был моноидом.

Структура: `WriterT[M, W, A] = () => M[(W, A)]` — ленивое вычисление, возвращающее пару (лог, значение) в контексте `M`.

Реализуйте экземпляр `Monad` для `WriterT[M, W, *]`.

```dotty
final case class WriterT[M[_], W, A](run: () => M[(W, A)])

given writerTMonad[M[_]: Monad as outerMonad, W: Monoid as outerMonoid]
    : Monad[[X] =>> WriterT[M, W, X]] with
  override def unit[A](a: => A): WriterT[M, W, A] = ???

  extension [A](fa: WriterT[M, W, A])
    override def flatMap[B](f: A => WriterT[M, W, B]): WriterT[M, W, B] = ???
```

**Примеры:**

```dotty
import Monad.given
import Monoid.given

type WriterIO[A] = WriterT[IO, String, A]

// Создание значения с пустым логом
val w1: WriterIO[Int] = unit(42)
w1.run()  // IO(("", 42))

// flatMap с накоплением логов
val w2 = w1.flatMap(x => WriterT(() => IO(("log1 ", x * 2))))
w2.run()  // IO(("log1 ", 84))

// Цепочка с несколькими логами
val computation = for
  a <- WriterT(() => IO(("step1 ", 10)))
  b <- WriterT(() => IO(("step2 ", 20)))
  c <- WriterT(() => IO(("sum ", a + b)))
yield c
computation.run()  // IO(("step1 step2 sum ", 30))
```

@:note(🔍 Показать решение)

`unit` создаёт ленивое вычисление, которое при вызове возвращает пару `(outerMonoid.empty, a)` в контексте `M`. 
В `flatMap` мы сначала запускаем `fa.run()`, получая `M[(W, A)]`. 
С помощью `flatMap` внутренней монады извлекаем пару `(wa, a)`, 
затем запускаем `f(a).run()`, получая `M[(W, B)]`, 
и с помощью `map` комбинируем логи через `outerMonoid.combine(wa, wb)`, возвращая `(w, b)`.

```dotty
given writerTMonad[M[_]: Monad as outerMonad, W: Monoid as outerMonoid]
    : Monad[[X] =>> WriterT[M, W, X]] with
  override def unit[A](a: => A): WriterT[M, W, A] =
    WriterT[M, W, A](() => outerMonad.unit((outerMonoid.empty, a)))

  extension [A](fa: WriterT[M, W, A])
    override def flatMap[B](f: A => WriterT[M, W, B]): WriterT[M, W, B] =
      WriterT[M, W, B]: () =>
        fa.run().flatMap: (wa, a) =>
          f(a).run().map((wb, b) => (outerMonoid.combine(wa, wb), b))
```

@:@

### Упражнение 2.17.4: Реализация MonadTransformer для WriterT

Для `WriterT` операция `lift` поднимает значение `M[A]` в `WriterT[M, W, A]`.

Реализуйте экземпляр `MonadTransformer` для `WriterT`.

```dotty
given writerTMonadTransformer[M[_]: Monad, W: Monoid as outerMonoid]
    : MonadTransformer[[Y[_], X] =>> WriterT[Y, W, X], M] with
  override def lift[A](ma: M[A]): WriterT[M, W, A] = ???
```

**Примеры:**

```dotty
val io: IO[Int] = IO(42)
val writerT: WriterT[IO, String, Int] = lift(io)
writerT.run()  // IO(("", 42))
```

@:note(🔍 Показать решение)

`lift` создаёт `WriterT`, который при вызове применяет `map` к `ma`, 
преобразуя каждое значение `a` в пару `(outerMonoid.empty, a)`. 
Таким образом, исходное значение сохраняется, а лог остаётся пустым.

```dotty
given writerTMonadTransformer[M[_]: Monad, W: Monoid as outerMonoid]
    : MonadTransformer[[Y[_], X] =>> WriterT[Y, W, X], M] with
  override def lift[A](ma: M[A]): WriterT[M, W, A] =
    WriterT[M, W, A](() => ma.map(a => (outerMonoid.empty, a)))
```

@:@

### Упражнение 2.17.5: Реализация Monad для StateT (трансформер монады State)

`StateT[M, S, A]` — это монадный трансформер, который добавляет эффект `State` (изменяемое состояние типа `S`) 
к внутренней монаде `M`.

Структура: `StateT[M, S, A] = S => M[(S, A)]` — функция, принимающая начальное состояние 
и возвращающая пару (новое состояние, значение) в контексте `M`.

Реализуйте экземпляр `Monad` для `StateT[M, S, *]`.

```dotty
import ru.scalabook.typeclasses.Monad

final case class StateT[M[_], S, A](run: S => M[(S, A)])

given stateTMonad[M[_]: Monad as outerMonad, S]
    : Monad[[X] =>> StateT[M, S, X]] with
  override def unit[A](a: => A): StateT[M, S, A] = ???

  extension [A](fa: StateT[M, S, A])
    override def flatMap[B](f: A => StateT[M, S, B]): StateT[M, S, B] = ???
```

**Примеры:**

```dotty
import Monad.given

type StateIO[A] = StateT[IO, Int, A]

// Создание значения без изменения состояния
val s1: StateIO[Int] = unit(42)
s1.run(5)  // IO((5, 42))

// flatMap с передачей состояния
val increment: StateIO[Int] = StateT(s => IO((s + 1, s)))
val result = increment.flatMap(x => unit(x * 2))
result.run(10)  // IO((11, 20))

// for-комплексные выражения с состоянием
val program = for
  a <- increment
  b <- increment
  c <- unit(a + b)
yield c
program.run(5)  // IO((7, 5+6=11))
```

@:note(🔍 Показать решение)

`unit` создаёт `StateT`, который принимает состояние `s` и возвращает в контексте `M` пару `(s, a)` — состояние не меняется. 
В `flatMap` мы получаем новое состояние `s1` и значение `a` из `fa.run(s)`, 
затем запускаем `f(a).run(s1)`, передавая обновлённое состояние дальше.

```dotty
given stateTMonad[M[_]: Monad as outerMonad, S]
    : Monad[[X] =>> StateT[M, S, X]] with
  override def unit[A](a: => A): StateT[M, S, A] =
    StateT[M, S, A](s => outerMonad.unit((s, a)))

  extension [A](fa: StateT[M, S, A])
    override def flatMap[B](f: A => StateT[M, S, B]): StateT[M, S, B] =
      StateT[M, S, B]: s =>
        fa.run(s).flatMap((s1, a) => f(a).run(s1))
```

@:@

### Упражнение 2.17.6: Реализация MonadTransformer для StateT

Для `StateT` операция `lift` поднимает значение `M[A]` в `StateT[M, S, A]`, 
создавая вычисление, которое не изменяет состояние и возвращает исходное значение.

Реализуйте экземпляр `MonadTransformer` для `StateT`.

```dotty
given stateTMonadTransformer[M[_]: Monad, S]
    : MonadTransformer[[Y[_], X] =>> StateT[Y, S, X], M] with
  override def lift[A](ma: M[A]): StateT[M, S, A] = ???
```

**Примеры:**

```dotty
val io: IO[Int] = IO(42)
val stateT: StateT[IO, String, Int] = lift(io)
stateT.run("state")  // IO(("state", 42))

// Комбинация поднятых значений с изменением состояния
val update: StateT[IO, Int, Unit] = StateT(s => IO((s + 1, ())))
val result = for
  a <- lift(IO(10))
  _ <- update
  b <- lift(IO(20))
yield a + b
result.run(5)  // IO((6, 30))
```

@:note(🔍 Показать решение)

`lift` создаёт `StateT`, который принимает состояние `s` 
и возвращает в контексте `M` пару `(s, a)`, где `a` — значение из `ma`. 
Состояние остаётся неизменным, а `ma` выполняется в контексте внешней монады.

```dotty
given stateTMonadTransformer[M[_]: Monad, S]
    : MonadTransformer[[Y[_], X] =>> StateT[Y, S, X], M] with
  override def lift[A](ma: M[A]): StateT[M, S, A] =
    StateT[M, S, A](s => ma.map(a => (s, a)))
```

@:@

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [A comprehensive introduction to Cats-mtl](https://typelevel.org/blog/2018/10/06/intro-to-mtl.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad+transformers.html)
- [Scalaz Monad Transformers](https://noelwelsh.com/posts/scalaz-monad-transformers/)

[sample]: https://gitverse.ru/scalabook/typeclasses

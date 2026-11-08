# Defer — ленивое создание эффектов

## Введение

В Scala (как и в большинстве JVM-языков) аргументы функций **вычисляются строго** (call-by-value) — то есть до вызова функции. 
Это создаёт проблемы для рекурсивных определений и ленивых эффектов.

```dotty
// Без Defer - выполняется сразу
def withoutDefer: IO[Boolean] =
  val marker =
    println("⚠ Выполнится при определении!")
    true
  IO.pure(marker)

val bad = withoutDefer
// ⚠ Выполнится при определении!
// val bad: IO[Boolean] = IO(true)

// С Defer - выполняется при запуске
def withDefer(using D: Defer[IO]): IO[Boolean] =
  D.defer:
    val marker =
      println("✅ Выполнится только при .unsafeRunSync()")
      true
    IO.pure(marker)

val good = withDefer
// val good: IO[Boolean] = IO(...)

good.unsafeRunSync()
// ✅ Выполнится только при .unsafeRunSync()
// val res0: Boolean = true
```

## Определение

`Defer[F[_]]` предоставляет возможность **отложить создание эффекта** до момента его реального использования:

```dotty
trait Defer[F[_]]:
  def defer[A](fa: => F[A]): F[A]
```

**Что делает `defer`:**

- Принимает **by-name параметр** (`=> F[A]`) — выражение не вычисляется при вызове
- Возвращает **эффект**, который при выполнении вычислит переданное выражение
- Позволяет "отложить" создание внутреннего эффекта

## Расширенные возможности Defer

### `fix` — Создание рекурсивных эффектов

Метод `fix` позволяет определить **рекурсивное значение** в терминах самого себя:

```dotty
def fix[A](fn: F[A] => F[A]): F[A]
```

**Как это работает:**

```dotty
// fix(fn) = fn(fix(fn))  — рекурсивное уравнение
```

**Пример: Веб-сервер с рекурсивными маршрутами**

```dotty
sealed trait Route[F[_]]
case class Leaf[F[_]](handler: F[Unit])         extends Route[F]
case class Branch[F[_]](routes: List[Route[F]]) extends Route[F]

def buildRouter[F[_]: {Defer, Monad}](routes: List[Route[F]]): F[Unit] =
  Defer[F].fix[Unit]: self =>
    routes match
      case Nil                   => Monad[F].unit
      case Leaf(handler) :: rest =>
        handler >> Defer[F].defer(buildRouter(rest))
      case Branch(branches) :: rest =>
        buildRouter(branches) >> Defer[F].defer(buildRouter(rest))
```

### `recursiveFn` — Рекурсивные функции, возвращающие эффект

Создаёт рекурсивную функцию, где каждый шаг лениво обёрнут в `defer`:

```dotty
def recursiveFn[A, B](fn: (A => F[B]) => (A => F[B])): A => F[B]
```

## Итог

### ✅ **Используйте `Defer`:**

- Строите **DSL с отложенной интерпретацией**
- Хотите **предотвратить немедленное выполнение** эффекта при передаче в функцию
- Реализуете **ленивую инициализацию** ресурсов
- Пишете **парсеры с рекурсивными грамматиками**

### ❌ **Не используйте `Defer`:**

- Нужно **кэширование результата** (используйте `lazy val` или `memoize`)
- Просто оборачиваете побочный эффект (используйте `Sync[F].delay`)
- Работаете с уже ленивым эффектом (например, `IO` и так ленивый)
- Вам нужна **чистая ленивость без эффектов** (используйте `cats.Eval`)

### 💡 **Золотое правило:**

```dotty
// Без Defer — строгая оценка
def strict[F[_]: Monad](fa: F[A]): F[A] = fa

// С Defer — ленивая оценка
def lazy_[F[_]: Defer: Monad](fa: => F[A]): F[A] = Defer[F].defer(fa)
```

`Defer` — это мост между строгим миром Scala и ленивым миром эффектов. 
Он позволяет строить рекурсивные структуры данных и функций, 
которые в противном случае привели бы к переполнению стека или преждевременному выполнению побочных эффектов.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Baeldung: Difference Between delay, defer, and deferred in Cats Effect](https://www.baeldung.com/scala/cats-effect-delay-defer-vs-deferred)

[doc]: https://typelevel.org/cats-effect/docs/concepts

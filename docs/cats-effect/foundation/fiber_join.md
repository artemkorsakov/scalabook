{%
showPrev = true
prevPage = fiber_cancel.html
showNext = true
nextPage = ../typeclasses.html
%}

# Объединение волокон

Не все параллельные операции — это «запустил и забыл». В реальных приложениях обычно требуется:

- Запустить несколько задач параллельно.
- Дождаться их завершения.
- Собрать результаты.
- Обработать успехи и ошибки.

**Java Threads** предлагают для этого метод `join()`, который блокирует текущий поток до завершения другого. 
Это не даёт информации о том, _как_ завершился поток (успех, ошибка, прерывание).

**Волокна Cats Effect** предоставляют гораздо более мощный механизм — `Fiber#join`, 
который возвращает не просто значение, а **`Outcome`** — структуру, кодирующую три возможных состояния завершения.

## Базовый пример: параллельное выполнение двух задач

```dotty
// Не используйте это в продакшене — есть более безопасные альтернативы!
def both[F[_]: Spawn, A, B](fa: F[A], fb: F[B]): F[(A, B)] =
  for
    fiberA <- fa.start
    fiberB <- fb.start
    a      <- fiberA.joinWithNever
    b      <- fiberB.joinWithNever
  yield (a, b)
```

**Что здесь происходит:**

1. Запускаем `fa` и `fb` в отдельных волокнах (неблокирующий старт).
2. `joinWithNever` ожидает завершения каждого волокна и извлекает результат.
3. Если волокно было отменено, `joinWithNever` **зависает навсегда** (never) — подходит не для всех случаев.

> **Почему `joinWithNever`?** Это упрощённый метод над `join`, который скрывает сложность работы с `Outcome`. 
> Но для реального кода нужно понимать, что происходит под капотом.

### `Outcome` — три лица завершения волокна

```dotty
sealed trait Outcome[F[_], E, A]
object Outcome:
  case class Succeeded[F[_], E, A](value: F[A]) extends Outcome[F, E, A]
  case class Errored[F[_], E, A](error: E)      extends Outcome[F, E, A]
  case class Canceled[F[_], E, A]()             extends Outcome[F, E, A]
```

**Что означают эти три варианта:**

| Вариант          | Когда возникает                      | Что содержит                          | Как обрабатывать                            |
|------------------|--------------------------------------|---------------------------------------|---------------------------------------------|
| **`Succeeded`**  | Волокно завершилось успешно          | Эффект `F[A]` (не сам `A`!)           | `flatMap` или `>>` для извлечения значения  |
| **`Errored`**    | Волокно упало с ошибкой              | Ошибка типа `E` (обычно `Throwable`)  | `raiseError` или трансформация              |
| **`Canceled`**   | Волокно было отменено до завершения  | Ничего                                | Зависит от семантики — см. ниже             |

> **Важный нюанс:** `Succeeded` содержит не чистое значение `A`, а **эффект** `F[A]`. Почему? 
> Потому что Cats Effect поддерживает ленивые эффекты — само значение может быть вычислено только внутри контекста `F`. 
> Обычно вы просто вызываете `flatMap` или используете `>>`, чтобы «распаковать» его.

## Подробный пример с обработкой всех вариантов

```dotty
import cats.MonadThrow
import cats.effect.{Fiber, IO, MonadCancel, Outcome, Spawn}
import cats.syntax.all.*

def joinWithCustomLogic[F[_]: MonadThrow, A](
    fiber: Fiber[F, Throwable, A]
)(defaultValue: A)(using MonadCancel[F, Throwable]): F[A] =
  fiber.join.flatMap:
    case Outcome.Succeeded(fa) =>
      // Успех — просто извлекаем значение
      fa

    case Outcome.Errored(e) =>
      // Ошибка — пробрасываем дальше
      MonadThrow[F].raiseError(e)

    case Outcome.Canceled() =>
      // Отмена — используем значение по умолчанию
      MonadCancel[F].canceled.as(defaultValue)
```

**Использование:**

```dotty
val task: IO[String] = IO.sleep(1.second) >> IO.pure("Привет")
val fiber: IO[Fiber[IO, Throwable, String]] = task.start

// Ждём результат, при отмене возвращаем "Таймаут"
val result: IO[String] = fiber.flatMap(joinWithCustomLogic(_)("Таймаут"))
```

## Проблема: что делать с `Outcome.Canceled()`?

Отмена — самый сложный случай. У вас есть несколько стратегий, каждая со своими компромиссами.

### Стратегия 1: Интерпретировать отмену как ошибку

```dotty
case Outcome.Canceled() =>
  MonadThrow[F].raiseError(new RuntimeException("Дочернее волокно было отменено"))
```

**Когда использовать:** Если отмена дочернего волокна — это исключительная ситуация в вашей предметной области. 
Например, вы ждали результат, и его отсутствие нарушает контракт.

**Проблема:** Если текущее волокно находится внутри `uncancelable`, 
`raiseError` всё равно сработает — ошибка пройдёт, но отмена не произойдёт. 
Это может быть хорошо или плохо в зависимости от контекста.

### Стратегия 2: Отменить себя при отмене дочернего

```dotty
case Outcome.Canceled() =>
  MonadCancel[F].canceled  // ← возвращает F[Unit]
```

**Проблема:** Типы не совпадают! Нам нужно `F[A]`, а `canceled` возвращает `F[Unit]`. Компилятор не пропустит.

**Решение — использовать `>> never`:**

```dotty
case Outcome.Canceled() =>
  MonadCancel[F].canceled >> Spawn[F].never[A]
```

Что это даёт:

- Сначала пытаемся отменить текущее волокно (`canceled`).
- Если отмена разрешена — текущее волокно остановится.
- Если текущее волокно защищено `uncancelable` — `canceled` ничего не сделает, и мы перейдём к `never`, который зависнет навсегда.

> Именно эту логику реализует `joinWithNever` из примера ранее.

### Стратегия 3: Вернуть значение по умолчанию (через `Option`)

Самый практичный подход — использовать `Option[A]`:

```dotty
def joinToOption[F[_]: MonadThrow, A](
    fiber: Fiber[F, Throwable, A]
)(using MonadCancel[F, Throwable]): F[Option[A]] =
  fiber.join.flatMap:
    case Outcome.Succeeded(fa) =>
      fa.map(Some(_))

    case Outcome.Errored(e) =>
      MonadThrow[F].raiseError(e)

    case Outcome.Canceled() =>
      MonadCancel[F].canceled.as(None)
```

**Преимущества:**

- Чёткое разделение: `None` означает отмену, `Some(a)` — успех.
- Тип `Option[A]` честно отражает возможность "отсутствия результата".
- Легко комбинировать с `orElse`, `getOrElse`, `fold`.

**Недостаток:** Если вам принципиально нужен именно `A` (например, для дальнейшего `flatMap` без лишних обёрток), придётся делать `getOrElse`.

## Реальный пример: параллельные запросы с отказоустойчивостью

```dotty
import cats.effect.IO
import cats.syntax.all.*
import scala.concurrent.duration.*

case class User(id: String, name: String)
case class Order(id: String, amount: Double)

def fetchUser(userId: String): IO[User] = IO.sleep(1.second) >>
  IO.pure(User(userId, "Alice"))
def fetchOrders(userId: String): IO[List[Order]] = IO.sleep(2.seconds) >>
  IO.pure(List(Order("1", 100.0)))

def loadUserData(userId: String): IO[(User, List[Order])] =
  for
    fiberUser   <- fetchUser(userId).start
    fiberOrders <- fetchOrders(userId).start

    // Ждём оба результата, но обрабатываем отмену через Option
    userOpt   <- joinToOption(fiberUser)
    ordersOpt <- joinToOption(fiberOrders)

    // Комбинируем, обрабатывая возможные отмены
    result <- (userOpt, ordersOpt) match
      case (Some(user), Some(orders)) => IO.pure((user, orders))
      case (None, Some(orders))       => IO.raiseError(
          new Exception("User fetch was cancelled, but orders are ready")
        )
      case (Some(user), None) => IO.raiseError(
          new Exception("Orders fetch was cancelled, but user is ready")
        )
      case (None, None) =>
        IO.raiseError(new Exception("Both fetches were cancelled"))
  yield result
```

**Что здесь важно:** Мы не теряем информацию об отмене — каждый `None` сигнализирует, 
что соответствующее волокно было отменено до завершения.

## Продвинутый паттерн: гонка (racing) через `join`

```dotty
def race[F[_]: {Spawn, MonadThrow}, A, B](
    fa: F[A],
    fb: F[B]
)(using MonadCancel[F, Throwable]): F[Either[A, B]] =
  for
    fiberA <- fa.start
    fiberB <- fb.start

    // Ждём первого завершившегося
    result <- fiberA.join.race(fiberB.join).flatMap {
      case Left(outcomeA) =>
        // A завершился первым → отменяем B
        fiberB.cancel >> outcomeToResult(outcomeA).map(Left(_))

      case Right(outcomeB) =>
        // B завершился первым → отменяем A
        fiberA.cancel >> outcomeToResult(outcomeB).map(Right(_))
    }
  yield result

def outcomeToResult[F[_]: MonadThrow, A](outcome: Outcome[F, Throwable, A])
    : F[A] =
  outcome match
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e)    => MonadThrow[F].raiseError(e)
    case Outcome.Canceled()    =>
      MonadThrow[F].raiseError(new Exception("Race cancelled"))
```

**Этот код реализует классическую "гонку" двух задач:**

- Какая завершится первой — та и побеждает.
- Проигравшая задача немедленно отменяется.
- Если победитель упал с ошибкой — ошибка пробрасывается.

Cats Effect предоставляет встроенный `IO.race`, но этот пример показывает, 
как низкоуровневые `join` и `Outcome` позволяют строить сложную конкурентную логику.

## Семантика `joinWithNever`: когда она подходит?

```dotty
def joinWithNever[F[_]: Spawn, A](fiber: Fiber[F, Throwable, A])(using
    MonadCancel[F, Throwable]
)
    : F[A] =
  fiber.join.flatMap:
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e)    => MonadThrow[F].raiseError(e)
    case Outcome.Canceled()    => MonadCancel[F].canceled >> Spawn[F].never[A]
```

**Поведение при отмене дочернего волокна:**

1. Пытаемся отменить текущее волокно.
2. Если отмена удалась — текущее волокно останавливается.
3. Если отмена не удалась (мы внутри `uncancelable`) — зависаем навсегда (`never`).

**Когда это полезно:**

- Вы уверены, что никогда не окажетесь в неотменяемом контексте.

**Когда это опасно:**

- Внутри `uncancelable` вы зависнете навсегда — это может быть эквивалентно утечке ресурса.
- Если вы не понимаете последствий, проще использовать `joinToOption` или явную обработку.

## Итог

Таблица решений для `Outcome.Canceled()`

| Ваша потребность                                   | Рекомендуемая стратегия                    | Код                                                       |
|----------------------------------------------------|--------------------------------------------|-----------------------------------------------------------|
| Отмена — это ошибка                                | `raiseError`                               | `MonadThrow[F].raiseError(new Exception("Cancelled"))`    |
| Отменить себя + зависнуть, если нельзя             | `canceled >> never` (как `joinWithNever`)  | `MonadCancel[F].canceled >> Spawn[F].never[A]`            |
| Использовать значение по умолчанию (но осторожно)  | `canceled.as(default)`                     | `MonadCancel[F].canceled.as(defaultValue)`                |
| Чётко различать success/error/cancel               | `Option` или `Either`                      | `canceled.as(None)` или `canceled.as(Left(cancelError))`  |
| Перезапустить задачу при отмене                    | `join` + цикл                              | `fiber.join.flatMap { case Canceled() => retry }`         |

1. **`Fiber#join` возвращает `Outcome`** — три варианта: успех, ошибка, отмена.
2. **`Outcome.Succeeded` содержит не значение, а эффект** — требует дополнительного `flatMap`.
3. **`Outcome.Canceled()` — самый сложный случай** — требует явного решения для каждой предметной области.
4. **`joinWithNever` — удобный, но не универсальный** — подходит только когда "зависание" при отмене приемлемо.
5. **Использование `Option` или `Either`** — наиболее прагматичный способ обработать отмену.
6. **Вы можете строить сложные конкурентные паттерны** (гонки, ограниченный параллелизм, таймауты) на основе низкоуровневого `join`.

Главный вывод: Cats Effect даёт вам **выбор**. В отличие от Java-потоков, где `join()` скрывает все детали, 
здесь вы контролируете реакцию на успех, ошибку и отмену. 
Это мощь, но и ответственность — выбирайте стратегию осознанно под вашу задачу.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect MonadCancel](https://typelevel.org/cats-effect/docs/typeclasses/monadcancel)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)

[doc]: https://typelevel.org/cats-effect/docs/concepts

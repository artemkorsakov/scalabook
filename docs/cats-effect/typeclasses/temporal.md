# Temporal — работа со временем и таймаутами

`Temporal[F[_]]` — это класс типов, который **расширяет `Concurrent[F[_]]`** 
и добавляет возможность **приостанавливать выполнение волокна на определённое время** без блокировки потоков операционной системы.

Проще говоря, `Temporal` — это "часы с будильником" для ваших эффектов. 
Он позволяет организовать задержки, таймауты и другие временны́е операции в чисто функциональном, неблокирующем и отменяемом стиле.

### Простейший пример

```dotty
import cats.effect.Temporal
import scala.concurrent.duration.*

def myProgram[F[_]: Temporal]: F[Unit] =
  Temporal[F].sleep(5.seconds) // ← приостановка на 5 секунд
```

Этот код выглядит так же просто, как `Thread.sleep(5000)`, но работает **радикально иначе** под капотом.

## Почему нельзя использовать `Thread.sleep`?

Попытаемся реализовать задержку так:

```dotty
// ❌ ОЧЕНЬ ПЛОХАЯ ИДЕЯ
import cats.effect.Sync

def badDelay[F[_]: Sync]: F[Unit] =
  Sync[F].delay(Thread.sleep(5.seconds.toMillis))
```

**Почему это категорически неправильно?**

| Проблема                          | Объяснение                                                                                                                                                                            |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Блокирует поток ОС**            | `Thread.sleep` останавливает целый системный поток, на котором выполняется волокно.                                                                                                   |
| **Убивает масштабируемость**      | Если у вас 1000 одновременно "спящих" задач, `Thread.sleep` потребует 1000 потоков ОС (→ перерасход памяти, частые переключения контекста).                                           |
| **Нарушает работу планировщика**  | Cats Effect использует ограниченный пул потоков (обычно равен числу ядер CPU). `Thread.sleep` "забирает" один из этих драгоценных потоков, делая его недоступным для других волокон.  |
| **Устойчивость к отмене**         | `Thread.sleep` не реагирует на сигналы отмены Cats Effect. Отменить такое волокно практически невозможно.                                                                             |

**Правильный подход — `Temporal[F].sleep`:**

```dotty
// ✅ ПРАВИЛЬНО
import cats.effect.Temporal

def goodDelay[F[_]: Temporal]: F[Unit] =
  Temporal[F].sleep(5.seconds)
```

`sleep` семантически приостанавливает **волокно**, но не трогает поток ОС. 
Планировщик просто убирает это волокно из очереди выполнения и планирует "пробуждение" через системный таймер. 
Поток тем временем может выполнять другие волокна.

## Базовые методы `Temporal`

Хотя полный интерфейс `Temporal` включает несколько методов, достаточно понимать два основных:

### sleep

Приостанавливает текущее волокно на заданное время (или **до отмены**).

```dotty
def sleep(duration: FiniteDuration): F[Unit]
```

**Важные свойства:**

- **Не блокирует поток** — планировщик Cats Effect убирает волокно и возвращается к нему через указанное время.
- **Отменяемость** — если волокно, спящее внутри `sleep`, отменить, оно проснётся с ошибкой отмены.
- **Точность** — зависит от платформы и планировщика, но для большинства приложений достаточно точна.

### timeout

Выполняет эффект `fa`, но если он не завершается за отведённое время, **принудительно отменяет его** и возвращает ошибку таймаута.

```dotty
def timeout[A](fa: F[A], duration: Duration): F[A]
```

Это, пожалуй, самый часто используемый метод `Temporal`.

## Глубокое погружение: как работает `sleep` под капотом?

Упрощённо, внутри `sleep` происходит следующее:

1. Создаётся специальный `Deferred[F, Unit]` — "сигнал пробуждения".
2. Планировщику Cats Effect отправляется запрос: "Разбуди меня через `duration`".
3. Планировщик добавляет задачу в таймер-коллекцию (например, `HashedWheelTimer` на JVM).
4. Волокно переводится в "спящий" режим — оно больше не занимает поток.
5. Когда таймер срабатывает, планировщик получает уведомление и перезапускает волокно.

**Если волокно отменяют во время сна:**

- Планировщику отправляется команда отменить таймер.
- Волокно получает сигнал отмены и может выполнить финализацию.

Это всё происходит **асинхронно** и без блокировки потоков.

## Практические примеры

### Пример 1: Таймаут для внешнего сервиса (timeoutTo)

```dotty
import cats.effect.Temporal
import scala.concurrent.duration.*

def fetchFromRemoteService[F[_]: Temporal]: F[String] =
  // имитация медленного сервиса
  Temporal[F].sleep(5.seconds) >> Temporal[F].pure("результат")

def cachedValue[F[_]: Temporal]: F[String] =
  Temporal[F].pure("кешированный ответ")

val data: IO[String] = fetchFromRemoteService[IO]
  .timeoutTo(2.seconds, cachedValue) // ← ждём 2 секунды, потом берём кеш

// Через 2 секунды вернётся "кешированный ответ"
```

### Пример 2: Гонка с таймаутом

```dotty
def fetchWithFallback[F[_]: Temporal, A](
    primary: F[A],
    fallback: F[A],
    timeout: FiniteDuration
): F[A] =
  Temporal[F].race(primary, Temporal[F].sleep(timeout) >> fallback).flatMap {
    case Left(result)  => Temporal[F].pure(result) // успех вовремя
    case Right(result) => Temporal[F].pure(result) // fallback сработал
  }

fetchWithFallback[IO, String](
  IO.sleep(2.seconds).as("проигравший"),
  IO.pure("fallback"),
  1.second
).unsafeRunSync() 
// val res0: String = "fallback"
```

## `timeout` vs `timeoutTo` vs `race`

| Метод                                  | Что делает                                                | Тип возврата       |
|----------------------------------------|-----------------------------------------------------------|--------------------|
| `timeout(dur)`                         | Отменяет `fa` по таймауту, выбрасывает `TimeoutException` | `F[A]`             |
| `timeoutTo(dur, fallback)`             | Отменяет `fa` по таймауту, выполняет `fallback`           | `F[A]`             |
| `race(fa, Temporal.sleep(dur) >> fb)`  | Первый завершившийся побеждает                            | `F[Either[A, B]]`  |


## Полный пример: надёжный клиент с таймаутами и повторами

```dotty
import cats.effect.{IO, IOApp, Temporal}

import scala.concurrent.duration.*

object ResilientClient extends IOApp.Simple:
  val run: IO[Unit] = fetchWithRetry(3, 500.millis)
    .flatMap(result => IO.println(s"Успех! Данные: $result"))
    .handleErrorWith(err => IO.println(s"Крайняя неудача: ${err.getMessage}"))

  private def fetchWithRetry(attempts: Int, delay: FiniteDuration): IO[String] =
    def loop(remaining: Int): IO[String] =
      fetchData(attempts - remaining + 1)
        .timeout(1.second)
        .handleErrorWith { error =>
          if remaining > 1 then Temporal[IO].sleep(delay) >> loop(remaining - 1)
          else IO.raiseError(new Exception("Все попытки провалились"))
        }
    loop(attempts)

  private def fetchData(attempt: Int): IO[String] =
    IO.println(s"Попытка №$attempt...") >>
      IO.sleep(2.seconds) >> // имитация работы
      IO.raiseError(new Exception("Ошибка сети"))

end ResilientClient
```

**Что здесь происходит:**

- Каждая попытка содержит таймаут 1 секунду.
- Если попытка упала или истекло время, ждём `500ms` и повторяем.
- Всего 3 попытки.

```text
Попытка №1...
Попытка №2...
Попытка №3...
Крайняя неудача: Все попытки провалились
```

## Итог

`Temporal` даёт вам **чистый, композируемый и безопасный способ** работы со временем в функциональных конкурентных программах.

Три концепции:

1. **`sleep`** — неблокирующая задержка для волокна.
2. **`timeout`** — ограничение времени выполнения эффекта.
3. **Отмена** — всё, что внутри `Temporal`, прекрасно сочетается с системой отмены Cats Effect.

Благодаря `Temporal` вы можете писать код с задержками и таймаутами, 
который масштабируется до миллионов одновременно ожидающих волокон, но при этом остаётся простым и читаемым.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Temporal](https://typelevel.org/cats-effect/docs/typeclasses/temporal)

[doc]: https://typelevel.org/cats-effect/docs/concepts

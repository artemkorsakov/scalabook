{%
showPrev = true
prevPage = hotswap.html
showNext = true
nextPage = console.html
%}

# Dispatcher — Мост между миром эффектов и нечистым кодом

## Мотивация: проблема выполнения эффектов в колбэках

В идеальном мире весь ваш код написан в стиле `F[_]` (например, `IO`), и вы никогда не сталкиваетесь с побочными эффектами. 
Но реальность такова, что многие библиотеки (GUI-фреймворки, веб-сокеты, базы данных, сообщения из очередей) 
предоставляют **императивный API** на основе колбэков:

```dotty
abstract class ImpureInterface:
  def onMessage(msg: String): Unit // вызывается внешним миром
  def init(): Unit = onMessage("init")
```

Вам нужно реализовать `onMessage` и выполнить в нём **эффект** (например, положить сообщение в `Queue`). 
Прямой вызов `queue.offer(msg)` не сработает, потому что он возвращает `IO[Unit]` — **описание** эффекта, а не его выполнение.

```dotty
// ❌ НЕЛЬЗЯ ТАК: IO инкапсулирует эффект, но не выполняет его
new ImpureInterface:
  override def onMessage(msg: String): Unit =
    queue.offer(msg) // возвращает IO[Unit], который просто "висит" в воздухе
```

В обычных условиях мы бы использовали `IO.println(msg).unsafeRunSync()`, но это **крайне опасно**:

- Блокирует поток
- Нарушает асинхронную модель Cats Effect
- Может привести к взаимоблокировкам и утечкам волокон

Здесь на помощь приходит `Dispatcher`.

## Что такое `Dispatcher`?

`Dispatcher[F[_]]` — это утилита на основе [`Supervisor`](supervisor.md), 
которая позволяет **безопасно выполнять эффекты `F` на границе с нечистым кодом**. 
Он предоставляет методы, которые получают эффект `F[A]` и **запускают его** в контролируемой среде 
(в отдельном волокне под надзором), возвращая результат в императивном стиле.

## Создание `Dispatcher`

`Dispatcher` создаётся как `Resource`, что гарантирует корректное завершение всех запущенных через него волокон при закрытии:

```dotty
import cats.effect.IO
import cats.effect.std.Dispatcher

val resource: Resource[IO, Dispatcher[IO]] = Dispatcher.sequential[IO]
```

Есть два основных режима работы:

| Метод создания              | Поведение                                                                                |
|:----------------------------|:-----------------------------------------------------------------------------------------|
| `Dispatcher.sequential[F]`  | Запускает все эффекты в одном выделенном потоке (порядок гарантирован, FIFO).            |
| `Dispatcher.parallel[F]`    | Запускает эффекты параллельно, используя пул потоков (максимальная производительность).  |

> Для большинства случаев (обработка колбэков, вставка в очередь) подойдёт `sequential`.
> Для высоконагруженных систем, где колбэки могут приходить одновременно, лучше использовать `parallel`.

## Методы `Dispatcher`

```dotty
trait Dispatcher[F[_]] extends DispatcherPlatform[F]:

  /** Возвращает (Future[A], отменяющая функция). Самый гибкий метод. */
  def unsafeToFutureCancelable[A](fa: F[A]): (Future[A], () => Future[Unit])

  /** Запускает эффект, возвращает Future[A] (без явной отмены). */
  def unsafeToFuture[A](fa: F[A]): Future[A] =
    unsafeToFutureCancelable(fa)._1

  /** Запускает эффект, возвращает функцию отмены. */
  def unsafeRunCancelable[A](fa: F[A]): () => Future[Unit] =
    unsafeToFutureCancelable(fa)._2

  /** Запускает эффект и забывает о результате (fire-and-forget). Неотменяем.
    */
  def unsafeRunAndForget[A](fa: F[A]): Unit

  // Доступно только на JVM:
  // Блокирует текущий поток до завершения эффекта!
  def unsafeRunSync[A](fa: F[A]): A 
end Dispatcher
```

### Таблица выбора метода

| Задача                             | Метод                       | Возвращает             | Отменяем?                                                |
|:-----------------------------------|:----------------------------|:-----------------------|:---------------------------------------------------------|
| "Запустил и забыл"                 | `unsafeRunAndForget`        | `Unit`                 | ❌ Нет                                                    |
| Нужен результат, но не блокировка  | `unsafeToFuture`            | `Future[A]`            | ❌ Нет (но `Future` можно отменить через `Future.cancel`) |
| Нужно вручную отменить             | `unsafeRunCancelable`       | `() => Future[Unit]`   | ✅ Да                                                     |
| Нужен результат и отмена           | `unsafeToFutureCancelable`  | `(Future[A], отмена)`  | ✅ Да                                                     |
| Блокирующий вызов (только тесты)   | `unsafeRunSync`             | `A`                    | ❌ Нет (блокирует поток!)                                 |

> **Осторожно:** `unsafeRunSync` блокирует поток ОС! Используйте его **только** в тестах 
> или демонстрационных примерах, но **никогда** в продакшен-коде.

## Интеграция с императивным интерфейсом

Вернёмся к задаче: реализовать `ImpureInterface`, который будет реально выполнять `queue.offer(msg)`.

### Без `Dispatcher` — не работает

```dotty
import cats.effect.IO
import cats.effect.std.Queue

val program =
  for
    queue <- Queue.unbounded[IO, String]
    impureInterface = new ImpureInterface:
      override def onMessage(msg: String): Unit =
        queue.offer(msg) // ❌ IO[Unit] просто игнорируется!
    _     <- IO.delay(impureInterface.init()) // вызывает onMessage("init")
    value <- queue.tryTake
    _     <- IO.println(s"Результат: $value") // Всегда None!
  yield ()
```

### С `Dispatcher` — работает

```dotty
import cats.effect.IO
import cats.effect.std.{Dispatcher, Queue}

Dispatcher.sequential[IO].use { dispatcher =>
  for
    queue           <- Queue.unbounded[IO, String]
    impureInterface <- IO.delay {
      new ImpureInterface:
        override def onMessage(msg: String): Unit =
          dispatcher.unsafeRunSync(queue.offer(msg))
        // ✅ Эффект ВЫПОЛНЕН
    }
    _     <- IO.delay(impureInterface.init())
    value <- queue.tryTake
    _     <- IO.println(s"Результат: $value") // Some("init")!
  yield ()
}
```

**Что изменилось?**

- `dispatcher.unsafeRunSync(queue.offer(msg))` **запускает** `IO`.
- Сообщение `"init"` попадает в очередь.
- Мы забираем его через `tryTake`.

## Важные нюансы и предостережения

### 1. `Dispatcher` — не бесплатный

Создание `Dispatcher` дешёвое, но не бесплатное (он создаёт внутренний `Supervisor` и пул потоков). 
Рекомендуется создавать его там, где он нужен, и не делать глобальным синглтоном.

### 2. Ошибки в `unsafeRunAndForget` и `unsafeRunCancelable`

Если эффект, запущенный через `unsafeRunAndForget`, завершится с ошибкой, 
она будет **проигнорирована** (логирование — на усмотрение реализации). 
Для обработки ошибок используйте `unsafeToFuture` и работайте с `Future.failed`.

### 3. Отмена ≠ освобождение ресурсов

Отмена через `cancel` отправляет сигнал отмены волокну, но не гарантирует немедленной остановки. 
Эффекты внутри могут выполняться дольше, но `bracket` и `uncancelable` будут соблюдены.

### 4. `unsafeRunSync` — только для тестов

Этот метод **блокирует текущий поток** и предназначен исключительно для тестирования и REPL. 
В продакшен-коде его использование может привести к deadlock и ужасной производительности.

### 5. Закрытие `Dispatcher` (через `Resource.use`) отменяет все запущенные эффекты

Когда вы выходите из `use` блока, `Dispatcher` вызывает `supervisor.cancel` для всех запущенных волокон. 
Это важно для предотвращения утечек.

## `Dispatcher` в сравнении с альтернативами

| Механизм                          | Назначение                             | Плюсы                             | Минусы                                   |
|:----------------------------------|:---------------------------------------|:----------------------------------|:-----------------------------------------|
| **`Dispatcher`**                  | Выполнение эффектов из нечистого кода  | Безопасно, управляемо, отменяемо  | Требует `Resource` (не вечный)           |
| `unsafeRunSync`                   | Запуск `IO` в тестах                   | Простота                          | Блокирует поток, опасен в продакшене     |
| `IO.println(...).unsafeRunAsync`  | "Изобразить выполнение"                | Быстро                            | Нет управления отменой, трудно отладить  |

## Итог: когда использовать `Dispatcher`?

✅ **Используйте `Dispatcher`, если:**

- Вы интегрируетесь с **императивным API** (колбэки, слушатели, GUI-события, Java-библиотеки).
- Вам нужно **выполнить эффект** `F` в ответ на внешний вызов.

❌ **Не используйте `Dispatcher`, если:**

- Вся ваша программа уже внутри `F` (просто комбинируйте эффекты).
- Вам нужен только один `unsafeRunSync` в тесте — проще использовать `unsafeRunSync()` на самом `IO`.

**Ключевая идея:** `Dispatcher` — это контролируемый, безопасный 
и мощный "люк" из чистого функционального мира в мир императивных колбэков. 
Он позволяет использовать все преимущества Cats Effect и при этом работать с библиотеками, 
которые не были написаны в функциональном стиле.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Dispatcher](https://typelevel.org/cats-effect/docs/std/dispatcher)

[doc]: https://typelevel.org/cats-effect/docs/concepts

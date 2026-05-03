{%
showPrev = true
prevPage = sync.html
showNext = true
nextPage = concurrent.html
%}

# Spawn — создание и управление волокнами

`Spawn[F[_]]` (или `GenSpawn[F[_], E]` с явным типом ошибки) — это **класс типов**, 
который представляет собой фундамент параллельного программирования в Cats Effect. 
Он предоставляет возможность **порождать легковесные волокна** и управлять ими.

В экосистеме Cats Effect иерархия классов типов выглядит так:

```
MonadError[F, E]
    ↓
MonadCancel[F, E]    ← можно отменять вычисления
    ↓
Spawn[F, E]          ← можно создавать параллельные волокна
    ↓
Concurrent[F, E]     ← добавляются примитивы синхронизации (Ref, Deferred)
    ↓
Temporal[F, E]       ← возможностью приостанавливать выполнение потока
    ↓
Async[F]             ← работа с callback-based API
```

**Ключевая идея:** `Spawn` — это минимальный набор операций, необходимый для **конкурентного выполнения эффектов**. 
Всё, что требует параллелизма — `start`, `race`, операции вроде `parTraverse` — в конечном счёте опирается на `Spawn`.

## Полная сигнатура класса типов

```dotty
import cats.effect.*

trait Spawn[F[_], E] extends MonadCancel[F, E], Unique[F]:

  /** Запускает эффект `fa` в новом волокне. Возвращает `Fiber`, позволяющий
    * управлять этим волокном.
    */
  def start[A](fa: F[A]): F[Fiber[F, E, A]]

  /** Эффект, который никогда не завершается (висящий). Полезен для тестов,
    * fallback-веток, реализации бесконечных серверов.
    */
  def never[A]: F[A]

  /** "Передача управления" — добровольно уступает процессор планировщику.
    * Позволяет другим волокнам выполниться и является точкой отмены.
    */
  def cede: F[Unit]

  /** Гонка между двумя эффектами с доступом к проигравшему волокну.
    * Низкоуровневый примитив для построения сложных конкурентных операций.
    */
  def racePair[A, B](fa: F[A], fb: F[B]): F[Either[
    (Outcome[F, E, A], Fiber[F, E, B]), // Победил fa
    (Fiber[F, E, A], Outcome[F, E, B])  // Победил fb
  ]]
end Spawn
```

> **Примечание:** В Cats Effect 3 тип ошибки `E` обычно фиксирован как `Throwable`: `type Spawn[F[_]] = GenSpawn[F, Throwable]`

## Метод `start` — создание волокна

```dotty
def start[A](fa: F[A]): F[Fiber[F, E, A]]
```

1. Вызов `start` **не выполняет** `fa` немедленно.
2. Он создаёт новое волокно и связывает его с пулом потоков планировщика Cats Effect.
3. Волокно начинает выполняться **одновременно** с текущим волокном — без блокировки вызывающего кода.
4. Возвращается объект `Fiber`, который можно использовать для:
    - Ожидания результата (`.join`)
    - Отмены выполнения (`.cancel`)
    - Проверки состояния (`.poll`)

### Простой пример

```dotty
import cats.effect.*
import scala.concurrent.duration.*

object StartExample extends IOApp.Simple:
  def run: IO[Unit] =
    for
      // Запускаем фоновую задачу
      fiber <- IO.println("Фоновая работа...").foreverM.start

      // Основная работа
      _ <- IO.println("Основная работа")
      _ <- IO.sleep(2.seconds)

      // Останавливаем фоновую задачу
      _ <- fiber.cancel
      _ <- IO.println("Готово")
    yield ()
```

**Вывод:**

```
Основная работа
Фоновая работа...
...
...
...
Фоновая работа...
Готово
```

| Свойство           | Описание                                                                 |
|--------------------|--------------------------------------------------------------------------|
| **Легковесность**  | Можно создавать миллионы волокон без проблем с памятью                   |
| **Отменяемость**   | Волокно можно отменить в любой безопасной точке                          |
| **Неблокирующий**  | `start` не ждёт выполнения `fa` — возвращается мгновенно                 |
| **Планирование**   | Волокно будет запланировано на ближайшей доступной ёмкости пула потоков  |

## Метод `never` — бесконечное ожидание

```dotty
def never[A]: F[A]
```

`never` — это эффект, который **никогда не завершается**. Он может быть:

- Отменён извне (тогда завершится с `Outcome.Canceled`)
- Бесконечно ждать, игнорируя все сигналы (если защищён `uncancelable`)

### `never` vs `sleep(Duration.Inf)`

| Аспект             | `never`                     | `sleep(Inf)`           |
|--------------------|-----------------------------|------------------------|
| **Поведение**      | Никогда не завершается      | Ждёт бесконечность     |
| **Семантика**      | Чистое "висящее" состояние  | Ожидание таймера       |
| **Отмена**         | Да, в точках отмены         | Да, при пробуждении    |
| **Ресурсы**        | Ничего не выделяет          | Может выделять таймер  |
| **Использование**  | Fallback, заглушки          | Таймауты, задержки     |

## Метод `cede` — добровольная передача управления

```dotty
def cede: F[Unit]
```

`cede` (от лат. "уступать") — это **кооперативная точка переключения контекста**:

1. Текущее волокно **добровольно** уступает процессор планировщику.
2. Планировщик может выбрать для выполнения другое готовое волокно.
3. Если других готовых волокон нет, текущее волокно продолжит выполнение сразу.

### Когда использовать `cede`?

```dotty
// 1. В CPU-интенсивных циклах — для отзывчивости
def cpuHeavy[F[_]: {Spawn, MonadCancelThrow}](n: Int): F[Unit] =
  def loop(i: Int): F[Unit] =
    if i >= n then MonadCancelThrow[F].unit
    else
      // Позволяем другим волокнам поработать и проверяем отмену
      Spawn[F].cede >> computeChunk(i).flatMap(_ => loop(i + 1))
  loop(0)

// 2. Для справедливого планирования
def fairLoop[F[_]: Spawn](items: List[String]): F[Unit] =
  items.traverse_ { item =>
    for
      _ <- processItem(item) // обработка
      _ <- Spawn[F].cede     // даём шанс другим волокнам
    yield ()
  }

// 3. В бесконечных циклах — чтобы избежать "голодания"
def heartbeat[F[_]: {Spawn, Temporal}]: F[Unit] =
  (Spawn[F].cede >> Temporal[F].sleep(1.second) >> heartbeat).foreverM
```

### `cede` как точка отмены

Важное свойство: `cede` **является точкой отмены**. 
Если волокно получило сигнал отмены, при вызове `cede` оно будет остановлено.

```dotty
def cancelableLoop[F[_]: Spawn]: F[Unit] =
  def loop(): F[Unit] =
    Spawn[F].cede >> heavyComputation() *> loop()

  loop()    
```

## Метод `racePair` — низкоуровневая гонка

```dotty
def racePair[A, B](fa: F[A], fb: F[B]): F[Either[
  (Outcome[F, E, A], Fiber[F, E, B]),
  (Fiber[F, E, A], Outcome[F, E, B])
]]
```

### Зачем нужен `racePair`, если есть `race`?

`race` (который строится на `racePair`) возвращает только **результат победителя**. `racePair` даёт **доступ к проигравшему волокну** — это позволяет:

- Отменить проигравшего (по умолчанию)
- Продолжить его выполнение в фоне
- Получить его результат позже
- Выполнить финализацию с доступом к `Outcome`

### Пример: гонка с сохранением проигравшего

```dotty
import cats.MonadThrow
import cats.effect.*

def raceWithLoser[A, B](
    taskA: IO[A],
    taskB: IO[B]
): IO[Either[(A, Fiber[IO, Throwable, B]), (Fiber[IO, Throwable, A], B)]] =
  Spawn[IO].racePair(taskA, taskB).flatMap:
    case Left((outcomeA, fiberB)) =>
      // Победила A — извлекаем результат
      outcomeToResult(outcomeA).map(a => Left((a, fiberB)))

    case Right((fiberA, outcomeB)) =>
      // Победила B
      outcomeToResult(outcomeB).map(b => Right((fiberA, b)))

def outcomeToResult[F[_]: MonadThrow, A](outcome: Outcome[F, Throwable, A])
    : F[A] =
  outcome match
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e)    => MonadThrow[F].raiseError(e)
    case Outcome.Canceled()    =>
      MonadThrow[F].raiseError(new Exception("Race cancelled"))
```

### Реализация `race` через `racePair`

```dotty
def race[F[_]: {Spawn, MonadThrow}, A, B](fa: F[A], fb: F[B]): F[Either[A, B]] =
  Spawn[F].racePair(fa, fb).flatMap:
    case Left((outcomeA, fiberB)) =>
      fiberB.cancel >> outcomeToResult(outcomeA).map(Left(_))

    case Right((fiberA, outcomeB)) =>
      fiberA.cancel >> outcomeToResult(outcomeB).map(Right(_))
```

## Законы `Spawn`

Любая корректная реализация `Spawn` должна удовлетворять законам:

1. **Закон `start`**:

   ```dotty
   start(fa).flatMap(_.join) <-> MonadCancel[F].uncancelable(_ => fa)
   ```

   Запустить волокно и сразу же присоединиться к нему эквивалентно выполнению `fa` в неотменяемом блоке.

2. **Закон `never`**:

   ```dotty
   race(never[A], fa) <-> fa.map(Right(_))
   race(fa, never[A]) <-> fa.map(Left(_))
   ```

   Гонка с `never` даёт победить другому участнику.

3. **Закон `cede`**:

   ```dotty
   cede >> fa <-> fa >> cede <-> fa
   ```

   `cede` — это "единица" в [моноиде](../../typeclass/monoid/monoid.md) переключений контекста (идемпотентно и не меняет семантику).

4. **Ассоциативность `racePair`**: сложный закон, гарантирующий, что гонки можно компоновать.

## Итог: что даёт `Spawn`?

| Возможность                        | Как выражена                          |
|------------------------------------|---------------------------------------|
| **Конкурентное выполнение**        | `start` и `race`                      |
| **Управление жизненным циклом**    | `Fiber` с методами `join`, `cancel`   |
| **Кооперативная многозадачность**  | `cede` для передачи управления        |
| **Бесконечные процессы**           | `never` для "вечно висящих" задач     |
| **Низкоуровневый контроль гонок**  | `racePair` с доступом к проигравшему  |

**Главная идея:** `Spawn` — это "минимальный параллельный примитив", 
достаточный для построения любого параллельного кода.

В реальных приложениях вы редко вызываете `Spawn[F].start` напрямую — чаще используете `parTraverse`, `race`, `timeout` 
и другие высокоуровневые комбинаторы, которые внутри себя полагаются на `Spawn`.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Spawn](https://typelevel.org/cats-effect/docs/typeclasses/spawn)

[doc]: https://typelevel.org/cats-effect/docs/concepts

{%
showPrev = true
prevPage = tracing.html
showNext = true
nextPage = fiber-dumps.html
%}

# Тестирование времени выполнения

## Проблема: как тестировать асинхронный и недетерминированный код

В идеальном мире большую часть бизнес-логики приложения можно выделить в [_чистые_ функции (pure functions)](../fp/fp/pure_function.md), не связанные с `IO`. 
Их легко тестировать обычными модульными тестами: передал входные данные — проверил выходные. 
Но реальный мир сложнее. Часто приходится писать тесты для логики, которая **неразрывно связана с эффектами**:

- Функции, которые читают файлы или ходят по сети.
- Код, который работает параллельно (race-условия, гонки).
- Логика, которая **работает со временем**: таймауты, повторные попытки (retries), задержки, планирование.

Именно последний пункт — работа со временем — создаёт наибольшие трудности.

### Почему тестировать время сложно?

**Две главные причины:**

1. **Неточность (Imprecision):** Время по своей природе неточно. `IO.realTime` при каждом запуске будет давать разные значения. 
   Даже `IO.monotonic` — более стабильный, но не идеальный. JVM — не система реального времени, 
   операционные системы (даже Linux) — не ОС реального времени, 
   а оборудование почти никогда не предназначено для детерминированного выполнения. 
   Это означает, что тесты, которые прямо или косвенно измеряют время, должны иметь **допустимый запас погрешности (error margin)**. 
   Иначе они будут часто падать, особенно на нагруженных CI-серверах.
2. **Медлительность (Slowness):** `IO.sleep(duration)` — буквально означает "усни". 
   Если тестируемая функция засыпает на час, тест будет идти **как минимум час**. 
   Поэтому на практике тесты используют очень короткие интервалы (например, `100.millis`). 
   Фреймворки вроде [MUnit](https://scalameta.org/munit/) и [Specs2](https://etorreborre.github.io/specs2/) 
   запускают тесты параллельно, чтобы сгладить эту проблему, но даже короткие задержки накапливаются, делая тесты медленными. 
   К тому же, это усугубляет **неточность**: при коротких задержках погрешность может быть соизмерима с самой задержкой.

Это крайне нежелательно. Но есть решение.

### Решение: `TestControl` — симуляция времени

К счастью, `IO` и его класс типов `Temporal` кодируют все временные примитивы (`IO.realTime`, `IO.monotonic`, `IO.sleep`) в виде эффектов. 
Большинство программ экосистемы Cats Effect используют именно эти функции, а не прямые системные вызовы. 
Поэтому Cats Effect может предложить элегантное решение: **`TestControl`**.

> **Важно:** Если ваша функция использует `IO`, но НЕ работает с временем, `TestControl` вам, скорее всего, не нужен. 
> Используйте стандартные библиотеки тестирования: [Weaver test](https://typelevel.org/weaver-test/overview/installation.html), 
> [MUnit Cats Effect](https://github.com/typelevel/munit-cats-effect) 
> или [Cats Effect Testing](https://github.com/typelevel/cats-effect-testing).

Для использования `TestControl` добавьте зависимость:

```dotty
libraryDependencies += "org.typelevel" %% "cats-effect-testkit" % "3.7.0" % Test
```

## Что такое `TestControl`?

`TestControl` — это **симулированная (mock) среда выполнения** для `IO`. 
Она полностью реализует всю функциональность `IO`: волокна, асинхронные колбэки и **поддержку времени**. 
Но у этой симуляции есть три важных отличия от стандартной `IORuntime`:

### 1. Однопоточность и недетерминизм

`TestControl` использует **только один поток** для выполнения всех `IO` (как в JavaScript). 
Это позволяет выявлять ошибки параллелизма (data races), маскируя их? Но на самом деле `TestControl` **не детерминирован**. 
В отличие от реальной production-среды (даже однопоточной), `TestControl` **случайным образом** выбирает, 
какое из готовых к выполнению волокон запустить следующим. 
Это помогает выявлять тонкие ошибки, связанные с последовательностью выполнения, 
которые в реальной среде могут воспроизводиться раз в тысячу запусков.

### 2. Управляемое время (Controlled Time)

Внутреннее представление времени в `TestControl` изменяется **только по команде извне**. 
Все три временны'е функции — `realTime`, `monotonic` и `sleep` — ведут себя согласованно в рамках этой симуляции:

- Если вы «усыпите» волокно на `256.millis`, измерив время до и после, вы увидите, 
  что второе измерение будет **ровно на 256.millis** больше первого (или больше, если вы продвинете время дальше).

**Важный нюанс:** `TestControl` **не может** симулировать время, затраченное на вычисления. 
В его вселенной **все вычисления, все побочные эффекты (включая дисковый или сетевой I/O) происходят мгновенно и не ускоряют время**. 
Это может иметь неожиданные последствия (см. раздел «Подводные камни»).

### 3. Пошаговое выполнение (Step-by-Step Execution)

`TestControl` позволяет **управлять выполнением программы извне**. Вы можете:

- Продвинуть выполнение до определённой точки.
- Приостановить _всю программу_.
- Проверить состояние, наличие результатов, текущее симулированное время.
- Продвинуть время вперёд и снова запустить выполнение.

Фактически, `TestControl` — это мощный **отладчик** для любого `IO`-приложения, встроенный в тестовую среду.

## Пример: функция с экспоненциальной задержкой (retry)

Для демонстрации рассмотрим функцию `retry`. 
Она реализует простую стратегию повторных попыток с экспоненциальной задержкой и случайным джиттером (jitter).

```dotty
import cats.effect.IO
import cats.effect.std.Random
import scala.concurrent.duration.*

def retry[A](
    ioa: IO[A],
    delay: FiniteDuration,
    max: Int,
    random: Random[IO]
): IO[A] =
  if max <= 1 then ioa
  else
    ioa.handleErrorWith: _ =>
      random.betweenLong(0L, delay.toNanos).flatMap: ns =>
        IO.sleep(ns.nanos) *> retry(ioa, delay * 2, max - 1, random)
```

**Логика работы:**

- Повторяет действие `ioa` до `max` раз или до первого успеха.
- При ошибке засыпает на **случайный** интервал от `0` до текущей `delay`.
- Каждый раз `delay` удваивается.
- Если все попытки неудачны, пробрасывает последнюю ошибку.

Это идеальный кандидат для `TestControl`: логика сложна, включает и время, и случайность, и эффекты.

## Режим 1: Полное выполнение (`executeEmbed`)

Простой случай: мы хотим проверить, что `retry` в конце концов достигает успеха.

```dotty
import cats.effect.IO
import cats.effect.std.Random
import cats.effect.testkit.TestControl
import scala.concurrent.duration.*

// Пример теста с MUnit Cats Effect
test("retry at least 3 times until success") {
  case object TestException extends RuntimeException

  var attempts = 0
  val action   = IO {
    attempts += 1
    if attempts != 3 then throw TestException
    else "success!"
  }

  val program = Random.scalaUtilRandom[IO].flatMap { random =>
    retry(action, 1.minute, 5, random)
  }

  TestControl.executeEmbed(program).assertEquals("success!")
}
```

**Что здесь происходит:**

- `action` выбрасывает исключение при первой и второй попытке, но на третьей возвращает `"success!"`.
- `retry` настроена на максимум 5 попыток с начальной задержкой в 1 минуту.
- **В реальной среде** этот тест мог бы занять **до 31 минуты** (от `1 + 2 + 4 + 8` минут)!
- **С `TestControl.executeEmbed`** он выполняется за **несколько миллисекунд**. 
  Функция `executeEmbed` полностью выполняет программу в симулированной среде, автоматически управляя временем.

Если `program` завершается с ошибкой, отменяется или не завершается (например, зацикливается), 
`executeEmbed` пробросит исключение, и тест упадёт.

> **Примечание:** `TestControl` может обнаружить **взаимоблокировки (deadlocks)**, например `IO.never`. 
> Но он не может решить [проблему остановки (halting problem)](https://en.wikipedia.org/wiki/Halting_problem). 
> Программа вида `IO.unit.foreverM` (не deadlock, а бесконечный цикл) никогда не завершится и при тестировании.

## Режим 2: Пошаговое выполнение (`execute`)

Более сложный случай: мы хотим проверить, что между попытками программа «засыпает» на правильный интервал (от 0 до растущей задержки). 
Это сложно сделать с `executeEmbed`, не добавляя побочных каналов в сам `retry`.

На помощь приходит `TestControl.execute`.

```dotty
test("backoff appropriately between attempts") {
  case object TestException extends RuntimeException

  val action  = IO.raiseError(TestException) // Всегда падает
  val program = Random.scalaUtilRandom[IO].flatMap { random =>
    retry(action, 1.minute, 5, random)
  }

  TestControl.execute(program).flatMap { control =>
    for
      _ <- control.results.assertEquals(None)
      _ <- control.tick

      _ <- (0 until 4).toList.traverse { i =>
        for
          _        <- control.results.assertEquals(None)
          interval <- control.nextInterval
          _        <- IO(assert(interval >= 0.nanos))
          _        <- IO(assert(interval < (1L << i).minute))
          _        <- control.advanceAndTick(interval)
        yield ()
      }

      _ <- control.results.assertEquals(Some(Outcome.failed(TestException)))
    yield ()
  }
}
```

### Пошаговое объяснение

1. **`TestControl.execute(program)`**: Запускает программу в симуляции, но **не выполняет её**. 
   Возвращает `IO[TestControl[A]]`, через который мы управляем выполнением.
2. **`control.results.assertEquals(None)`**: Проверяем, что программа ещё не завершилась.
3. **`control.tick`**: Запускает выполнение до первого `IO.sleep`. Волокно засыпает, `tick` возвращает управление.
4. **Цикл по попыткам (0 до 4):**
    - **`control.nextInterval`**: Возвращает минимальное время, на которое нужно продвинуть симулированные часы, 
      чтобы разбудить хотя бы одно волокно. Это и будет наша задержка (от 0 до `delay`).
    - **Проверяем диапазон:** `interval` должен быть от `0.nanos` до `(1 << i).minute` (1, 2, 4, 8 минут...).
    - **`control.advanceAndTick(interval)`**: Продвигаем симулированное время на `interval` и запускаем выполнение. 
      Программа просыпается, получает ошибку, запускает следующую попытку и снова засыпает.
5. После 5 неудачных попыток программа завершается с ошибкой. **`control.results`** должен вернуть `Some(Outcome.failed(...))`.

## Важные функции `TestControl`

| Функция                     | Что делает                                                                                                                     |
|:----------------------------|:-------------------------------------------------------------------------------------------------------------------------------|
| `tick`                      | Выполняет программу, **не продвигая время**, пока все волокна не заснут (или программа не завершится).                         |
| `advance(duration)`         | Продвигает симулированное время на `duration`, **не запуская** программу.                                                      |
| `advanceAndTick(duration)`  | Продвигает время (`advance`) и сразу выполняет `tick`.                                                                         |
| `tickAll`                   | Цикл: `tick` → `nextInterval` → `advanceAndTick(nextInterval)`, пока программа не завершится или не будет обнаружен deadlock.  |
| `nextInterval`              | Возвращает минимальное время до ближайшего пробуждения спящих волокон (или `Duration.Inf`, если спящих нет).                   |
| `results`                   | Возвращает `Option[Outcome[Id, Throwable, A]]` — результат программы (если она завершена).                                     |
| `isDeadlocked`              | `true`, если программа остановилась (нет активных волокон), но не завершилась (типичный deadlock).                             |

## Как работает `executeEmbed`?

`executeEmbed` — это удобная обёртка над `execute`:

```dotty
def executeEmbed[A](program: IO[A], config: IORuntimeConfig = IORuntimeConfig(), seed: Option[String] = None): IO[A] =
  execute(program, config = config, seed = seed).flatMap: control =>
    val onCancel = IO.defer(IO.raiseError(new CancellationException()))
    val onNever = IO.raiseError(new NonTerminationException())
    val embedded = control.results.flatMap(_.map(_.embed(onCancel)).getOrElse(onNever))
    control.tickAll *> embedded
```

- `tickAll` — выполняет программу до самого конца (или до deadlock).
- Результат извлекается и «встраивается» (embed) обратно в `IO`. Отмена и незавершение превращаются в исключения.

## Подводные камни и важные предостережения

`TestControl` — это **симуляция**, и некоторые программы могут вести себя в ней иначе, чем в production. 
**Всегда, если возможно, сначала тестируйте с реальной средой.** 
`TestControl` используйте только тогда, когда вам действительно нужен контроль над временем.

### Патологический пример: бесконечный `cede`

```dotty
IO.cede.foreverM.start.flatMap { fiber =>
  IO.sleep(1.second) *> fiber.cancel
}
```

- **В JVM/Node.js:** Программа завершится через ~1 секунду.
- **В `TestControl`:** Программа **никогда не завершится**! И даже `isDeadlocked` не станет `true`. 
  Почему? Потому что волокно с `foreverM` никогда не засыпает. Оно постоянно находится в состоянии «активно», 
  и `tick` будет бесконечно переключаться на него, никогда не возвращая управление, чтобы мы могли продвинуть время.

**Что делать?** Не полагайтесь на `tick` в таких случаях. Используйте `tickOne` или более сложные схемы.

### Ошибка: продвижение времени до `IO.sleep`

```dotty
TestControl.execute(IO.sleep(1.second) >> IO.realTime).flatMap: control =>
  for
    _ <- control.advanceAndTick(1.second)
    _ <- control.results.assertEquals(Some(Outcome.succeeded(1.second)))
  yield ()
```

Этот тест **упадёт**. `results` будет `None`. Почему? 
`advanceAndTick` продвинул время ДО того, как `IO.sleep` успел выполниться. 
В симуляции `sleep` сработает после `advance`, и волокно уснёт на 1 секунду.

**Исправление:** Сначала сделайте `tick`, чтобы выполнение дошло до `sleep`, потом продвигайте время.

```dotty
TestControl.execute(IO.sleep(1.second) >> IO.realTime).flatMap: control =>
  for
    _ <- control.tick
    _ <- control.advanceAndTick(1.second)
    _ <- control.results.assertEquals(Some(Outcome.succeeded(1.second)))
  yield ()
```

## Заключение

`TestControl` — невероятно мощный инструмент для тестирования асинхронного, временного и конкурентного кода на Cats Effect.

- Используйте **`executeEmbed`** для простых сценариев, где нужно только детерминированное и быстрое выполнение.
- Используйте **`execute`** с пошаговым управлением для сложных сценариев, 
  где нужно проверять внутреннее состояние, задержки и последовательность выполнения.

Помните о подводных камнях и всегда проектируйте тесты с учётом того, что `TestControl` — это симуляция, 
а не 100% замена production-среде. Но для 99% задач, связанных со временем, это идеальный инструмент.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Test Runtime](https://typelevel.org/cats-effect/docs/core/test-runtime)

[doc]: https://typelevel.org/cats-effect/docs/concepts

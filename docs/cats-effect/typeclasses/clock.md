# Clock — работа со временем

## Введение

В функциональных эффектах работа со временем — это не просто вызов `System.currentTimeMillis()`. 
В контексте эффектов `F[_]` нам нужен **чистый**, **ссылочно прозрачный** и **тестируемый** способ получения времени. 
`Clock[F[_]]` предоставляет именно такой интерфейс.

### Проблемы прямого вызова системных функций времени

```dotty
// ❌ Проблемный код
def measureTime[F[_]: Sync, A](fa: F[A]): F[(A, Long)] =
  for
    start  <- Sync[F].delay(System.nanoTime()) // Побочный эффект
    result <- fa
    end    <- Sync[F].delay(System.nanoTime())
  yield (result, end - start)
```

**Проблемы:**

- Привязанность к конкретной реализации (`System.nanoTime`)
- Сложно тестировать (нельзя подменить время)
- Нет различения между монотонным и стенным временем
- Возможны race conditions при конкурентном доступе

`Clock` решает все эти проблемы, предоставляя **абстрактный**, **чистый** интерфейс к временным функциям.

## Определение и ключевые концепции

### Базовый контракт

```dotty
trait Clock[F[_]]:
  def monotonic: F[FiniteDuration] // Монотонное время
  def realTime: F[FiniteDuration]  // Системное (стенное) время
```

### Два типа времени: почему это важно?

| Характеристика                  | `monotonic`                                          | `realTime`                                          |
|---------------------------------|------------------------------------------------------|-----------------------------------------------------|
| **Что измеряет**                | Время с произвольной фиксированной точки в прошлом   | Абсолютное системное время (Unix epoch)             |
| **Может ли идти назад?**        | Нет (строго увеличивается)                           | Да (при синхронизации NTP, переводе часов)          |
| **Зависит от часового пояса?**  | Нет                                                  | Да                                                  |
| **Аналог в Java**               | `System.nanoTime()`                                  | `System.currentTimeMillis()`                        |
| **Основное использование**      | Измерение интервалов, тайм-ауты, производительность  | Логирование с метками времени, работа с календарём  |

### Практический пример различий

```dotty
import cats.effect.{Clock, IO}
import scala.concurrent.duration.*

val clock = Clock[IO]

// ❌ ОПАСНО: измеряем время через realTime
for
  start  <- clock.realTime
  result <- someEffect
  end    <- clock.realTime
  duration = end - start // Может быть отрицательным!
yield result

// ✅ ПРАВИЛЬНО: используем monotonic для интервалов
for
  start  <- clock.monotonic
  result <- someEffect
  end    <- clock.monotonic
  duration = end - start // Всегда неотрицательное
yield result
```

## Детальный разбор методов

### `monotonic: F[FiniteDuration]`

**Что возвращает:** Продолжительность с некоторой фиксированной, но **произвольной** точки отсчёта.

```dotty
val t1 = clock.monotonic
val t2 = clock.monotonic

(t1, t2).mapN: (first, second) =>
  // first может быть любым (0, 123456, и т.д.)
  // Но second - first всегда >= 0
  val elapsed = second - first
  println(s"Прошло $elapsed")
```

**Ключевые свойства:**

- **Монотонность:** Если вызов `A` произошёл раньше вызова `B`, то `value_A <= value_B`
- **Консистентность:** `В` рамках одной JVM все вызовы используют одну точку отсчёта
- **Высокая точность:** Обычно наносекундная (зависит от платформы)
- **Независимость от системных часов:** Перевод часов не влияет на значения

**Когда использовать:**

- Измерение времени выполнения операций
- Реализация тайм-аутов
- Пуллы соединений (timeout idle connections)
- Метрики производительности
- Анимации и физические симуляции

### `realTime: F[FiniteDuration]`

**Что возвращает:** Продолжительность с **Unix epoch** (1 января 1970, 00:00:00 UTC).

```dotty
val now = clock.realTime
now.map: duration =>
  val seconds = duration.toSeconds
  val millis  = duration.toMillis
  println(s"Unix timestamp: $seconds секунд, $millis миллисекунд")
```

**Ключевые свойства:**

- **Абсолютное значение:** Можно преобразовать в дату и время
- **Может меняться немонотонно:** NTP-синхронизация, переход на летнее время
- **Зависит от системной конфигурации:** Часовой пояс, настройки синхронизации
- **Точность:** Обычно миллисекундная (микросекунды на некоторых платформах)

**Когда использовать:**

- Логирование с человеко-читаемыми метками времени
- Генерация временных меток для БД
- Работа с календарными датами
- Вычисление возраста/длительности в календарных единицах

## Расширенные возможности Clock

### Методы-расширения (синтаксис)

```dotty
import cats.effect.Clock
import cats.syntax.all.*

def withTimestamp[F[_]: {Clock, Monad}, A](fa: F[A]): F[(A, FiniteDuration)] =
  for
    start  <- Clock[F].monotonic
    result <- fa
    end    <- Clock[F].monotonic
  yield (result, end - start)

// Использование
val ioWithTime = withTimestamp(IO.println("Hello"))
```

## Практические примеры использования

### Измерение производительности

```dotty
def measure[F[_]: {Clock, Monad}, A](fa: F[A]): F[(A, FiniteDuration)] =
  for
    start  <- Clock[F].monotonic
    result <- fa
    end    <- Clock[F].monotonic
  yield (result, end - start)

// Использование
val program = measure(IO.sleep(1.second) >> IO.println("Done"))
program.flatMap { case (_, duration) =>
  IO.println(s"Execution time: ${duration.toMillis}ms")
}
```

### Генерация уникальных ID на основе времени

```dotty
def timeBasedId[F[_]: {Clock, Sync}]: F[String] =
  for
    now    <- Clock[F].realTime
    nanos  <- Clock[F].monotonic.map(_.toNanos)
    random <- Sync[F].delay(scala.util.Random.nextInt(1000))
  yield s"${now.toMillis}-${nanos % 10000}-$random"
```

## Особенности и подводные камни

### Точность и разрешение

```dotty
// Разное поведение на разных платформах
Clock[IO].monotonic.map: duration =>
  // JVM: наносекунды (обычно)
  // JS: миллисекунды (performance.now)
  // Native: зависит от реализации
  println(duration.toNanos)
```

### realTime и часовые пояса

```dotty
Clock[IO].realTime.map: duration =>
  // duration - это UTC время, НЕ локальное!
  val localTime = duration.toMillis + TimeZone.getDefault.getRawOffset
  // Будьте осторожны с летним временем!
```

## Итог: Золотые правила работы с Clock

### ✅ **Правильно:**

```dotty
// Измерение интервалов
val elapsed =
  for
    start <- Clock[F].monotonic
    _     <- operation
    end   <- Clock[F].monotonic
  yield end - start

// Логирование абсолютного времени
val logEntry =
  for
    now <- Clock[F].realTime
  yield s"[${now.toMillis}] Сообщение"

// Абстракция для тестирования
def service[F[_]: {Clock, Monad}] = ???
```

### ❌ **Неправильно:**

```dotty
// Интервалы через realTime
val bad =
  for
    start <- Clock[F].realTime
    _     <- operation
    end   <- Clock[F].realTime
  yield end - start // Может быть отрицательным!

// Привязка к конкретной реализации
def getTime[F[_]: Sync]: F[Long] =
  Sync[F].delay(System.currentTimeMillis()) // Лучше использовать Clock!

// Сравнение монотонных значений из разных экземпляров
val clock1 = Clock[F]
val clock2 = Clock[F]
// clock1.monotonic и clock2.monotonic могут иметь разные точки отсчёта!
```

### 💡 **Ключевые выводы:**

1. **`monotonic` для измерения интервалов** — всегда используйте его для тайм-аутов, метрик производительности
2. **`realTime` для абсолютного времени** — только для меток времени в логах/БД
3. **Тестируйте с контролируемым временем** — создавайте кастомные реализации Clock для детерминированных тестов
4. **Никогда не предполагайте одинаковую точность** — поведение зависит от платформы
5. **`Clock` — это побочный эффект** — каждое обращение может давать разный результат

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Clock](https://typelevel.org/cats-effect/docs/typeclasses/clock)

[doc]: https://typelevel.org/cats-effect/docs/concepts

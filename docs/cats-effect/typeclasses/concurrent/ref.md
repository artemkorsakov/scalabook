{%
showPrev = true
prevPage = ../concurrent.html
showNext = true
nextPage = deferred.html
%}

# `Ref` — Атомарная ссылка для конкурентного доступа

`Ref[F[_], A]` — это **конкурентно-безопасная, атомарная ссылка** на значение типа `A` в контексте эффекта `F`. 
Представьте её как функциональный аналог `AtomicReference` из Java, но с несколькими ключевыми преимуществами:

- **Чистота:** Все операции обёрнуты в эффект `F`, сохраняя ссылочную прозрачность.
- **Отменяемость:** Работает внутри системы отмены Cats Effect.
- **Композиционность:** Легко комбинируется с другими эффектами в for-comprehension.
- **Легковесность:** Использует неблокирующие алгоритмы (CAS — Compare-And-Swap).

### Базовый интерфейс

Вот упрощённое определение `Ref` с основными операциями:

```dotty
abstract class Ref[F[_], A]:
  /** Читает текущее значение. */
  def get: F[A]

  /** Заменяет текущее значение на новое (без возврата старого). */
  def set(a: A): F[Unit]

  /** Обновляет значение с помощью функции. */
  def update(f: A => A): F[Unit]

  /** Атомарно изменяет состояние, возвращая произвольное значение `B`. */
  def modify[B](f: A => (A, B)): F[B]

  // ... и многое другое (updateAndGet, getAndUpdate, getAndSet и т.д.)
```

> **Важно:** `Ref` инициализируется **обязательно** начальным значением. Пустого состояния не существует.

### `modify` — Ядро атомарности

Из всех методов `modify` — самый важный. Он позволяет **за один атомарный шаг**:

1.  Прочитать текущее состояние.
2.  Вычислить новое состояние и побочный результат.
3.  Заменить состояние.
4.  Вернуть побочный результат.

**Как это работает под капотом:** `modify` использует цикл `compare-and-set` (CAS). 
Если во время выполнения другой поток изменил значение, операция повторяется. 
Это гарантирует, что вся последовательность действий (чтение → вычисление → запись) будет выполнена без вмешательства.

```dotty
// Пример: атомарное получение и сброс счётчика
def getAndReset[F[_], A](ref: Ref[F, A])(initialValue: A): F[A] =
  ref.modify: currentValue =>
    (initialValue, currentValue) // (новое состояние, возвращаемое значение)
```

## Основные операции с примерами

Давайте рассмотрим каждую операцию на простом примере счётчика.

```dotty
import cats.effect.{IO, Ref}

val program: IO[Unit] =
  for
    // Создаём Ref с начальным значением 0
    counter <- Ref[IO].of(0)
    // `get` — Чтение значения
    current <- counter.get
    _       <- IO.println(s"Результат get: $current")
    // `set` — Полная замена значения
    _       <- counter.set(1)
    current <- counter.get
    _       <- IO.println(s"Результат set(1): $current")
    // `update` — Простое обновление (без возврата результата)
    _       <- counter.update(_ + 1)
    current <- counter.get
    _       <- IO.println(s"Результат update(_ + 1): $current")
    // `updateAndGet` — Обновление с возвратом нового значения
    incrementAndGet <- counter.updateAndGet(_ + 1)
    _               <- IO.println(s"Результат updateAndGet(_ + 1): $incrementAndGet")
    // `getAndUpdate` — Обновление с возвратом старого значения
    getAndIncrement <- counter.getAndUpdate(_ + 1)
    _               <- IO.println(s"Результат getAndUpdate(_ + 1): $getAndIncrement")
    // `modify` — Произвольное атомарное преобразование
    // Увеличиваем чётные значения на 1, нечётные оставляем как есть,
    // и возвращаем булев флаг: было ли значение изменено
    current <- counter.get
    _       <- IO.println(s"Значение перед modify: $current")
    changeIfEven <- counter.modify { current =>
      if current % 2 == 0 then (current + 1, true)
      else (current, false)
    }
    _ <- IO.println(s"Было ли число четным: $changeIfEven")
  yield ()
```

**Вывод:**

```text
Результат get: 0
Результат set(1): 1
Результат update(_ + 1): 2
Результат updateAndGet(_ + 1): 3
Результат getAndUpdate(_ + 1): 3
Значение перед modify: 4
Было ли число четным: true
```

## Счётчик параллельных операций

Это самый классический сценарий для `Ref`. Несколько параллельных волокон одновременно обновляют один счётчик.

### Код примера

```dotty
import cats.effect.{IO, IOApp, Sync}
import cats.effect.kernel.Ref
import cats.syntax.all.*

class Worker[F[_]](id: Int, ref: Ref[F, Int])(implicit F: Sync[F]):

  // Безопасный аналог println для эффектов
  private def putStrLn(value: String): F[Unit] =
    F.blocking(println(value))

  def start: F[Unit] =
    for
      // 1. Читаем текущее значение до обновления
      before <- ref.get
      _      <- putStrLn(show"Worker #$id >> текущий: $before")

      // 2. Атомарно увеличиваем счётчик и получаем новое значение
      after <- ref.updateAndGet(_ + 1)
      _     <- putStrLn(show"Worker #$id >> после: $after")
    yield ()

object RefExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      // Общий ресурс для всех воркеров
      ref <- Ref[IO].of(0)

      // Создаём трёх воркеров
      w1 = new Worker[IO](1, ref)
      w2 = new Worker[IO](2, ref)
      w3 = new Worker[IO](3, ref)

      // Запускаем их параллельно
      _ <- List(w1.start, w2.start, w3.start).parSequence.void
    yield ()
```

**Возможный вывод:**

```
Worker #3 >> текущий: 0
Worker #1 >> текущий: 0
Worker #2 >> текущий: 0
Worker #1 >> после: 1
Worker #3 >> после: 2
Worker #2 >> после: 3
```

1. **Начало гонки:** Все три воркера одновременно прочитали `get` и увидели **0**. 
   Это называется _состояние гонки_ (race condition), но оно здесь безопасно, так как мы только читаем.
2. **Атомарное обновление:** Операция `updateAndGet` — атомарная. 
   Несмотря на то, что все три рабочих начали с `0`, они по очереди (внутри CAS-цикла) вызовут `_ + 1` и запишут результат.
3. **Непредсказуемый порядок:** Порядок выполнения определяет планировщик. 
   В результате `after` значения могут быть `1`, `2`, `3` (или любая их перестановка), 
   но **суммарный результат всегда будет 3**, так как каждый воркер увеличил счётчик ровно один раз.

> **Ключевой вывод:** `Ref` гарантирует **атомарность** каждой отдельной операции (`get`, `updateAndGet`), 
> но не гарантирует атомарность последовательности `get` -> `updateAndGet`. Для этого используется `modify`.

## Важное ограничение: Не храните изменяемые данные!

`Ref` использует сравнение ссылок (`==`) внутри CAS-логики. Это работает только с **неизменяемыми (immutable)** данными.

```dotty
// ❌ ПЛОХО: Хранение изменяемого списка
Ref[IO].of(mutable.ListBuffer(1, 2, 3)).flatMap: badRef =>
  badRef.update(_ += 4) // Не сработает как ожидается!
  
// ✅ ХОРОШО: Хранение неизменяемого списка
Ref[IO].of(List(1, 2, 3)).flatMap: goodRef =>
  goodRef.update(0 :: _) // Работает правильно 
```

Если вы измените содержимое объекта _по ссылке_ (например, добавите элемент в `ArrayBuffer`), 
`Ref` об этом не узнает, и атомарность нарушится. 
**Всегда используйте неизменяемые структуры данных** (case class, List, Vector, Map, Set).

## `Ref` против `AtomicReference`

| Характеристика             | `java.util.concurrent.atomic.AtomicReference`  | `cats.effect.kernel.Ref`              |
|----------------------------|------------------------------------------------|---------------------------------------|
| **Среда**                  | JVM (платформозависим)                         | JVM, JS, Native (платформонезависим)  |
| **Модель вычислений**      | Прямые вызовы методов                          | Эффекты в `F` (например, `IO`)        |
| **Отменяемость**           | Нет                                            | Да (интеграция с `MonadCancel`)       |
| **Блокировка**             | Неблокирующий (CAS)                            | Неблокирующий (CAS)                   |
| **Ссылочная прозрачность** | Нет                                            | Да (через эффекты)                    |
| **Композиция операций**    | Требует ручного цикла CAS                      | `modify` даёт чистую композицию       |

## Итог: Когда использовать `Ref`?

✅ **Используйте `Ref`, если вам нужно:**

- Атомарное чтение/запись одного значения из нескольких волокон.
- Простое накопление состояния (счётчики, флаги, агрегаторы).
- Кеширование неизменяемых данных.
- Управление состоянием конечного автомата (FSM).

❌ **Не используйте `Ref`, если вам нужно:**

- Блокировать волокна, пока не выполнится условие (используйте `Deferred`).
- Координировать сложную очередь сообщений (используйте `Queue`).
- Ограничивать параллелизм (используйте `Semaphore`).

`Ref` — это фундаментальный строительный блок. 
Вместе с `Deferred` он лежит в основе всех конкурентных структур Cats Effect (`Semaphore`, `Queue`, `Mutex` и других).

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Ref](https://typelevel.org/cats-effect/docs/std/ref)
- [Concurrency in Cats Effect 3](https://typelevel.org/blog/concurrency-in-ce3.html)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)
- [Synchronisation and Concurrent Access in Cats Effect 3 Part 7](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)

[doc]: https://typelevel.org/cats-effect/docs/concepts

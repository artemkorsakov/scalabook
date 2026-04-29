{%
showPrev = true
prevPage = queue.html
showNext = true
nextPage = pqueue.html
%}

# Dequeue — Асинхронная двусторонняя очередь

`Dequeue[F[_], A]` (Double-Ended Queue) — это **конкурентная двусторонняя очередь**, 
которая расширяет функциональность обычной `Queue`, 
позволяя добавлять и извлекать элементы **с обоих концов**: с головы (front) и с хвоста (back).

Представьте себе обычную очередь, где элементы попадают в конец, а обрабатываются из начала (FIFO — First-In-First-Out). 
`Dequeue` добавляет гибкость: вы можете:

- Добавить элемент в начало (`offerFront`).
- Добавить элемент в конец (`offerBack`).
- Забрать элемент с начала (`takeFront`).
- Забрать элемент с конца (`takeBack`).

### Базовый интерфейс

```dotty
trait Dequeue[F[_], A] extends Queue[F, A]:
  // Операции с хвостом (задней частью)
  def offerBack(a: A): F[Unit]
  def tryOfferBack(a: A): F[Boolean]
  def takeBack: F[A]
  def tryTakeBack: F[Option[A]]

  // Операции с головой (передней частью)
  def offerFront(a: A): F[Unit]
  def tryOfferFront(a: A): F[Boolean]
  def takeFront: F[A]
  def tryTakeFront: F[Option[A]]

  // Переворачивает очередь (замена головы и хвоста)
  def reverse: F[Unit]

  // Наследуемые от Queue методы делегируются (по умолчанию)
  override def offer(a: A): F[Unit]       = offerBack(a) // offer -> offerBack
  override def tryOffer(a: A): F[Boolean] = tryOfferBack(a)
  override def take: F[A]                 = takeFront    // take -> takeFront
  override def tryTake: F[Option[A]]      = tryTakeFront
end Dequeue
```

## Виды `Dequeue` и их политики

Как и `Queue`, `Dequeue` может быть создана с различной поведенческой политикой относительно вместимости.

| Тип очереди                              | Поведение `offer*` при заполнении                                                            | Поведение `take*` при пустоте                                                                      |
|:-----------------------------------------|:---------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------|
| **Ограниченная (bounded)**               | Блокируется, пока не освободится место.                                                      | Блокируется, пока не появится элемент.                                                             |
| **Неограниченная (unbounded)**           | Никогда не блокируется. Всегда успешна.                                                      | Блокируется, пока не появится элемент.                                                             |
| **Синхронная (synchronous)**             | Блокируется, пока противоположная операция (`takeFront` или `takeBack`) не заберёт элемент.  | Блокируется, пока противоположная операция (`offerFront` или `offerBack`) не предоставит элемент.  |
| **Отбрасывающая (dropping)**             | Никогда не блокируется. Новый элемент отбрасывается при переполнении.                        | Блокируется, пока не появится элемент.                                                             |
| **Циркулярный буфер (circular buffer)**  | Никогда не блокируется. Самый старый элемент перезаписывается.                               | Блокируется, пока не появится элемент.                                                             |

> **Важно:** Размер `Dequeue` — это сумма элементов, которые находятся в очереди. 
> Ограничение "bounded" относится к общему числу элементов, независимо от того, с какого конца они добавлены.

## Зачем нужна двусторонность?

`Dequeue` незаменима в сценариях, где требуется более сложная логика, чем простая FIFO-очередь.

### 1. Приоритетная обработка (стек + очередь)

Вы можете использовать `Dequeue` как **комбинацию очереди и стека**.

- **Обычные задачи** отправляются в конец через `offerBack` и обрабатываются из начала через `takeFront`.
- **Срочные задачи** отправляются в начало через `offerFront`. Они будут обработаны **сразу после текущей** задачи.

```dotty
import cats.effect.IO
import cats.effect.std.Dequeue

class TaskProcessor(queue: Dequeue[IO, String]):

  def addNormalTask(task: String): IO[Unit] = queue.offerBack(task)
  def addUrgentTask(task: String): IO[Unit] = queue.offerFront(task)

  def processNext: IO[Unit] =
    queue.takeFront.flatMap: task =>
      IO.println(s"Обрабатываю задачу: $task")
```

### 2. Очередь с ограниченным буфером (ограниченный Deque)

Классический пример — **ограниченный буфер** для обмена данными между производителем и потребителем, 
но где потребитель может "заглянуть в прошлое" или перечитать последние элементы.

```dotty
class LimitedBuffer(queue: Dequeue[IO, Int]):
  def addToBuffer(value: Int): IO[Unit] = queue.offerBack(value)
  def readLast: IO[Int]  = queue.takeBack  // Читаем самый новый элемент
  def readFirst: IO[Int] = queue.takeFront // Читаем самый старый элемент
```

### 3. Очередь с повторами (retry queue)

Система обработки сообщений: если обработка сообщения временно невозможна, 
можно отправить его **обратно в начало** очереди, чтобы попробовать снова, 
а не класть в конец, где оно будет ждать дольше.

```dotty
def processMessage(msg: String): IO[Boolean] =
  ??? // true - успех, false - нужно повторить

def processor(queue: Dequeue[IO, String]): IO[Unit] =
  queue.takeFront.flatMap { msg =>
    processMessage(msg).ifM(
      ifTrue = IO.unit,
      ifFalse = queue.offerFront(msg) // Отправляем обратно в начало
    )
  }.foreverM
```

## Пример 1: Базовое использование `Dequeue` как двусторонней очереди

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Dequeue
import cats.syntax.all.*

object DequeueExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      dq <- Dequeue.bounded[IO, String](5)

      _ <- dq.offerBack("A")  // [A]
      _ <- dq.offerBack("B")  // [A, B]
      _ <- dq.offerFront("C") // [C, A, B]
      _ <- dq.offerFront("D") // [D, C, A, B]

      // Извлекаем с разных концов
      first <- dq.takeFront // Извлекаем D
      last  <- dq.takeBack  // Извлекаем B

      _ <- IO.println(
        s"Первый: $first, Последний: $last"
      ) // Первый: D, Последний: B
    yield ()
```

**Вывод:**

```
Первый: D, Последний: B
```

## Пример 2: `Dequeue` как очередь с приоритетом (Stack + Queue)

Более сложный пример — система заказов, где есть обычные заказы и VIP-заказы.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Dequeue
import scala.concurrent.duration.*

object PriorityQueueExample extends IOApp.Simple:

  case class Order(id: Int, isVIP: Boolean, description: String)

  def run: IO[Unit] =
    for
      dq <- Dequeue.bounded[IO, Order](100)

      // Отправляем заказы
      _ <- submitOrder(dq)(Order(1, false, "Обычный товар"))
      _ <- IO.sleep(100.millis)
      _ <- submitOrder(dq)(Order(2, true, "VIP срочный товар"))
      _ <- IO.sleep(100.millis)
      _ <- submitOrder(dq)(Order(3, false, "Ещё один обычный"))
      _ <- IO.sleep(100.millis)
      _ <- submitOrder(dq)(Order(4, true, "Ещё VIP"))

      // Запускаем обработчик в фоне
      processorFiber <- processOrder(dq).start
      _              <- IO.sleep(1.second) // Даём время на обработку
      _              <- processorFiber.cancel
    yield ()

  // Функция для отправки заказа в очередь
  private def submitOrder(dq: Dequeue[IO, Order])(order: Order): IO[Unit] =
    if order.isVIP then dq.offerFront(order)
    else dq.offerBack(order)

  // Обработчик заказов
  private def processOrder(dq: Dequeue[IO, Order]): IO[Unit] =
    dq.takeFront.flatMap { order =>
      IO.println(
        s"Обрабатываю заказ #${order.id} (VIP: ${order.isVIP}): ${order.description}"
      )
    }.foreverM
end PriorityQueueExample

// Обрабатываю заказ #4 (VIP: true): Ещё VIP
// Обрабатываю заказ #2 (VIP: true): VIP срочный товар
// Обрабатываю заказ #1 (VIP: false): Обычный товар
// Обрабатываю заказ #3 (VIP: false): Ещё один обычный
```

**Ожидаемый порядок обработки:** VIP-заказы (2 и 4) будут обработаны **перед** обычными, несмотря на то, что были отправлены позже.

## Функториальность: `DequeueSource` и `DequeueSink`

Как и `Queue`, `Dequeue` разделяется на два интерфейса:

- **`DequeueSource[F[_], A]`** — отвечает только за извлечение элементов (`takeFront`, `takeBack`, `tryTakeFront`, `tryTakeBack`, `reverse`, `Functor`).
- **`DequeueSink[F[_], A]`** — отвечает только за добавление элементов (`offerFront`, `offerBack`, `tryOfferFront`, `tryOfferBack`, `Contravariant`).

Это позволяет вам гибко преобразовывать типы данных при чтении и записи.

### Пример: Ковариантность (`map`) для источника

Имеем `Dequeue[IO, Int]`, нужен `DequeueSource[IO, String]`, который читает строки.

```dotty
import cats.Functor
import cats.effect.std.{Dequeue, DequeueSource}
import cats.effect.IO

val intDequeue: Dequeue[IO, Int]            = ???
val stringSource: DequeueSource[IO, String] =
  Functor[[A] =>> DequeueSource[IO, A]].map(intDequeue)(_.toString)
// Теперь вызов stringSource.takeFront вернёт IO[String]
```

### Пример: Контравариантность (`contramap`) для приёмника

Имеем `Dequeue[IO, Int]`, нужен `DequeueSink[IO, Boolean]`, который кладёт `Boolean`, превращая `true` в `1`, а `false` — в `0`.

```dotty
import cats.Contravariant
import cats.effect.std.{Dequeue, DequeueSink}

val intDequeue: Dequeue[IO, Int]       = ???
val boolSink: DequeueSink[IO, Boolean] =
  Contravariant[[A] =>> DequeueSink[IO, A]]
    .contramap(intDequeue)(if _ then 1 else 0)

boolSink.offerFront(true) // положили true, который превратился в 1
```

## `reverse`: Переворот очереди

Метод `reverse` меняет местами голову и хвост очереди за **O(1)**. 
Это полезно, когда нужно изменить порядок обработки элементов на противоположный.

```dotty
import cats.effect.IO
import cats.effect.std.Dequeue

val dq: Dequeue[IO, Int] = ???
// Предположим, в очереди: [1, 2, 3] (голова=1, хвост=3)
dq.reverse  // Теперь очередь: [3, 2, 1] (голова=3, хвост=1)
```

Это очень эффективно, поскольку не требует копирования элементов, а просто меняет ссылки на голову и хвост.

## `Dequeue` vs `Queue`

| Возможность                             |   `Queue[F, A]`   |   `Dequeue[F, A]`   |
|:----------------------------------------|:-----------------:|:-------------------:|
| Добавление в конец (back)               |     ✅ `offer`     |    ✅ `offerBack`    |
| Добавление в начало (front)             |         ❌         |   ✅ `offerFront`    |
| Извлечение из начала (front)            |     ✅ `take`      |    ✅ `takeFront`    |
| Извлечение из конца (back)              |         ❌         |    ✅ `takeBack`     |
| Переворот очереди                       |         ❌         |     ✅ `reverse`     |
| Functor (для источника)                 |  ✅ `QueueSource`  |  ✅ `DequeueSource`  |
| Contravariant (для приёмника)           |   ✅ `QueueSink`   |   ✅ `DequeueSink`   |
| Все политики (bounded, dropping, etc.)  |         ✅         |          ✅          |

**Вывод:** `Dequeue` — это надмножество `Queue`. Если вам не нужны операции с головой (началом), 
можно использовать `Queue`, но `Dequeue` предоставляет больше гибкости.

## Практические рекомендации

| Сценарий                                   | Рекомендация                                                                      |
|:-------------------------------------------|:----------------------------------------------------------------------------------|
| Нужен классический FIFO producer-consumer  | Используйте `Queue` (или `Dequeue`, если может понадобиться гибкость в будущем).  |
| Нужен LIFO (стек) или смешанная стратегия  | `Dequeue` с `offerFront` и `takeFront` (стек) или комбинация.                     |
| Нужно обрабатывать срочные задачи          | `Dequeue` с `offerFront` для приоритетных задач.                                  |
| Очередь с повторами (retry)                | `Dequeue` с `offerFront` для повторной попытки.                                   |
| Ограниченный буфер для скользящего окна    | `Dequeue` + `bounded` + `offerBack` / `takeFront`.                                |
| Нужно перебирать очередь с двух сторон     | `Dequeue` с `takeFront` и `takeBack`.                                             |

## Итог

`Dequeue` — это мощная и гибкая структура данных, которая расширяет возможности обычной очереди. 
Она незаменима, когда требуется более сложная логика обработки, чем классическая FIFO. Основные преимущества:

1. **Двусторонний доступ** — добавляйте и забирайте элементы с любого конца.
2. **Все политики `Queue`** — `bounded`, `unbounded`, `dropping`, `circularBuffer`, `synchronous`.
3. **Функториальность** — преобразуйте типы при чтении (`Functor`) и записи (`Contravariant`).
4. **Эффективный переворот** — `reverse` за O(1).

Используйте `Dequeue` для создания сложных конкурентных систем: приоритетных очередей, буферов с повторами, стеков и гибридных структур.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Dequeue](https://typelevel.org/cats-effect/docs/std/dequeue)

[doc]: https://typelevel.org/cats-effect/docs/concepts

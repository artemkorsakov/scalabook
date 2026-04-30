{%
showPrev = true
prevPage = dequeue.html
showNext = true
nextPage = semaphore.html
%}

# PQueue — Асинхронная очередь с приоритетом

`PQueue[F[_], A: Order]` — это **конкурентная очередь с приоритетом**, где каждый элемент имеет свой приоритет, 
определяемый экземпляром `Order[A]` из библиотеки Cats. 
В отличие от обычной `Queue` (FIFO), `PQueue` всегда извлекает **наименьший** элемент согласно заданному порядку.

Представьте себе очередь в больнице или аэропорту: пациенты с более тяжёлыми состояниями (высокий приоритет) 
обслуживаются раньше, независимо от того, когда они пришли. `PQueue` автоматически поддерживает этот порядок.

### Базовый интерфейс

```dotty
trait PQueue[F[_], A: Order]:
  /** Извлекает элемент с наивысшим приоритетом (наименьший согласно Order).
    * Блокирует волокно, если очередь пуста.
    */
  def take: F[A]

  /** Пытается извлечь элемент без блокировки. */
  def tryTake: F[Option[A]]

  /** Добавляет элемент в очередь с учётом его приоритета. Блокируется только в
    * ограниченной (bounded) очереди при переполнении.
    */
  def offer(a: A): F[Unit]

  /** Пытается добавить элемент без блокировки. */
  def tryOffer(a: A): F[Boolean]
```

### Ключевая особенность: `Order`

`PQueue` использует `Order[A]` из Cats — класс типов для сравнения элементов.

```dotty
import cats.Order

// Определяем порядок: чем меньше число, тем выше приоритет
given Order[Int] = Order.fromLessThan(_ < _)

// Или для строк: чем короче, тем выше приоритет
given Order[String] = Order.by(_.length)
```

## Виды `PQueue` и их политики

Как и `Queue`, `PQueue` может быть создана с различной поведенческой политикой относительно вместимости.

| Тип очереди                              | Поведение `offer` при заполнении                                       | Поведение `take` при пустоте                | Создание                           |
|:-----------------------------------------|:-----------------------------------------------------------------------|:--------------------------------------------|:-----------------------------------|
| **Ограниченная (bounded)**               | **Блокируется**, пока не освободится место.                            | **Блокируется**, пока не появится элемент.  | `PQueue.bounded(capacity)`         |
| **Неограниченная (unbounded)**           | Никогда не блокируется. Всегда успешна.                                | **Блокируется**, пока не появится элемент.  | `PQueue.unbounded`                 |

> **Важно:** В отличие от `Queue`, `PQueue` **не имеет синхронного (synchronous)** варианта, 
> так как приоритетная очередь не может работать в режиме "рука-об-руку" (handoff) без потери семантики приоритетов.

## Приоритет: Что значит "наивысший"?

Все операции `PQueue` используют **наименьший** элемент согласно `Order[A]` как элемент с наивысшим приоритетом.

```dotty
import cats.Order
import cats.effect.std.PQueue
import cats.effect.IO

// Пример с числами: Order[Int] по умолчанию — числовой порядок
// Наивысший приоритет у наименьшего числа (1 > 2)
val numberQueue: IO[PQueue[IO, Int]] = PQueue.bounded[IO, Int](10)

for
  pq    <- numberQueue
  _     <- pq.offer(5)
  _     <- pq.offer(1)
  _     <- pq.offer(3)
  first <- pq.take
  _     <- IO.println(s"Первый: $first") // Первый: 1
yield ()
```

**Что происходит "под капотом":**

1. Внутри `PQueue` использует бинарную кучу (binary heap) для хранения элементов.
2. При `offer` элемент вставляется в кучу — сложность **O(log n)**.
3. При `take` извлекается корень (минимальный элемент) — сложность **O(log n)**.

## Пример 1: Сортировка

Ироничная, но наглядная демонстрация: можно отсортировать список, 
просто сложив все элементы в `PQueue` и затем извлекая их по одному.

```dotty
import cats.Order
import cats.effect.IO
import cats.effect.std.PQueue
import cats.effect.unsafe.implicits.global

val list = List(1, 4, 3, 7, 5, 2, 6, 9, 8)

// Определяем порядок: от меньшего к большему
given Order[Int] = Order.fromLessThan(_ < _)

def sortWithPriorityQueue(list: List[Int]): IO[List[Int]] =
  for
    pq     <- PQueue.bounded[IO, Int](10)
    _      <- list.traverse(pq.offer) // Добавляем все элементы
    sorted <- List.fill(list.length)(()).traverse(_ => pq.take)
  yield sorted

sortWithPriorityQueue(list).flatMap(IO.println).unsafeRunSync()
// Вывод: List(1, 2, 3, 4, 5, 6, 7, 8, 9)
```

**Ключевой вывод:** `PQueue` всегда отдаёт элементы в порядке возрастания приоритета (наименьший — первый), 
что делает её естественным выбором для любых задач, требующих упорядоченной обработки.

## Пример 2: Система обработки заказов с приоритетами

Реальный пример: интернет-магазин, где заказы от VIP-клиентов должны обрабатываться раньше обычных.

```dotty
import cats.Order
import cats.effect.std.PQueue
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.*

object OrderProcessing extends IOApp.Simple:

  // Заказ с уровнем приоритета (чем меньше число, тем выше приоритет)
  case class UserOrder(id: Int, priority: Int, description: String)

  // Определяем порядок для заказов: сначала сравниваем priority, затем id
  given Order[UserOrder] = Order.by(o => (o.priority, o.id))

  val run: IO[Unit] =
    for
      pq <- PQueue.bounded[IO, UserOrder](100)

      // Отправляем заказы с разным приоритетом
      _ <- pq.offer(UserOrder(1, 10, "Обычный товар"))
      _ <- IO.sleep(100.millis)
      _ <- pq.offer(UserOrder(2, 1, "VIP срочный заказ"))
      _ <- IO.sleep(100.millis)
      _ <- pq.offer(UserOrder(3, 5, "Премиум заказ"))
      _ <- IO.sleep(100.millis)
      _ <- pq.offer(UserOrder(4, 1, "Ещё один VIP"))

      // Обработчик заказов: всегда берёт самый приоритетный
      processor <- processOrders(pq).start

      _ <- IO.sleep(3.seconds) // Даём время на обработку
      _ <- processor.cancel
    yield ()

  private def processOrders(pq: PQueue[IO, UserOrder]): IO[Unit] =
    pq.take.flatMap { order =>
      IO.println(s"[${System.currentTimeMillis()}] Обрабатываю: $order") *>
        IO.sleep(500.millis) // имитация обработки
    }.foreverM
end OrderProcessing
```

**Ожидаемый порядок обработки:** VIP-заказы (priority=1) будут обработаны первыми, 
независимо от того, что обычный заказ (priority=10) пришёл раньше.

## Функториальность: `PQueueSource` и `PQueueSink`

Как и другие очереди, `PQueue` разделяется на два интерфейса:

- **`PQueueSource[F[_], A]`** — только чтение (`take`, `tryTake`), имеет экземпляр `Functor`.
- **`PQueueSink[F[_], A]`** — только запись (`offer`, `tryOffer`), имеет экземпляр `Contravariant`.

### Ковариантность (`map`) для источника

Имеем `PQueue[IO, Int]`, нужен `PQueueSource[IO, String]`, который читает строки.

```dotty
import cats.Functor
import cats.effect.std.{PQueue, PQueueSource}
import cats.effect.IO

val intQueue: PQueue[IO, Int] = ??? // создаём где-то
val stringSource: PQueueSource[IO, String] =
  Functor[[A] =>> PQueueSource[IO, A]].map(intQueue)(_.toString)
```

### Контравариантность (`contramap`) для приёмника

Имеем `PQueue[IO, Int]`, нужен `PQueueSink[IO, Boolean]`, который кладёт `Boolean`, 
превращая `true` в `1`, а `false` — в `5` (чем меньше число — тем выше приоритет).

```dotty
import cats.Contravariant
import cats.effect.std.{PQueue, PQueueSink}
import cats.effect.IO

val intQueue: PQueue[IO, Int] = ???
val boolSink: PQueueSink[IO, Boolean] =
  Contravariant[[A] =>> PQueueSink[IO, A]]
    .contramap(intQueue)(if _ then 1 else 5)

boolSink.offer(true)  // положили true → 1 (высокий приоритет)
boolSink.offer(false) // положили false → 5 (низкий приоритет)
```

## `PQueue` vs `Queue` vs `Dequeue`

| Возможность                     | `Queue`  | `Dequeue`  | `PQueue`  |
|:--------------------------------|:--------:|:----------:|:---------:|
| **FIFO**                        |    ✅     |     ✅      |     ❌     |
| **Двусторонняя**                |    ❌     |     ✅      |     ❌     |
| **Приоритет (по Order)**        |    ❌     |     ❌      |     ✅     |
| **Bounded / Unbounded**         |    ✅     |     ✅      |     ✅     |
| **Dropping / Circular**         |    ✅     |     ✅      |     ❌     |
| **Functor** (для чтения)        |    ✅     |     ✅      |     ✅     |
| **Contravariant** (для записи)  |    ✅     |     ✅      |     ✅     |

## Важные ограничения и особенности

### 1. `Order` должен быть консистентным

```dotty
// ❌ Плохо: противоречивый порядок
given Order[Int] = Order.fromLessThan((x, y) => x != y)

// ✅ Хорошо: полный и транзитивный порядок
given Order[Int] = Order.fromLessThan((x, y) => x < y) // стандартный
```

### 2. Приоритет определяется при вставке

Если вы измените поля объекта, влияющие на `Order`, очередь не пересортируется автоматически. 
Приоритет фиксируется в момент `offer`.

### 3. Нет операции "изменить приоритет"

В `PQueue` нельзя просто так взять и увеличить приоритет уже находящегося в очереди элемента. 
Для этого нужно удалить его и вставить заново.

## Практические рекомендации

| Сценарий                                    | Рекомендация                                                   |
|:--------------------------------------------|:---------------------------------------------------------------|
| **Сортировка данных**                       | Используйте `unbounded` или `bounded` с большим запасом.       |
| **Обработка заказов с разным приоритетом**  | `bounded` с разумным лимитом.                                  |
| **Планировщик задач с дедлайнами**          | `bounded` + `Order.by(_.deadline)`.                            |
| **Неограниченный поток событий**            | `unbounded` — но следите за памятью!                           |

## Итог

`PQueue` — это мощная структура для **упорядоченной по приоритету** обработки данных в конкурентной среде. 
Основные преимущества:

1.  **Автоматическая сортировка** — элементы всегда извлекаются в порядке возрастания приоритета.
2.  **Все знакомые политики** — `bounded`, `unbounded`.
3.  **Функториальность** — легко преобразовывать типы при чтении и записи.
4.  **Интеграция с Cats** — использует `Order` для гибкого определения "что важнее".

Используйте `PQueue` везде, где требуется обслуживать элементы в порядке их важности, а не в порядке поступления: 
системы тикетирования, планировщики задач, обработка событий реального времени, медицинские или аварийные системы.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Priority Queue](https://typelevel.org/cats-effect/docs/std/pqueue)

[doc]: https://typelevel.org/cats-effect/docs/concepts

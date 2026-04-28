{%
showPrev = true
prevPage = temporal.html
showNext = true
nextPage = ../std.html
%}

# Async — асинхронный FFI

`Async[F[_]]` — это класс типов, который расширяет `Temporal[F[_]]` 
и предоставляет [асинхронный интерфейс внешних функций (FFI - Foreign Function Interface)](https://ru.wikipedia.org/wiki/%D0%98%D0%BD%D1%82%D0%B5%D1%80%D1%84%D0%B5%D0%B9%D1%81_%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B8%D1%85_%D1%84%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%B9). 
Он позволяет "приостанавливать" (семантически) эффекты, 
результаты которых вычисляются **в другом месте** — например, в callback'е из Java-библиотеки, в другом потоке, или в `Future`.

Простыми словами: `Async` — это мост между **асинхронным миром коллбэков** и **функциональным миром Cats Effect**.

### Зачем это нужно?

Представьте, что вы используете библиотеку, которая работает так:

```java
// Java-библиотека для HTTP-запросов
HttpClient.get("https://api.example.com", new Callback() {
    void onSuccess(String response) { /* ... */ }
    void onFailure(Throwable error) { /* ... */ }
});
```

`Async` позволяет вам **завернуть** такой коллбэк в чистое `IO[A]`, 
не блокируя потоки и не погружаясь в ["ад коллбэков" (Callback Hell)](http://callbackhell.com/).

## Проблема: ад коллбэков

Коллбэки порождают глубокую вложенность и сложную обработку ошибок:

```dotty
// Типичный "ад коллбэков"
getUser( userId, (user) => {
  getOrders( user.id, (orders) => {
    getPayments( user.id, (payments) => {
      // обработка
    }, onError)
  }, onError)
}, onError)
```

С `Async` мы превращаем это в читаемый `for`-comprehension:

```dotty
for
  user     <- getUser(userId)
  orders   <- getOrders(user.id)
  payments <- getPayments(user.id)
yield (user, orders, payments)
```

## Базовые методы Async

### 1. `async_` — простейший FFI (ныне устаревший, но полезен для понимания)

```dotty
def async_[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]
```

Это **самый простой** способ создать `F[A]` из callback-функции.

- Вы получаете коллбэк `cb: Either[Throwable, A] => Unit`.
- Вы вызываете `cb(Right(a))` при успехе или `cb(Left(error))` при ошибке.
- Cats Effect сам позаботится об ожидании этого коллбэка.

**Важное ограничение:** Внутри `k` **нельзя выполнять эффекты** `F` (например, `F.delay`), 
а также **нельзя задать финализатор для отмены**.

#### Пример: обёртка для `onComplete` из `Future`

```dotty
import scala.concurrent.{Future, ExecutionContext}
import cats.effect.Async

def fromFuture[F[_]: Async, A](fut: Future[A])(using
    ec: ExecutionContext
): F[A] =
  Async[F].async_[A] { cb =>
    fut.onComplete:
      case Success(a) => cb(Right(a))
      case Failure(e) => cb(Left(e))
  }
```

### 2. `async` — полный FFI (с эффектами и финализацией)

```dotty
def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A]
```

Это **расширенная версия**, которая решает ограничения `async_`:

- **Эффекты:** Вы можете выполнять `F` внутри `k` (например, логирование, изменение `Ref`).
- **Финализация:** Вы можете вернуть `Some(finalizer)`, который будет вызван **при отмене волокна**.

**Как это работает:**

1. Вы получаете коллбэк `cb: Either[Throwable, A] => Unit`.
2. Вы **внутри эффекта** `F` регистрируете асинхронную операцию.
3. Вы сохраняете `cb`, чтобы вызвать его позже.
4. Возвращаете `Some(F[Unit])` — действие, которое отменит операцию (если это возможно).
5. Если отмены не требуется, возвращаете `None`.

#### Пример: из реального мира — `fromCompletableFuture`

```dotty
import java.util.concurrent.CompletableFuture
import cats.effect.Async

def fromCompletableFuture[F[_]: Async, A](fut: F[CompletableFuture[A]]): F[A] =
  Async[F].flatMap(fut): cf =>
    Async[F].async[A]: cb =>
      Async[F].delay {
        // Регистрируем обработчик результата
        val handler: (A, Throwable) => Unit = (result, error) =>
          if error != null then cb(Left(error))
          else cb(Right(result))

        val stage = cf.handle[Unit] { (result, error) =>
          handler(result, error)
        }

        // Финализатор: отменяем CompletableFuture, если волокно отменят
        Some(Async[F].delay(stage.cancel(false)))
      }
```

## Манипуляции с пулами потоков: `executionContext` и `evalOn`

`Async` даёт вам контроль над **тем, где выполняется ваш эффект**.

### `executionContext: F[ExecutionContext]`

Возвращает текущий `ExecutionContext`, используемый планировщиком Cats Effect.

```dotty
def printThread[F[_]: Async]: F[Unit] =
  Async[F].executionContext.flatMap: ec =>
    Async[F].delay(println(s"Выполняюсь в: ${Thread.currentThread().getName}"))
```

### `evalOn[A](fa: F[A], ec: ExecutionContext): F[A]`

Выполняет эффект `fa` **на указанном** `ExecutionContext`, а затем возвращается в исходный.

```dotty
val ec1         = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
val mainProgram =
  for
    _ <- printThread                        // io-compute-1
    _ <- Async[IO].evalOn(printThread, ec1) // pool-1-thread-1
    _ <- printThread                        // io-compute-1 (вернулись обратно)
  yield ()
```

**Где это полезно?**

- Вы переносите **CPU-интенсивные** операции на специальный пул потоков.
- Вы интегрируетесь с **блокирующим** API (например, JDBC), перенося его на отдельный пул.

#### Пример: изоляция блокирующего кода

```dotty
import scala.concurrent.ExecutionContext

val blockingEc = ExecutionContext.fromExecutor(
  Executors.newCachedThreadPool()
)

def readFileBlocking[F[_]: Async](path: String): F[String] =
  Async[F].evalOn(
    Async[F].delay(...долгое чтение из файла...),
    blockingEc
  )
```

## Полный пример: интеграция с callback-based библиотекой

Представьте, что у нас есть простая библиотека для работы с Redis (callback-стиль):

```dotty
// Гипотетическая Java-библиотека
trait RedisClient:
  def get(
      key: String,
      cb: (Either[Throwable, String]) => Unit
  ): Unit

  def set(
      key: String,
      value: String,
      cb: (Either[Throwable, Unit]) => Unit
  ): Unit
```

Мы хотим обернуть её в типобезопасный `IO`-интерфейс, используя `Async`:

```dotty
class Redis[F[_]: Async](client: RedisClient):
  def get(key: String): F[String] =
    Async[F].async[String] { cb =>
      Async[F].delay {
        client.get(key, result => cb(result))
        None // В этой библиотеке нет отмены, поэтому финализатор не нужен
      }
    }

  def set(key: String, value: String): F[Unit] =
    Async[F].async[Unit] { cb =>
      Async[F].delay {
        client.set(key, value, result => cb(result))
        None
      }
    }
end Redis

object Redis:
  def create[F[_]: Async]: Resource[F, Redis[F]] =
    Resource.make(
      Async[F].delay(new RedisClientImpl()) // создаём клиент
    )(client => Async[F].delay(client.close()))
      .map(new Redis(_))
```

Теперь можно писать красивый, отменяемый код:

```dotty
val program =
  Redis.create[IO].use: redis =>
    for
      _     <- redis.set("key", "value")
      value <- redis.get("key")
      _     <- IO.println(s"Получено: $value")
    yield ()
```

## Заключение

`Async` — это мост между миром функциональных эффектов и асинхронными операциями, основанными на коллбэках. Он даёт вам:

1. **Чистый FFI** — превращает коллбэки в `F[A]`.
2. **Поддержку отмены** — финализаторы автоматически вызываются при отмене волокна.
3. **Контроль над потоками** — `evalOn` и `executionContext`.
4. **Корректность** — сложная внутренняя механика (`Cont`) гарантирует соблюдение законов даже для монад-трансформеров.

Благодаря `Async` Cats Effect может интегрироваться 
с любым асинхронным API — от `java.nio.channels` до `js.fetch` — предоставляя везде одинаково удобный и безопасный интерфейс.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Async](https://typelevel.org/cats-effect/docs/typeclasses/async)

[doc]: https://typelevel.org/cats-effect/docs/concepts

# Hotswap — Безопасная замена ресурсов «на лету»

## Мотивация: проблема с вложенными `Resource`

Представьте, что вы пишете логгер, который должен **ротировать (переключать) файлы логов** каждые `n` байт. Как это обычно делается?

Наивное решение: внутри `use` одного ресурса открыть другой.

```dotty
// ❌ ПРОБЛЕМНЫЙ КОД: утечка ресурсов!
def badRotatingLogger(n: Int): Resource[IO, Logger[IO]] =
  file("0.log").flatMap { file0 =>
    Resource.eval {
      Ref[IO].of(0).map { bytesWritten =>
        new Logger[IO]:
          def log(msg: String): IO[Unit] =
            bytesWritten.get.flatMap { current =>
              if msg.length + current <= n then
                // Всё ещё пишем в текущий файл
                write(file0, msg) *> bytesWritten.update(_ + msg.length)
              else
                // ❌ ПЛОХО: открываем новый файл внутри use!
                file("1.log").use { file1 =>
                  write(file1, msg) *> bytesWritten.set(msg.length)
                }
            }
      }
    }
  }
```

**Что здесь не так?**

| Проблема                       | Объяснение                                                                                                                                                 |
|:-------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Утечка памяти**              | Внешний файл (`file0`) не будет закрыт, пока внутренний ресурс (`file1`) не освободится. А внутренний ресурс освобождается только после завершения `use`.  |
| **Вложенная финализация**      | `Resource` гарантирует обратный порядок освобождения (сначала `file1`, потом `file0`), но `file0` продолжает существовать всё время, пока жив `file1`.     |
| **Рост использования памяти**  | При многократной ротации логов все предыдущие файлы остаются открытыми, ожидая завершения последнего.                                                      |

**Корень проблемы:** Когда вы вызываете `Resource#use` внутри другого `Resource`, 
внутренний ресурс становится **зависимым** от внешнего. 
Внешний ресурс будет финализирован **только после** финализации внутреннего. 
Это приводит к накоплению "висящих" ресурсов.

## `NonEmptyHotswap`

`NonEmptyHotswap` — это примитив, который предоставляет **линейную последовательность ресурсов** в виде одного `Resource`. Он позволяет:

1.  **Начать** с некоторого начального ресурса.
2.  **Заменить** текущий ресурс на новый (`swap`), при этом старый ресурс будет **немедленно финализирован**.
3.  **Получить** текущий ресурс (`get`) для использования.

```dotty
sealed trait NonEmptyHotswap[F[_], R]:

  /** Заменяет текущий ресурс на новый. Предыдущий ресурс финализируется. */
  def swap(next: Resource[F, R]): F[R]

  /** Возвращает текущий ресурс (для использования внутри `use`). */
  def get: Resource[F, R]
```

**Ключевые гарантии:**

- `get` всегда возвращает **актуальный** ресурс.
- При вызове `swap` старый ресурс **немедленно** финализируется, а новый становится текущим.
- Когда внешний `Resource` (созданный через `NonEmptyHotswap`) финализируется, финализируется и **текущий** ресурс.
- **Безопасность:** невозможно использовать старый ресурс после `swap` — типичная система не позволит 
  этого сделать (старый `Resource` более недоступен).

## Пример: Ротация логов

Вернёмся к задаче с логгером. Вот как она решается корректно:

```dotty
import cats.effect.{IO, Ref, Resource}
import cats.effect.std.NonEmptyHotswap

def rotatingLogger(n: Int): Resource[IO, Logger[IO]] =

  // Вспомогательная функция: создаёт Resource для файла лога
  def file(name: String): Resource[IO, File] =
    Resource.make(openFile(name))(file => close(file))

  // Функция записи в файл
  def write(file: File, msg: String): IO[Unit] = ???

  // Создаём NonEmptyHotswap с начальным файлом "0.log"
  NonEmptyHotswap[IO, File](file("0.log")).flatMap { hotswap =>
    // Состояние: текущий индекс файла и количество записанных байт
    Resource.eval {
      for
        index <- Ref[IO].of(0) // номер текущего файла
        bytes <- Ref[IO].of(0) // сколько байт записано в текущий файл
      yield new Logger[IO]:
        def log(msg: String): IO[Unit] =
          bytes.get.flatMap { currentBytes =>
            val msgLen = msg.length

            // Если сообщение помещается в текущий файл
            if msgLen <= n - currentBytes then
              hotswap.get.use { currentFile =>
                write(currentFile, msg) *>
                  bytes.update(_ + msgLen)
              }
            // Иначе — ротируем файл
            else
              for
                // Сбрасываем счётчик байт
                _ <- bytes.set(msgLen)
                // Увеличиваем индекс файла
                newIdx <- index.updateAndGet(_ + 1)
                // Закрываем старый файл и открываем новый
                _ <- hotswap.swap(file(s"$newIdx.log"))
                // Пишем сообщение в новый файл
                _ <- hotswap.get.use(write(_, msg))
              yield ()
          }
    }
  }
end rotatingLogger
```

### Пошаговое объяснение

1.  **Инициализация:** `NonEmptyHotswap[IO, File](file("0.log"))` создаёт `Hotswap`, внутри которого уже активен файл `0.log`.
2.  **Логирование (обычный случай):** Если сообщение влезает в текущий файл, мы получаем текущий файл через `hotswap.get.use` и пишем в него.
3.  **Ротация (критический случай):** Когда сообщение не влезает:
    - Сбрасываем счётчик байт (`bytes.set(msgLen)`).
    - Увеличиваем индекс файла (`index.updateAndGet(_ + 1)`).
    - Вызываем `hotswap.swap(file(s"$newIdx.log"))`:
        - **Старый файл** (например, `0.log`) **немедленно закрывается**.
        - **Новый файл** (`1.log`) открывается и становится текущим.
    - Пишем сообщение в новый файл.
4.  **Завершение:** Когда внешний `Resource` (логгер) финализируется, `NonEmptyHotswap` автоматически закроет последний активный файл.

**Преимущества над "наивным" подходом:**

- Нет утечек: старые файлы закрываются сразу при ротации.
- Линейное использование памяти: только **один** файл открыт в любой момент времени.
- Безопасная композиция: `NonEmptyHotswap` сам управляет сложным жизненным циклом.

## Когда использовать `NonEmptyHotswap`, а когда — `Resource`?

| Задача                                             | Решение                   | Почему                                                  |
|:---------------------------------------------------|:--------------------------|:--------------------------------------------------------|
| Простая работа с одним ресурсом                    | `Resource`                | Явное управление `acquire`/`release`.                   |
| Цепочка ресурсов (A → B → C), где B зависит от A   | `Resource` + `flatMap`    | Обратный порядок освобождения.                          |
| **Бесконечная или динамическая ротация ресурсов**  | **`NonEmptyHotswap`**     | Старые ресурсы закрываются немедленно, не накапливаясь. |
| Пул соединений, где соединения переиспользуются    | `Resource` + `Semaphore`  | Другая задача.                                          |

## Практические примеры использования

### Пример 1: Циклическая ротация файлов (ограниченное количество)

```dotty
def rotatingFileLogger(
    maxFiles: Int,
    maxBytesPerFile: Int
): Resource[IO, Logger[IO]] =
  NonEmptyHotswap[IO, File](file("0.log")).flatMap { hotswap =>
    Resource.eval {
      Ref[IO].of(0).flatMap { fileIdx =>
        Ref[IO].of(0).flatMap { bytesWritten =>
          def nextFile: IO[Unit] =
            for
              idx <- fileIdx.updateAndGet(i => (i + 1) % maxFiles)
              _   <- hotswap.swap(file(s"$idx.log"))
              _   <- bytesWritten.set(0)
            yield ()

          IO.pure(new Logger[IO]:
            def log(msg: String): IO[Unit] =
              bytesWritten.get.flatMap { written =>
                if written + msg.length <= maxBytesPerFile then
                  hotswap.get.use(write(_, msg)) *>
                    bytesWritten.update(_ + msg.length)
                else
                  nextFile >> log(msg) // рекурсивно пишем в новый файл
              })
        }
      }
    }
  }
```

### Пример 2: Переключение между конфигурациями в рантайме

```dotty
case class Config(host: String, port: Int)

def resourceForConfig(config: Config): Resource[IO, HttpClient] = ???

def configurableClient(initialConfig: Config)
    : Resource[IO, (Config => IO[Unit], HttpClient)] =
  NonEmptyHotswap[IO, HttpClient](resourceForConfig(initialConfig)).flatMap {
    hotswap =>
      Resource.eval(Ref[IO].of(initialConfig)).flatMap { currentConfig =>
        val updater = (newConfig: Config) =>
          for
            _ <- currentConfig.set(newConfig)
            _ <- hotswap.swap(resourceForConfig(newConfig))
          yield ()

        hotswap.get.map { client =>
          (updater, client)
        }
      }
  }

// Использование:
configurableClient(Config("localhost", 8080)).use { case (update, client) =>
  for
    _ <- client.send(request1) // используем localhost:8080
    _ <- update(Config("remote.com", 9090))
    _ <- client.send(request2) // используем remote.com:9090
  yield ()
}
```

## Важные нюансы

### 1. `NonEmptyHotswap` — именно "непустой"

Название подчёркивает, что **всегда** есть текущий ресурс. Вы не можете создать `Hotswap` без начального ресурса. 
Это гарантирует, что `get` всегда возвращает валидный `Resource`.

### 2. `swap` — не возвращает старый ресурс

В отличие от некоторых других FFI, `swap` **не возвращает** старый ресурс для последующего использования. 
Это сделано намеренно: старый ресурс немедленно финализируется, и вы не должны иметь к нему доступа. 
Если вам нужно что-то сделать со старым ресурсом перед закрытием, используйте `Resource#onFinalize`.

### 3. Идемпотентность `swap`?

Нет, каждый `swap` открывает новый ресурс и закрывает текущий. 
Не вызывайте `swap` без необходимости — это дорогая операция (открытие/закрытие).

### 4. Отменяемость

`NonEmptyHotswap` корректно работает с отменой:

- Если волокно отменено во время `swap`, новый ресурс не будет активирован, 
  а старый останется (или будет закрыт, в зависимости от реализации).
- Финализация ресурса при отмене гарантирована.

## Итог: когда использовать `NonEmptyHotswap`?

✅ **Используйте `NonEmptyHotswap`, если:**

- Вам нужно **динамически заменять** один ресурс на другой в рантайме.
- Старый ресурс должен быть **немедленно освобождён** (а не ждать завершения всего `Resource`).
- Типичные примеры: ротация логов, переключение конфигураций, горячая замена соединений, "живые" очереди.

❌ **Не используйте `NonEmptyHotswap`, если:**

- У вас простая линейная цепочка ресурсов (используйте `Resource` + `flatMap`).
- Вам нужно атомарно заменить несколько ресурсов одновременно (этот примитив для одного).
- Вы работаете с пулом ресурсов, где ресурсы переиспользуются (там нужен `Semaphore` + `Resource`).

**Ключевая идея:** `NonEmptyHotswap` — это "убийца утечек" для сценариев, 
где ресурсы должны сменять друг друга в долгоживущем процессе. 
Он берёт на себя всю сложность финализации, оставляя вам чистый и понятный API для ротации.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Hotswap](https://typelevel.org/cats-effect/docs/std/hotswap)

[doc]: https://typelevel.org/cats-effect/docs/concepts

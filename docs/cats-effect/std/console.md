# Console — Простой ввод/вывод в консоль

## Что такое `Console`?

`Console[F[_]]` — это **класс типов**, предоставляющий стандартные методы для чтения 
и записи данных через **консоль** (стандартный ввод/вывод). 
Он предназначен для **простых интерактивных программ** (REPL, учебные примеры, небольшие утилиты), 
но **не подходит** для ведения журналов (логов) или серьёзного production-кода.

```dotty
trait Console[F[_]]:
  // Вывод (stdout)
  def print[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]
  def println[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]

  // Вывод ошибок (stderr)
  def error[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]
  def errorln[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]

  // Печать stack trace
  def printStackTrace(t: Throwable): F[Unit]

  // Чтение строки (stdin)
  def readLine: F[String]

  // Другие методы (readLineWithCharset, putStr, ...) — зависят от платформы
```

**Ключевая особенность:** методы вывода используют `cats.Show` для преобразования значений в строку. 
Если у вас нет своего экземпляра `Show`, используется стандартный `toString`.

## Когда использовать `Console`, а когда — нет?

| Ситуация                                                    | Инструмент                                      | Почему                                                     |
|:------------------------------------------------------------|:------------------------------------------------|:-----------------------------------------------------------|
| **Интерактивная программа** (опрос пользователя, CLI-игра)  | `Console`                                       | Простота, лаконичность.                                    |
| **REPL** (read-eval-print loop)                             | `Console`                                       | Идеально подходит.                                         |
| **Логирование (debug info)**                                | `IO.println` или `Sync[F].delay(println(...))`  | `Console` может быть избыточен.                            |
| **Логирование в production**                                | `slf4j`, `log4cats`, `logback`                  | `Console` не даёт уровней логов, форматирования, ротации.  |
| **Сложный текстовый интерфейс**                             | `fs2.io.stdin`, `fs2.io.stdout`                 | `readLine` не всегда отменяем (см. ниже).                  |

## Пример: приветствие пользователя

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Console

object HelloUser extends IOApp.Simple:
  val run: IO[Unit] =
    for
      _    <- Console[IO].println("Пожалуйста, введите ваше имя:")
      name <- Console[IO].readLine
      _    <- if name.nonEmpty then
        Console[IO].println(s"Привет, $name!")
      else
        Console[IO].errorln("Имя не может быть пустым!")
    yield ()
```

**Как это работает:**

1. `println` выводит строку в **stdout**.
2. `readLine` блокирует волокно (семантически) до тех пор, пока пользователь не введёт строку и не нажмёт Enter.
3. `errorln` выводит сообщение в **stderr** (поток ошибок).

## Платформенно-зависимые ограничения

`Console` работает на всех трёх целевых платформах Cats Effect (JVM, Scala.js, Scala Native), но **не везде одинаково**.

### JVM: проблема с отменой `readLine`

На JVM `readLine` базируется на `System.in.read()`, который **не поддерживает прерывание** стандартными средствами Java. 
Это приводит к фатальной проблеме:

```dotty
import scala.concurrent.duration.*

// ❌ ЭТО ПРИВЕДЁТ К ВЗАИМОБЛОКИРОВКЕ (deadlock) НА JVM!
IO.readLine.timeoutTo(3.seconds, IO.pure("таймаут"))
```

**Почему?**

- `System.in.read()` блокирует поток на уровне операционной системы.
- Cats Effect не может "разбудить" этот поток при отмене.
- Единственные способы прервать `readLine` — пользователь введёт строку или закроет `System.in`.

**Решение:** Используйте асинхронные альтернативы (например, `fs2.io.stdin` в сочетании с `fs2`).

### Scala Native: аналогичная проблема

`readLine` на Scala Native также основана на блокирующем вводе и не может быть отменена по таймауту.

### Scala.js: `readLine` не реализован!

На платформе JavaScript (Node.js или браузер) `readLine` **отсутствует**. Консольный ввод не является стандартной операцией.

**Альтернатива:** Используйте `fs2.io.stdin` из библиотеки `fs2-io` (Node.js) или организуйте ввод через HTML (браузер).

## Продвинутые возможности

### Кастомное форматирование через `Show`

Методы `Console` принимают неявный `Show[A]`. Это позволяет контролировать, как значения превращаются в строку.

```dotty
import cats.Show

case class User(name: String, age: Int)

given Show[User] = Show.show { u =>
  s"Пользователь: ${u.name}, возраст: ${u.age}"
}

val program = Console[IO].println(User("Анна", 25))
// Вывод: "Пользователь: Анна, возраст: 25"
```

### Печать stack trace

```dotty
def safeDiv(a: Int, b: Int): IO[Int] =
  IO(a / b).handleErrorWith { t =>
    Console[IO].errorln("Ошибка деления!") >>
      Console[IO].printStackTrace(t) >>
      IO.raiseError(t)
  }
```

## Альтернативы для production-логирования

Если вам нужно логировать в production, не используйте `Console`. Вместо этого:

| Библиотека               | Назначение                                           |
|:-------------------------|:-----------------------------------------------------|
| **`log4cats`**           | Функциональные логи с уровнями (debug, info, error)  |
| **`slf4j` + `logback`**  | Стандартная JVM-инфраструктура логирования           |
| **`natchez`**            | Распределённая трассировка (distributed tracing)     |

Пример с `log4cats`:

```dotty
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

logger.info("Приложение запущено") >>
logger.error(throwable)("Критическая ошибка")
```

## Итог: когда использовать `Console`?

✅ **Используйте `Console`, если:**

- Вы пишете **простое интерактивное приложение** (CLI-игра, обучающий пример, одноразовый скрипт).
- Вам нужен **быстрый вывод** информации в stdout/stderr.
- Вы работаете в **JVM или Scala Native** и готовы мириться с отсутствием отмены `readLine`.

❌ **Не используйте `Console`, если:**

- Вам нужно **production-логирование** (уровни, ротация, форматирование).
- Вы пишете на **Scala.js** и нуждаетесь в консольном вводе (используйте `fs2.io.stdin`).
- Требуется **отменяемый ввод** или **таймауты** на JVM (также переходите на `fs2`).
- Проект уже использует `log4cats` или подобные библиотеки.

**Ключевая идея:** `Console` — это простой и удобный мост к консоли для небольших программ, 
но у него есть серьёзные ограничения. 
Для больших приложений выбирайте специализированные инструменты логирования и асинхронного ввода/вывода.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Console](https://typelevel.org/cats-effect/docs/std/console)

[doc]: https://typelevel.org/cats-effect/docs/concepts

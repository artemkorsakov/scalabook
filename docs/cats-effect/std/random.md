# Random — Функциональный генератор случайных чисел

## Что такое `Random`?

`Random[F[_]]` — это **класс типов**, предоставляющий чисто функциональный интерфейс для генерации случайных чисел. 
Он абстрагирует различные источники энтропии (`scala.util.Random`, `java.util.Random`, `SecureRandom`, и т.д.) 
и позволяет использовать их в контексте эффекта `F`.

```dotty
trait Random[F[_]]:
  // Целые числа
  def nextInt: F[Int]
  def nextIntBounded(n: Int): F[Int] // от 0 (включительно) до n (не включая)
  def betweenInt(minInclusive: Int, maxExclusive: Int): F[Int]

  // Длинные целые
  def nextLong: F[Long]
  def nextLongBounded(n: Long): F[Long]
  def betweenLong(minInclusive: Long, maxExclusive: Long): F[Long]

  // Остальные числа
  ... 
  
  // Boolean и байты
  def nextBoolean: F[Boolean]
  def nextBytes(n: Int): F[Array[Byte]]

  // Работа с коллекциями
  def shuffleList[A](list: List[A]): F[List[A]]
  def shuffleVector[A](vector: Vector[A]): F[Vector[A]]

  // Генерация строк (буквы, цифры, алфавитно-цифровые)
  def nextAlphaNumeric: F[Char]
  def nextPrintableChar: F[Char]
  def nextString(length: Int): F[String]

  // Распределения
  def nextGaussian: F[Double] // нормальное распределение (μ=0, σ=1)
end Random
```

**Ключевая идея:** Все операции возвращают `F[A]`, что означает, 
что генерация случайного числа — это **эффект** (не чистая функция). 
Это важно, потому что каждый вызов должен давать потенциально другой результат.

## Зачем нужен `Random`, если есть `scala.util.Random`?

| Аспект                                | `scala.util.Random`                                  | `cats.effect.std.Random`                                   |
|:--------------------------------------|:-----------------------------------------------------|:-----------------------------------------------------------|
| **Чистота**                           | ❌ Каждый вызов — побочный эффект (мутация состояния) | ✅ Эффект обёрнут в `F`, сохраняется ссылочная прозрачность |
| **Платформы**                         | ❌ Только JVM                                         | ✅ JVM, JS, Native                                          |
| **Источник энтропии**                 | Фиксирован (`java.util.Random`)                      | Гибкий: `ScalaRandom`, `JavaRandom`, `SecureRandom`        |
| **Контроль над потокобезопасностью**  | ❌ Может страдать от гонок                            | ✅ Волокна Cats Effect не конфликтуют                       |
| **Композиция**                        | ❌ Императивная                                       | ✅ Монадическая (`flatMap`, `for`)                          |

## Источники случайности (Randomness Sources)

`cats.effect.std.Random` может работать поверх разных генераторов. Выбор зависит от ваших потребностей:

### 1. `scala.util.Random` (по умолчанию)

Использует `scala.util.Random` (под капотом — `java.util.Random` на JVM, свой на Scala.js).

```dotty
import cats.effect.IO
import cats.effect.std.Random

val random: IO[Random[IO]] = Random.scalaUtilRandom[IO]
```

**Особенности:**

- Быстрый, но **не криптостойкий** (предсказуемый).
- Имеет внутреннее состояние, которое мутируется при каждом вызове.
- Потокобезопасен (использует CAS).

### 2. `java.util.Random` (только JVM)

```dotty
import cats.effect.std.Random
import java.util.Random

val javaRandom: IO[Random[IO]] =
  Random.javaUtilRandom[IO](new java.util.Random(seed))
```

**Особенности:**

- Быстрее стандартного `scala.util.Random` в некоторых сценариях.
- Также не криптостойкий.

### 3. `java.security.SecureRandom` (криптостойкий)

```dotty
import cats.effect.std.SecureRandom

val secureRandom: IO[Random[IO]] = SecureRandom.javaSecuritySecureRandom[IO]
```

**Особенности:**

- Подходит для генерации ключей, токенов, паролей.
- **Медленнее** обычных генераторов.
- Блокирует, если энтропия в системе исчерпана (можно настроить).

## Примеры использования

### Базовый: бросок кубика (d6)

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.{Console, Random}

object DiceRoll extends IOApp.Simple:
  def rollD6(using Random[IO]): IO[Int] =
    Random[IO].betweenInt(1, 7) // от 1 до 6 включительно

  val run: IO[Unit] =
    for
      given Random[IO] <- Random.scalaUtilRandom[IO]
      roll             <- rollD6
      _                <- Console[IO].println(s"Выпало: $roll")
    yield ()
```

### Генерация случайного пароля

```dotty
def randomPassword(length: Int)(using Random[IO]): IO[String] =
  List.fill(length)(Random[IO].nextPrintableChar).sequence.map(_.mkString)

val program =
  for
    given Random[IO] <- Random.scalaUtilRandom[IO]
    pwd              <- randomPassword(12)
    _                <- IO.println(s"Ваш пароль: $pwd")
  yield ()
// Ваш пароль: "N}D.ETBr5{?  
```

### Перемешивание списка

```dotty
val items = List("яблоко", "банан", "вишня", "финик", "бузина")
val shuffled: IO[List[String]] =
  for
    given Random[IO] <- Random.scalaUtilRandom[IO]
    shuffled         <- Random[IO].shuffleList(items)
    _                <- IO.println(s"Перемешано: $shuffled")
  yield shuffled
```

### Генерация UUID (на основе SecureRandom)

```dotty
import java.util.UUID

def randomUUID(using Random[IO]): IO[UUID] =
  for
    mostSig  <- Random[IO].nextLong
    leastSig <- Random[IO].nextLong
  yield new UUID(mostSig, leastSig)

val secureProgram =
  for
    given Random[IO] <- SecureRandom.javaSecuritySecureRandom[IO]
    uuid             <- randomUUID
    _                <- IO.println(s"UUID: $uuid")
  yield ()
// UUID: c4dd3d7a-305f-f461-c838-10b29d9dbbd1  
```

## Random и `Sync` — почему это важно?

Все источники `Random` требуют `Sync[F]`. Это связано с тем, что:

1. **Генерация случайного числа — это побочный эффект** (мутация состояния генератора).
2. `Sync` предоставляет `delay`, который позволяет обернуть нечистый код в эффект.

```dotty
// Упрощённая реализация nextInt для scala.util.Random
def scalaUtilRandom[F[_]: Sync]: F[Random[F]] = Sync[F].delay {
  new Random[F]:
    private val rng     = new scala.util.Random()
    def nextInt: F[Int] = Sync[F].delay(rng.nextInt())
    // ...
}
```

## Сравнение с другими подходами к рандомизации в Scala

| Подход                                      | Чистота         | Платформы        | Криптостойкость  | Производительность  |
|:--------------------------------------------|:----------------|:-----------------|:-----------------|:--------------------|
| `scala.util.Random`                         | ❌               | JVM              | ❌                | Высокая             |
| `cats.effect.std.Random` (scalaUtilRandom)  | ✅               | JVM, JS, Native  | ❌                | Высокая             |
| `cats.effect.std.SecureRandom`              | ✅               | JVM              | ✅                | Низкая              |
| `cats.effect.std.Random` (javaUtilRandom)   | ✅               | JVM              | ❌                | Очень высокая       |
| `cats.effect.std.Random` (systemRandom)     | ✅               | Все              | ❌                | Средняя             |
| `IO(java.security.SecureRandom)` вручную    | ❌ (не всегда)   | JVM              | Зависит          | Низкая              |

## Итог: когда использовать какой источник?

| Сценарий                                                    | Рекомендуемый источник                              |
|:------------------------------------------------------------|:----------------------------------------------------|
| **Игры, симуляции, предсказуемые случайности**              | `Random.scalaUtilRandom`                            |
| **Высокопроизводительная генерация (JVM)**                  | `Random.javaUtilRandom` (с выделенным экземпляром)  |
| **Генерация токенов, ключей, паролей**                      | `SecureRandom.javaSecuritySecureRandom`             |
| **Кроссплатформенное приложение (минимальные требования)**  | `Random.systemRandom`                               |

**Ключевая идея:** `Random` — это функциональная, безопасная и гибкая абстракция для работы с рандомизацией в Cats Effect. 
Она позволяет выбирать подходящий источник энтропии и при этом писать чистый, композируемый и тестируемый код.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect Random](https://typelevel.org/cats-effect/docs/std/random)

[doc]: https://typelevel.org/cats-effect/docs/concepts

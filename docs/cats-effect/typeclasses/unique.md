{%
showPrev = true
prevPage = ../typeclasses.html
showNext = true
nextPage = clock.html
%}

# Unique — источник уникальных токенов

## Введение

В функциональном программировании с эффектами часто возникает потребность 
в **различении разных экземпляров одного и того же значения** 
или в **генерации уникальных идентификаторов** для отслеживания выполнения эффектов. 
Класс типов `Unique[F[_]]` предоставляет минималистичный, 
но мощный способ создания уникальных токенов в контексте эффекта `F[_]`.

## Проблема, которую решает Unique

Представьте, что вы пишете систему логирования, где нужно отслеживать цепочку вызовов:

```dotty
def processRequest[F[_]: Monad](request: Request): F[Response] =
  for
    _      <- log("Processing started")
    result <- handleRequest(request)
    _      <- log("Processing finished")
  yield result
```

Как различить разные запросы в логах? Можно использовать:

- **Случайные числа** — но нет гарантии уникальности
- **UUID** — избыточно для локального трекинга (128 бит, глобальная уникальность)
- **AtomicInteger** — требует `Sync`, но не выражается в типах

`Unique` даёт простое решение: **гарантированно уникальный локальный идентификатор** с минимальными накладными расходами.

## Определение и ключевые концепции

### Базовый контракт

```dotty
trait Unique[F[_]]:
  def unique: F[Unique.Token]
```

**Что это значит:**

- `unique` — это **эффект**, который при выполнении генерирует новый уникальный токен
- Каждый вызов `unique` возвращает **разный** экземпляр `Token`
- Токены сравниваются **только по ссылке** (reference equality)

### Сопутствующий объект и синтаксис

```dotty
object Unique:
  def apply[F[_]: Unique as UN]: UN.type = UN

  final class Token extends Serializable

  object Token:
    given tokenHash: Hash[Token] =
      Hash.fromUniversalHashCode[Token]
```

**Важные детали:**

- `Token` — финальный класс, наследование запрещено
- Сериализуемый (`Serializable`) — но с оговорками (см. ниже)
- Имеет экземпляр `Hash` (из Cats), основанный на `hashCode` (который может коллидировать!)

## Гарантии уникальности

### Что гарантировано

```dotty
val token1: F[Unique.Token] = Unique[F].unique
val token2: F[Unique.Token] = Unique[F].unique

(token1, token2).mapN { (x, y) => x === y } === Monad[F].pure(false)
```

Два разных вызова `unique` **всегда** дают неравные токены.

### Что НЕ гарантировано

1. **Глобальная уникальность между процессами** — токены уникальны только в рамках одной JVM/JavaScript VM
2. **Сохранение уникальности при сериализации** — после десериализации токены могут совпасть
3. **Уникальность `hashCode`** — возможны коллизии (хотя и маловероятны)
4. **Иммутабельность после сериализации** — токен теряет свою "уникальность"

```dotty
// НЕПРАВИЛЬНО: передача через сеть
val token = Unique[F].unique
sendOverNetwork(token)  // На другой стороне токен может совпасть с другим!

// НЕПРАВИЛЬНО: сохранение в БД
val token = Unique[F].unique
db.insert(token)  // При следующем запуске токен может быть сгенерирован заново
```

### Пример неправильного использования

```dotty
case class User(id: Unique.Token, name: String)

def createUser[F[_]: {Unique, Monad}](name: String): F[User] =
  Unique[F].unique.map(token => User(token, name))

// ПРОБЛЕМА: токен нельзя сохранить в БД!
```

**Почему это плохо:**

- При перезапуске приложения токены могут повторяться
- Две разные JVM могут сгенерировать одинаковые токены
- После десериализации `equals` может работать непредсказуемо

### Правильная альтернатива

```dotty
import java.util.UUID

case class User(id: UUID, name: String)

def createUser[F[_]: Sync](name: String): F[User] =
  Sync[F].delay(UUID.randomUUID()).map(uuid => User(uuid, name))
```

**Преимущества UUID:**

- Глобальная уникальность (вероятность коллизии ~10^-15)
- Безопасная сериализация
- Можно передавать по сети и хранить в БД
- Человеко-читаемое представление (опционально)

## Где и когда использовать Unique?

### **Локальный трекинг выполнения**

```dotty
def traced[F[_]: {Unique, Monad, Logger as log}, A](fa: F[A]): F[A] =
  for
    traceId <- Unique[F].unique
    _       <- logger.debug(s"Starting operation with traceId: $traceId")
    result  <- fa
    _       <- logger.debug(s"Finished operation with traceId: $traceId")
  yield result
```

### **Реализация кэша с идентификацией**

```dotty
class Cache[F[_]: {Unique, Ref.Make}, K, V]:
  private val storage: F[Ref[F, Map[Unique.Token, V]]] =
    Ref.of[F, Map[Unique.Token, V]](Map.empty)

  def store(value: V): F[Unique.Token] =
    for
      token <- Unique[F].unique
      cache <- storage
      _     <- cache.update(_ + (token -> value))
    yield token
```

### **Отладка race conditions**

```dotty
def debugRace[F[_]: {Unique, Monad}, A, B](
    fa: F[A],
    fb: F[B]
): F[Either[A, B]] =
  for
    raceId <- Unique[F].unique
    _      <- log(s"Starting race $raceId")
    result <- Spawn[F].race(fa, fb)
    _      <- log(s"Race $raceId finished with $result")
  yield result
```

### Неподходящие сценарии

- **Идентификаторы пользователей/заказов** → используйте `UUID` или автоинкремент в БД
- **Ссылки в API** → нужны стабильные идентификаторы
- **Долгосрочное хранение** → токены не переживают перезапуск
- **Распределённые системы** → нет гарантий между узлами

## Реализация Unique для различных эффектов

### Sync-реализация (через ссылочное равенство)

```dotty
given syncUnique[F[_]: Sync]: Unique[F] with
  def unique: F[Unique.Token] =
    Sync[F].delay(new Unique.Token()) 
```

**Как это работает:** В JVM каждый `new` создаёт новый объект в куче, и ссылочное равенство (`eq`) гарантирует уникальность.

## Итог: Когда и зачем использовать Unique?

### ✅ **Используйте, когда нужно:**

- Локально уникальный идентификатор в рамках одного эффекта
- Отслеживание выполнения без накладных расходов `UUID`
- Реализация низкоуровневых примитивов (кэши, трейсинг)
- Тестирование конкурентного кода

### ❌ **Не используйте, если:**

- Нужна глобальная уникальность (между процессами/серверами)
- Токены нужно сериализовать/хранить
- Идентификаторы видны пользователям
- Нужна гарантия уникальности после перезапуска

### 💡 **Золотое правило:**

`Unique.Token` — это **одноразовый идентификатор времени выполнения**, а не **персистентный бизнес-ключ**. 
Используйте его как локальный маркер, но не как замену `UUID` или автоинкременту в БД.

---

**Ссылки:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts

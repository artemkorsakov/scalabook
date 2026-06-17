{%
showPrev = true
prevPage = alleycats.html
showNext = true
nextPage = ../spire.html
%}

# Быстрый старт

Это руководство по быстрому старту едва затрагивает поверхность того, на что способен Cats, 
но предоставляет сжатое практическое введение в те паттерны, которые вы, скорее всего, захотите использовать в вашем Scala-проекте.

Если вы ежедневно работаете с такими конструкциями, 
как [`Future`](http://www.scala-lang.org/api/current/scala/concurrent/Future.html), 
[`Option`](http://www.scala-lang.org/api/current/scala/Option.html) 
или [`Either`](http://www.scala-lang.org/api/current/scala/util/Either.html), 
весьма вероятно, что Cats может упростить и улучшить читаемость вашего кода.

> **Примечание:** В этом руководстве используется версия Cats 2.13.0. 

Давайте пройдёмся по библиотеке пакет за пакетом, рассматривая синтаксис, доступный в каждом из них.

## Вспомогательные методы для Option

```dotty
import cats.syntax.all.*
```

Импорт этого пакета добавляет синтаксис `obj.some` — эквивалент `Some(obj)`.
Единственное реальное отличие состоит в том, что значение уже приведено к типу `Option[T]` (а не `Some[T]`), 
что помогает компилятору с выводом типов.

Использование `obj.some` вместо `Some(obj)` может улучшить читаемость, 
например, когда вам нужно предоставить фиктивную (dummy) реализацию методов сервиса для целей тестирования.

Рассмотрим пример. Если вы добавите в область видимости следующий неявный класс:

```dotty
import scala.concurrent.Future

extension [T](obj: T)
  def asFuture: Future[T] = Future.successful(obj)
```

то вы сможете использовать цепочку вызовов, как показано ниже:

```dotty
class Account { /* ... */ }

trait AccountService:
  def getAccountById(id: Int): Future[Option[Account]]

class DummyAccountServiceImpl extends AccountService:

  def dummyAccount: Account = ???

  /* ... */
  override def getAccountById(id: Int): Future[Option[Account]] =
    dummyAccount.some.asFuture
  /* ... */

```

Это гораздо читаемее, чем `Future.successful(Some(dummyAccount))`, особенно если этот паттерн часто повторяется. 
Цепочка `.some.asFuture` в конце позволяет сосредоточиться на том, что именно возвращается, 
а не на том, в какую обёртку это упаковано.

`none[T]`, в свою очередь, является сокращением для `Option.empty[T]`, 
то есть для `None`, но уже приведённого от `None.type` к `Option[T]`. 
Указание более специализированного типа иногда помогает компилятору Scala правильно вывести тип выражений, содержащих `None`.

## Вспомогательные методы для Either

```dotty
import cats.syntax.all.*
```

`obj.asRight` — это `Right(obj)`, а `obj.asLeft` — это `Left(obj)`.
В обоих случаях тип возвращаемого значения расширяется от `Right` или `Left` до `Either`.
Как и в случае с `.some`, эти вспомогательные методы удобно комбинировать с `.asFuture` для улучшения читаемости:

```dotty
case class User(accountId: Long) { /* ... */ }

trait UserService:
  def ensureUserExists(id: Int): Future[Either[Exception, User]]

class UserServiceSpec extends UserService:

  def dummyUser: User = ???

  /* ... */
  override def ensureUserExists(id: Int): Future[Either[Exception, User]] =
    dummyUser.asRight.asFuture // вместо Future.successful(Right(dummyUser))
  /* ... */
```

`Either.fromOption(option: Option[A], ifNone: => E)`, в свою очередь, 
является полезным вспомогательным методом для преобразования `Option` в `Either`.
Если переданный `option` равен `Some(x)`, результатом будет `Right(x)`.
В противном случае возвращается `Left` с указанным значением `ifNone`.

## Кортежи и `mapN`

Пакет `apply` предоставляет синтаксис `(..., ..., ...).mapN`, который позволяет интуитивно применять функцию, 
принимающую несколько параметров, к нескольким эффектным значениям (например, к будущим результатам).

Предположим, у нас есть 3 фьючерса: один типа `Int`, один типа `String` и один типа `User`, 
а также метод, принимающий три параметра — `Int`, `String` и `User`.

```dotty
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class User

class ProcessingResult { /* ... */ }

def intFuture: Future[Int]       = /* ... */ ???
def stringFuture: Future[String] = /* ... */ ???
def userFuture: Future[User]     = /* ... */ ???

def process(
    value: Int,
    contents: String,
    user: User
): ProcessingResult = /* ... */ ???
```

Наша цель — применить функцию к значениям, вычисляемым этими тремя фьючерсами.
С синтаксисом `apply` это становится очень простым и лаконичным:

```dotty
import cats.syntax.all.*

def processAsync: Future[ProcessingResult] =
  (intFuture, stringFuture, userFuture).mapN {
    (value, contents, user) =>
      process(value, contents, user)
  }
```

Неявные экземпляры (а именно, `Functor[Future]` и `Semigroupal[Future]`, необходимые для работы `mapN`, всегда видимы по умолчанию. 
Они находятся в соответствующих сопутствующих объектах, поэтому их не нужно импортировать явно.

Эту же идею можно выразить ещё короче:

```dotty
def processAsync2: Future[ProcessingResult] = (intFuture, stringFuture, userFuture).mapN(process)
```

Если какой-либо из связанных фьючерсов завершится с ошибкой, 
результирующий фьючерс также завершится с той же ошибкой (это поведение **fail-fast** — быстрый отказ).

**Важно:** все фьючерсы будут выполняться параллельно, в отличие от того, что происходит в `for`-выражении:

```dotty
def processAsync3: Future[ProcessingResult] =
  for
    value    <- intFuture
    contents <- stringFuture
    user     <- userFuture
  yield process(value, contents, user)
```

В приведённом выше фрагменте (который под капотом транслируется в вызовы `flatMap` и `map`) 
`stringFuture` не запустится до тех пор, пока `intFuture` не завершится успешно, 
и аналогично `userFuture` будет запущен только после завершения `stringFuture`. 
Но поскольку вычисления независимы друг от друга, их вполне можно запускать параллельно с помощью `mapN`.

## Traverse (обход коллекций)

```dotty
import cats.syntax.all.*
```

### `traverse`

Если у вас есть экземпляр `obj` типа `F[A]`, который можно отобразить (например, `Future`), 
и функция `fun` типа `A => G[B]`, то вызов `obj.map(fun)` даст вам `F[G[B]]`.
Во многих распространённых реальных случаях, например, когда `F` — это `Option`, а `G` — `Future`, 
вы получите `Option[Future[B]]`, что, скорее всего, не то, что вам нужно.

`traverse` приходит на помощь.
Если вы вызовете `traverse` вместо `map`, например `obj.traverse(fun)`, вы получите `G[F[A]]`, 
что в нашем случае будет `Future[Option[B]]` — это гораздо полезнее и проще для обработки, чем `Option[Future[B]]`.

```dotty
def updateUser(user: User): Future[User] = /* ... */ ???

def updateUsers(users: List[User]): Future[List[User]] =
  users.traverse(updateUser)
```

**На заметку:** В сопутствующем объекте [`Future`](http://www.scala-lang.org/api/current/scala/concurrent/Future$.html) 
также есть специальный метод `Future.traverse`, но версия Cats гораздо более читаема и может работать с любой структурой, 
для которой доступны определённые классы типов.

### `sequence`

`sequence` представляет ещё более простую концепцию: 
его можно представить как простую замену типов с `F[G[A]]` на `G[F[A]]` 
без отображения вложенного значения (в отличие от `traverse`).

```dotty
val foo: List[Future[String]] = List(Future("hello"), Future("world"))
val bar = foo.sequence // тип будет Future[List[String]]
```

В Cats `obj.sequence` фактически реализован как `obj.traverse(identity)`.

С другой стороны, `obj.traverse(fun)` примерно эквивалентен `obj.map(fun).sequence`.

### `flatTraverse`

Если у вас есть `obj` типа `F[A]` и функция `fun` типа `A => G[F[B]]`, 
то вызов `obj.map(fun)` даст результат типа `F[G[F[B]]]` — крайне маловероятно, что это то, что вы хотели.

Обход (traverse) объекта вместо отображения помогает немного: вы получите `G[F[F[B]]]`.
Поскольку `G` обычно является чем-то вроде `Future`, а `F` — `List` или `Option`, 
вы получите `Future[Option[Option[A]]]` или `Future[List[List[A]]]` — что довольно неудобно для обработки.

```dotty
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

lazy val valueOpt: Option[Int]                    = /* ... */ ???
def compute(value: Int): Future[Option[Int]]      = /* ... */ ???
def computeOverValue: Future[Option[Option[Int]]] =
  valueOpt.traverse(compute)
// имеет тип Future[Option[Option[Int]]], не очень хорошо
```

Решением может быть применение `_.flatten` к результату:

```dotty
def computeOverValue2: Future[Option[Int]] = valueOpt.traverse(compute).map(_.flatten)
```

Таким образом вы получите желаемый тип `G[F[B]]`.

Однако для этого есть удобное сокращение — `flatTraverse`:

```dotty
def computeOverValue3: Future[Option[Int]] = valueOpt.flatTraverse(compute)
```

И это решает нашу проблему окончательно.

## Монадные трансформеры (Monad Transformers)

### OptionT

```dotty
import cats.data.OptionT
```

Экземпляр [`OptionT[F, A]`](../../typeclass/monad/monad-transformer.md) можно рассматривать как обёртку над `F[Option[A]]`, 
которая добавляет несколько полезных методов, специфичных для вложенных типов и недоступных в самих `F` или `Option`.
Чаще всего вашим `F` будет `Future` (или иногда `DBIO` из Slick, но для этого потребуется реализация классов типов Cats, 
таких как `Functor` или `Monad`, для `DBIO`).
Такие обёртки, как `OptionT`, обычно называют [**монадными трансформерами**](../../typeclass/monad/monad-transformer.md).

Весьма распространённый паттерн — отображение внутреннего значения, 
хранящегося в `F[Option[A]]`, в `F[Option[B]]` с помощью функции типа `A => B`.
Это можно сделать с помощью довольно многословного синтаксиса:

```dotty
lazy val resultFuture: Future[Option[Int]] = ???

def mappedResultFuture: Future[Option[String]] =
  resultFuture.map: maybeValue =>
    maybeValue.map: value =>
      // Делаем что-то со значением и возвращаем String
      ???
```

С использованием `OptionT` это можно упростить:

```dotty
def mappedResultFuture2: OptionT[Future, String] =
  OptionT(resultFuture).map: value =>
    // Делаем что-то со значением и возвращаем String
    ???
```

Вызов `map` выше вернёт значение типа `OptionT[Future, String]`.

Чтобы получить базовое значение `Future[Option[String]]`, просто вызовите `.value` на экземпляре `OptionT`.
Также вполне жизнеспособным решением является полный переход на использование `OptionT[Future, A]` 
в типах параметров/возвращаемых значений методов и полный (или почти полный) отказ от `Future[Option[A]]` в объявлениях типов.

#### Способы создания OptionT

|               Метод                |   Принимает    |   Возвращает    |
|:----------------------------------:|:--------------:|:---------------:|
| `OptionT.apply` или `OptionT(...)` | `F[Option[A]]` | `OptionT[F, A]` |
|        `OptionT.fromOption`        |  `Option[A]`   | `OptionT[F, A]` |
|          `OptionT.liftF`           |     `F[A]`     | `OptionT[F, A]` |
|           `OptionT.pure`           |      `A`       | `OptionT[F, A]` |

В production-коде вы чаще всего будете использовать синтаксис `OptionT(...)` 
для обёртывания экземпляра `Future[Option[A]]` в `OptionT[F, A]`. 
Другие методы, в свою очередь, полезны для настройки фиктивных значений типа `OptionT` в модульных тестах.

#### Методы OptionT

|      Метод       |      Принимает       |   Возвращает    |
|:----------------:|:--------------------:|:---------------:|
|     `map[B]`     |       `A => B`       | `OptionT[F, B]` |
| `subflatMap[B]`  |   `A => Option[B]`   | `OptionT[F, B]` |
| `semiflatMap[B]` |     `A => F[B]`      | `OptionT[F, B]` |
|  `flatMapF[B]`   | `A => F[Option[B]]`  | `OptionT[F, B]` |
|   `flatMap[B]`   | `A => OptionT[F, B]` | `OptionT[F, B]` |

На практике вы, скорее всего, будете использовать `map` и `semiflatMap`.

Как всегда с `flatMap` и `map`, вы можете использовать их не только явно, но и внутри `for`-выражений, как в примере ниже:

```dotty
class Money { /* ... */ }

def findUserById(userId: Long): OptionT[Future, User] = /* ... */ ???

def findAccountById(accountId: Long): OptionT[Future, Account] = /* ... */ ???

def getReservedFundsForAccount(account: Account)
    : OptionT[Future, Money] = /* ... */ ???

def getReservedFundsForUser(userId: Long): OptionT[Future, Money] =
  for
    user    <- findUserById(userId)
    account <- findAccountById(user.accountId)
    funds   <- getReservedFundsForAccount(account)
  yield funds
```

Экземпляр `OptionT[Future, Money]`, возвращаемый `getReservedFundsForUser`, будет содержать `None`, 
если любой из трёх вызванных методов вернёт `OptionT`, соответствующий `None`.
В противном случае, если все три вызова вернут `Some`, конечный результат также будет содержать `Some`.

### EitherT

```dotty
import cats.data.EitherT
```

`EitherT[F, A, B]` — это [монадный трансформер](../../typeclass/monad/monad-transformer.md) для `Either`. 
Его можно рассматривать как обёртку над значением `F[Either[A, B]]`.

### Способы создания EitherT

|                Метод                |     Принимает     |                            Возвращает                            |
|:-----------------------------------:|:-----------------:|:----------------------------------------------------------------:|
| `EitherT.apply` или `EitherT(...)`  | `F[Either[A, B]]` |                        `EitherT[F, A, B]`                        |
|        `EitherT.fromEither`         |  `Either[A, B]`   |    `EitherT[F, A, B]` (оборачивает переданный `Either` в `F`)    |
| `EitherT.right` или `EitherT.liftF` |      `F[B]`       |   `EitherT[F, A, B]` (оборачивает значение в `F[B]` в `Right`)   |
|           `EitherT.left`            |      `F[A]`       |   `EitherT[F, A, B]` (оборачивает значение в `F[A]` в `Left`)    |
|           `EitherT.pure`            |        `A`        | `EitherT[F, A, B]` (оборачивает значение в `Right`, затем в `F`) |

Ещё один полезный способ создания `EitherT` — использование методов `toLeft` и `toRight` из `OptionT`:

```dotty
abstract class BaseException(message: String) extends Exception(message)

case class UserNotFoundException(message: String) extends BaseException(message)

def getUserById(userId: Int): Future[Option[User]] = /* ... */ ???

def ensureUserExists(userId: Int): EitherT[Future, BaseException, User] =
  OptionT(getUserById(userId))
    .toRight(left = UserNotFoundException(s"user not found, userId=$userId"))
```

`toRight` довольно похож на метод `Either.fromOption`, упомянутый ранее: 
как `fromOption` строил `Either` из `Option`, так и `toRight` создаёт `EitherT` из `OptionT`.
Если исходный `OptionT` хранит значение `Some`, оно будет обёрнуто в `Right`; 
в противном случае значение, переданное как параметр `left`, будет обёрнуто в `Left`.
Для передачи значения `left` внутри монады существует соответствующий метод `toRightF`.

`toLeft` — это аналог `toRight`, который оборачивает значение `Some` в `Left` и преобразует `None` в `Right`, 
содержащий переданное значение `right`.
На практике это используется реже, но может пригодиться, 
например, для проверки уникальности: мы возвращаем `Left`, если значение найдено, и `Right`, если его ещё нет в системе.

#### Методы EitherT (правосмещённые)

`EitherT` является _правосмещённым_ (right-biased), поэтому обычный `map` работает со значением `Right`.

|      Метод       | Принимает               | Возвращает         |
|:----------------:|-------------------------|--------------------|
|     `map[D]`     | `B => D`                | `EitherT[F, A, D]` |
| `subflatMap[D]`  | `B => Either[A, D]`     | `EitherT[F, A, D]` |
| `semiflatMap[D]` | `B => F[D]`             | `EitherT[F, A, D]` |
|  `flatMapF[D]`   | `B => F[Either[A, D]]`  | `EitherT[F, A, D]` |
|   `flatMap[D]`   | `B => EitherT[F, A, D]` | `EitherT[F, A, D]` |

> В `EitherT` также есть методы, работающие с `Left`, например `leftMap`, или с обеими сторонами, например `fold` и `bimap`.

#### Практический пример: цепочка проверок с быстрым отказом

```dotty
case class Item(state: String)
class ItemOrder { /* ... */ }

case class ItemNotFoundException(message: String) extends BaseException(message)
case class InvalidItemStateException(message: String)
    extends BaseException(message)

def getItemById(itemId: Int): Future[Option[Item]] = /* .. */ ???

def ensureItemExists(itemId: Int): EitherT[Future, BaseException, Item] =
  OptionT(getItemById(itemId))
    .toRight(ItemNotFoundException(s"item not found, itemId = $itemId"))

def ensureItemStateIs(
    actual: String,
    expected: String
): EitherT[Future, BaseException, Unit] =
  // Возвращает Unit, обёрнутый в Right, а затем в Future, если условие истинно,
  // иначе — исключение, обёрнутое в Left, а затем в Future.
  EitherT.cond(
    actual == expected,
    (),
    InvalidItemStateException(s"actual=$actual, expected=$expected")
  )

def placeOrderForItem(
    userId: Int,
    itemId: Int,
    count: Int
): Future[ItemOrder] = /* ... */ ???

def buyItem(
    userId: Int,
    itemId: Int,
    count: Int
): EitherT[Future, BaseException, ItemOrder] =
  for
    user <- ensureUserExists(userId)
    item <- ensureItemExists(itemId)
    _    <- ensureItemStateIs(item.state, "AVAILABLE_IN_STOCK")
    // EitherT.liftF необходим, чтобы преобразовать Future[ItemOrder] в EitherT[Future, BaseException, ItemOrder]
    placedOrder <- EitherT.liftF(placeOrderForItem(userId, itemId, count))
  yield placedOrder
```

В этом примере мы последовательно выполняем проверки. 
Если какая-либо проверка не проходит, результирующий `EitherT` будет содержать `Left`.
В противном случае (все проверки дают `Right`) конечный результат также будет содержать `Right`.
Это поведение **fail-fast**: поток `for`-выражения останавливается на первом же результате `Left`.

Если вам нужна валидация с накоплением ошибок (например, при работе с данными форм, введёнными пользователем), 
хорошим выбором может стать `cats.data.Validated`.

## Импорты

Самый простой подход к импортам Cats — импортировать всё, что обычно нужно:

```dotty
import cats.*
import cats.data.*
import cats.syntax.all.*
```

**`cats.*`** — приносит множество [классов типов](../../typeclass/index.md) (аналогов интерфейсов), 
таких как [`Monad`](../../typeclass/monad/monad.md), [`Semigroup`](../../typeclass/monoid/semigroup.md), 
[`Foldable`](../../typeclass/monad/foldable.md) и другие. 
Вместо всего пакета `cats` можно импортировать только нужные типы, например:

```dotty
import cats.Monad
import cats.Semigroup
import cats.Foldable
```

**`cats.data.*`** — приносит структуры данных, 
такие как [`Validated`](../../fp/handling-errors.md) и [`State`](../../fp/state.md). 
Вместо всего пакета можно импортировать только нужные:

```dotty
import cats.data.Validated
import cats.data.State
```

**`cats.syntax.all.*`** — добавляет синтаксическое обогащение для некоторых типов, 
предоставляя удобные методы, такие как правосмещённые комбинаторы для `Either`:

```dotty 
import cats.syntax.all.*

// Cats добавляет правосмещённые комбинаторы для стандартного Either
val e: Either[String, Int] = Right(3)
e.map(_ + 1)

// Cats добавляет метод orEmpty для стандартного Option
val o: Option[String] = None
o.orEmpty
```

Если вы предпочитаете импортировать только необходимое, вы можете импортировать из `cats.syntax` для синтаксического обогащения.
Например, чтобы импортировать синтаксис для `Semigroup`:

```dotty 
import cats.syntax.semigroup.*

"Hello, " |+| "World!"
```

Этот импорт добавляет синтаксис `|+|`.

---

**Ссылки:**

- [Cats home page][home]

[home]: https://typelevel.org/cats/

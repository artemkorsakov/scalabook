# Pure functional HTTP APIs in Scala

В этом разделе приведен пример реализации на Scala 3 чистой имплементации http-сервиса 
из книги [Grassel Jens - Pure functional HTTP APIs in Scala][book].

Для реализации будем использовать следующие библиотеки:

- http4s
- Doobie (в качестве базы данных)
- Flyway для миграции БД (или запуска скриптов)
- Circe для JSON кодеков
- Iron для [использования уточняющих типов][refined] (вместо библиотеки refined, как в примере книги)
- PostgreSQL JDBC драйвер
- pureconfig (для загрузки файлов конфигурации)

> В примерах используются Scala `3.3.0` и последние версии библиотек на июль 2023.

## Спецификация сервиса 

Сначала укажем точную область действия и API сервиса. 
Сервис должен предоставлять конечные точки HTTP API для: 

- создания типа данных продукта, определяемого уникальным идентификатором
- добавления переводов имени продукта по коду языка и уникальному идентификатору
- возврата существующих переводов для продукта
- возврата списка всех существующих продуктов с их переводами

### Модель данных 

Определим модель очень простой, чтобы не переборщить с реализацией. 

- Код языка определяется стандартом ISO 639-1 (например, двухбуквенный код). 
- Перевод должен содержать код языка и название продукта (непустая строка). 
- Продукт должен содержать уникальный идентификатор (UUID версии 4) и список переводов.

### База данных 

Данные будут храниться в реляционной базе данных (RDBMS). 
Поэтому нам нужно определить таблицы и отношения в базе данных. 

#### Таблица продуктов 

Таблица `products` должна содержать только уникальный идентификатор, который также является первичным ключом. 

#### Таблица имен 

Таблица `names` должна содержать столбец для идентификатора продукта, один для кода языка и один для имени. 
Его первичный ключ представляет собой комбинацию идентификатора продукта и кода языка. 
Все столбцы должны быть не нулевыми.
Отношение к продуктам реализуется ограничением внешнего ключа к таблице продуктов через идентификатор продукта.

### HTTP API

HTTP API должен предоставлять следующие эндпоинты на заданных путях:

| **Path**          | **HTTP method** | **Function**                                    |
|-------------------|-----------------|-------------------------------------------------|
| `/products`       | POST            | Создание продукта                               |
| `/products`       | GET             | Получение всех продуктов и переводов            |
| `/product/{UUID}` | PUT             | Добавление перевода                             |
| `/product/{UUID}` | GET             | Получение всех переводов для заданного продукта |

Данные должны быть закодированы в JSON с использованием следующей спецификации:

JSON для перевода:

```json
{
  "lang": "ISO-639-1 Code",
  "name": "A non empty string."
}
```

JSON для продукта:

```json
{
  "id": "The-UUID-of-the-product",
  "names": [ ... список переводов ... ]
}
```

## Модели

Для начала реализуем простые и понятные модели. 
Нам нужен класс для хранения переводов или, лучше, одного перевода.

```dotty
final case class Translation(lang: LanguageCode, name: ProductName)
```

В качестве типов параметров используются уточняющие типы, ограничивающие множество значений областью, которую считаем валидной.
Подробнее об уточняющих типах и зачем они нужны [рассказывается в статье][refined].
В данном случае используются такие типы:

```dotty
type LanguageCode = String :| Match["^[a-z]{2}$"]
type ProductName = String :| Not[Blank]
```

В качестве `LanguageCode` используется двухсимвольная строка латинского алфавита в нижнем регистре, 
а в качестве `ProductName` - непустая строка.
Они необходимы для того, чтобы нельзя было создавать `Translation`, например, с `name` равным `null` или `""`.

Необходимые кодеки для кодирования и раскодирования в Json 
[предоставляются библиотекой интеграции Iron с Circe](https://iltotore.github.io/iron/docs/modules/circe.html)

Теперь о модели продукта.
В качестве типа идентификатора используем уточняющий тип, соответствующий формату UUID.
А в качестве типа списка переводов - непустое множество `NonEmptySet` из библиотеки cats.

```dotty
type ProductId = String :| Match["^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"]

final case class Product(id: ProductId, names: NonEmptySet[Translation])
```

#### Тестирование

Отдельно остановимся на тестировании. 
В качестве тестового фреймворка можно выбрать любой фреймворк, я выбрал [MUnit](https://scalameta.org/munit).
Кроме того, используем [интеграцию со ScalaCheck](https://scalameta.org/munit/docs/integrations/scalacheck.html)
для покрытия большего количества вариантов.

Необходимо проверить декодирование `Translation` со следующими кейсами:

- `decode[Translation](s).isLeft`, где `s` - строка не JSON формата
- `decodeAccumulating[Translation](json)` выдает заданный список ошибок 
  (в данном случае `"DecodingFailure at .lang: Should match ^[a-z]{2}$"` и `"DecodingFailure at .name: !(Should only contain whitespaces)"`), 
  где `json` имеет валидный формат, но невалидные значения, например,
  `val json = """{"lang":"rus","name":""}"""`: `lang` - невалидный код, например, `rus` вместо `ru`, а `name` - пустая строка.
- Представим, что есть две переменные `t: Translation` и `val json = s"""{"lang":${t.lang.asJson.noSpaces},"name":${t.name.asJson.noSpaces}}"""`
    - `decode[Translation](json)` должен равняться `t`
    - `t.asJson.noSpaces` должен равняться `json`
    - как следствие двух предыдущих: `decode[Translation](t.asJson.noSpaces)` должен равняться `t`

Аналогично проверяется `Product`:

- `decode[Product](s).isLeft`, где `s` - строка не JSON формата
- `decodeAccumulating[Product](json)` выдает заданный список ошибок,
  где `json` имеет валидный формат, но невалидные значения, например,
  `val json = """{"id":"id12","names":[]}"""`: `id` - произвольная строка, неподходящая по формату к UUID, а `names` - пустая коллекция.
- Представим, что есть две переменные `p: Product` и `val json = s"""{"id":${p.id.asJson.noSpaces},"names":${p.names.asJson.noSpaces}}"""`
    - `decode[Product](json)` должен равняться `p`
    - `p.asJson.noSpaces` должен равняться `json`
    - как следствие двух предыдущих: `decode[Product](p.asJson.noSpaces)` должен равняться `p`

[Исходный код тестов](https://github.com/artemkorsakov/pfhais/tree/my_solutions/pure-scala-3/pure/src/test/scala/com/wegtam/books/pfhais/models)

## Обработка конфигурации

Теперь определимся с конфигурацией.
У нас будет два конфига: `ApiConfig` - конфигурация для http API и `DatabaseConfig` - для работы с БД.
`ApiConfig` должен состоять из `host` и `port`, где `host` - непустая строка, а `port` - число от `1` до `65535`.
`DatabaseConfig` должен состоять из `driver` (непустая строка), `url` (валидный url), 
`user` (непустая строка), `pass` (непустая строка).

Вот как будет выглядеть код в Scala 3:

```dotty
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import pureconfig.*
import pureconfig.generic.derivation.default.*

type NonEmptyString = String :| Not[Blank]
type DatabaseUrl = String :|
  Match["""(\b(https?|ftp|file)://)?[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"""]
type PortNumber = Int :| Interval.Closed[1, 65535]

given ConfigReader[NonEmptyString] =
  ConfigReader.fromString[NonEmptyString](ConvertHelpers.optF(_.refineOption))
given ConfigReader[DatabaseUrl] =
  ConfigReader.fromString[DatabaseUrl](ConvertHelpers.optF(_.refineOption))
given ConfigReader[PortNumber] =
  ConfigReader.fromString[PortNumber](ConvertHelpers.optF(_.toIntOption.flatMap(_.refineOption)))
  
final case class ApiConfig(host: NonEmptyString, port: PortNumber) derives ConfigReader

final case class DatabaseConfig(
    driver: NonEmptyString,
    url: DatabaseUrl,
    user: NonEmptyString,
    pass: NonEmptyString
) derives ConfigReader
```

В первую очередь мы определяем три уточняющих типа: `NonEmptyString`, `DatabaseUrl`, `PortNumber`.

Затем определяются конфиги `ApiConfig` и `DatabaseConfig`. 
Конфиги будут читаться с помощью библиотеки **pureconfig**, 
поэтому используем `derives ConfigReader` - [встроенную поддержку наследования классов типов в Scala 3](https://pureconfig.github.io/docs/scala-3-derivation.html).
К сожалению, на данный момент (июль 2023) для библиотеки iron 
[нет модуля взаимодействия с pureconfig](https://iltotore.github.io/iron/docs/modules/index.html),
поэтому необходимо дополнительно определить `given ConfigReader[T]` для каждого уточняющего типа.

Теперь конфиги могут быть прочитаны следующим образом:

```dotty
val apiConfig = ConfigFactory.parseString(s"""api{"host":"api.example.com","port":1234}""")
ConfigSource.fromConfig(apiConfig).at("api").load[ApiConfig]
// Right(ApiConfig(api.example.com,1234))

val databaseConfig = ConfigFactory.parseString(
  s"""database {
    |  "driver": "org.postgresql.Driver",
    |  "url": "jdbc:postgresql://localhost:5422/test-database",
    |  "user": "pure",
    |  "pass": "password"
    |}""".stripMargin
)
ConfigSource.fromConfig(databaseConfig).at("database").load[DatabaseConfig]
// Right(DatabaseConfig(org.postgresql.Driver,jdbc:postgresql://localhost:5422/test-database,pure,password))
```

[Рассмотренный пример в Scastie](https://scastie.scala-lang.org/Ci4qPxbJQ1iAdpy5GmWhxg)

#### Тестирование

Для проверки корректности создания `ApiConfig` необходимо рассмотреть 4 случая:

- входящая строка в формате Json пустая или невалидная - `"{}"`: `ConfigSource.fromConfig(config).at("api").load[ApiConfig].isLeft`,
  где `val config = ConfigFactory.parseString("{}")`
- `host` - невалидный (пустая строка), `port` - валидный (от `1` до `65535`): как в примере выше, только
  `val config = ConfigFactory.parseString("""api{"host":"","port":1234}""")`
- `host` - валидный (непустая строка), `port` - невалидный (меньше `1` или больше `65535`): как в примере выше, только
  `val config = ConfigFactory.parseString("""api{"host":"localhost","port":65536}""")`
- `host` и `port` валидные: `ConfigSource.fromConfig(config).at("api").load[ApiConfig]` должен равняться
  `config: ApiConfig`, где `val config = ConfigFactory.parseString(s"""api{"host":"${config.host}","port":${config.port}}""")`

Аналогично добавляются тесты для `DatabaseConfig`.

[Исходный код тестов](https://github.com/artemkorsakov/pfhais/tree/my_solutions/pure-scala-3/pure/src/test/scala/com/wegtam/books/pfhais/config)

## Уровень базы данных

Для простоты будем придерживаться Flyway для миграции базы данных. 
Напишем код миграции, используя шаблон интерпретатора (в Scala он стал известен под названием "tagless final").

```dotty
trait DatabaseMigrator[F[_]]:

  def migrate(url: DatabaseUrl, user: NonEmptyString, pass: NonEmptyString): F[Int]
```

Мы определяем трейт, который описывает функциональность, требуемую интерпретатором, 
и используем параметр высшего типа, чтобы иметь возможность абстрагироваться от типа. 
Теперь давайте продолжим с интерпретатором Flyway.

```dotty
final class FlywayDatabaseMigrator extends DatabaseMigrator[IO]:

  override def migrate(url: DatabaseUrl, user: NonEmptyString, pass: NonEmptyString): IO[Int] =
    IO {
      val flyway: Flyway = Flyway.configure()
        .dataSource(url, user, pass)
        .load()
      flyway.migrate().migrationsExecuted
    }
```

#### Doobie

Поскольку мы уже начали с использования "tagless final", мы могли бы продолжить и определить базу для нашего репозитория.

```dotty
trait Repository[F[_]]:

  def loadProduct(id: ProductId): F[Seq[(ProductId, LanguageCode, ProductName)]]

  def loadProducts(): Stream[F, (ProductId, LanguageCode, ProductName)]

  def saveProduct(p: Product): F[Int]

  def updateProduct(p: Product): F[Int]
```

В этом примере используются уточняющие типы, кроме того, возвращаемый тип `loadProducts` связан с `fs2.Stream`, 
потому что мы хотим добиться чисто функциональной потоковой передачи. 

Итак, давайте посмотрим, как выглядит репозиторий, использующий **doobie**.

```dotty
final class DoobieRepository[F[_]: Sync](tx: Transactor[F]) extends Repository[F]:
  import DoobieRepository.given

  override def loadProduct(id: ProductId): F[Seq[(ProductId, LanguageCode, ProductName)]] = ???

  override def loadProducts(): Stream[F, (ProductId, LanguageCode, ProductName)] = ???

  override def saveProduct(p: Product): F[Int] = ???

  override def updateProduct(p: Product): F[Int] = ???

end DoobieRepository

object DoobieRepository:
  given Read[(ProductId, LanguageCode, ProductName)] =
    Read[(String, String, String)].map { case (x, y, z) => (x.refine, y.refine, z.refine) }

  given Write[(ProductId, LanguageCode, ProductName)] =
    Write[(String, String, String)].contramap(p => (p._1, p._2, p._3))
```

Метод получения продукта по заданному идентификатору выглядит так:

```dotty
override def loadProduct(id: ProductId): F[Seq[(ProductId, LanguageCode, ProductName)]] =
  sql"""SELECT products.id, names.lang_code, names.name 
        FROM products
        JOIN names ON products.id = names.product_id
        WHERE products.id = ${id.toString}"""
    .query[(ProductId, LanguageCode, ProductName)]
    .to[Seq]
    .transact(tx)
```

Метод `loadProduct` просто возвращает все строки для одного продукта из базы данных. 
Параметр будет правильно интерполирован Doobie, поэтому здесь нам не нужно беспокоиться о SQL-инъекциях. 
Указываем тип запроса, инструктируем Doobie преобразовать его в последовательность и отдаем транзактору. 

> Обратите внимание, что код в этот момент не запускается! 
> Doobie просто предоставляет свободную структуру (читай [бесплатные монады](../typeclass/monad/free-monads.md)), 
> которую можно интерпретировать позже.

Метод `loadProducts` эквивалентен первому, но возвращает данные обо всех продуктах, 
и в виде потока с использованием библиотеки fs2, обеспечивающей чистую функциональную потоковую передачу.

```dotty
override def loadProducts(): Stream[F, (ProductId, LanguageCode, ProductName)] =
  sql"""SELECT products.id, names.lang_code, names.name
      FROM products
      JOIN names ON products.id = names.product_id
      ORDER BY products.id"""
    .query[(ProductId, LanguageCode, ProductName)]
    .stream
    .transact(tx)
```

При сохранении продукта используется монадическая нотация для нашей программы, чтобы иметь короткое замыкание в случае сбоя. 
Doobie также поместит все команды в транзакцию базы данных. 
Сама функция попытается создать "главную" запись в таблице товаров и впоследствии сохранить все переводы.

```dotty
override def saveProduct(p: Product): F[Int] =
  val namesSql    = "INSERT INTO names (product_id, lang_code, name) VALUES (?, ?, ?)"
  val namesValues = p.names.toNonEmptyList.map(t => (p.id, t.lang, t.name))
  val program = for
    pi <- sql"INSERT INTO products (id) VALUES(${p.id.toString})".update.run
    ni <- Update[(ProductId, LanguageCode, ProductName)](namesSql).updateMany(namesValues)
  yield pi + ni
  program.transact(tx)
```

Метод `updateProduct` также использует монадическую нотацию, как и метод `saveProduct`. 
Разница в том, что сначала он удаляет все известные переводы, прежде чем сохранить заданные.

```dotty
override def updateProduct(p: Product): F[Int] =
  val namesSql    = "INSERT INTO names (product_id, lang_code, name) VALUES (?, ?, ?)"
  val namesValues = p.names.toNonEmptyList.map(t => (p.id, t.lang, t.name))
  val program = for
    dl <- sql"DELETE FROM names WHERE product_id = ${p.id.toString}".update.run
    ts <- Update[(ProductId, LanguageCode, ProductName)](namesSql).updateMany(namesValues)
  yield dl + ts
  program.transact(tx)
```

#### http4s routes

???

---

**Ссылки:**

- [Исходный код разобранных примеров на Scala 3][source]
- Grassel Jens - Pure functional HTTP APIs in Scala:
    - [Книга][book]
    - [Исходный код книги][book source]

[book]: https://leanpub.com/pfhais
[refined]: ../libs/refined.md
[book source]: https://github.com/jan0sch/pfhais
[source]: https://github.com/artemkorsakov/pfhais/tree/my_solutions/pure-scala-3
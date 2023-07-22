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

## Чистая обработка конфигурации

Для начала определимся с конфигурацией.
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
Они необходимы для того, чтобы нельзя было создавать `ApiConfig`, например, с `host` равным `null` или `""`.
Подробнее об уточняющих типах и зачем они нужны [рассказывается в статье][refined].

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
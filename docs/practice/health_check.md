{%
showPrev = true
prevPage = authorization/create.html
showNext = true
nextPage = ui_authorization.html
%}

# Система мониторинга работоспособности

Важно иметь механизм контроля состояния сервисов. Health check (проверка работоспособности) позволяет:

- **Обнаруживать проблемы** до того, как они повлияют на пользователей
- **Информировать системы оркестрации** (Kubernetes, Docker Swarm) о необходимости перезапуска
- **Мониторить зависимые сервисы** (базы данных, кэши, очереди)
- **Предоставлять метрики** для систем наблюдения (Prometheus, Grafana)

Приложение реализует проверку состояния, включающую два критических компонента: подключение к PostgreSQL и Redis.

### Модель данных для статуса приложения

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/AppStatus.scala
final case class AppStatus(redis: RedisStatus, postgres: PostgresStatus)
    derives Encoder

object AppStatus:
  // Эти типы не взаимозаменяемы на уровне типов, хотя внутри это простые статусы
  opaque type RedisStatus    = Status
  opaque type PostgresStatus = Status

  object RedisStatus:
    def apply(status: Boolean): RedisStatus =
      Status.find(status)

  object PostgresStatus:
    def apply(status: Boolean): PostgresStatus =
      Status.find(status)

  enum Status(isOk: Boolean):
    case Okay()        extends Status(true)
    case Unreachable() extends Status(false)

  object Status:
    def find(status: Boolean): RedisStatus =
      if status then Status.Okay()
      else Status.Unreachable()

  given encoderStatus: Encoder[Status] =
    Encoder.encodeString.contramap(_.productPrefix)
end AppStatus
```

**Ключевые особенности модели:**

1. **Opaque types** - обеспечивают типобезопасность, не позволяя перепутать `RedisStatus` с `PostgresStatus`
2. **Строгая типизация состояний** - перечисление гарантирует только два возможных значения
3. **JSON сериализация** - готовность к отправке через HTTP API

### Интерфейс сервиса HealthCheck

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/HealthCheck.scala
trait HealthCheck[F[_]]:
  def status: F[AppStatus]
```

### Реализация HealthCheck

```dotty
object HealthCheck:
  def make[F[_]: Temporal](
      postgres: PostgreSQLHealthCheck[F],
      redis: RedisCommands[F, String, String]
  ): HealthCheck[F] =
    new HealthCheck[F]:
      /** Проверка состояния Redis
        * Использует ping-команду, которая должна вернуть непустой ответ "PONG"
        */
      val redisHealth: F[RedisStatus] =
        redis.ping
          .map(_.nonEmpty)                          // Проверяем, что ответ не пустой
          .timeout(1.second)                        // Ограничиваем время ожидания
          .map(RedisStatus.apply)                   // Преобразуем в статус Redis
          .orElse(RedisStatus(false).pure[F].widen) // В случае ошибки - Unreachable

      /** Проверка состояния PostgreSQL.
        * Выполняет простой запрос к системной таблице
        */
      val postgresHealth: F[PostgresStatus] =
        postgres
          .status
          .timeout(1.second)                           // Ограничиваем время ожидания
          .map(PostgresStatus.apply)                   // Преобразуем в статус Postgres
          .orElse(PostgresStatus(false).pure[F].widen) // В случае ошибки - Unreachable

      val status: F[AppStatus] =
        (redisHealth, postgresHealth).parMapN(AppStatus.apply)

end HealthCheck
```

**Архитектурные решения в реализации:**

1. **Параллельное выполнение** - `parMapN` запускает обе проверки одновременно, 
   сокращая общее время ответа до максимального времени одной проверки (1 секунда)
2. **Таймауты** - каждая проверка ограничена 1 секундой, предотвращая "зависание" health check
3. **Отказоустойчивость** - комбинатор `orElse` гарантирует, что ошибки подключения не "роняют" весь сервис
4. **Композиция эффектов** - чистое функциональное программирование с Cats Effect

### Реализация PostgreSQLHealthCheck

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLHealthCheck.scala
trait PostgreSQLHealthCheck[F[_]]:
  def status: F[Boolean]
        
object PostgreSQLHealthCheck:
  def make[F[_]: MonadCancelThrow](xa: Transactor[F])
      : PostgreSQLHealthCheck[F] =
    new PostgreSQLHealthCheck[F]:
      // Запрос к системной таблице pg_stat_activity
      // Возвращает список активных процессов - если список не пуст, БД работает
      def status: F[Boolean] =
        sql"""SELECT pid FROM pg_stat_activity;"""
          .query[Int]
          .to[List]
          .transact(xa)
          .map(_.nonEmpty)          
```

**Почему именно такой запрос?**

- `pg_stat_activity` всегда доступна даже в самых ограниченных конфигурациях PostgreSQL
- Запрос минимальный по нагрузке (возвращает только ID процессов)
- Проверяет не только подключение, но и способность выполнять запросы

### Проверка Redis

Для Redis используется встроенная команда `PING`:

- Клиент Redis предоставляет метод `.ping`, который отправляет команду `PING`
- В ответ ожидается строка `PONG` (или любой непустой ответ)
- Непустой ответ интерпретируется как успешное подключение

### Пример ответа health check

```json
{
  "redis": "Okay",
  "postgres": "Okay"
}
```

В случае проблем:

```json
{
  "redis": "Unreachable",
  "postgres": "Unreachable"
}
```

## Настройка зависимостей для web-фреймворка

Для реализации UI мы будем использовать [web-фреймворк Krop](https://www.creativescala.org/krop/).
Krop — это Scala 3 фреймворк, построенный поверх http4s и Cats Effect. 
Он предоставляет удобный DSL для описания маршрутов и обработчиков, при этом остаётся полностью типобезопасным: 
соответствие между запросом и ответом проверяется компилятором. 

Перед началом работы необходимо подключить сам Krop и его зависимости.

### Подключение зависимостей

Все зависимости проекта собраны в объекте `Dependencies` в файле `project/Dependencies.scala`.
Для удобства библиотеки сгруппированы по назначению: `circe` — сериализация, `http4s` — HTTP-слой, 
`iron` — уточнённые типы, `krop` — web-фреймворк.

```dotty
// Библиотека для JSON сериализации/десериализации
object circe {
  private val version = "0.14.16"

  val core    = "io.circe" %% "circe-core"    % version
  val generic = "io.circe" %% "circe-generic" % version
  val literal = "io.circe" %% "circe-literal" % version
  val parser  = "io.circe" %% "circe-parser"  % version
}

// Основная HTTP библиотека
object http4s {
  private val version = "1.0.0-M47"

  val client      = "org.http4s" %% "http4s-client"       % version
  val core        = "org.http4s" %% "http4s-core"         % version
  val dsl         = "org.http4s" %% "http4s-dsl"          % version
  val emberClient = "org.http4s" %% "http4s-ember-client" % version
  val emberServer = "org.http4s" %% "http4s-ember-server" % version
  val server      = "org.http4s" %% "http4s-server"       % version

  val jwt = "dev.profunktor" %% "http4s-jwt-auth" % "2.0.13"
}

// Интеграция Iron с Circe
object iron {
  private val version = "3.3.2"
  val circe           = "io.github.iltotore" %% "iron-circe"      % version
  // ... другие зависимости Iron
}

object krop {
  private val version = "0.11.0"
  val asset           = "org.creativescala" %% "krop-asset" % version
  val core            = "org.creativescala" %% "krop-core"  % version
}
```

**Зачем нужна каждая группа библиотек:**

| Библиотека | Назначение                                                                                                                                                                                                                                                               |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **circe**  | JSON-сериализация и десериализация. `core` — основные типы (`Json`, `Encoder`, `Decoder`); `generic` — автоматический вывод кодеков через `derives`; `literal` — удобный синтаксис `json"""..."""` в тестах; `parser` — парсинг строк в `Json`.                          |
| **http4s** | HTTP-слой. `core` — базовые типы (`Request`, `Response`, `Status`); `dsl` — DSL для описания запросов (`GET(uri"...")`); `ember-server` — движок сервера; `ember-client` — движок клиента для интеграционных тестов; `http4s-jwt-auth` — middleware для JWT-авторизации. |
| **krop**   | Web-фреймворк. `core` — `Route`, `Handler`, `Request`, `Response`; `asset` — статические ресурсы (CSS, JS, иконки).                                                                                                                                                      |

Не забудьте добавить зависимости в `pdpLibs`:

```dotty
val pdpLibs: Seq[ModuleID] =
  Seq(
    // ...
    circe.core,
    circe.generic,
    circe.parser,
    ciris.core,
    http4s.core,
    http4s.emberClient,
    http4s.server,
    http4s.jwt,
    krop.asset,
    krop.core,
    // ...
  )
```

### Описание маршрутов (Routes)

В Krop маршрут (`Route`) — это декларация того, **какой HTTP-запрос** и **какой ответ** должен обрабатываться.
Маршруты описываются декларативно и выносятся в отдельный объект `Routes`, чтобы их было удобно переиспользовать
в хэндлерах и тестах.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/routes/Routes.scala
object Routes:
  val health =
    Route(
      Request.get(Path.root / "health_check"),
      Response.ok(Entity.json)
    )
```

**Разбор маршрута:**

- **`Request.get(Path.root / "health_check")`** — описывает GET-запрос на путь `/health_check`.
  `Path.root` — это корневой путь, `/ "health_check"` — добавление сегмента.
- **`Response.ok(Entity.json)`** — описывает успешный ответ (`200 OK`) с телом в формате JSON.

Важно: `Route` — это **только описание**, а не сам обработчик. 
Он говорит, что «если придёт такой запрос, отвечай так», но не содержит логики. 
Логика появится в хэндлере.

### Создание хэндлера (Handler)

Хэндлер связывает маршрут с конкретной реализацией. В Krop для этого используется метод `.handle` или `.handleIO`.
Второй вариант нужен, когда логика требует эффекта `IO` (например, обращения к БД или Redis).

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/handlers/HealthHandler.scala
final case class HealthHandler(healthCheck: HealthCheck[IO]):
  val handler: Handler =
    Routes.health.handleIO(() =>
      healthCheck.status.map(_.asJson)
    )
```

**Что здесь происходит:**

1. **Зависимость `healthCheck: HealthCheck[IO]`** передаётся в конструктор. Это позволяет легко подменить
   реализацию в тестах (например, использовать мок, который всегда возвращает «ОК»).
2. **`Routes.health`** — берём ранее описанный маршрут.
3. **`.handleIO(() => ...)`** — указываем, что при получении запроса нужно выполнить эффект `IO`.
   Обратите внимание на `()` — это говорит о том, что запрос не содержит параметров.
4. **`healthCheck.status`** — вызов сервиса, который проверяет состояние Redis и PostgreSQL.
5. **`.map(_.asJson)`** — превращаем результат (`AppStatus`) в `Json` с помощью автоматического энкодера Circe.

**Результат:** `Handler` — это функция из `Request` в `F[Response]`, готовая к регистрации в приложении.

### Тестирование хэндлера

Для тестирования хэндлеров используется базовый класс `HandlerSuite`, который предоставляет 
хелперы для проверки HTTP-ответов: `expectJsonAndStatus`, `expectStatus`, `expectBody`.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/http/handlers/HealthHandler.scala
object HealthHandlerSuite extends HandlerSuite:
  override type Res = HealthCheck[IO]

  override def sharedResource: Resource[IO, Res] =
    Resource.pure(
      new HealthCheck[IO]:
        def status: IO[AppStatus] =
          IO.pure(AppStatus(
            RedisStatus(true),
            PostgresStatus(false)
          ))
    )

  /** Тест проверяет, что GET запрос к /health_check возвращает:
    *   - Статус 200 OK
    *   - JSON с актуальным состоянием всех зависимостей
    */
  test("GET /health_check returns 200 OK with status"): healthCheck =>
    val handler: Handler = HealthHandler(healthCheck).handler

    val request = GET(uri"/health_check")

    val expectedBody =
      json"""{
               "redis": "Okay",
               "postgres": "Unreachable"
             }"""

    expectJsonAndStatus(handler, request)(expectedBody, HttpStatus.Ok)
```

**Разбор теста:**

1. **`override type Res = HealthCheck[IO]`** — тип ресурса, который будет доступен в тестах.
   В нашем случае — это мок-сервис `HealthCheck[IO]`.
2. **`sharedResource`** — описывает, как создать ресурс. Возвращаем `Resource.pure` с мок-реализацией,
   которая всегда говорит: «Redis доступен, PostgreSQL — нет».
3. **`test("...")`** — каждый тест получает готовый `healthCheck` из `sharedResource`.
4. **`GET(uri"/health_check")`** — создаём HTTP-запрос с помощью http4s DSL. `uri` — строковый интерполятор.
5. **`expectJsonAndStatus`** — сравнивает статус и тело ответа с ожидаемыми. Если что-то не совпало —
   покажет diff в отчёте о тестах.

**Почему это важно:**

- Тест **не требует реальной БД или Redis** — используется мок.
- Тест **проверяет контракт** хэндлера: что он возвращает 200 OK и корректный JSON.
- Если кто-то случайно изменит формат ответа (например, добавит поле), тест сразу это покажет.

### Создание точки входа (Main)

`Main` — это место, где всё собирается вместе: чтение конфигурации, создание ресурсов,
инициализация сервисов и запуск HTTP-сервера.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/Main.scala
object Main extends IOApp.Simple:
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    readConfig().flatMap: config =>
      AppResources.make[IO](config)
        .evalMap { resources =>
          val services: Services[IO] =
            Services.make[IO](resources.transactor, resources.redis)

          Security.make[IO](
            config,
            resources.transactor,
            resources.redis
          ).map: security =>
            makeApplication(services, security)
        }
        .flatMap: application =>
          ServerBuilder(
            config.httpServerConfig.port,
            config.httpServerConfig.host,
            application
          )
            .build
            .server
        .useForever

  private def readConfig(): IO[Config] =
    IO.fromEither(
      ConfigSource.default.load[Config].left.map(errors =>
        new IllegalArgumentException(s"Invalid config: ${errors.prettyPrint()}")
      )
    )

  private def makeApplication(
      services: Services[IO],
      security: Security[IO]
  ): Application =
    val healthHandler =
      HealthHandler(services.healthCheck)

    healthHandler.handler
      .orElse(assets)
      .orElse(Application.notFound)
```

**Пошаговый разбор:**

1. **`IOApp.Simple`** — базовая точка входа для Cats Effect приложений. Достаточно реализовать метод `run: IO[Unit]`.
2. **`given Logger[IO]`** — даём приложению логгер на основе SLF4J.
3. **`readConfig()`** — читаем конфигурацию из `application.conf` через библиотеку PureConfig.
   Если конфигурация невалидна — падаем с понятной ошибкой.
4. **`AppResources.make[IO](config)`** — создаём ресурсы: `Transactor` для PostgreSQL и клиент Redis.
   Возвращаем `Resource`, чтобы гарантировать корректное освобождение соединений при завершении.
5. **`.evalMap { resources => ... }`** — внутри ресурса создаём сервисы (`Services`) и `Security` (набор
   JWT-компонентов), затем — `Application`.
6. **`ServerBuilder(...).build.server`** — создаём HTTP-сервер на указанном хосте и порту из конфигурации.
7. **`.useForever`** — запускаем сервер и работаем, пока приложение не будет остановлено (Ctrl+C).
8. **`makeApplication`** — собирает итоговое приложение из хэндлеров. `orElse` регистрирует обработчики
   по цепочке: если один не подошёл — пробуется следующий. `Application.notFound` — запасной вариант.

### Запуск и проверка

Теперь можно запустить приложение в sbt-консоли:

```bash
sbt pdp/run
```

И открыть в браузере или через `curl` адрес `http://localhost:8081/health_check`:

```bash
curl http://localhost:8081/health_check
```

В ответ вы получите JSON с текущим состоянием зависимостей:

```json
{
  "redis": "Okay",
  "postgres": "Okay"
}
```

**Если что-то пошло не так:**

- **Порт занят** — измените порт в `application.conf`.
- **Не удаётся подключиться к БД** — проверьте, что PostgreSQL запущен и доступен по адресу из конфигурации.
- **JSON не соответствует ожиданиям** — проверьте, что у `AppStatus` корректно выведен `Encoder` (через `derives Encoder`).

### Что дальше

Мы настроили минимальный работающий HTTP-сервер с одним эндпоинтом `/health_check`.
Дальше шаг за шагом будем добавлять новые эндпоинты: регистрацию, логин, работу с задачами,
настройки пользователя. Каждый новый эндпоинт будет оформлен как отдельный `Handler`,
зарегистрирован в `Main` через `orElse` и покрыт тестами.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala
[http4s]: https://http4s.org/

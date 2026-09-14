{%
showPrev = true
prevPage = authorization/create.html
showNext = true
nextPage = api/users_routes.html
%}

# Клиентское API

## Система мониторинга работоспособности (Health Check)

В современных распределённых системах критически важно иметь механизм контроля состояния сервисов. 
Health check (проверка работоспособности) позволяет:

- **Обнаруживать проблемы** до того, как они повлияют на пользователей
- **Информировать системы оркестрации** (Kubernetes, Docker Swarm) о необходимости перезапуска
- **Мониторить зависимые сервисы** (базы данных, кэши, очереди)
- **Предоставлять метрики** для систем наблюдения (Prometheus, Grafana)

Наше приложение реализует комплексную проверку состояния, 
включающую два критических компонента: подключение к PostgreSQL и Redis.

### Модель данных для статуса приложения

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/AppStatus.scala
/** Комплексный статус приложения, объединяющий состояние всех зависимостей
  *
  * @param redis
  *   статус подключения к Redis
  * @param postgres
  *   статус подключения к PostgreSQL
  */
final case class AppStatus(redis: RedisStatus, postgres: PostgresStatus)

object AppStatus:
  // Использование opaque types для типобезопасности
  // Эти типы не взаимозаменяемы на уровне типов, хотя внутри это простые статусы
  opaque type RedisStatus    = Status
  opaque type PostgresStatus = Status

  object RedisStatus:
    /** Создание статуса Redis из булева значения */
    def apply(status: Boolean): RedisStatus =
      Status.find(status)

  object PostgresStatus:
    /** Создание статуса PostgreSQL из булева значения */
    def apply(status: Boolean): PostgresStatus =
      Status.find(status)

  /** Перечисление возможных состояний
    *
    * Использование enum гарантирует, что статус может быть только в одном из
    * двух состояний
    */
  enum Status(isOk: Boolean):
    case Okay()        extends Status(true)
    case Unreachable() extends Status(false)

  object Status:
    def find(status: Boolean): RedisStatus =
      if status then Status.Okay()
      else Status.Unreachable()

  given encoderStatus: Encoder[Status] =
    Encoder.encodeString.contramap(_.productPrefix)

  given encoderAppStatus: Encoder[AppStatus] = deriveEncoder[AppStatus]
end AppStatus
```

**Ключевые особенности модели:**

1. **Opaque types** - обеспечивают типобезопасность, не позволяя перепутать `RedisStatus` с `PostgresStatus`
2. **Строгая типизация состояний** - перечисление гарантирует только два возможных значения
3. **JSON сериализация** - готовность к отправке через HTTP API

### Интерфейс сервиса HealthCheck

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/HealthCheck.scala
/** Сервис для проверки работоспособности приложения */
trait HealthCheck[F[_]]:
  /** Получение текущего статуса всех зависимостей
    *
    * @return
    *   F[AppStatus] - эффект с комплексным статусом
    */
  def status: F[AppStatus]
```

### Реализация HealthCheck

```dotty
object HealthCheck:
  /** Создание экземпляра HealthCheck
    *
    * @tparam F
    *   тип эффекта с поддержкой временных операций
    * @param postgres
    *   специализированный health check для PostgreSQL
    * @param redis
    *   клиент Redis для проверки подключения
    * @return
    *   готовый сервис HealthCheck
    */
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

      /** Комплексный статус приложения.
        * Выполняет обе проверки параллельно для оптимизации времени ответа
        */
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
/** Специализированный сервис для проверки состояния PostgreSQL */
trait PostgreSQLHealthCheck[F[_]]:
  /** Проверка доступности PostgreSQL
    *
    * @return
    *   true если БД отвечает на запросы
    */
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

### Преимущества подхода

1. **Проактивный мониторинг** - возможность обнаружить проблемы до жалоб пользователей
2. **Изоляция проблем** - чёткое указание, какой именно сервис недоступен
3. **Быстродействие** - параллельные проверки с таймаутами
4. **Надёжность** - fallback на "недоступен" при любых ошибках
5. **Расширяемость** - легко добавить новые проверки (например, для Kafka, S3)
6. **Функциональная чистота** - все эффекты явно описаны в типах

## Настройка зависимостей для HTTP сервера

Для создания клиентского API мы используем библиотеку [Http4s][http4s] - 
функциональную и производительную HTTP-библиотеку для Scala, построенную на Cats Effect.

### Подключение зависимостей

```dotty
// Библиотека для JSON сериализации/десериализации
object circe {
  private val version = "0.14.15"  

  val core    = "io.circe" %% "circe-core"    % version  
  val generic = "io.circe" %% "circe-generic" % version  
  val literal = "io.circe" %% "circe-literal" % version  
  val parser  = "io.circe" %% "circe-parser"  % version  
}

// Основная HTTP библиотека
object http4s {
  private val version = "0.23.33"  

  val circe  = "org.http4s" %% "http4s-circe"  % version  
  val client = "org.http4s" %% "http4s-client" % version  
  val core   = "org.http4s" %% "http4s-core"   % version  
  val dsl    = "org.http4s" %% "http4s-dsl"    % version  
  val server = "org.http4s" %% "http4s-server" % version  

  val jwt = "dev.profunktor" %% "http4s-jwt-auth" % "2.0.13" 
}

// Интеграция Iron с Circe
object iron {
  private val version = "3.3.0"
  val circe           = "io.github.iltotore" %% "iron-circe"      % version 
  // ... другие зависимости Iron
}
```

## HTTP endpoint для проверки работоспособности

### Реализация маршрутов

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/HealthRoutes.scala
/** HTTP маршруты для проверки работоспособности приложения
  *
  * @tparam F
  *   тип эффекта (IO, Task и т.д.)
  * @param healthCheck
  *   сервис для получения статуса всех зависимостей
  */
final case class HealthRoutes[F[_]: Monad](
    healthCheck: HealthCheck[F]
) extends Http4sDsl[F]: // Трейт предоставляет удобные конструкторы ответов: Ok, NotFound...

  /** Базовый путь для всех endpoint-ов health check */
  private[routes] val prefixPath = "/health_check"

  /** Внутренние HTTP маршруты
    * Определяет, как обрабатывать входящие запросы
    */
  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      // GET /health_check - получение статуса всех систем
      case GET -> Root =>
        // Выполняем health check и возвращаем результат как JSON
        // Статус ответа всегда 200 OK, независимо от состояния систем
        Ok(healthCheck.status)
    }

  /** Публичные маршруты с привязанным префиксом
    * Router автоматически добавляет prefixPath ко всем маршрутам
    */
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
```

### Детальный разбор компонентов

#### 1. **Параметры класса**

- `F[_]: Monad` - любой тип эффекта, поддерживающий [монадические операции](../typeclass/monad.md)
- `healthCheck: HealthCheck[F]` - сервис, выполняющий реальные проверки

#### 2. `Http4sDsl[F]`

Трейт, предоставляющий:

- Конструкторы статусов: `Ok()`, `NotFound()`, `BadRequest()` и т.д.
- Операторы для работы с запросами: `->`, `Root`, `/:`, `IntVar` и т.д.
- Конвертеры между различными HTTP типами

#### 3. `HttpRoutes.of[F]`

Создаёт частичную функцию (PartialFunction) от `Request[F]` к `F[Response[F]]`:

- Если запрос соответствует паттерну `GET -> Root` - обрабатываем
- Если нет - пробрасываем дальше по цепочке middleware

#### 4. `Router`

Компонент для композиции маршрутов:

- Добавляет префикс пути ко всем внутренним маршрутам
- Позволяет группировать связанные endpoint-ы
- Облегчает организацию кода по доменным областям

### Формат ответа

**Успешный ответ (все системы работают):**

```json
{
   "redis": "Okay",
   "postgres": "Okay"
}
```

**Ответ с проблемами (Redis недоступен):**

```json
{
   "redis": "Unreachable",
   "postgres": "Okay"
}
```

### Преимущества такого подхода

1. **Декларативность** - маршруты описываются в стиле, близком к спецификации HTTP
2. **Композируемость** - маршруты можно легко комбинировать и переиспользовать
3. **Типобезопасность** - компилятор проверяет корректность типов на всех уровнях
4. **Функциональная чистота** - все эффекты явно описаны в сигнатурах
5. **Тестируемость** - легко тестировать отдельные маршруты без запуска сервера

## Тестирование HTTP endpoint-ов

Тестирование HTTP слоя является критически важным для обеспечения корректной работы API.
Http4s предоставляет уникальную возможность тестировать маршруты без запуска сервера,
поскольку `HttpRoutes` — это просто функции из `Request` в `Option[Response]`.
Это делает тестирование эффективным, детерминированным и быстрым.

### Философия тестирования в Http4s

**Ключевая идея**: сервер — это функция `Request => F[Response]`.
Это означает, что мы можем тестировать маршруты, просто вызывая их с различными запросами и проверяя ответы,
без необходимости поднимать HTTP сервер, открывать порты и управлять сетевыми соединениями.

**Преимущества подхода:**

- **Скорость** — тесты выполняются за миллисекунды, так как нет сетевого взаимодействия
- **Детерминированность** — отсутствие проблем с портами, таймаутами и сетевыми ошибками
- **Изоляция** — каждый тест работает с чистыми функциями
- **Генеративное тестирование** — легко комбинировать с property-based testing

Давайте подробно рассмотрим инфраструктуру для тестирования endpoint-ов и конкретный пример тестирования health check маршрута.

### Базовая инфраструктура для HTTP тестирования

Для удобного и выразительного тестирования HTTP endpoint-ов создан базовый трейт `HttpSuite`, 
который предоставляет набор вспомогательных методов:

```dotty
/** Базовый трейт для всех HTTP тестов. Предоставляет удобные методы для
  * проверки ответов от маршрутов.
  */
trait HttpSuite extends IOSuite:
  given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  given log: Logger[IO] = loggerFactory.getLogger

  /** Проверяет и статус, и тело ответа одновременно. */
  def expectHttpBodyAndStatus(
      routes: HttpRoutes[IO],
      req: Request[IO]
  )(expectedBody: Json, expectedStatus: Status): IO[Expectations] =
    // Выполняем запрос через маршруты
    routes.run(req).value.flatMap:
      case Some(resp) =>
        // Если маршрут найден - проверяем тело и статус
        resp.asJson.map: json =>
          expect.same(resp.status, expectedStatus) |+|
            // Игнорируем null поля для сравнения
            expect.same(
              json.dropNullValues,
              expectedBody.dropNullValues
            )
      // Маршрут не найден - тест провален      
      case None => IO.pure(failure("Route not found"))
  
  /** Проверяет только HTTP статус ответа.
    *
    * Полезно для проверок без тела ответа (204 No Content, 404 Not Found и
    * т.д.)
    */
  def expectHttpStatus(
      routes: HttpRoutes[IO],
      req: Request[IO]
  )(expectedStatus: Status): IO[Expectations] =
    routes.run(req).value.map:
      case Some(resp) => expect.same(resp.status, expectedStatus)
      case None       => failure("route not found")
```

#### Ключевые особенности HttpSuite

1. **Комбинирование проверок** - использование `|+|` ([Semigroup](../typeclass/monoid/semigroup.md)) для объединения нескольких проверок
2. **Работа с JSON** - встроенная поддержка Circe для парсинга и сравнения
3. **Обработка null значений** - `dropNullValues` для игнорирования незначимых полей
4. **Проверка ошибок** - отдельный метод для тестирования сценариев с исключениями

### Тестирование Health Check endpoint

Рассмотрим конкретный пример тестирования маршрута для проверки состояния сервиса:

```dotty
/** Тесты для HealthRoutes - маршрута проверки состояния сервиса. */
object HealthRoutesSuite extends HttpSuite:
  // Тип разделяемого ресурса - HealthCheck сервис
  override type Res = HealthCheck[IO]

  /** Создание разделяемого ресурса для всех тестов.
    *
    * Используем фиктивную реализацию HealthCheck с предсказуемыми значениями.
    */
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
    // Создаём маршруты с тестовой реализацией HealthCheck
    val routes = HealthRoutes(healthCheck).routes

    // Формируем GET запрос к endpoint
    val request = GET(uri"/health_check")

    // Ожидаемое тело ответа
    val expectedBody =
      json"""{
               "redis": "Okay",
               "postgres": "Unreachable"
             }"""

    // Выполняем проверку статуса и тела ответа
    expectHttpBodyAndStatus(routes, request)(expectedBody, HttpStatus.Ok)
end HealthRoutesSuite
```

Тест проверит:

- ✓ Маршрут существует (не вернул `None`)
- ✓ Статус ответа = 200 OK
- ✓ Тело ответа соответствует ожидаемому JSON

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala
[http4s]: https://http4s.org/

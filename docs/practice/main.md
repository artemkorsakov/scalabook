{%
showPrev = true
prevPage = api/tasks_routes.html
showNext = true
nextPage = sources.html
%}

# Запуск сервера

В этом разделе мы завершим сборку нашего приложения, объединив все ранее созданные компоненты в единое работающее целое. 
Мы настроим конфигурацию, создадим модули сервисов и HTTP API, определим управление ресурсами и, наконец, запустим сервер.

## Конфигурация приложения

Любое серьезное приложение требует гибкой настройки параметров окружения: 
адресов серверов, параметров подключения к базам данных, секретных ключей и так далее. 
Для этого мы используем библиотеку **PureConfig** в связке с **Iron** для обеспечения типобезопасности на этапе компиляции.

### Модели конфигурации

Создадим набор `case class`-ов, которые описывают структуру нашего конфигурационного файла. 
Использование уточненных типов позволяет нам наложить ограничения на значения прямо в определении типа, 
гарантируя, что некорректная конфигурация просто не скомпилируется или вызовет ошибку при загрузке.

```dotty
// Модель для конфигурации HTTP-сервера
final case class HttpServerConfig(
    host: Host,
    port: Port
) derives ConfigReader

object HttpServerConfig:
  // Читатель для типа Host: проверяем, что строка — корректный IP-адрес или имя хоста
  given ConfigReader[Host] =
    ConfigReader[String].emap: host =>
      Host.fromString(host).toRight(UserValidationFailed(s"Invalid host: $host"))

  // Читатель для типа Port: проверяем, что число входит в допустимый диапазон портов
  given ConfigReader[Port] =
    ConfigReader[Int].emap: port =>
      Port.fromInt(port).toRight(UserValidationFailed(s"Invalid port: $port"))

// Тип для непустой строки без пробелов по краям.
type NonEmptyString = NonEmptyString.T
object NonEmptyString extends RefinedType[String, Not[Empty] & Trimmed]

// Уточненный тип для пользовательского номера порта.
type UserPortNumber = Int :| Interval.Closed[1024, 49151]

// Модель для конфигурации PostgreSQL
final case class PostgreSQLConfig(
    host: NonEmptyString,
    port: UserPortNumber, // Используем уточненный тип
    user: NonEmptyString,
    password: Secret[NonEmptyString], // Используем Secret для конфиденциальных данных
    database: NonEmptyString,
    max: Int :| Positive
)

// Модель для конфигурации Redis
final case class RedisConfig(uri: NonEmptyString)

// Тип для ключа JWT.
type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.T
object JwtAccessTokenKeyConfig extends RefinedType[String, Not[Empty] & Trimmed]

// Модель для конфигурации JWT-токенов
final case class TokenConfig(
    jwtAccessTokenKey: Secret[JwtAccessTokenKeyConfig],
    expiration: FiniteDuration
) derives ConfigReader

// Читатель для Secret[A]. Мы можем прочитать A, для которого определен ConfigReader,
// а затем обернуть его в Secret.
given secretReader[A: {ConfigReader, Show}]: ConfigReader[Secret[A]] =
  ConfigReader[A].map(Secret.apply)

/** `Config` — корневой объект конфигурации. */
final case class Config(
    httpServerConfig: HttpServerConfig,
    localization: LocalizationConfig, // из раздела "Локализация"
    postgresql: PostgreSQLConfig,
    redis: RedisConfig,
    token: TokenConfig
) derives ConfigReader
```

### Файл конфигурации (`application.conf`)

Все описанные выше модели будут заполнены из файла `application.conf`, 
который должен находиться в директории `src/main/resources`. 
Здесь мы задаем конкретные значения для всех параметров.

```conf
# Конфигурация HTTP-сервера
http-server-config = {
  host = "0.0.0.0"  
  port = 8080
}

# Конфигурация локализации (была подробно описана в предыдущих разделах)
localization = {
  languages = [
    # ... содержимое, как в примере локализации
  ]
}

# Конфигурация подключения к PostgreSQL
postgresql = {
  host = "localhost"
  port = 5432
  user = "postgres"
  password = "my-password"
  database = "pdp"
  max = 10  
}

# Конфигурация подключения к Redis
redis = {
  uri = "redis://localhost"
}

# Конфигурация JWT-токенов
token {
  jwt-access-token-key = "my-secret-key-for-jwt"
  expiration = 1 day
}
```

Такой подход позволяет централизованно управлять всеми настройками, 
а использование уточненных типов и `Secret` добавляет слой безопасности, 
предотвращая случайное использование некорректных или незащищенных данных.

## Модульная архитектура сервисов

Для поддержания чистоты архитектуры и следования принципу единственной ответственности, 
мы группируем связанную функциональность в отдельные модули. 
Наше приложение будет состоять из трех основных модулей: `Security`, `Services` и `HttpApi`.

### Модуль `Security`

Этот модуль инкапсулирует всю логику, связанную с безопасностью и аутентификацией. 
Он отвечает за создание JWT-аутентификации, сервиса аутентификации и сервиса для поиска пользователей.

```dotty
sealed trait Security[F[_]] private (
    // Стратегия аутентификации JWT
    val userJwtAuth: JwtSymmetricAuth,
    // Сервис для операций входа/выхода
    val auth: AuthService[F],         
    // Сервис для поиска пользователей по токену 
    val usersAuth: UsersAuthService[F, UserInfo] 
)

object Security:
  def make[F[_]: {Sync, MonadCancelThrow}](
      cfg: Config,
      transactor: Transactor[F],              // Doobie-транзактор для работы с БД
      redis: RedisCommands[F, String, String] // Клиент Redis
  ): F[Security[F]] =
    (
      JwtExpire.make[F], // Сервис для проверки срока жизни JWT
      Crypto.make[F]     // Сервис для хэширования паролей
    ).mapN { case (jwtExpire, crypto) =>
      // 1. Настройка JWT-аутентификации
      val userJwtAuth: JwtSymmetricAuth =
        JwtAuth.hmac(
          cfg.token.jwtAccessTokenKey.value.value.toCharArray,
          JwtAlgorithm.HS256
        )

      // 2. Создание сервиса для работы с токенами
      val tokens = Tokens.make[F](
        jwtExpire,
        cfg.token.jwtAccessTokenKey.value,
        cfg.token.expiration
      )

      // 3. Создание сервиса для работы с пользователями в PostgreSQL
      val usersService = new PostgreSQLUsersService[F](transactor)

      // 4. Создание сервиса аутентификации, который объединяет всё вышеперечисленное
      val auth = AuthService.make[F](
        tokenExpiration = cfg.token.expiration,
        tokens = tokens,
        usersService = usersService,
        redis = redis,
        crypto = crypto
      )

      // 5. Создание сервиса для аутентификации пользователей на основе Redis
      val usersAuth = UsersAuthService.make[F](redis)

      new Security[F](userJwtAuth, auth, usersAuth) {}
    }
end Security
```

### Модуль `Services`

Этот модуль объединяет все бизнес-сервисы приложения, которые работают с данными. 
Он создает экземпляры сервисов для работы с типами задач, разделами, целями и задачами, 
а также сервис проверки работоспособности.

```dotty
sealed trait Services[F[_]] private (
    healthCheck: HealthCheck[F],
    settingsService: SettingsService[F],
    taskLifecycleService: TaskLifecycleService[F]
)

object Services:
  def make[F[_]: {Sync, MonadCancelThrow, Temporal}](
      transactor: Transactor[F],
      redis: RedisCommands[F, String, String]
  ): Services[F] =
    // 1. Сервис проверки работоспособности PostgreSQL и Redis
    val healthCheck =
      HealthCheck.make[F](PostgreSQLHealthCheck.make[F](transactor), redis)

    // 2. Создание низкоуровневых сервисов для работы с БД
    val typeService    = new PostgreSQLUserTypeService[F](transactor)
    val sectionService = new PostgreSQLSectionService[F](transactor)
    val goalService    = new PostgreSQLGoalService[F](transactor)
    val taskService    = new PostgreSQLTaskService[F](transactor)
    val historyService = new PostgreSQLHistoryService[F](transactor)

    // 3. Сервис для операций с настройками (CRUD для типов, разделов, целей, задач)
    val settingsService = SettingsService[F](
      typeService,
      sectionService,
      goalService,
      taskService
    )

    // 4. Сервис для управления жизненным циклом задач (выполнение, откладывание и т.д.)
    val taskLifecycleService = TaskLifecycleService[F](
      typeService,
      sectionService,
      goalService,
      taskService,
      historyService
    )

    new Services[F](
      healthCheck,
      settingsService,
      taskLifecycleService
    ) {}
```

Обратите внимание, что все конкретные реализации сервисов (например, `PostgreSQLUserTypeService`) 
создаются и используются внутри модуля. 
Внешний мир взаимодействует только с высокоуровневыми интерфейсами (`SettingsService`, `TaskLifecycleService`), 
которые уже содержат в себе бизнес-логику.

## Сборка HTTP API

Модуль `HttpApi` — это завершающий этап сборки. 
Он принимает готовые модули `Services` и `Security` и комбинирует их, 
создавая полный набор HTTP-маршрутов, обернутых в необходимые промежуточные обработчики (middleware).

```dotty
sealed trait HttpApi[F[_]: Async] private (
    services: Services[F],
    security: Security[F]
):
  // Промежуточный слой для JWT-аутентификации.
  // Он проверяет JWT в запросе и, если он валиден, находит пользователя.
  private val usersMiddleware =
    JwtAuthMiddleware[F, UserInfo](
      security.userJwtAuth,
      security.usersAuth.findUser
    )

  // ----- Определение всех маршрутов -----
  // Маршруты, не требующие аутентификации (открытые)
  private val loginRoutes   = LoginRoutes[F](security.auth).routes
  private val userRoutes    = UserRoutes[F](security.auth).routes
  private val healthRoutes  = HealthRoutes[F](services.healthCheck).routes

  // Маршруты, требующие аутентификации (защищенные)
  private val logoutRoutes  = LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userTypeRoutes =
    UserTypeRoutes[F](services.settingsService).routes(usersMiddleware)
  private val sectionRoutes =
    SectionRoutes[F](services.settingsService).routes(usersMiddleware)
  private val goalRoutes =
    GoalRoutes[F](services.settingsService).routes(usersMiddleware)
  private val taskRoutes =
    TaskRoutes[F](services.taskLifecycleService).routes(usersMiddleware)

  // Объединение всех маршрутов в один
  private val openRoutes: HttpRoutes[F] =
    healthRoutes <+> loginRoutes <+> logoutRoutes <+> userRoutes <+>
      userTypeRoutes <+> sectionRoutes <+> goalRoutes <+> taskRoutes

  // Группировка всех маршрутов под префиксом версии API
  private val routes: HttpRoutes[F] = Router(
    Version.v1 -> openRoutes
  )

  // ----- Промежуточные обработчики (Middleware) -----
  // Стек middleware применяется снизу вверх (последний в композиции - самый внешний)
  private val middleware: HttpRoutes[F] => HttpRoutes[F] =
    // 1. Убирает завершающий слэш, если он есть
    val slash: HttpRoutes[F] => HttpRoutes[F] = AutoSlash(_)

    // 2. Настройка CORS: разрешаем запросы с любых источников
    val policy: HttpRoutes[F] => HttpRoutes[F] =
      routes => CORS.policy
          .withAllowOriginAll
          .withAllowCredentials(false)
          .apply(routes)

    // 3. Устанавливаем таймаут для всех запросов в 60 секунд
    val timeout: HttpRoutes[F] => HttpRoutes[F] = Timeout(60.seconds)(_)

    slash andThen policy andThen timeout

  // Логгеры для входящих и исходящих запросов и ответов
  private val loggers: HttpApp[F] => HttpApp[F] =
    val reqLogger: HttpApp[F] => HttpApp[F] =
      RequestLogger.httpApp(true, true)(_)
    val resLogger: HttpApp[F] => HttpApp[F] =
      ResponseLogger.httpApp(true, true)(_)

    reqLogger andThen resLogger

  // Финальное приложение: логгеры -> middleware -> маршруты -> обработка 404
  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

end HttpApi

object HttpApi:
  def make[F[_]: Async](
      services: Services[F],
      security: Security[F]
  ): HttpApi[F] =
    new HttpApi[F](services, security) {}
```

Этот модуль служит "клеем", который собирает все части приложения в единый, готовый к запуску HTTP-сервис.

## Управление ресурсами

Некоторые компоненты нашего приложения, такие как подключение к базе данных или Redis-клиент, являются _ресурсами_. 
Они должны быть созданы, использованы и корректно закрыты. Для этого в Cats Effect используется тип `Resource`. 
Мы создадим единый модуль `AppResources`, который будет отвечать за инициализацию и жизненный цикл всех таких ресурсов.

```dotty
sealed trait AppResources[F[_]](
    val transactor: Transactor[F],              // Doobie-транзактор
    val redis: RedisCommands[F, String, String] // Redis-клиент
)

object AppResources:
  def make[F[_]: {Async, Logger, MkRedis}](
      config: Config
  ): Resource[F, AppResources[F]] =
    // Функция для создания ресурса подключения к PostgreSQL
    def makePGResource(pgcfg: PostgreSQLConfig): Resource[F, Transactor[F]] = {
      val url =
        s"jdbc:postgresql://${pgcfg.host.value}:${pgcfg.port}/${pgcfg.database.value}"
      for
        hikariConfig <- Resource.pure {
          val config = new HikariConfig()
          config.setDriverClassName("org.h2.Driver")
          config.setJdbcUrl(url)
          config.setUsername(pgcfg.user.value)
          config.setPassword(pgcfg.password.value.value)
          config.setMaximumPoolSize(pgcfg.max)
          config
        }
        // Создаем транзактор из JDBC URL
        xa <- HikariTransactor.fromHikariConfig[F](hikariConfig)
        _  <- Resource.eval(checkPostgresConnection(xa))
        _  <- Resource.eval(migration(xa))
      yield xa
    }

    // Вспомогательная функция для проверки подключения к PostgreSQL
    def checkPostgresConnection(xa: Transactor[F]): F[Unit] =
      sql"select version();"
        .query[String]
        .option
        .transact(xa)
        .flatMap {
          case Some(v) => Logger[F].info(s"Connected to Postgres $v")
          case None    =>
            new RuntimeException("Invalid Postgres connection")
              .raiseError[F, Unit]
        }

    // Функция для запуска миграций базы данных
    def migration(xa: Transactor[F]): F[Unit] =
      new PostgreSQLMigrationService(xa).migrate()

    // Функция для создания ресурса подключения к Redis
    def makeRedisResource(c: RedisConfig)
        : Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(c.uri.value).evalTap(checkRedisConnection)

    // Вспомогательная функция для проверки подключения к Redis
    def checkRedisConnection(
        redis: RedisCommands[F, String, String]
    ): F[Unit] =
      redis.info.flatMap:
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }

    // Объединяем оба ресурса в один с помощью parMapN.
    // Это значит, что они будут инициализироваться параллельно.
    (
      makePGResource(config.postgresql),
      makeRedisResource(config.redis)
    ).parMapN(new AppResources[F](_, _) {})
  end make
```

### Запуск HTTP-сервера как ресурса

Сам HTTP-сервер также является ресурсом. 
Мы создадим класс типов `MkHttpServer`, который будет отвечать за создание сервера (в нашем случае на базе Ember).

```dotty
trait MkHttpServer[F[_]]:
  def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]

object MkHttpServer:
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = summon

  // Реализация для F с необходимыми классами типов
  given forAsyncLogger[F[_]: {Async, Logger, Network}]: MkHttpServer[F] =
    new MkHttpServer[F]:
      def newEmber(
          cfg: HttpServerConfig,
          httpApp: HttpApp[F]
      ): Resource[F, Server] =
        EmberServerBuilder
          .default[F]
          .withHost(cfg.host)
          .withPort(cfg.port)
          .withHttpApp(httpApp)
          .build
          .evalTap(showEmberBanner[F])

  // Вывод красивого баннера с адресом сервера
  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )
end MkHttpServer
```

## Точка входа (Main)

Теперь, когда у нас есть все строительные блоки, мы можем собрать их вместе в главном объекте приложения. 
Мы унаследуем `IOApp.Simple`, что даст нам точку входа с предварительно настроенным `IO` и управлением ресурсами.

```dotty
object Main extends IOApp.Simple:

  // Создаем логгер для всего приложения
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    // 1. Загружаем конфигурацию
    ConfigSource.default.load[Config] match
      case Left(errors) =>
        IO.raiseError(
          new IllegalArgumentException(
            s"Invalid config: ${errors.prettyPrint()}"
          )
        )
      case Right(config) =>
        Logger[IO].info(s"Loaded config $config") >>
          // 2. Создаем ресурсы приложения (PostgreSQL, Redis)
          AppResources.make[IO](config)
            .evalMap { resources => // После создания ресурсов, создаем модули
              val services = Services.make[IO](resources.transactor, resources.redis)

              Security.make[IO](
                config,
                resources.transactor,
                resources.redis
              ).map: security =>
                HttpApi.make[IO](services, security).httpApp
            }
            .flatMap: httpApp => // После создания HttpApp, запускаем сервер
              MkHttpServer[IO].newEmber(config.httpServerConfig, httpApp)
            .useForever // Запускаем сервер и держим его включенным вечно
end Main
```

### Разбор работы `Main`

1. **Загрузка конфигурации**: `ConfigSource.default.load[Config]` читает `application.conf` и пытается создать объект `Config`. 
   Если конфигурация невалидна (например, нарушены ограничения уточненных типов), 
   мы выбрасываем исключение с подробным описанием ошибок.
2. **Создание ресурсов**: `AppResources.make[IO](config)` создает `Resource[IO, AppResources[IO]]`. 
   Этот ресурс включает в себя транзактор для PostgreSQL и клиент для Redis. 
   На этом этапе также происходят проверка подключения к базам данных и запуск миграций.
3. **Сборка модулей**: Метод `evalMap` на `Resource` позволяет нам получить доступ к созданным ресурсам 
   и, используя их, построить наши модули `Services` и `Security`, а затем и `HttpApi`. 
   `evalMap` гарантирует, что это произойдет после успешного создания ресурсов, но до того, как ресурсы будут освобождены.
4. **Запуск сервера**: `flatMap` позволяет перейти от `Resource[IO, HttpApp[IO]]` к `Resource[IO, Server]`, используя `MkHttpServer`. 
   Сервер запускается с настроенным `httpApp`.
5. **Ожидание**: `useForever` — это удобный метод, который эквивалентен `.use(_ => IO.never)`. 
   Он запускает сервер и переводит основную программу в бесконечное ожидание, не давая ей завершиться. 
   Сервер будет работать до тех пор, пока процесс не будет остановлен извне.

## Общая архитектура

На следующей диаграмме представлен общий обзор компонентов нашего приложения и их взаимосвязей.

```mermaid
graph TD
    subgraph Main
        A[Main] --> B(Загрузка Config)
        B --> C{AppResources}
        C --> D[PostgreSQL Transactor]
        C --> E[Redis Client]
    end

    subgraph Modules
        D --> F[Services Module]
        E --> F
        D --> G[Security Module]
        E --> G

        F --> H[HealthCheck Service]
        F --> I[Settings Service]
        F --> J[TaskLifecycle Service]

        G --> K[JWT Auth]
        G --> L[Auth Service]
        G --> M[Users Auth Service]
    end

    subgraph HttpApi
        F --> N[HttpApi Module]
        G --> N
        N --> O[Routes]
        N --> P[Middleware]
        N --> Q[Loggers]
    end

    subgraph Server
        Q --> R[HttpApp]
        R --> S[Ember Server]
    end

    S --> T[Running Application]
```

## Резюме

Мы завершили создание нашего приложения, пройдя путь от конфигурации до запуска живого сервера. Мы увидели, как:

- **Типобезопасная конфигурация** с использованием PureConfig и Iron помогает предотвратить ошибки на ранних этапах.
- **Модульная архитектура** (`Security`, `Services`, `HttpApi`) позволяет четко разделить ответственность и упрощает тестирование и поддержку.
- **Управление ресурсами** через `Resource` из Cats Effect гарантирует, что все подключения к внешним системам будут корректно созданы и освобождены.
- **Композиция** всех частей в `Main` демонстрирует, как простые, изолированные компоненты собираются в сложную, но надежную систему.

Теперь у нас есть полностью функциональное приложение, готовое к развертыванию. 

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala

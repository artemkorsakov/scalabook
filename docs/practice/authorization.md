# Авторизация

Система авторизации является ключевым компонентом приложения, позволяющим пользователям создавать 
личные рабочие пространства и управлять своими задачами в безопасной и изолированной среде. 

## Функциональное назначение

Реализация механизма регистрации и аутентификации открывает перед пользователями следующие возможности:

- **Создание персонального аккаунта** с уникальным идентификатором, 
  что позволяет системе разграничивать данные между разными пользователями
- **Безопасный вход в систему** с проверкой учётных данных
- **Изоляция пользовательских данных** - каждый авторизованный пользователь работает исключительно со своими задачами, 
  целями и прогрессом, не имея доступа к чужим данным
- **Мультипользовательский режим** - приложение может одновременно обслуживать множество пользователей, 
  каждый из которых использует его как персональный инструмент для саморазвития

## Модель данных пользователей

Система хранения учётных записей спроектирована с учётом требований безопасности и включает следующие ключевые аспекты:

### Безопасность хранения паролей

В БД хранятся только **хеши паролей**, а не сами пароли в открытом виде. 
Это означает, что даже в случае компрометации базы данных злоумышленники не получат доступа к реальным паролям пользователей. 
При аутентификации система проверяет введённый пароль, преобразуя его в хеш по тому же алгоритму и сравнивая с сохранённым значением.

### Классы данных

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/UserInfo.scala
// Публичная информация о пользователе (без чувствительных данных)
final case class UserInfo(
    id: UserId,
    username: UserName
) derives Decoder, Encoder

// pdp/src/main/scala/ru/scalabook/pdp/models/User.scala
// Полная модель пользователя в системе
// Хранит все данные, включая хеш пароля
final case class User(
    id: UserId,
    username: UserName,
    passwordHash: EncryptedPassword
)
```  

### Валидация данных с помощью уточненных типов

Для обеспечения целостности данных на уровне типов используются уточненные типы с чёткими ограничениями:

- `UserId`: непрозрачный уточненный тип
- `UserName`: строка от 2 до 20 символов, обрезанная от пробелов
- `Password`: сырой пароль от 8 до 100 символов, обрезанный от пробелов
- `EncryptedPassword`: зашифрованный пароль (хеш)

```dotty
object User:
  type UserId = UserId.T
  object UserId extends PdpUUID

  type UserName = UserName.T
  object UserName
      extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]

  type Password = Password.T
  object Password
      extends RefinedType[String, MinLength[8] & MaxLength[100] & Trimmed]

  type EncryptedPassword = EncryptedPassword.T
  object EncryptedPassword
      extends RefinedType[String, Not[Empty] & Trimmed]
``` 

## Сервис хранения данных пользователей

### Абстрактный интерфейс

Вслед за сервисами для работы с задачами и разделами, мы создаём абстрактный сервис для управления пользовательскими учётными записями:

```dotty
trait UsersService[F[_]]:
  /** Создание нового пользователя */
  def create(username: UserName, passwordHash: EncryptedPassword): F[UserId]

  /** Получение пользователя по идентификатору */
  def get(id: UserId): F[Option[User]]

  /** Поиск пользователя по имени */
  def find(username: UserName): F[Option[User]]

  /** Обновление данных пользователя */
  def update(user: User): F[Boolean]

  /** Удаление пользователя */
  def delete(id: UserId): F[Boolean]
end UsersService
```

### Мок-реализация для тестирования

Для проверки работоспособности создадим моканную реализация:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/mock/MockUsersService.scala
private[services] class MockUsersService[F[_]: Sync](
    users: Ref[F, Map[UserId, User]]
) extends UsersService[F]:
  def create(username: UserName, passwordHash: EncryptedPassword): F[UserId] =
    val id   = UserId(UUID.randomUUID())
    val user = User(id, username, passwordHash)
    users.update(_ + (id -> user)).as(id)

  def get(id: UserId): F[Option[User]] =
    users.get.map(_.values.find(_.id == id))

  def find(username: UserName): F[Option[User]] =
    users.get.map(_.values.find(_.username == username))

  def update(user: User): F[Boolean] =
    users.update(_ + (user.id -> user)).as(true)

  def delete(id: UserId): F[Boolean] =
    users.update(_ - id).as(true)
end MockUsersService

object MockUsersService:
  def make[F[_]: Sync]: Resource[F, UsersService[F]] =
    Resource.eval:
      Ref[F].of(Map.empty[UserId, User]).map: users =>
        new MockUsersService(users)
```

### Реализация для PostgreSQL

Продуктовая реализация использует Doobie для работы с базой данных:

```dotty
private[pdp] class PostgreSQLUsersService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends UsersService[F]:

  def create(username: UserName, passwordHash: EncryptedPassword): F[UserId] =
    sql"""INSERT INTO users (name, password) 
          VALUES ($username, $passwordHash) 
          RETURNING id"""
      .query[UserId]
      .unique
      .transact(xa)

  def get(id: UserId): F[Option[User]] =
    sql"""SELECT id, name, password FROM users WHERE id = $id"""
      .query[User].option.transact(xa)

  def find(username: UserName): F[Option[User]] =
    fr"SELECT id, name, password FROM users WHERE name = $username LIMIT 1"
      .query[User]
      .option
      .transact(xa)

  def update(user: User): F[Boolean] =
    sql"""UPDATE users SET name = ${user.username}, password = ${user.passwordHash} WHERE id = ${user.id}"""
      .update.run.transact(xa).map(_ > 0)

  def delete(id: UserId): F[Boolean] =
    sql"""DELETE FROM users WHERE id = $id"""
      .update.run.transact(xa).map(_ > 0)
```

## Ключевые архитектурные решения

1. **Безопасность на уровне типов** - использование уточненных типов гарантирует валидность данных на этапе компиляции
2. **Разделение ответственности** - отдельные классы для разных операций (регистрация, вход, публичные данные)
3. **Хеширование паролей** - в базе хранятся только хеши, что минимизирует ущерб от возможной утечки данных
4. **Абстракция эффектов** - использование `F[_]` позволяет легко менять контекст эффектов
5. **Декларативный маппинг** - описание схемы таблицы отделено от логики работы с данными

Такая архитектура обеспечивает безопасную и масштабируемую систему авторизации, 
готовую к использованию в многопользовательском режиме.

## Сервис авторизации

Сервис авторизации является центральным компонентом системы безопасности, 
отвечающим за аутентификацию пользователей и управление сессиями. 
Рассмотрим его архитектуру, внутренние механизмы и взаимодействие с другими компонентами.

### Интерфейс сервиса авторизации

```dotty
trait AuthService[F[_]]:
  /** Регистрация нового пользователя */
  def newUser(username: UserName, password: Password): F[(SignedJwt, UserId)]

  /** Вход существующего пользователя в систему */
  def login(username: UserName, password: Password): F[(SignedJwt, UserId)]

  /** Выход пользователя из системы (инвалидация токена) */
  def logout(token: SignedJwt, username: UserName): F[Boolean]

/** Сервис для валидации пользователей по JWT токенам Используется в middleware
  * для аутентификации запросов
  */
trait UsersAuthService[F[_], A]:
  /** Поиск пользователя по JWT токену */
  def findUser(token: SignedJwt): F[Option[A]]
```

Тип `SignedJwt` приходит из библиотеки [Jots][jots] — чисто функционального JWT-модуля, 
построенного на основе Cats Effect.

### Управление токенами

JWT токены используются для поддержания пользовательской сессии. 
Они хранятся в Redis с ограниченным временем жизни для возможности принудительного завершения сессий.

Для работы с Redis будем использовать библиотеку [Redis4Cats][redis4cats] — это чисто функциональный 
и асинхронный клиент Redis, построенный на основе Cats Effect, Fs2 и Java-библиотеки Lettuce.

Добавляем зависимости:

```dotty
object redis4cats {
  private val version = "2.0.6"
  val effects         = "dev.profunktor" %% "redis4cats-effects"  % version
  val log4cats        = "dev.profunktor" %% "redis4cats-log4cats" % version
}

val pdpLibs: Seq[ModuleID] =
  Seq(
    // ...
    redis4cats.effects,
    redis4cats.log4cats,
    // ...
  )
```

#### Генерация токенов

Компонент `Tokens` отвечает за создание JWT токенов. Библиотека [Jots][jots] 
предоставляет типобезопасный API: claims описываются обычным case-классом, 
а подпись выполняется сервисом `JwtSigning[F]`.

```dotty
// Полезная нагрузка (claims) токена
final case class TokenClaims(
    userId: UUID,
    issuedAt: Long,
    expiresAt: Long
)

object TokenClaims:
  given Encoder.AsObject[TokenClaims] =
    Encoder.forProduct3("userId", "iat", "exp")(claims =>
      (claims.userId.toString, claims.issuedAt, claims.expiresAt)
    )

  given JwtEncoder[TokenClaims] =
    JwtEncoder.encodeClaims
end TokenClaims

trait Tokens[F[_]]:
  /** Создание нового JWT токена
    *
    * @return
    *   сгенерированный JWT токен
    */
  def create: F[SignedJwt]

object Tokens:
  def make[F[_]: {GenUUID, Sync}](
      jwtSigning: JwtSigning[F], // Сервис подписания токенов
      expiration: FiniteDuration // Время жизни токена
  ): Tokens[F] =
    new Tokens[F]:
      def create: F[SignedJwt] =
        for
          uuid <- GenUUID[F].make
          now  <- Sync[F].delay(Instant.now().getEpochSecond)
          claims = TokenClaims(uuid, now, now + expiration.toSeconds)
          token <- claims.asJwt.signWith(jwtSigning)
        yield token
  end make
end Tokens
```

#### Конфигурация

Константы конфигурации вынесены во внешний файл `application.conf`:

```conf
// pdp/src/main/resources/application.conf

token = {
  jwt-access-token-key = "5h0pp1ng_k4rt"
  expiration = 60.minutes
}
```

В конфигурационном файле определены следующие новые параметры:

```dotty
type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.T

object JwtAccessTokenKeyConfig extends RefinedType[String, Not[Empty] & Trimmed]

final case class TokenConfig(
    jwtAccessTokenKey: Secret[JwtAccessTokenKeyConfig],
    expiration: FiniteDuration
) derives ConfigReader

object TokenConfig:
  given ConfigReader[Secret[JwtAccessTokenKeyConfig]] =
    ConfigReader[JwtAccessTokenKeyConfig].map(Secret.apply)

/** `Config` — корневой объект конфигурации. */
final case class Config(
    httpServerConfig: HttpServerConfig,
    postgresql: PostgreSQLConfig,
    redis: RedisConfig,
    token: TokenConfig
) derives ConfigReader
```

#### Настройка подписания и проверки токенов

Библиотека [Jots][jots] сама управляет временными метками токена, поэтому всю работу с подписью 
и проверкой выполняют два билдера: `JwtSigningBuilder` и `JwtVerificationBuilder`.

```dotty
for
  crypto <- Crypto.make[F]
  secretKeyValue = cfg.token.jwtAccessTokenKey.value.value
  secretKey      <- SecretKey(secretKeyValue).liftTo[F]

  // Сервис подписания токенов (используется в Tokens.create)
  userJwtSigning <- JwtSigningBuilder
    .default[F]
    .hmac(HS256, secretKey)
    .build

  // Сервис проверки подписи (используется в JwtAuthMiddleware)
  userJwtVerification <- JwtVerificationBuilder
    .default[F]
    .hmac(HS256, secretKey)
    .build

  val tokens = Tokens.make[F](userJwtSigning, cfg.token.expiration)
  ...
yield new Security[F](userJwtVerification, auth, usersAuth) {}
```

- **`SecretKey(secretKeyValue)`** — утилита поднятия строки ключа в безопасный тип `SecretKey`.
- **`JwtSigningBuilder.default[F].hmac(HS256, secretKey).build`** — собирает `JwtSigning[F]`
  для подписания токенов алгоритмом HMAC-SHA256.
- **`JwtVerificationBuilder...build`** — собирает `JwtVerification[F]` для проверки подписи.
- Оба билдера используют **один и тот же секретный ключ** — важно для согласованности 
  подписи и проверки.

#### Генерация уникальных идентификаторов

Для создания уникальных ID токенов используется генератор UUID:

```dotty
trait GenUUID[F[_]]:
  def make: F[UUID]

  def read(str: String): F[UUID]

object GenUUID:
  def apply[F[_]: GenUUID]: GenUUID[F] = summon

  given forSync[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F]:
      def make: F[UUID] = Sync[F].delay(UUID.randomUUID())

      def read(str: String): F[UUID] =
        ApplicativeThrow[F].catchNonFatal(UUID.fromString(str))
```

### Криптографические операции

Безопасность паролей обеспечивается компонентом `Crypto`, использующим алгоритм BCrypt:

```dotty
trait Crypto:
  def encrypt(value: Password): Either[String, EncryptedPassword]

  def check(password: Password, encrypted: EncryptedPassword): Boolean
end Crypto

object Crypto:
  def make[F[_]: Sync]: F[Crypto] =
    Sync[F].delay:
      new Crypto:
        def encrypt(password: Password): Either[String, EncryptedPassword] =
          val hashed = BCrypt.hashpw(password.value, BCrypt.gensalt)
          EncryptedPassword.either(hashed)

        def check(password: Password, encrypted: EncryptedPassword): Boolean =
          BCrypt.checkpw(password.value, encrypted.value)
end Crypto
```

**Принцип работы BCrypt:**

- Автоматически генерирует случайный salt для каждого пароля
- Встраивает salt в результирующий хеш
- Устойчив к атакам по словарю и радужным таблицам
- Настраиваемое количество итераций (work factor) для замедления подбора

### Безопасность

1. **Пароли не хранятся в открытом виде** - только BCrypt хеши
2. **Токены имеют ограниченное время жизни** - автоматическая инвалидация
3. **Возможность принудительного завершения сессий** - через удаление из Redis
4. **Уникальные идентификаторы токенов** - предотвращает повторное использование
5. **JWT подпись** - гарантирует целостность токена

---

**Ссылки:**

- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]
- [Jots — JWT для Scala и Cats Effect][jots]

[repo]: https://gitverse.ru/scalabook/pdp
[pfp]: https://leanpub.com/pfp-scala
[redis4cats]: https://github.com/profunktor/redis4cats
[jots]: https://github.com/vlovgr/jots

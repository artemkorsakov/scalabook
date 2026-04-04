{%
showPrev = true
prevPage = lifecycle/complete.html
showNext = true
nextPage = api.html
%}

# Авторизация

Система авторизации является ключевым компонентом нашего приложения, позволяющим пользователям создавать 
личные рабочие пространства и управлять своими задачами в безопасной и изолированной среде. 
Рассмотрим её архитектуру и реализацию детально.

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

Критически важным решением является хранение в БД только **хешей паролей**, а не самих паролей в открытом виде. 
Это означает, что даже в случае компрометации базы данных злоумышленники не получат доступа к реальным паролям пользователей. 
При аутентификации система проверяет введённый пароль, преобразуя его в хеш по тому же алгоритму и сравнивая с сохранённым значением.

### Классы данных

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/User.scala

// Модель для регистрации нового пользователя
// Содержит имя пользователя и уже зашифрованный пароль
final case class UserRegistration(
    username: UserName,
    passwordHash: EncryptedPassword
)

// Модель для входа в систему
// Содержит имя пользователя и пароль в открытом виде для проверки
final case class LoginRequest(
    username: UserName,
    password: Password
)

// Публичная информация о пользователе (без чувствительных данных)
final case class UserInfo(
    id: UserId,
    username: UserName
)

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

- `UserID`: непрозрачный тип, содержащий значение типа `java.util.UUID`
- `UserName`: строка от 2 до 20 символов, обрезанная от пробелов
- `Password`: сырой пароль от 8 до 100 символов, обрезанный от пробелов
- `EncryptedPassword`: зашифрованный пароль (хеш) с теми же ограничениями длины

```dotty
object User:
  opaque type UserId = UUID
  object UserId:
    def apply(uuid: UUID): UserId = uuid

  type UserName = UserName.T
  object UserName
      extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]

  type Password = Password.T
  object Password
      extends RefinedType[String, MinLength[8] & MaxLength[100] & Trimmed]

  type EncryptedPassword = EncryptedPassword.T
  object EncryptedPassword
      extends RefinedType[String, MinLength[8] & MaxLength[100] & Trimmed]
``` 

### Маппинг на таблицу базы данных

Модель `User` имеет декларативное описание маппинга на таблицу БД:

```dotty
given TableSchema[User] with
  def tableName: TableName = TableName("users")

  def columns: Columns =
    Columns(
      // Первичный ключ с авто-генерацией UUID
      ColumnDefinition(ColumnName.Id, ColumnType.UUID)
        .asPK()
        .default(ColumnDefault.UUID),
      // Имя пользователя - обязательное поле
      ColumnDefinition(ColumnName("name"), ColumnType.Varchar)
        .asNotNull(),
      // Хеш пароля - обязательное поле
      ColumnDefinition(ColumnName("password"), ColumnType.Varchar)
        .asNotNull()
    )   
```

## Сервис хранения данных пользователей

### Абстрактный интерфейс

Вслед за сервисами для работы с задачами и разделами, мы создаём абстрактный сервис для управления пользовательскими учётными записями:

```dotty
private[services] trait UsersService[F[_]]:
  /** Создание нового пользователя
    *
    * @param newUser
    *   данные для регистрации с зашифрованным паролем
    * @return
    *   UUID созданного пользователя
    */
  def create(newUser: UserRegistration): F[UserId]

  /** Получение пользователя по идентификатору
    *
    * @param id
    *   UUID пользователя
    * @return
    *   пользователь, если найден
    */
  def get(id: UserId): F[Option[User]]

  /** Поиск пользователя по имени
    *
    * @param username
    *   имя пользователя
    * @return
    *   пользователь, если найден
    */
  def find(username: UserName): F[Option[User]]

  /** Обновление данных пользователя
    *
    * @param user
    *   обновлённая модель пользователя
    */
  def update(user: User): F[Boolean]

  /** Удаление пользователя
    *
    * @param id
    *   UUID пользователя для удаления
    */
  def delete(id: UserId): F[Boolean]
end UsersService
```

### Мок-реализация для тестирования

Для целей разработки и тестирования предоставляется моканная реализация:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/mock/MockUsersService.scala

private[services] class MockUsersService[F[_]: Sync](
    users: Ref[F, Map[UserId, User]]
) extends UsersService[F]:
  def create(newUser: UserRegistration): F[UserId] =
    for
      uuid <- GenUUID[F].make
      id   = UserId(uuid)
      user = User(id, newUser.username, newUser.passwordHash)
      _ <- users.update(_ + (id -> user))
    yield id

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

Продуктовая реализация использует Doobie для работы с базой данных и макросы для генерации типовых SQL-запросов:

```dotty
private[pdp] class PostgreSQLUsersService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends UsersService[F]:

  def create(newUser: UserRegistration): F[UserId] =
    generateInsertReturningId[User, UserRegistration, Int](newUser)
      .query[UserId]
      .unique
      .transact(xa)

  def get(id: UserId): F[Option[User]] =
    generateSelectById[User, UserId](id)
      .query[User].option.transact(xa)

  def find(username: UserName): F[Option[User]] =
    fr"SELECT id, name, password FROM users WHERE name = $username LIMIT 1"
      .query[User]
      .option
      .transact(xa)

  def update(user: User): F[Boolean] =
    generateUpdateById[User, UserId](user)
      .update.run.transact(xa).map(_ > 0)

  def delete(id: UserId): F[Boolean] =
    generateDeleteById[User, UserId](id)
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
  /** Регистрация нового пользователя
    *
    * @param username
    *   выбранное имя пользователя
    * @param password
    *   пароль в открытом виде (будет зашифрован)
    * @return
    *   JWT токен для аутентификации
    * @throws UserNameInUse
    *   если пользователь с таким именем уже существует
    */
  def newUser(username: UserName, password: Password): F[JwtToken]

  /** Вход существующего пользователя в систему
    *
    * @param username
    *   имя пользователя
    * @param password
    *   пароль для проверки
    * @return
    *   JWT токен для аутентификации
    * @throws UserNotFound
    *   если пользователь не найден
    * @throws InvalidPassword
    *   если пароль неверен
    */
  def login(username: UserName, password: Password): F[JwtToken]

  /** Выход пользователя из системы (инвалидация токена)
    *
    * @param token
    *   JWT токен для аннулирования
    * @param username
    *   имя пользователя для дополнительной проверки
    */
  def logout(token: JwtToken, username: UserName): F[Boolean]

/** Сервис для валидации пользователей по JWT токенам Используется в middleware
  * для аутентификации запросов
  */
trait UsersAuthService[F[_], A]:
  /** Поиск пользователя по JWT токену
    *
    * @param token
    *   JWT токен из запроса
    * @param claim
    *   декодированные данные из токена
    * @return
    *   Optional с данными пользователя, если токен валиден
    */
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
```

### Управление токенами

JWT токены используются для поддержания пользовательской сессии. 
Они хранятся в Redis с ограниченным временем жизни для возможности принудительного завершения сессий.

Для работы с Redis будем использовать библиотеку [Redis4Cats][redis4cats] — это чисто функциональный 
и асинхронный клиент Redis, построенный на основе Cats Effect, Fs2 и Java-библиотеки Lettuce.

Добавляем зависимости:

```dotty
object redis4cats {
  private val version = "2.0.3"
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

Компонент `Tokens` отвечает за создание JWT токенов с уникальными идентификаторами:

```dotty
trait Tokens[F[_]]:
  /** Создание нового JWT токена
    *
    * @return
    *   сгенерированный JWT токен
    */
  def create: F[JwtToken]

object Tokens:
  def make[F[_]: {GenUUID, Monad}](
      jwtExpire: JwtExpire[F], // Сервис управления временем жизни
      jwtAccessTokenKey: JwtAccessTokenKeyConfig, // Секретный ключ для подписи
      expiration: FiniteDuration                  // Время жизни токена
  ): Tokens[F] =
    val secretKey =
      JwtSecretKey(jwtAccessTokenKey.value.toCharArray)

    new Tokens[F]:
      def create: F[JwtToken] =
        for
          // Генерация уникального идентификатора для токена
          uuid <- GenUUID[F].make
          // Создание claim с временем жизни
          claim <- jwtExpire.expiresIn(
            JwtClaim(uuid.asJson.noSpaces),
            expiration
          )
          // Подписание токена
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
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
    localization: LocalizationConfig,
    token: TokenConfig
) derives ConfigReader
```

#### Управление временем жизни токенов

Компонент `JwtExpire` обеспечивает корректную установку времени истечения токенов:

```dotty
trait JwtExpire[F[_]]:
  /** Установка времени истечения для claim
    *
    * @param claim
    *   исходный JWT claim
    * @param exp
    *   длительность жизни токена
    * @return
    *   claim с установленным временем истечения
    */
  def expiresIn(claim: JwtClaim, exp: FiniteDuration): F[JwtClaim]

object JwtExpire:
  def make[F[_]: Sync]: F[JwtExpire[F]] =
    JwtClock[F].utc.map:
      implicit clock: Clock =>
        new JwtExpire[F]:
          def expiresIn(claim: JwtClaim, exp: FiniteDuration): F[JwtClaim] =
            Sync[F].delay(claim.issuedNow.expiresIn(exp.toMillis))
```

#### Работа со временем в JWT

Для корректной работы с временными метками используется абстракция часов:

```dotty
trait JwtClock[F[_]]:
  /** Получение UTC часов для JWT операций */
  def utc: F[Clock]

object JwtClock:
  def apply[F[_]: JwtClock]: JwtClock[F] = summon

  given forSync[F[_]: Sync]: JwtClock[F] =
    new JwtClock[F]:
      def utc: F[Clock] = Sync[F].delay(Clock.systemUTC())
```

#### Генерация уникальных идентификаторов

Для создания уникальных ID токенов используется генератор UUID:

```dotty
trait GenUUID[F[_]]:
  /** Генерация случайного UUID */
  def make: F[UUID]

  /** Парсинг UUID из строки */
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
  /** Шифрование пароля
    *
    * @param value
    *   сырой пароль
    * @return
    *   Either с ошибкой валидации или зашифрованным паролем
    */
  def encrypt(value: Password): Either[String, EncryptedPassword]

  /** Проверка соответствия пароля и хеша
    *
    * @param password
    *   сырой пароль для проверки
    * @param encrypted
    *   сохранённый хеш
    * @return
    *   true если пароль соответствует хешу
    */
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

Такая архитектура обеспечивает надёжную и масштабируемую систему авторизации, готовую к использованию в продакшене.

## Создание сервиса авторизации

Фабричный метод `make` является центральным местом сборки всех компонентов системы авторизации. 
Он демонстрирует, как различные сервисы и зависимости объединяются для создания целостного механизма аутентификации.

### Сигнатура метода

```dotty
def make[F[_]: {MonadThrow as MT}](
    tokenExpiration: FiniteDuration,         // Время жизни токена (например, 1 час)
    tokens: Tokens[F],                       // Генератор JWT токенов
    usersService: UsersService[F],           // Сервис работы с пользователями БД
    redis: RedisCommands[F, String, String], // Redis хранилище для сессий
    crypto: Crypto                           // Криптографический сервис для паролей
): AuthService[F]
```

**Параметры контекста:**

- `MonadThrow[F]` - требование к эффектному типу `F` возможности обрабатывать ошибки (поднимать их в контекст эффекта)

### Детальная реализация методов

#### 1. Регистрация нового пользователя (`newUser`)

Метод реализует полный цикл создания учётной записи:

```dotty
def newUser(username: UserName, password: Password): F[JwtToken] =
  // Шаг 1: Проверка уникальности имени пользователя
  usersService.find(username).flatMap:
    case Some(_) =>
      // Если пользователь уже существует - сигнализируем об ошибке
      UserNameInUse(username).raiseError[F, JwtToken]

    case None =>
      // Шаг 2: Шифрование пароля с обработкой возможных ошибок
      val encryptedPasswordF =
        MT.fromEither(
          crypto.encrypt(password)
            .leftMap(new IllegalArgumentException(_))
        )      

      // Шаг 3: Последовательное выполнение операций
      for
        // 3.1 Получаем зашифрованный пароль
        encryptedPassword <- encryptedPasswordF

        // 3.2 Создаём объект для регистрации
        newUser = UserRegistration(username, encryptedPassword)

        // 3.3 Сохраняем пользователя в БД и получаем ID
        userId <- usersService.create(newUser)

        // 3.4 Генерируем JWT токен для новой сессии
        token <- tokens.create

        // 3.5 Подготавливаем публичные данные пользователя для Redis
        user = UserInfo(userId, username).asJson.noSpaces

        // 3.6 Сохраняем связку токен -> данные пользователя
        _ <- redis.setEx(token.value, user, tokenExpiration)

        // 3.7 Сохраняем обратную связку username -> текущий токен
        _ <- redis.setEx(username.show, token.value, tokenExpiration)
      yield token       
```

**Ключевые моменты регистрации:**

1. **Валидация на раннем этапе** - проверка существования пользователя до любых других операций
2. **Безопасная обработка шифрования** - преобразование `Either` в `F` с сохранением контекста ошибки
3. **Атомарность операций** - использование for-comprehension для последовательного выполнения
4. **Двойное связывание в Redis**:
   - Прямая связь: `token.value -> UserInfo` для быстрого доступа по токену
   - Обратная связь: `username -> token.value` для быстрого поиска текущего токена пользователя

#### 2. Вход пользователя в систему (`login`)

Метод аутентификации с проверкой учётных данных:

```dotty
def login(username: UserName, password: Password): F[JwtToken] =
  // Шаг 1: Поиск пользователя в БД
  usersService.find(username).flatMap:
    case None =>
      // Пользователь не найден - ошибка аутентификации
      UserNotFound(username).raiseError[F, JwtToken]

    case Some(user) if !crypto.check(password, user.passwordHash) =>
      // Пароль не совпадает с хешем - ошибка аутентификации
      InvalidPassword(user.username).raiseError[F, JwtToken]

    case Some(user) =>
      // Шаг 2: Проверяем наличие активной сессии
      redis.get(username.show).flatMap:
        case Some(t) =>
          // У пользователя уже есть активная сессия - возвращаем существующий токен
          // Это позволяет избежать множественных сессий для одного пользователя
          JwtToken(t).pure[F]

        case None =>
          // Шаг 3: Создаём новую сессию
          tokens.create.flatTap: token =>
            // flatTap позволяет выполнить побочные эффекты, сохраняя результат
            redis.setEx(
              token.value,
              user.asJson.noSpaces,
              tokenExpiration
            ) *>
              redis.setEx(username.show, token.value, tokenExpiration)        
```

**Логика аутентификации:**

1. **Поэтапная проверка** - сначала существование пользователя, затем пароль
2. **Единая сессия** - политика "одна активная сессия на пользователя"
3. **Восстановление сессии** - если сессия уже существует в Redis, возвращаем её токен
4. **Побочные эффекты** - использование `flatTap` для сохранения в Redis без потери токена

#### 3. Выход из системы (`logout`)

```dotty
def logout(token: JwtToken, username: UserName): F[Boolean] =
  for
    deltoc  <- redis.del(token.value)   // Удаляем связь токен -> пользователь
    detuser <- redis.del(username.show) // Удаляем связь пользователь -> токен  
  yield deltoc > 0 && detuser > 0  
```

**Особенности реализации:**

- Использование `*>` для последовательного выполнения операций
- Приведение к `Unit` в конце для соответствия сигнатуре, показывающей отсутствие результата, только побочные эффекты
- Атомарность операций гарантирует консистентность состояния

#### Сервис аутентификации пользователей

Отдельный компонент для middleware, проверяющий токены в запросах:

```dotty
object UsersAuthService:
  def make[F[_]: Functor](
      redis: RedisCommands[F, String, String]
  ): UsersAuthService[F, UserInfo] =
    new UsersAuthService[F, UserInfo]:
      def findUser(token: JwtToken)(claim: JwtClaim)
          : F[Option[UserInfo]] =
        redis
          .get(token.value)     // Получаем JSON из Redis по токену
          .map(_.flatMap: u =>  // Парсим JSON в Option[UserInfo]
            decode[UserInfo](u).toOption
          )      
```

**Принцип работы:**

1. Middleware перехватывает HTTP-запрос и извлекает токен из заголовка
2. Вызывает `findUser` с полученным токеном
3. Если токен валиден (существует в Redis) - возвращает `UserInfo`
4. Если токена нет в Redis или он некорректен - возвращает `None`, запрос отклоняется

### Схема взаимодействия компонентов

```
HTTP Request (с токеном)
       ↓
[Auth Middleware] → UsersAuthService.findUser(token)
       ↓                    ↓
    Запрос разрешен   Redis lookup token.value
       ↓                    ↓
  Бизнес-логика    ←    UserInfo (если найден)
       ↓
HTTP Response

Регистрация/Вход:
Client → AuthService (newUser/login)
       ↓
   UsersService (БД)
       ↓
   Token Generation
       ↓
   Redis Storage (сессии)
       ↓
   JWT Token → Client
```

### Преимущества такой архитектуры

1. **Разделение ответственности** - каждый компонент отвечает за свою узкую задачу
2. **Тестируемость** - все зависимости могут быть замоканы
3. **Масштабируемость** - Redis как центральное хранилище сессий позволяет горизонтально масштабировать приложение
4. **Безопасность** - пароли никогда не передаются в открытом виде, сессии управляются централизованно
5. **Производительность** - Redis обеспечивает быстрый доступ к данным сессий
6. **Отказоустойчивость** - при падении одного экземпляра приложения сессии сохраняются в Redis

### Обработка ошибок

Сервис определяет несколько типов ошибок для разных сценариев:

- **UserNameInUse(username)** - попытка регистрации с существующим именем
- **UserNotFound(username)** - попытка входа несуществующего пользователя
- **InvalidPassword(username)** - неверный пароль при входе
- **IllegalArgumentException** - ошибка при шифровании пароля (невалидный вход)

Все ошибки поднимаются в контекст `F` через `raiseError`, что позволяет централизованно обрабатывать их в HTTP слое.

## Тестирование сервиса авторизации

Тестирование является критически важным этапом разработки, 
особенно для таких чувствительных компонентов как система авторизации. 
Давайте подробно рассмотрим, как организовано тестирование с использованием TestContainers 
для изолированного окружения и специфических ассертов для проверки различных сценариев.

### Настройка тестового окружения

Для обеспечения изолированного и воспроизводимого тестирования используется подход с общими ресурсами через TestContainers:

```dotty
object SharedResources extends GlobalResource:
  // Ключ для доступа к контейнеру Redis в глобальном контексте
  val RedisKey: Option[String] = "redis".some

  /** Инициализация общих ресурсов для всех тестов
    * @param global глобальный контекст для хранения разделяемых ресурсов
    */
  def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for
      // Запуск Redis контейнера
      redisContainer <- redisResource()
      // Сохранение контейнера в глобальном контексте для доступа из тестов
      _              <- global.putR(redisContainer, RedisKey)
    yield ()

  /** Создание ресурса Redis контейнера
    * @return Resource с запущенным Redis контейнером
    */
  private def redisResource(): Resource[IO, RedisContainer] =
    Resource.make {
      IO {
        val container = RedisContainer() // Создаём контейнер с Redis
        container.start()                // Запускаем контейнер
        container
      }
    }(container => IO(container.stop())) // Гарантированно останавливаем после тестов
```

**Почему такой подход?**

1. **Изоляция** - каждый тестовый запуск работает с чистой базой данных
2. **Воспроизводимость** - тесты не зависят от внешних сервисов
3. **Параллелизм** - общие ресурсы оптимизируют использование контейнеров
4. **Автоматическая очистка** - `Resource` гарантирует остановку контейнеров

### Структура тестового класса

```dotty
class AuthServiceSuite(global: GlobalRead) extends IOSuite:
  // Настройка логирования - отключаем для чистоты вывода тестов
  given Logger[IO] = NoOpLogger[IO]

  // Конфигурационные параметры для тестов
  private val tokenExpiration: FiniteDuration = 30.minutes
  private val jwtAccessTokenKey: JwtAccessTokenKeyConfig =
    JwtAccessTokenKeyConfig("bar")  // Секретный ключ для подписи
  private val jwtClaim: JwtClaim = JwtClaim("test")  // Тестовый claim

  // Тип разделяемого ресурса - кортеж из двух сервисов
  type Res = (AuthService[IO], UsersAuthService[IO, UserInfo])

  /** Инициализация разделяемого ресурса для всех тестов
    * Создаёт и настраивает все зависимости
    */
  override def sharedResource: Resource[IO, Res] =
    for
      // Получаем уже запущенный Redis контейнер из глобального контекста
      redisContainer <- global.getOrFailR[RedisContainer](RedisKey)

      // Создаем клиент Redis
      redis          <- Redis[IO].utf8(redisContainer.redisUri)

      // Очищаем Redis перед тестами
      _              <- Resource.eval(redis.flushAll)

      // Получаем реальный сервис БД (PostgreSQL) из общего ресурса
      usersService <- global.getOrFailR[PostgreSQLUsersService](UsersServiceKey)

      // Создаем компонент для работы с JWT
      jwtExpire     <- Resource.eval(JwtExpire.make[IO])
      tokens        <- Resource.eval(
        Tokens.make[IO](jwtExpire, jwtAccessTokenKey, tokenExpiration).pure[IO]
      )

      // Создаем криптографический сервис
      crypto <- Resource.eval(Crypto.make[IO])

      // Собираем всё вместе в конечные сервисы
    yield (
      AuthService.make[IO](
        tokenExpiration = tokenExpiration,
        tokens = tokens,
        usersService = usersService,
        redis = redis,
        crypto = crypto
      ),
      UsersAuthService.make[IO](redis)
    )
    end for
  end sharedResource
```

### Детальное описание тестов

#### 1. Поиск несуществующего токена

```dotty
test("UsersAuthService.findUser should return None if the JWT is not found"):
  resource =>
    val (_, usersAuthService) = resource

    for
      notFoundUser <- usersAuthService.findUser(JwtToken("invalid"))(jwtClaim)
    yield expect.all(notFoundUser.isEmpty)
```

**Что проверяет:**

- Обращение к Redis с несуществующим токеном
- Корректность обработки отсутствия данных
- Возврат `None` вместо ошибки

**Почему это важно:**
Middleware не должна падать при невалидных токенах, а должна корректно отклонять запросы.

#### 2. Полный lifecycle пользователя

```dotty
test("UsersAuthService.findUser should return an user if it exists"):
  resource =>
    val (authService, usersAuthService) = resource

    val username = UserName("first_user")
    val password = Password("first_password")

    for
      // Регистрация нового пользователя
      newUserToken    <- authService.newUser(username, password)
      maybeNewUser    <- usersAuthService.findUser(newUserToken)(jwtClaim)

      // Вход существующего пользователя
      loggedUserToken <- authService.login(username, password)
      maybeLoggedUser <- usersAuthService.findUser(loggedUserToken)(jwtClaim)

      // Выход из системы
      status          <- authService.logout(loggedUserToken, username)

      // Проверка инвалидации токена после выхода
      maybeLoggedOutNewUser <-
        usersAuthService.findUser(newUserToken)(jwtClaim)
    yield whenSuccess(maybeNewUser) { user =>
      expect.same(username, user.username)
    } |+|
      whenSuccess(maybeLoggedUser) { user =>
        expect.same(username, user.username)
      } |+|
      expect(status) |+|
      expect(maybeLoggedOutNewUser.isEmpty)
    end for
```

**Сценарий проверяет:**

1. ✓ Успешную регистрацию и получение токена
2. ✓ Корректность данных найденного пользователя
3. ✓ Успешный вход и получение токена
4. ✓ Работу logout (инвалидацию токена)
5. ✓ Невозможность использовать старый токен после выхода

#### 3. Проверка уникальности username

```dotty
test("UsersAuthService.newUser should not create an user with existed username"):
  resource =>
    val (authService, usersAuthService) = resource

    val username  = UserName("second_user")
    val password  = Password("second_password")
    val password2 = Password("second_password2")

    for
      // Первая регистрация - успешна
      newUserToken  <- authService.newUser(username, password)
      maybeNewUser  <- usersAuthService.findUser(newUserToken)(jwtClaim)

      // Попытка повторной регистрации с тем же именем
      existedUser   <- authService.newUser(username, password2).attempt

      // Проверка что старый пользователь не изменился
      maybeNewUser2 <- usersAuthService.findUser(newUserToken)(jwtClaim)
    yield matches((maybeNewUser, maybeNewUser2)) {
      case (Some(user), Some(user2)) =>
        expect.all(
          user.id == user2.id,             // ID не изменился
          user.username == user2.username, // Имя не изменилось
          existedUser == Left(UserNameInUse(username)) // Получили правильную ошибку
        )
    }
```

**Ключевые проверки:**

- Защита от дублирования имён пользователей
- Сохранение целостности существующих данных
- Корректный тип ошибки

#### 4. Проверка входа несуществующего пользователя

```dotty
test("UsersAuthService.login should not login non-existent or invalid credentials"):
  resource =>
    val (authService, _) = resource

    val username = UserName("non-existent_user")
    val password = Password("non-existent_password")

    for
      nonExistent <- authService.login(username, password).attempt
    yield matches(nonExistent) { case Left(error) =>
      expect.same(UserNotFound(username), error)
    }
```

**Проверяет:**

- Невозможность входа с несуществующим username
- Корректную ошибку для несуществующего пользователя
- Отсутствие утечек информации (ошибка не указывает, что именно неверно)

#### 5. Проверка неверного пароля

```dotty
test("UsersAuthService.login should not login if a passport is invalid"):
  resource =>
    val (authService, _) = resource

    val username  = UserName("third_user")
    val password  = Password("third_password")
    val password2 = Password("third_password2")

    for
      _               <- authService.newUser(username, password)
      invalidPassport <- authService.login(username, password2).attempt
    yield matches(invalidPassport) { case Left(error) =>
      expect.same(InvalidPassword(username), error)
    }
```

**Важные аспекты:**

- Пользователь существует, но пароль неверен
- Получаем специфичную ошибку о неверном пароле

#### 6. Вход после выхода

```dotty
test("UsersAuthService.login should login after logout"):
  resource =>
    val (authService, usersAuthService) = resource

    val username = UserName("forth_user")
    val password = Password("forth_password")

    for
      token     <- authService.newUser(username, password)
      _         <- authService.login(username, password)
      status    <- authService.logout(token, username)
      newToken  <- authService.login(username, password)
      maybeUser <- usersAuthService.findUser(newToken)(jwtClaim)
    yield whenSuccess(maybeUser): user =>
      expect.same(username, user.username) |+|
        expect(status)
```

**Проверяет:**

- Возможность повторного входа после выхода
- Генерацию нового токена при повторном входе
- Корректность данных в новом токене

### Покрытие сценариев

Тесты покрывают все основные пользовательские сценарии:

| Сценарий                        | Проверяет                        |
|---------------------------------|----------------------------------|
| Регистрация нового пользователя | Создание, уникальность username  |
| Вход существующего пользователя | Аутентификация, генерация токена |
| Выход из системы                | Инвалидация токена               |
| Повторный вход                  | Создание новой сессии            |
| Неверные учётные данные         | Обработка ошибок                 |
| Несуществующий пользователь     | Безопасность (отсутствие утечек) |

### Принципы тестирования

1. **Изоляция** - каждый тест работает с чистыми данными
2. **Детерминированность** - одинаковые входные данные дают одинаковый результат
3. **Покрытие happy path** - основные сценарии успешной работы
4. **Покрытие ошибок** - все возможные ошибочные ситуации
5. **Проверка инвариантов** - данные остаются консистентными

### Метрики качества

- ✅ Все публичные методы сервиса протестированы
- ✅ Позитивные и негативные сценарии
- ✅ Граничные случаи
- ✅ Интеграция с реальными БД (через TestContainers)
- ✅ Изолированное окружение для каждого теста

Такое всестороннее тестирование гарантирует надёжную работу системы авторизации в продакшене.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala
[redis4cats]: https://github.com/profunktor/redis4cats

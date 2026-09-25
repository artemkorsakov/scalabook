{%
showPrev = true
prevPage = ../authorization.html
showNext = true
nextPage = ../health_check.html
%}

# Реализация сервиса авторизации

## Создание сервиса авторизации

Фабричный метод `make` является центральным местом сборки всех компонентов системы авторизации. 
Он демонстрирует, как различные сервисы и зависимости объединяются для создания целостного механизма аутентификации.

### Сигнатура метода

```dotty
def make[F[_]: MonadThrow](
    tokenExpiration: FiniteDuration,         // Время жизни токена (например, 1 час)
    tokens: Tokens[F],                       // Генератор JWT токенов
    usersService: UsersService[F],           // Сервис работы с пользователями БД
    redis: RedisCommands[F, String, String], // Redis хранилище для сессий
    crypto: Crypto                           // Криптографический сервис для паролей
): AuthService[F]
```

**Параметры контекста:**

- `MonadThrow[F]` - требование к типу с эффектом `F` возможности обрабатывать ошибки (поднимать их в контекст эффекта)

## Детальная реализация методов

### 1. Регистрация нового пользователя (`newUser`)

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
        MonadThrow[F].fromEither(
          crypto.encrypt(password)
            .leftMap(new IllegalArgumentException(_))
        )      

      // Шаг 3: Последовательное выполнение операций
      for
        // 3.1 Получаем зашифрованный пароль
        encryptedPassword <- encryptedPasswordF

        // 3.2 Сохраняем пользователя в БД и получаем ID
        userId <- usersService.create(username, encryptedPassword)

        // 3.3 Генерируем JWT токен для новой сессии
        token <- tokens.create

        // 3.4 Подготавливаем публичные данные пользователя для Redis
        user = UserInfo(userId, username).asJson.noSpaces

        // 3.5 Сохраняем связку токен -> данные пользователя
        _ <- redis.setEx(token.value, user, tokenExpiration)

        // 3.6 Сохраняем обратную связку username -> текущий токен
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

### 2. Вход пользователя в систему (`login`)

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
          // flatTap позволяет выполнить побочные эффекты, сохраняя результат
          tokens.create.flatTap: token =>
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

### 3. Выход из системы (`logout`)

```dotty
def logout(token: JwtToken, username: UserName): F[Boolean] =
  for
    deltoc  <- redis.del(token.value)   // Удаляем связь токен -> пользователь
    detuser <- redis.del(username.show) // Удаляем связь пользователь -> токен  
  yield deltoc > 0 && detuser > 0  
```

**Особенности реализации:**

- Использование for-comprehension для последовательного выполнения операций
- Атомарность операций гарантирует консистентность состояния

### Сервис аутентификации пользователей

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

## Схема взаимодействия компонентов

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

Тестирование является важным этапом разработки, особенно для таких чувствительных компонентов как система авторизации. 
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

## Детальное описание тестов

### 1. Поиск несуществующего токена

```dotty
test("UsersAuthService.findUser should return None if the JWT is not found"):
  resource =>
    val (_, usersAuthService) = resource

    for
      notFoundUser <- usersAuthService.findUser(JwtToken("invalid"))(jwtClaim)
    yield expect.all(notFoundUser.isEmpty)
```

**Что проверяет:**

- ✓ Обращение к Redis с несуществующим токеном
- ✓ Корректность обработки отсутствия данных
- ✓ Возврат `None` вместо ошибки

**Почему это важно:**
Middleware не должна падать при невалидных токенах, а должна корректно отклонять запросы.

### 2. Полный lifecycle пользователя

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

- ✓ Успешную регистрацию и получение токена
- ✓ Корректность данных найденного пользователя
- ✓ Успешный вход и получение токена
- ✓ Работу logout (инвалидацию токена)
- ✓ Невозможность использовать старый токен после выхода

### 3. Проверка уникальности username

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

- ✓ Защита от дублирования имён пользователей
- ✓ Сохранение целостности существующих данных
- ✓ Корректный тип ошибки

### 4. Проверка входа несуществующего пользователя

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

- ✓ Невозможность входа с несуществующим username
- ✓ Корректную ошибку для несуществующего пользователя
- ✓ Отсутствие утечек информации (ошибка не указывает, что именно неверно)

### 5. Проверка неверного пароля

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

- ✓ Пользователь существует, но пароль неверен
- ✓ Получаем специфичную ошибку о неверном пароле

### 6. Вход после выхода

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

- ✓ Возможность повторного входа после выхода
- ✓ Генерацию нового токена при повторном входе
- ✓ Корректность данных в новом токене

## Покрытие сценариев

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

- ✓ Все публичные методы сервиса протестированы
- ✓ Позитивные и негативные сценарии
- ✓ Граничные случаи
- ✓ Интеграция с реальными БД (через TestContainers)
- ✓ Изолированное окружение для каждого теста

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

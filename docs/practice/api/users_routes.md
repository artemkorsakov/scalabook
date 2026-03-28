{%
showPrev = true
prevPage = ../api.html
showNext = true
nextPage = settings_routes.html
%}

# Endpoint-ы авторизации

Система авторизации предоставляет публичные endpoint-ы для регистрации, входа и выхода пользователей. 
Эти endpoint-ы доступны без предварительной аутентификации и являются точками входа в систему.

## Endpoint-ы авторизации

### Регистрация нового пользователя

```
POST /auth/users
```

Регистрация позволяет создать новую учётную запись в системе.

**Формат запроса:**

```json
{
  "username": "akorsakov", // Имя пользователя, от 2 до 20 символов
  "password": "qwerty"     // Пароль, от 8 до 100 символов
}
```

**Коды ответа:**

| Код                 | Описание                    | Сценарий                                                     |
|---------------------|-----------------------------|--------------------------------------------------------------|
| **201 Created**     | Пользователь успешно создан | Все проверки пройдены, пользователь зарегистрирован          |
| **400 Bad Request** | Невалидные данные в запросе | Пустое имя, пароль короче 8 символов, несоответствие формату |
| **409 Conflict**    | Логин уже занят             | Пользователь с таким именем уже существует в системе         |

**Успешный ответ (201):**

```json
{
  "access_token": "eyJ0eXA..." // JWT токен для последующей аутентификации
}
```

**Детали валидации:**

- Имя пользователя проверяется на соответствие уточненному типу `UserName` (2-20 символов, обрезано от пробелов)
- Пароль проверяется на соответствие `Password` (8-100 символов)
- При несоответствии возвращается 400 с описанием ошибки валидации
- Проверка уникальности имени выполняется на уровне сервиса

### Вход в систему

```
POST /auth/login
```

Аутентификация существующего пользователя.

**Формат запроса:**

```json
{
  "username": "akorsakov",
  "password": "qwerty"
}
```

**Коды ответа:**

| Код               | Описание                         | Сценарий                                  |
|-------------------|----------------------------------|-------------------------------------------|
| **200 OK**        | Успешная аутентификация          | Пользователь существует, пароль верен     |
| **403 Forbidden** | Невалидные параметры авторизации | Пользователь не найден или пароль неверен |

**Успешный ответ (200):**

```json
{
  "access_token": "eyJ0eXA..." // JWT токен для сессии
}
```

**Важное замечание о безопасности:**

В отличие от типичной реализации, где при неверном пароле возвращается 400, а при несуществующем пользователе - 404, 
здесь в обоих случаях возвращается **403 Forbidden**. 
Это сделано намеренно для предотвращения утечки информации о существовании пользователей. 
При атаке перебором злоумышленник не сможет определить, существует ли пользователь или нет, 
что существенно усложняет подбор учётных данных.

### Выход из системы

```
POST /auth/logout
```

Завершение сессии пользователя и инвалидация токена.

**Заголовки запроса:**

```
Authorization: Bearer eyJ0eXA...  // Токен для инвалидации
```

**Коды ответа:**

| Код                | Описание           | Сценарий                             |
|--------------------|--------------------|--------------------------------------|
| **204 No Content** | Успешный выход     | Токен найден и удалён из хранилища   |

**Особенности:**

- Не требует тела запроса
- Токен извлекается из заголовка `Authorization`
- Токен удаляется из Redis, делая его недействительным для последующих запросов
- Даже если токен не существует, возвращается 204 для предотвращения утечек информации

## API для логирования и обработки ошибок

### Кастомный декодер `decodeR`

Стандартный механизм декодирования HTTP4S возвращает **422 Unprocessable Entity** при ошибках валидации. 
Однако для нашего API мы хотим возвращать **400 Bad Request** с конкретным сообщением об ошибке, 
особенно при использовании уточненных типов.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/PdpRoutes.scala
trait PdpRoutes[F[_]: {JsonDecoder, MonadThrow, Concurrent}]
    extends Http4sDsl[F]:
  given entityDecoder[A: Decoder]: EntityDecoder[F, A] =
    accumulatingJsonOf[F, A]

  /** Расширение для Request, добавляющее декодирование с улучшенной обработкой
    * ошибок
    */
  extension (req: Request[F])

    /** Декодирует тело запроса в тип A и обрабатывает ошибки валидации
      *
      * @param f
      *   функция, обрабатывающая успешно декодированное значение
      * @tparam A
      *   тип, в который декодируется запрос (должен иметь Decoder)
      * @return
      *   {{{F[Response[F]]}}} - HTTP ответ
      *
      * @note
      *   Отличается от стандартного подхода:
      *   - Для ошибок валидации уточненных типов возвращает 400 с сообщением
      *   - Для прочих ошибок декодирования возвращает 422
      */
    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      req.as[A].attempt.flatMap:
        case Right(a) => f(a)
        case Left(e)  => e match
            case ex: InvalidMessageBodyFailure =>
              Status.UnprocessableContent(
                ex.cause.map(_.getMessage).getOrElse(ex.getMessage)
              )
            case ex: MessageFailure =>
              ex.toHttpResponse(HttpVersion.`HTTP/1.1`).pure[F]
            case ex =>
              Status.BadRequest(ex.getMessage)

  end extension
end PdpRoutes                       
```

**Преимущества подхода:**

1. **Пользовательский опыт** - клиент получает понятное сообщение о том, какие именно данные невалидны
2. **Соответствие REST** - 400 BadRequest более уместен для ошибок валидации, чем 422

**Пример работы:**

```json
// Запрос с невалидным именем (слишком коротким)
POST /auth/users
{
  "username": "a",  // Меньше 2 символов
  "password": "validpassword123"
}

// Ответ с decodeR (400):
{
  "error": "DecodingFailure at .username: Should have a minimum length of 2 ..."
}

// Стандартный ответ (422):
{
  "error": "Invalid JSON"
}
```

## Реализация HTTP маршрутов

### Маршруты для регистрации

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/UserRoutes.scala
final case class UserRoutes[F[_]: {JsonDecoder, MonadThrow, Concurrent}](
    authService: AuthService[F]
) extends PdpRoutes[F]:

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F]:
      case req @ POST -> Root / "users" =>
        // Используем кастомный декодер для валидации
        req.decodeR[LoginRequest]: user =>
          authService
            .newUser(user.username, user.password) // Вызов бизнес-логики
            .flatMap(Created(_))                   // Успех: 201 с токеном
            .recoverWith:
              // Специфичная ошибка: пользователь уже существует
              case UserNameInUse(username) => Conflict(username)

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
```

**Поток обработки:**

1. **Декодирование** - `decodeR` проверяет входные данные
2. **Валидация** - уточненные типы проверяют форматы
3. **Бизнес-логика** - `authService.newUser` создаёт пользователя
4. **Обработка ошибок** - `recoverWith` преобразует ошибки сервиса в HTTP коды

### Маршруты для входа

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/LoginRoutes.scala
final case class LoginRoutes[F[_]: {JsonDecoder, MonadThrow, Concurrent}](
    authService: AuthService[F]
) extends PdpRoutes[F]:

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F]:
      case req @ POST -> Root / "login" =>
        req.decodeR[LoginRequest]: user =>
          authService
            .login(user.username, user.password) // Проверка учётных данных
            .flatMap(Ok(_))                      // Успех: 200 с токеном
            .recoverWith:
              // Обе ошибки маскируются под 403 Forbidden
              case UserNotFound(_) | InvalidPassword(_) => Forbidden()

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)  
```

**Стратегия безопасности:**

| Ошибка          | Стандартный подход | Наш подход     | Причина                               |
|-----------------|--------------------|----------------|---------------------------------------|
| UserNotFound    | 404 Not Found      | 403 Forbidden  | Скрываем существование пользователя   |
| InvalidPassword | 400 Bad Request    | 403 Forbidden  | Не даём информации о валидности имени |

### Маршруты для выхода

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/LogoutRoutes.scala
package ru.scalabook.pdp.http.routes

import cats.Monad
import cats.syntax.all.*
import dev.profunktor.auth.AuthHeaders
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.*
import ru.scalabook.pdp.model.User
import ru.scalabook.pdp.services.AuthService

final case class LogoutRoutes[F[_]: Monad](
    authService: AuthService[F]
) extends Http4sDsl[F]:

  private[routes] val prefixPath = "/auth"

  // Используем AuthedRoutes, так как требуется аутентифицированный пользователь
  private val httpRoutes: AuthedRoutes[UserInfo, F] =
    AuthedRoutes.of:
      case ar @ POST -> Root / "logout" as user =>
        // Извлекаем токен из заголовка Authorization
        AuthHeaders
          .getBearerToken(ar.req)
          // Инвалидируем токен в Redis (traverse_ игнорирует результат)
          .traverse_(authService.logout(_, user.username)) *>
          NoContent()  // Всегда возвращаем 204, даже если токена не было

  def routes(authMiddleware: AuthMiddleware[F, UserInfo]): HttpRoutes[F] =
    Router(prefixPath -> authMiddleware(httpRoutes))  
```

**Ключевые моменты:**

1. **Извлечение токена**: `AuthHeaders.getBearerToken` парсит заголовок `Authorization: Bearer <token>`
2. **Безопасное игнорирование**: `traverse_` выполняет операцию для `Option`, игнорируя результат
3. **Всегда 204**: Даже если токен не найден, возвращаем успех для предотвращения утечек
4. **Аутентификация**: Маршрут защищён middleware, требует валидного пользователя

## Полная схема API

```
Публичные endpoint-ы (без аутентификации):
├── POST /auth/users     → Регистрация
├── POST /auth/login     → Вход
└── POST /auth/logout    → Выход (требует токен в заголовке)
```

### Безопасность

1. **Маскировка ошибок** - 403 скрывает информацию о существовании пользователей
2. **Валидация на уровне типов** - уточненные типы предотвращают некорректные данные
3. **Инвалидация токенов** - при выходе токен удаляется из Redis
4. **JWT с ограниченным сроком** - токены автоматически истекают

## Тестирование HTTP маршрутов

Тестовый класс [AuthRoutesSuite](https://gitverse.ru/scalabook/pdp/content/master/pdp%2Fsrc%2Ftest%2Fscala%2Fru%2Fscalabook%2Fpdp%2Fhttp%2Froutes%2FAuthRoutesSuite.scala):

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/http/routes/AuthRoutesSuite.scala
class AuthRoutesSuite(global: GlobalRead) extends HttpSuite:
```

Вспомогательные типы и константы

```dotty
// Модель для невалидных запросов (используем String вместо уточненных типов)
final case class InvalidLoginRequest(
    username: String, // Невалидный username может быть любой строкой
    password: String  // Невалидный пароль может быть любой строкой
)

// Автоматический deriving для кодера (нам нужно только отправлять запросы)
given encoder: Encoder[InvalidLoginRequest] =
  deriveEncoder[InvalidLoginRequest]

// URI для тестируемых endpoint-ов
private val createUri: Uri = uri"/auth/users"
private val loginUri: Uri  = uri"/auth/login"
private val logoutUri: Uri = uri"/auth/logout"
```

**Зачем нужен `InvalidLoginRequest`?**
При тестировании мы хотим проверять не только валидные, но и невалидные запросы. 
`LoginRequest` использует уточненные типы, которые не позволят создать экземпляр с невалидными данными. 
Поэтому мы создаём отдельный case class с обычными строками для негативных тестов.

Настройка разделяемых ресурсов:

```dotty
type Res = AuthService[IO]

override def sharedResource: Resource[IO, Res] =
  for
    authServices <- global.getOrFailR[AuthServices](AuthServicesKey)
  yield authServices.authService
```

Используем глобальные ресурсы (из предыдущего раздела), 
чтобы получить реальный сервис авторизации с подключением к тестовым БД. 
Это обеспечивает интеграционное тестирование с реальными зависимостями.

#### Тест 1: Успешная регистрация

```dotty
test("POST /auth/users should successfully register a new user with valid credentials"):
  service =>
    val loginUser = LoginRequest(
      username = UserName("CheckCreateUser"),
      password = Password("qwerty12345")
    )
    val req    = POST(loginUser, createUri)
    val routes = UserRoutes[IO](service).routes

    expectHttpStatus(routes, req)(HttpStatus.Created)     
```

**Что проверяет:**

- Отправка валидного запроса на `/auth/users`
- Ожидаемый статус ответа — 201 Created
- Базовая проверка, что endpoint регистрации работает

#### Тест 2: Невалидные данные (негативный тест)

```dotty
test(
  "POST /auth/users should return BadRequest if the request contains invalid data"
): service =>
  val loginUser = InvalidLoginRequest(
    username = "TooLongUserNameMoreThan20Symbols",  // Нарушает MinLength[20]
    password = "qwerty"  // Нарушает MinLength[8]
  )
  val req    = POST(loginUser, createUri)
  val routes = UserRoutes[IO](service).routes

  expectHttpStatus(routes, req)(HttpStatus.BadRequest)   
```

**Что проверяет:**

- Отправка запроса с данными, нарушающими ограничения уточненных типов
- Ожидаемый статус — 400 Bad Request (благодаря нашему `decodeR`)
- Корректная работа валидации на уровне маршрутов

#### Тест 3: Полностью невалидная структура запроса

```dotty
test(
  "POST /auth/users should return BadRequest if the request is invalid"
): service =>
  val userInfo =
    UserInfo(
      id = UserId(UUID.randomUUID),
      username = UserName("CheckBadRequest")
    )
  val req    = POST(userInfo, createUri)
  val routes = UserRoutes[IO](service).routes

  expectHttpStatus(routes, req)(HttpStatus.BadRequest)
```

**Что проверяет:**

- Отправка запроса с неправильной структурой JSON (не соответствует `LoginRequest`)
- Ожидаемый статус — 400 Bad Request
- Работа декодера при несоответствии типов

#### Тест 4: Конфликт при дублировании username

```dotty
test(
  "POST /auth/users should return Conflict when registering with an existing username"
): service =>
  val loginUser = LoginRequest(
    username = UserName("CheckDuplicateUser"),
    password = Password("qwerty12345")
  )
  val req    = POST(loginUser, createUri)
  val routes = UserRoutes[IO](service).routes

  // Сначала создаём пользователя
  routes.run(req).value.flatMap:
    case Some(_) =>
      // Затем пробуем создать снова — ожидаем конфликт
      expectHttpStatus(routes, req)(HttpStatus.Conflict)
    case None =>
      failure("Setup failed: could not create initial user").pure[IO]
```

**Что проверяет:**

- Бизнес-логику уникальности `username`
- Преобразование `UserNameInUse` ошибки в 409 Conflict
- Состояние системы между запросами

#### Тест 5: Полный lifecycle (регистрация → логин → логаут)

```dotty
test(
  "POST /auth/login should authenticate user and return valid token for subsequent logout"
): service =>
  val loginUser = LoginRequest(
    username = UserName("CheckLoginUser"),
    password = Password("qwerty12345")
  )
  val createRequest = POST(loginUser, createUri)
  val userRoutes    = UserRoutes[IO](service).routes

  val checkToken: String => IO[Expectations] = token =>
    // 1. Тестируем логин
    val loginRequest = POST(loginUser, loginUri)
    val loginRoutes  = LoginRoutes[IO](service).routes

    val expectedBody =
      Json.fromFields(Map("access_token" -> Json.fromString(token)))

    val loginExpectation = expectHttpBodyAndStatus(loginRoutes, loginRequest)(
      expectedBody,
      HttpStatus.Ok
    )

    // 2. Создаём middleware для тестирования logout
    val authMiddleware: AuthMiddleware[IO, User] =
      AuthMiddleware(Kleisli.pure(User(
        // Не используется в тесте, можно генерировать любой
        id = UserId(UUID.randomUUID),
        // Нужна именно эта строка
        username = loginUser.username,
        // Не используется в тесте
        passwordHash = EncryptedPassword("qwerty12345") 
      )))

    // 3. Тестируем logout
    val logoutRequest     = POST(logoutUri)
    val logoutRoutes      = LogoutRoutes[IO](service).routes(authMiddleware)
    val logoutExpectation =
      expectHttpStatus(logoutRoutes, logoutRequest)(HttpStatus.NoContent)

    // 4. Комбинируем ожидания
    for
      exp1 <- loginExpectation
      exp2 <- logoutExpectation
    yield exp1 |+| exp2  // Используем semigroup для комбинации

  // Запускаем тест с созданием пользователя
  createUser(userRoutes, createRequest)(checkToken) 
```

**Что проверяет:**

1. **Регистрация** — создание пользователя (через `createUser`)
2. **Логин** — получение токена при входе
3. **Логаут** — успешный выход с токеном

**Ключевые моменты:**

- Используем `Kleisli.pure` для создания заглушки middleware
- Комбинируем несколько ожиданий с помощью `|+|` ([Semigroup](../../typeclass/monoid/semigroup.md))
- Проверяем, что токен из регистрации работает для входа

##### Вспомогательная функция createUser

```dotty
private def createUser(
    userRoutes: HttpRoutes[IO],
    createRequest: Request[IO]
)(checkToken: String => IO[Expectations]): IO[Expectations] =
  userRoutes.run(createRequest).value.flatMap:
    case Some(createResponseIO) =>
      createResponseIO.asJson.flatMap: json =>
        extractToken(json) match
          case None =>
            failure(
              s"Response missing access_token field. Response body: $json"
            ).pure[IO]
          case Some(token) =>
            checkToken(token)
    case None =>
      failure("Setup failed: could not create initial user").pure[IO]

private def extractToken(json: Json): Option[String] =
  for
    obj      <- json.asObject
    tokenObj <- obj("access_token")
    token    <- tokenObj.asString
  yield token 
```

**Назначение:**

- Упрощает тесты, требующие предварительного создания пользователя
- Извлекает токен из ответа регистрации
- Передаёт токен в функцию проверки для дальнейших тестов

#### Тест 6: Неверное имя пользователя при входе

```dotty
test("POST /auth/login should return Forbidden when username is incorrect"):
  service =>
    val loginUser = LoginRequest(
      username = UserName("CheckInvalidName"),
      password = Password("qwerty12345")
    )

    val createRequest = POST(loginUser, createUri)
    val userRoutes    = UserRoutes[IO](service).routes

    val checkToken: String => IO[Expectations] = _ =>
      val invalidUsername =
        loginUser.copy(username = UserName("invalidUsername"))
      val loginRoutes = LoginRoutes[IO](service).routes

      expectHttpStatus(
        loginRoutes,
        POST(invalidUsername, loginUri)
      )(HttpStatus.Forbidden)

    createUser(userRoutes, createRequest)(checkToken)   
```

**Что проверяет:**

- Попытка входа с несуществующим `username`
- Ожидаемый статус — 403 Forbidden (маскировка ошибки)
- Что система не раскрывает информацию о существовании пользователей

#### Тест 7: Неверный пароль при входе

```dotty
test("POST /auth/login should return Forbidden when password is incorrect"):
  service =>
    val loginUser = LoginRequest(
      username = UserName("CheckInvalidPass"),
      password = Password("qwerty12345")
    )

    val createRequest = POST(loginUser, createUri)
    val userRoutes    = UserRoutes[IO](service).routes

    val checkToken: String => IO[Expectations] = _ =>
      val invalidPassword =
        loginUser.copy(password = Password("invalidPassword"))

      val loginRoutes = LoginRoutes[IO](service).routes

      expectHttpStatus(
        loginRoutes,
        POST(invalidPassword, loginUri)
      )(HttpStatus.Forbidden)

    createUser(userRoutes, createRequest)(checkToken)
```

**Что проверяет:**

- Попытка входа с существующим `username`, но неверным паролем
- Ожидаемый статус — 403 Forbidden (маскировка ошибки)
- Что система не отличает неверный пароль от несуществующего пользователя

#### Покрытие тестами

| Endpoint          | Сценарий                              | Статус |
|-------------------|---------------------------------------|--------|
| POST /auth/users  | Валидные данные                       | 201    |
| POST /auth/users  | Невалидные данные (нарушение refined) | 400    |
| POST /auth/users  | Неправильная структура JSON           | 400    |
| POST /auth/users  | Дублирование username                 | 409    |
| POST /auth/login  | Валидные данные                       | 200    |
| POST /auth/login  | Несуществующий пользователь           | 403    |
| POST /auth/login  | Неверный пароль                       | 403    |
| POST /auth/logout | Валидный токен                        | 204    |

### Принципы тестирования

1. **Изоляция** — каждый тест независим и работает с чистыми данными
2. **Детерминированность** — одинаковые входные данные дают одинаковый результат
3. **Покрытие happy path** — все основные сценарии успешной работы
4. **Покрытие ошибок** — все возможные ошибочные ситуации
5. **Безопасность** — проверка маскировки ошибок (403 вместо 404)
6. **Композиционность** — сложные тесты строятся из простых

### Преимущества подхода

1. **Функциональный** — тестируем функции, не поднимая сервер
2. **Быстрый** — тесты выполняются за миллисекунды
3. **Надёжный** — нет проблем с портами и сетевыми таймаутами
4. **Понятный** — каждый тест проверяет один конкретный сценарий
5. **Поддерживаемый** — легко добавлять новые тесты и модифицировать существующие

Такое всестороннее тестирование гарантирует, что система авторизации работает корректно и безопасно во всех сценариях использования.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala


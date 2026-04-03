{%
showPrev = true
prevPage = users_routes.html
showNext = true
nextPage = tasks_routes.html
%}

# Endpoint-ы настроек

## Система авторизации в HTTP слое

### Концепция аутентифицированных запросов

В [HTTP4s](https://http4s.org/) для работы с аутентифицированными пользователями используется специальный механизм, 
основанный на типах-обёртках. 
Ключевым понятием является `AuthedRequest[F[_], A]` - структура, которая объединяет HTTP запрос (`Request[F]`) 
и контекст аутентификации произвольного типа `A` (в нашем случае - модель пользователя `User`).

```dotty
// Упрощённое представление
final case class AuthedRequest[F[_], A](
  context: A,      // Данные аутентифицированного пользователя
  req: Request[F]  // Исходный HTTP запрос
)
```

Для работы с такими запросами HTTP4s предоставляет специализированный тип `AuthedRoutes[A, F]` - аналог обычных `HttpRoutes[F]`, 
но с доступом к контексту пользователя в каждом обработчике:

```dotty
val authedRoutes: AuthedRoutes[User, IO] =
  AuthedRoutes.of:
    // В каждом case у нас есть доступ к объекту user
    case GET -> Root as user =>
      Ok(s"Welcome, ${user.username}")
```

### Техническая реализация

Под капотом `AuthedRoutes[T, F]` является псевдонимом типа для `Kleisli[OptionT[F, *], AuthedRequest[F, T], Response[F]]`. 
Это делает его полностью совместимым с композиционной природой HTTP4s и позволяет использовать все стандартные комбинаторы.

Процесс аутентификации состоит из трёх ключевых компонентов:

1. **Функция аутентификации** - преобразует `Request[F]` в `Either[String, User]`, где `Left` означает ошибку аутентификации, 
   а `Right` - успешно аутентифицированного пользователя
2. **Обработчик ошибок** - определяет, какой HTTP ответ вернуть при неудачной аутентификации
3. **Middleware** - компонент, который объединяет первые два и преобразует `AuthedRoutes` в обычные `HttpRoutes`

```dotty
// 1. Функция аутентификации
val authUser: Kleisli[F, Request[F], Either[String, User]] =
  Kleisli { request =>
    // Извлекаем токен из заголовка Authorization
    extractToken(request).map: token =>
      validateToken(token).map(user => user.asRight)
    .getOrElse("No token".asLeft.pure[F])
  }

// 2. Обработчик ошибок
val onFailure: AuthedRoutes[String, F] =
  AuthedRoutes.of: case ctxRequest =>
    // ctxRequest.context содержит сообщение об ошибке
    OptionT.liftF(Forbidden(ctxRequest.context))

// 3. Создание middleware
val middleware = AuthMiddleware(authUser, onFailure)

// 4. Применение middleware к защищённым маршрутам
val authedRoutes: AuthedRoutes[UserInfo, F] = ???
val publicRoutes: HttpRoutes[F] = middleware(authedRoutes)
```

### JWT аутентификация

Для JWT аутентификации мы используем библиотеку [Http4s JWT Auth](https://github.com/profunktor/http4s-jwt-auth), 
которая предоставляет удобную абстракцию поверх стандартного middleware.

#### Настройка JWT

```dotty
import dev.profunktor.auth.jwt.*
import pdi.jwt.*

// 1. Конфигурация JWT
val jwtAuth = JwtAuth.hmac(
  "секретный-ключ-для-подписи-токенов",
  JwtAlgorithm.HS256
)

// 2. Функция валидации пользователя по токену
val usersAuth: JwtToken => JwtClaim => F[Option[User]] =
  token => claim =>
    // Извлекаем user_id из claim и находим пользователя в БД
    for
      userId <- claim.content.asJson.hcursor.get[UUID]("user_id").toOption
      user   <- userService.find(userId)
    yield user

// 3. Создание JWT middleware
val jwtMiddleware: AuthMiddleware[F, UserInfo] =
  JwtAuthMiddleware[F, UserInfo](jwtAuth, usersAuth)
```

#### Структура JWT Claim

JWT токен содержит стандартные поля (iss, exp, iat) и пользовательские данные в поле `content`:

```json
{
  "iss": "scalabook.ru",
  "exp": 1700000000,
  "iat": 1699996400,
  "content": {
    "user_id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe"
  }
}
```

### HTTP статусы авторизации

Защищённые endpoint-ы могут возвращать следующие статусы, связанные с авторизацией:

| Статус           | Код | Описание                                                                |
|------------------|-----|-------------------------------------------------------------------------|
| **Unauthorized** | 401 | Пользователь не аутентифицирован. Требуется предоставить валидный токен |
| **Forbidden**    | 403 | Пользователь аутентифицирован, но не имеет прав для выполнения операции |

## Реализация endpoint-ов для пользовательских типов

### Общая структура

```dotty
final case class UserTypeRoutes[F[_]: {JsonDecoder, MonadThrow, Concurrent, Logger}](
    settingsService: SettingsService[F]
) extends PdpRoutes[F]:

  // Базовый путь для всех endpoint-ов этого сервиса
  private[routes] val prefixPath = "/user_types"

  // Защищённые маршруты с доступом к пользователю
  private val httpRoutes: AuthedRoutes[UserInfo, F] =
    AuthedRoutes.of:
      // GET /user_types - получение списка всех типов
      case GET -> Root as user =>
        Ok(
          settingsService
            .getTaskTypes(user.id)
            .map(_.map(_.toShort))
        )

      // POST /user_types - создание нового типа
      case ar @ POST -> Root as user =>
        ar.req.decodeR[UserTypeName]: name =>
          val newType = CreateUserType(name, user.id)
          settingsService
            .createTaskType(newType)
            .flatMap(Created(_))
            .recover()

      // PUT /user_types/update - обновление существующего типа
      case ar @ PUT -> Root / "update" as user =>
        ar.req.decodeR[ShortUserType]: ShortUserType =>
          val updated =
            UserType(
              id = ShortUserType.id,
              name = ShortUserType.name,
              userId = user.id,
              displayed = ShortUserType.displayed,
              ordinal = ShortUserType.ordinal
            )

          settingsService
            .updateTaskType(updated)
            .flatMap(Ok(_))
            .recover()

      // PUT /user_types/reorder - изменение порядка сортировки
      case ar @ PUT -> Root / "reorder" as user =>
        ar.req.decodeR[NonEmptyVector[UserTypeId]]: ids =>
          settingsService
            .reorderTaskTypes(ids, user.id)
            .flatMap(Ok(_))
            .recover()

      // DELETE /user_types/{id} - удаление типа по ID
      case DELETE -> Root / UserTypeIdVar(id) as user =>
        settingsService.deleteTaskType(id, user.id)
          .flatMap(_ => NoContent())
          .recover()

  // Публичный метод, возвращающий HTTP маршруты с применённой авторизацией
  def routes(authMiddleware: AuthMiddleware[F, UserInfo]): HttpRoutes[F] =
    Router(prefixPath -> authMiddleware(httpRoutes))
end UserTypeRoutes      
```

### Детальный разбор компонентов

#### 1. Контекстные ограничения

```dotty
final case class UserTypeRoutes[F[_]: {JsonDecoder, MonadThrow}]
```

- **`JsonDecoder`** - требование наличия возможности декодировать JSON в типы `F`. Позволяет использовать `.asJsonDecode[T]` на запросе
- **`MonadThrow`** - требование к эффекту `F` поддерживать обработку ошибок. Необходимо для работы DSL HTTP4s и для `recoverWith`

#### 2. Расширение Http4sDsl

```dotty
extends Http4sDsl[F]
```

Http4sDsl предоставляет удобные методы для создания HTTP ответов:

- `Ok()`, `Created()`, `NoContent()` - успешные ответы
- `BadRequest()`, `Forbidden()`, `Unauthorized()` - ответы с ошибками
- Возможность передавать любые типы, для которых есть `EntityEncoder`

#### 3. Структура AuthedRoutes

```dotty
private val httpRoutes: AuthedRoutes[UserInfo, F] =
  AuthedRoutes.of:
    // сопоставление с шаблоном с доступом к user
    case method -> path as user =>
      // бизнес-логика
```

Ключевой синтаксис: `as user` извлекает аутентифицированного пользователя из контекста запроса и связывает его с переменной `user`.

### Детальная спецификация endpoint-ов

#### GET /user_types

Получение списка всех типов, доступных текущему пользователю.

**Запрос:**

```
GET /user_types
Authorization: Bearer <jwt-token>
```

**Успешный ответ (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Блог",
    "displayed": true,
    "ordinal": 1
  },
  {
    "id": 2,
    "name": "OpenSource",
    "displayed": true,
    "ordinal": 2
  }
]
```

**Ошибки:**

- `401 Unauthorized` - отсутствует или невалидный токен

#### POST /user_types

Создание нового пользовательского типа.

**Запрос:**

```
POST /user_types
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "OpenSource"
}
```

**Успешный ответ (201 Created):**

```
Location: /user_types/3
```

Тело ответа содержит ID созданного типа.

**Ошибки:**

- `400 Bad Request` - тип с таким именем уже существует
- `400 Bad Request` - некорректное имя (например, слишком короткое)
- `401 Unauthorized` - отсутствует или невалидный токен

#### PUT /user_types/update

Обновление существующего пользовательского типа.

**Запрос:**

```
PUT /user_types/update
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "id": 1,
  "name": "Личный блог",
  "displayed": true,
  "ordinal": 1
}
```

**Успешный ответ (200 OK):**
Тело ответа содержит обновлённый объект типа.

**Ошибки:**

- `400 Bad Request` - тип с таким именем уже существует
- `400 Bad Request` - попытка обновить чужой тип
- `401 Unauthorized` - отсутствует или невалидный токен

#### PUT /user_types/reorder

Изменение порядка сортировки типов. Принимает список ID в новом порядке.

**Запрос:**

```
PUT /user_types/reorder
Authorization: Bearer <jwt-token>
Content-Type: application/json

[3, 1, 2]
```

**Успешный ответ (200 OK):**
Тело ответа содержит обновлённый список типов в новом порядке.

**Ошибки:**

- `400 Bad Request` - передан пустой список
- `400 Bad Request` - некоторые ID не принадлежат пользователю
- `401 Unauthorized` - отсутствует или невалидный токен

#### DELETE /user_types/{id}

Удаление пользовательского типа по ID.

**Запрос:**

```
DELETE /user_types/1
Authorization: Bearer <jwt-token>
```

**Успешный ответ (204 No Content):**
Тело ответа отсутствует.

**Ошибки:**

- `400 Bad Request` - попытка удалить несуществующий или чужой тип
- `401 Unauthorized` - отсутствует или невалидный токен

### Вспомогательные компоненты

#### Модель типа без идентификатора пользователя

```dotty
final case class ShortUserType(
    id: UserTypeId,
    name: UserTypeName,
    displayed: Boolean,
    ordinal: Ordinal
)
```

#### Экстрактор пути для UserTypeId

```dotty
package ru.scalabook.pdp.http.routes

import ru.scalabook.pdp.model.UserType.UserTypeId

object UserTypeIdVar:
  /** Позволяет использовать паттерн-матчинг вида: DELETE -> Root / UserTypeIdVar(id)
    * Автоматически конвертирует строку из URL в UserTypeId
    */
  def unapply(str: String): Option[UserTypeId] =
    for
      int <- str.toIntOption           // Парсим строку в Int
      id  <- UserTypeId.option(int)    // Валидируем Int как UserTypeId
    yield id   
```

HTTP4s предоставляет встроенные экстракторы для примитивных типов: `IntVar`, `LongVar`, `UUIDVar`. 
Рекомендуется создавать аналогичные экстракторы для доменных типов.

### Обработка ошибок

Все endpoint-ы используют единый подход к обработке ошибок:

1. **Ошибки валидации** от сервиса (`IllegalArgumentException`) преобразуются в `400 Bad Request`
2. **Ошибки аутентификации** обрабатываются middleware и преобразуются в `401 Unauthorized`
3. **Ошибки авторизации** (попытка доступа к чужому ресурсу) преобразуются в `403 Forbidden`

```dotty
settingsService
  .createTaskType(newType)
  .flatMap(Created(_))
  .recover()  // Преобразуем ошибку сервиса в HTTP ответ
```

### Маршрутизация

Метод `routes` собирает всё вместе:

```dotty
def routes(authMiddleware: AuthMiddleware[F, UserInfo]): HttpRoutes[F] =
  Router(
    prefixPath -> authMiddleware(httpRoutes)  // Применяем middleware к защищённым маршрутам
  )
```

`Router` позволяет группировать маршруты по префиксам, что особенно полезно при объединении нескольких наборов endpoint-ов:

```dotty
val allRoutes = Router(
  "/api/users" -> userRoutes,
  "/api/settings" -> settingsRoutes,
  "/api/user_types" -> userTypeRoutes.routes(jwtMiddleware)
)
```

## Принципы проектирования

1. **Разделение ответственности** - бизнес-логика в `SettingsService`, HTTP слой только для маршрутизации и преобразования
2. **Типобезопасность** - использование доменных типов (`UserTypeId`, `UserTypeName`) вместо примитивов
3. **Композиционность** - middleware для сквозных задач (аутентификация)
4. **Явная обработка ошибок** - все ошибки бизнес-логики явно преобразуются в HTTP статусы
5. **Разделы и цели** - реализация роутов для разделов и целей выполняется аналогично.

Эти endpoint-ы формируют основу для управления пользовательскими настройками и типами, 
обеспечивая безопасный и типобезопасный доступ к функциональности приложения.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala

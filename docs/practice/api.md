{%
showPrev = true
prevPage = lifecycle/authorization.html
showNext = true
nextPage = sources.html
%}

# Клиентское АПИ

Для создания клиентского сервиса будет использовать библиотеку [Http4s][http4s].

Добавляем в зависимости:

```dotty
object circe {
  private val version = "0.14.15"

  val core    = "io.circe" %% "circe-core"    % version
  val generic = "io.circe" %% "circe-generic" % version
  val parser  = "io.circe" %% "circe-parser"  % version
}

object http4s {
  private val version = "0.23.33"

  val core  = "org.http4s"     %% "http4s-core"     % version
  val circe = "org.http4s"     %% "http4s-circe"    % version
  val jwt   = "dev.profunktor" %% "http4s-jwt-auth" % "2.0.13"
}

object iron {
  private val version = "3.2.3"
  val circe           = "io.github.iltotore" %% "iron-circe"      % version
  ...
}
```

## Проверка работоспособности

Сервис HealthCheck сообщает о состоянии как Redis, так и PostgreSQL, которые мы
собираемся предоставить через HTTP для удобного доступа.

```dotty
package ru.scalabook.pdp.http.routes

import cats.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.services.*

final case class HealthRoutes[F[_]: Monad](
    healthCheck: HealthCheck[F]
) extends Http4sDsl[F]:
  private[routes] val prefixPath = "/health_check"

  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(healthCheck.status)
    }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
```

Успешный GET-запрос приводит к получению тела ответа, показанного ниже.

```json
{
  "redis":
  {
    "status": "Okay"
  },
  "postgres":
  {
    "status": "Okay"
  }
}
```

Или "Unreachable", если что-то не так с любым из соединений.

Эта HTTP-конечная точка всегда возвращает успешный ответ (200 OK). Однако мы можем
утверждать, что дизайн можно изменить, чтобы возвращать другие коды состояния ответа, если что-то не работает. 
В этой области существует множество допустимых вариантов дизайна, но мы будем придерживаться этого простого.





## Публичное API без авторизации

```http
POST /users

Request body:

{
  "username": "akorsakov",
  "password": "qwerty"
}

Response code:
– 201: Пользователь успешно создан
– 400: Невалидные данные, например, пустой логин
– 409: Логин уже занят

Response body on success:

{
  "access_token": "eyJ0eXA..."
}
```

```http
POST /auth/login

Request body:

{
  "username": "akorsakov",
  "password": "qwerty"
}

Response code:
– 200: пользователь успешно залогирован
– 403: невалидные параметры авторизации

Response body on success:

{
  "access_token": "eyJ0eXA..."
}
```

```http
POST /auth/logout

Response code:
– 204: пользователь успешно вышел из системы
– 403: невалидные параметры авторизации

Request body и Response body не требуются
```

## Публичное API с авторизацией

Это HTTP-маршруты, для работы которых требуется авторизация зарегистрированных пользователей. 
Все они могут возвращать следующие статусы ответа в дополнение к указанным выше:

- 401: неавторизованный пользователь, необходимо авторизоваться.
- 403: у пользователя нет разрешения на выполнение этого действия.

## Работа с пользовательскими типами

Получение всех типов, доступных для пользователя:

```http
GET /user_types

Response code:
– 200: Возвращает список пользовательских типов

Response body on success:

[
  {
    "id": 1,
    "name": "Блог"
  },
  {
    "id": 2,
    "name": "Opensourse"
  }
]
```

Создание сервиса:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/UserTypeRoutes.scala
package ru.scalabook.pdp.http.routes

import cats.MonadThrow
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.scalabook.pdp.services.SettingsService
import ru.scalabook.pdp.model.UserType
import ru.scalabook.pdp.model.UserType.given

final case class UserTypeRoutes[F[_]: MonadThrow](
    settingsService: SettingsService[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/user_types"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(settingsService.getTaskTypes)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}

```

Здесь происходит несколько вещей:

- У нас есть новое ограничение `MonadThrow[F]`, необходимое для `HttpRoutes.of[F]`, а также необходимое для создания ответа.
- В качестве аргумента для нашего класса используется алгебра `SettingsService[F]`.
- Мы расширяем `Http4sDsl[F]`, чтобы получить доступ к методам DSL, специфичным для нашего `F`.
- Существует приватный `prefixPath`, указывающий на корень нашей конечной точки.
- У нас есть приватный `httpRoutes`, определяющий все наши конечные точки, в данном случае только одну.
- Наконец, у нас есть публичный `routes`, который использует `Router`, 
  позволяющий добавлять `prefixPath` к группе конечных точек, обозначенных как `HttpRoutes`.

Нам необходимо circe-кодек:

```dotty
import io.circe.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.all.*

... // другие import-ы

object UserType:
  ...

  given encoder: Encoder[UserType] = deriveEncoder[UserType]
```

Добавление типа:

```http
POST /user_types

Response code:
– 201: Новый тип успешно добавлен.
– 409: Тип с таким наименованием уже существует.

Request body:

{
  "name": "OpenSource"
}

No Response body.
```

Обновление типа:

```http
PUT /user_types

Response code:
– 200: пользовательский тип обновлен
– 400: ошибки обновления, например, имя уже занято

Request body:

{
  "id": 1,
  "name": "OpenSource"
}

No Response body.
```

Удаление типа:

```http
DELETE /user_types/{id}

– 204: Тип с идентификатором id был удален, если таковой был.

No Request body and no Response body.
```

## Работа с разделами

## Работа с целями

## Работа с задачами


Работа с задачами определенного типа и раздела:

```dotty
package ru.scalabook.pdp.http.routes

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.services.*

final case class TaskRoutes[F[_]: MonadThrow](
    taskLifecycleService: TaskLifecycleService[F],
    taskService: TaskService[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/tasks"

  object BrandQueryParam
      extends OptionalQueryParamDecoderMatcher[TaskType]("task_type")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? BrandQueryParam(maybeTaskType) =>
      maybeTaskType match
        case None =>
          Ok(taskService.getAll)
        case Some(taskType) =>
          taskService.getAll
            .map(tasks => tasks.filter(_.taskType == taskType))
            .flatMap(Ok(_))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
```

Для работы необходимы кодеки:

```dotty
enum TaskType:
  case Book, Article, Podcast, Video, Course, Other

object TaskType:
  ...

  given QueryParamDecoder[TaskType] =
    QueryParamDecoder[String].emap: str =>
      TaskType.values
        .find(t => t.toString.equalsIgnoreCase(str))
        .toRight(ParseFailure(s"Неизвестный тип '$str'", ""))
```

```dotty  
object Status:
  ...

  given encoder: Encoder[Status] = deriveEncoder[Status] 
  
package object model {
  ...
  
  /** Конвертация в JSON для длительности. */
  given encoderDuration: Encoder[FiniteDuration] =
    Encoder.encodeLong.contramap(_.toMillis)
}
  
object Task:
  given encoder: Encoder[Task] = deriveEncoder[Task]   
```

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[http4s]: https://http4s.org/

{%
showPrev = true
prevPage = ../api.html
showNext = true
nextPage = ../sources.html
%}

# Роуты для задач

## Авторизация

Для доступа к аутентифицированному пользователю нам необходимо использовать `AuthedUser[F, User]`,
который является классом-оболочкой, изоморфным `(User, Request[F])`, где `User` — это некоторый произвольный тип данных,
который мы объявляем для представления пользователя в нашей системе.
Однако на самом деле это псевдоним типа для `ContextRequest`, определенный как полиморфный класс-оболочка.

```dotty
final case class ContextRequest[F[_], A](
  context: A, req: Request[F]
)
```

Аналогичным образом, нам следует использовать `AuthedRoutes[User, F]` вместо `HttpRoutes[F]`, если мы
хотим получить доступ к аутентифицированному пользователю в каждом запросе.
Например:

```dotty
val authedRoutes: AuthedRoutes[User, IO] =
  AuthedRoutes.of {
    case GET -> Root as user =>
      Ok(s"Welcome, ${user.name}")
  }
```

`AuthedRoutes[T, F]` — это псевдоним типа для `Kleisli[OptionT[F, *]`, `AuthedRequest[F, T]`, `Response[F]`,
аналогично `HttpRoutes`, за исключением того, что тип запроса теперь содержит информацию
об аутентифицированном пользователе.
Http4s позволяет нам определить, как аутентифицировать пользователя.
Все, что нам нужно, это функция, которая решает, может ли пользователь быть аутентифицирован
или нет, учитывая `Request[F]`, и функция, которая определяет, что делать в случае сбоя.
Как только у нас появятся обе функции, мы можем применить их к `AuthMiddleware`,
который можно использовать как еще одно обычное промежуточное ПО.
В качестве демонстрации см. пример ниже.

```dotty
val authUser: Kleisli[F, Request[F], Either[String, User]] =
  Kleisli.pure(myUser.asRight) // Authenticate user
val onFailure: AuthedRoutes[String, F] =
  Kleisli(req => OptionT.liftF(Forbidden(req.context)))
val middleware = AuthMiddleware(authUser, onFailure)
val authedRoutes: AuthedRoutes[User, F] = ???
val routes: HttpRoutes[F] = middleware(authedRoutes)
```

Наиболее распространенными методами аутентификации являются cookie и bearer token.
Примеры обоих вариантов можно найти в официальной документации, однако мы сосредоточимся на втором.

### JWT Auth

В качестве библиотеки мы выберем [Http4s JWT Auth4](https://github.com/profunktor/http4s-jwt-auth),
которая предлагает некоторые функциональные возможности поверх Http4s и [JWT Scala](https://github.com/jwt-scala/jwt-scala).
Http4s предоставляет `AuthMiddleware`, упомянутый ранее.
Это псевдоним типа для сложного типа.

```dotty
type AuthMiddleware[F[_], T] = Middleware[OptionT[F, *], AuthedRequest[F, T], Response[F], Request[F], Response[F]]
```

Однако не пугайтесь, вы всё поймёте, когда мы рассмотрим промежуточные обработчики вскоре, с некоторыми примерами.
Пока что их можно рассматривать как функции `AuthedRoutes[T, F] => HttpRoutes[F]`.

Вместо обычного `AuthMiddleware` мы будем использовать пользовательский `JwtAuthMiddleware`,
определенный в Http4s JWT Auth.

```dotty
val usersAuth: JwtToken => JwtClaim => F[Option[User]] =
  t => c => User("Joe").some.pure[F]
val usersMiddleware: AuthMiddleware[F, User] =
  JwtAuthMiddleware[F, User](jwtAuth, usersAuth)
```

Для этого требуется `JwtAuth` и функция `JwtToken => JwtClaim => F[Option[A]]`, как показано выше.
Первую можно создать следующим образом.

```dotty
val jwtAuth = JwtAuth.hmac("53cr3t", JwtAlgorithm.HS256)
```

После того, как мы определили все необходимое, мы можем использовать наш `AuthMiddleware`
как любой другой промежуточный обработчик.
Например:

```dotty
val routes: HttpRoutes[F] = usersMiddleware(authedRoutes)
```

Следуя тому же принципу, мы могли бы реализовать аутентификацию для других типов пользователей,
например, для администраторов.

```dotty
val adminAuth: JwtToken => JwtClaim => F[Option[AdminUser]] =
  t => c => AdminUser("admin").some.pure[F]
val adminMiddleware: AuthMiddleware[F, AdminUser] =
  JwtAuthMiddleware[F, AdminUser](jwtAuth, adminAuth)
```

Вот как мы могли бы это использовать.

```dotty
val adminRoutes: AuthedRoutes[AdminUser, F] =
  AuthedRoutes.of {
    case POST -> Root as adminUser =>
      Ok(s"You have admin rights, ${adminUser.show}!")
  }
  
val routes: HttpRoutes[F] = adminMiddleware(adminRoutes)
```

Стоит отметить, что мы можем комбинировать HTTP-маршруты пользователей и администраторов,
достигая функциональности, позволяющей иметь разные роли.

```dotty
val allRoutes: HttpRoutes[F] =
  usersMiddleware(authedRoutes) <+> adminMiddleware(adminRoutes)
```

Http4s обладает высокой композиционной способностью.


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
import cats.data.NonEmptyList
import cats.syntax.all.*
import io.circe.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.all.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.*
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.model.UserType.{UserTypeId, UserTypeName, given}
import ru.scalabook.pdp.services.SettingsService

final case class UserTypeRoutes[F[_]: {JsonDecoder, MonadThrow}](
    settingsService: SettingsService[F]
) extends Http4sDsl[F]:

  private[routes] val prefixPath = "/user_types"

  private val httpRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of {
      // Список всех пользовательских типов
      case GET -> Root as user =>
        Ok(settingsService.getTaskTypes)

      // Добавление нового типа
      case ar @ POST -> Root as user =>
        ar.req.asJsonDecode[UserTypeName].flatMap: name =>
          settingsService
            .createTaskType(name)
            .flatMap(_ => Created())
            .recoverWith {
              case ex: IllegalArgumentException => BadRequest(ex.getMessage)
            }

      // Обновить пользовательский тип
      case ar @ PUT -> Root / "update" as user =>
        ar.req.asJsonDecode[UserType].flatMap: userType =>
          settingsService
            .updateTaskType(userType)
            .flatMap(_ => Ok())
            .recoverWith {
              case ex: IllegalArgumentException => BadRequest(ex.getMessage)
            }

      // Пересортировать пользовательские типы
      case ar @ PUT -> Root / "reorder" as user =>
        ar.req.asJsonDecode[NonEmptyList[UserTypeId]].flatMap: ids =>
          settingsService
            .reorderTaskTypes(ids)
            .flatMap(_ => Ok())
            .recoverWith {
              case ex: IllegalArgumentException => BadRequest(ex.getMessage)
            }

      // Удалить пользовательский тип
      case DELETE -> Root / UserTypeIdVar(id) as user =>
        settingsService.deleteTaskType(id)
          .flatMap(_ => NoContent())
          .recoverWith {
            case ex: IllegalArgumentException => BadRequest(ex.getMessage)
          }
    }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] =
    Router(prefixPath -> authMiddleware(httpRoutes))
end UserTypeRoutes
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

Давайте разберем это по частям, поскольку здесь много всего происходит.

- У нас есть новое ограничение `JsonDecoder`, которое представляет собой просто интерфейс,
  позволяющий нам декодировать наш запрос как требуемую сущность `A`, имея `Decoder[A]`.
- Мы используем `AuthedRoutes[CommonUser, F]` вместо `HttpRoutes[F]`.
- Наши маршруты принимают в качестве аргумента `AuthMiddleware[F, CommonUser]`.
- Мы декодируем данные в наших POST и PUT конечных точках, что делается с помощью `JsonDecoder` (для этого требуется `Decoder[Cart]`).


В последнем методе DELETE мы также захватываем переменную пути с помощью `UserTypeIdVar`.
Его определение:

```dotty
package ru.scalabook.pdp.http

import ru.scalabook.pdp.model.UserType.UserTypeId

package object routes {
  object UserTypeIdVar:
    def unapply(str: String): Option[UserTypeId] =
      for
        int <- str.toIntOption
        id  <- UserTypeId.option(int)
      yield id
}
```

Пользователям рекомендуется определять пользовательские объекты таким образом и использовать предоставленные объекты, когда это
целесообразно. Http4s поддерживает `IntVar`, `LongVar` и `UUIDVar`, среди прочих.


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
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.services.*

final case class TaskRoutes[F[_]: {JsonDecoder, MonadThrow}](
    taskLifecycleService: TaskLifecycleService[F],
    taskService: TaskService[F]
) extends Http4sDsl[F]:

  private[routes] val prefixPath = "/tasks"

  object BrandQueryParam
      extends OptionalQueryParamDecoderMatcher[TaskType]("task_type")

  private val httpRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of {
      case GET -> Root :? BrandQueryParam(maybeTaskType) as user =>
        maybeTaskType match
          case None =>
            Ok(taskService.getAll)
          case Some(taskType) =>
            taskService.getAll
              .map(_.filter(_.taskType == taskType))
              .flatMap(Ok(_))
    }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] =
    Router(prefixPath -> authMiddleware(httpRoutes))
end TaskRoutes
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
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala

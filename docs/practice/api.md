{%
showPrev = true
prevPage = authorization.html
showNext = true
nextPage = sources.html
%}

# Клиентское АПИ






## Проверка работоспособности

Нашему приложению необходимо сообщать о своем состоянии работоспособности,
что обычно включает в себя, помимо прочего, проверку подключения к базе данных.
Мы можем смоделировать это следующим образом.

```dotty  
// pdp/src/main/scala/ru/scalabook/pdp/model/AppStatus.scala
package ru.scalabook.pdp.model

import ru.scalabook.pdp.model.AppStatus.*

final case class AppStatus(redis: RedisStatus, postgres: PostgresStatus)

object AppStatus:
  opaque type RedisStatus    = Status
  opaque type PostgresStatus = Status

  enum Status(isOk: Boolean):
    case Okay()        extends Status(true)
    case Unreachable() extends Status(false)  
```  

API:

```dotty  
// pdp/src/main/scala/ru/scalabook/pdp/services/HealthCheck.scala
package ru.scalabook.pdp.services

import ru.scalabook.pdp.model.AppStatus

trait HealthCheck[F[_]]:
  def status: F[AppStatus] 
```



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

## API для логирования

Ещё одна новая функция, которую стоит отметить, — это `decodeR`, 
пользовательская функция декодирования, которая обрабатывает ошибки проверки 
и возвращает код ответа 400 (Неверный запрос) вместе с сообщением об ошибке, 
вместо кода ответа по умолчанию 422 (Необрабатываемая сущность), 
когда входные данные недопустимы, например, пустое имя. 

```dotty
package ru.scalabook.pdp.http.routes

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl

trait PdpRoutes[F[_]: {JsonDecoder, MonadThrow}] extends Http4sDsl[F]:
  extension (req: Request[F])
    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      req.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.startsWith("Predicate") =>
              BadRequest(c.getMessage)
            case _ => UnprocessableContent()
          }
        case Right(a) => f(a)
      }

```

Сам сервис:

```dotty
package ru.scalabook.pdp.http.routes

import cats.MonadThrow
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.server.Router
import ru.scalabook.pdp.auth.*
import ru.scalabook.pdp.model.LoginUser
import ru.scalabook.pdp.services.AuthService

final case class LoginRoutes[F[_]: {JsonDecoder, MonadThrow}](
    authService: AuthService[F]
) extends PdpRoutes[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        authService
          .login(user.username, user.password)
          .flatMap(Ok(_))
          .recoverWith {
            case UserNotFound(_) | InvalidPassword(_) => Forbidden()
          }
      }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
```

В любом другом случае мы бы возвращали NotFound (404) при ошибке UserNotFound 
или BadRequest (400) с конкретным сообщением об ошибке при ошибке InvalidPassword.
Однако здесь мы возвращаем Forbidden (403) в обоих случаях, чтобы избежать утечки информации.
Поскольку это открытая конечная точка, любой потенциально может осуществить атаку методом перебора, 
и получение ошибки 404 покажет, что имя пользователя существует, и нам просто нужно взломать пароль.
Также следует отметить использование метода расширения toDomain, 
который преобразует уточненные значения в значения общего домена.

### Logout

Здесь у нас что-то новое: `AuthHeaders.getBearerToken`.

```dotty
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

  private val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {

    case ar @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(ar.req)
        .traverse_(authService.logout(_, user.username)) *> NoContent()

  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
```

Мы обращаемся к заголовкам запроса, чтобы найти текущий токен доступа и аннулировать его, 
то есть удалить из нашего кеша, как мы увидим в интерпретаторе аутентификации.

### Users

За регистрацию новых пользователей будут отвечать следующие HTTP-маршруты.

```dotty
package ru.scalabook.pdp.http.routes

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.server.Router
import ru.scalabook.pdp.auth.*
import ru.scalabook.pdp.model.LoginUser
import ru.scalabook.pdp.services.AuthService

final case class UserRoutes[F[_]: {JsonDecoder, MonadThrow}](
    authService: AuthService[F]
) extends PdpRoutes[F]:

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "users" =>
      req.decodeR[LoginUser] { user =>
        authService
          .newUser(user.username, user.password)
          .flatMap(Created(_))
          .recoverWith {
            case UserNameInUse(username) => Conflict(username)
          }
      }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
end UserRoutes
```

Обратите внимание, что мы можем регистрировать только новых обычных пользователей; создание новых администраторов невозможно. 
Ещё раз, для проверки данных мы используем `decodeR`, для преобразования данных — `toDomain`, 
а для обработки ошибок бизнес-логики — `recoverWith`.

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

## HTTP-сервер

Сейчас мы быстро рассмотрим, как создать наш сервер. 
До этого момента мы видели только функции, но для запуска HTTP-сервера нам нужно нечто большее. 
Позвольте представить вам нашу реализацию сервера по умолчанию — Ember.

```dotty
val httpApp: HttpApp[F] = ???

EmberServerBuilder
.default[IO]
.withHttpApp(httpApp)
.build
```

Метод `build` возвращает `Resource[F, Server[F]]`. 
Поскольку `Resource` образует монаду, мы можем последовательно запускать несколько ресурсов, 
таких как удаленная база данных или брокер сообщений, и запускать их все одновременно.
Имейте в виду, что Ember — относительно новый интерпретатор. 
Если вы ищете проверенный сервер, я бы рекомендовал Blaze. То же самое относится и к клиентской части.

> Ember — новейший HTTP-сервер и клиент для Http4s

## Redis for Cats

[Redis4Cats][redis4cats] — это чисто функциональный и асинхронный клиент Redis, построенный на основе Cats Effect, Fs2 и Java-библиотеки Lettuce.

Добавляем зависимости:

```sbt
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

#### Аутентификация

Ранее уже упоминалось, что алгебра аутентификации, возможно, потребует изменений, и это неизбежно произойдет. 
Мы определим две разные алгебры; первая, аутентификация, является наиболее общей.

```dotty
trait AuthService[F[_]]:
  def newUser(username: UserName, password: Password): F[JwtToken]

  def login(username: UserName, password: Password): F[JwtToken]

  def logout(token: JwtToken, username: UserName): F[Unit]
```

Наша вторая алгебра специализируется на получении определенного типа пользователя, что обозначается ее вторым параметром типа A.

```dotty
trait UsersAuthService[F[_], A]:
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
```

Функция `findUser` каррирована для упрощения интеграции с JWT-аутентификацией Http4s. 
Помните, что функция `authenticate` из `JWTAuthMiddleware` имеет вид `JwtToken => JwtClaim => F[Option[A]]`.

Далее рассмотрим интерпретатор для `CommonUser`.

```dotty
object UsersAuthService:
  def make[F[_]: Functor](
      redis: RedisCommands[F, String, String]
  ): UsersAuthService[F, UserWithoutPassword] =
    new UsersAuthService[F, UserWithoutPassword]:
      def findUser(token: JwtToken)(claim: JwtClaim)
          : F[Option[UserWithoutPassword]] =
        redis
          .get(token.value)
          .map(_.flatMap(u => decode[UserWithoutPassword](u).toOption))
```

Теперь давайте посмотрим, как выглядит структура интерпретатора аутентификации, а затем проанализируем каждую функцию шаг за шагом.

```dotty
object AuthService:
  def make[F[_]: {MonadThrow as MT}](
      tokenExpiration: FiniteDuration,
      tokens: Tokens[F],
      usersService: UsersService[F],
      redis: RedisCommands[F, String, String],
      crypto: Crypto
  ): AuthService[F] =
```

Его конструктор принимает пять различных аргументов:

- `tokenExpiration: FiniteDuration` — время жизни токена
- `Tokens[F]` позволяет создавать новые токены JWT
  ```dotty
  trait Tokens[F[_]]:
      def create: F[JwtToken]
  ```
- `UsersService[F]` позволяет находить и создавать новых пользователей
- `RedisCommands` — это интерфейс Redis.
- `Crypto` позволяет нам шифровать и расшифровывать пароли.
  ```dotty
  trait Crypto:
      def encrypt(value: Password): Either[String, EncryptedPassword]
      def decrypt(value: EncryptedPassword): Either[String, Password]
  ```

Реализация:

```dotty
object Tokens:
  def make[F[_]: {GenUUID, Monad}](
      jwtExpire: JwtExpire[F],
      config: TokenConfig
  ): Tokens[F] =
    val secretKey =
      JwtSecretKey(config.jwtAccessTokenKey.value.value.toCharArray)

    new Tokens[F]:
      def create: F[JwtToken] =
        for
          uuid  <- GenUUID[F].make
          claim <- jwtExpire.expiresIn(
            JwtClaim(uuid.asJson.noSpaces),
            config.expiration
          )
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        yield token
```

И декодирование

```dotty
object Crypto:
  private val charsetName    = "UTF-8"
  private val transformation = "AES/CBC/PKCS5Padding"

  def make[F[_]: Sync](passwordSalt: PasswordSalt): F[Crypto] =
    Sync[F]
      .delay {
        val random  = new SecureRandom()
        val ivBytes = new Array[Byte](16)
        random.nextBytes(ivBytes)
        val iv       = new IvParameterSpec(ivBytes);
        val salt     = passwordSalt.value.getBytes(charsetName)
        val keySpec  = new PBEKeySpec("password".toCharArray, salt, 65536, 256)
        val factory  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val bytes    = factory.generateSecret(keySpec).getEncoded
        val sKeySpec = new SecretKeySpec(bytes, "AES")
        val eCipher  = Cipher.getInstance(transformation)
        eCipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv)
        val dCipher = Cipher.getInstance(transformation)
        dCipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv)
        (eCipher, dCipher)
      }
      .map {
        case (ec, dc) =>
          new Crypto:
            def encrypt(password: Password): Either[String, EncryptedPassword] =
              val base64 = Base64.getEncoder
              val bytes  = password.value.getBytes(charsetName)
              val result =
                new String(base64.encode(ec.doFinal(bytes)), charsetName)
              EncryptedPassword.either(result)

            def decrypt(password: EncryptedPassword): Either[String, Password] =
              val base64 = Base64.getDecoder
              val bytes  = base64.decode(password.value.getBytes(charsetName))
              val result = new String(dc.doFinal(bytes), charsetName)
              Password.either(result)
      }
end Crypto
```

Наша первая функция — `newUser`.

```dotty
def newUser(username: UserName, password: Password): F[JwtToken] =
  usersService.find(username).flatMap:
    case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
    case None    =>
      val encryptedPasswordF =
        MT.fromEither(
          crypto.encrypt(password)
            .leftMap(new IllegalArgumentException(_))
        )

      for
        encryptedPassword <- encryptedPasswordF
        newUser = NewUser(username, encryptedPassword)
        uuid  <- usersService.create(newUser)
        token <- tokens.create
        user = UserWithoutPassword(uuid, username).asJson.noSpaces
        _ <- redis.setEx(token.value, user, tokenExpiration)
        _ <- redis.setEx(username.show, token.value, tokenExpiration)
      yield token
```

Здесь мы пытаемся найти пользователя в PostgreSQL. 
Если он не существует, мы продолжаем его создание; в противном случае мы вызываем ошибку `UserNameInUse`.

Создание пользователя означает его сохранение в PostgreSQL, создание JWT-токена, 
сериализацию пользователя в формате JSON и сохранение как токена, 
так и сериализованного пользователя в Redis для быстрого доступа с указанием времени истечения срока действия.

Далее следует наша функция авторизации.

```dotty
def login(username: UserName, password: Password): F[JwtToken] =
  usersService.find(username).flatMap:
    case None => UserNotFound(username).raiseError[F, JwtToken]
    case Some(user) if crypto.encrypt(password).contains(user.password) =>
      InvalidPassword(user.username).raiseError[F, JwtToken]
    case Some(user) =>
      redis.get(username.show).flatMap:
        case Some(t) => JwtToken(t).pure[F]
        case None    =>
          tokens.create.flatTap: token =>
            redis.setEx(
              token.value,
              user.asJson.noSpaces,
              tokenExpiration
            ) *>
              redis.setEx(username.show, token.value, tokenExpiration)
```

Мы пытаемся найти пользователя в PostgreSQL. Если он не существует, мы просто генерируем ошибку `UserNotFound`; 
если существует, мы проверяем, совпадает ли сохраненный зашифрованный пароль с тем, 
который был передан функции, и генерируем ошибку `InvalidPassword`, если они не совпадают. 
Если совпадают, мы ищем токен по пользователю в Redis (на случай, если пользователь уже авторизован). 
Если мы получаем токен, мы возвращаем его; 
в противном случае мы создаем новый токен и сохраняем как пользователя, 
так и токен с указанием времени истечения срока действия.

Когда мы получаем существующий токен, мы также можем продлить его срок действия, то есть обновить его срок действия.
Однако это будет иметь смысл только в том случае, если время истечения срока действия наших JWT больше, 
чем то, которое мы настраиваем для наших токенов, хранящихся в Redis.

Наконец, у нас есть функция выхода из системы, которая является самой простой.

```dotty
def logout(token: JwtToken, username: UserName): F[Unit] =
  redis.del(token.value) *> redis.del(username.show).void
```

Всё, что это делает, — это удаляет токен и пользователя из Redis, если таковые имеются.

## Health check

Это покажет статус подключения как Redis, так и Postgres. 
Если статус OK, значит, соединение установлено; в противном случае — `Unreachable`. 
Давайте посмотрим на интерпретатор.

```dotty
object HealthCheck:
  def make[F[_]: Temporal](
      postgres: PostgreSQLHealthCheck[F],
      redis: RedisCommands[F, String, String]
  ): HealthCheck[F] =
    new HealthCheck[F]:
      val redisHealth: F[RedisStatus] =
        redis.ping
          .map(_.nonEmpty)
          .timeout(1.second)
          .map(RedisStatus.apply)
          .orElse(RedisStatus(false).pure[F].widen)

      val postgresHealth: F[PostgresStatus] =
        postgres
          .status
          .timeout(1.second)
          .map(PostgresStatus.apply)
          .orElse(PostgresStatus(false).pure[F].widen)

      val status: F[AppStatus] =
        (redisHealth, postgresHealth).parMapN(AppStatus.apply)

end HealthCheck
```

Реализация `PostgreSQLHealthCheck`:

```dotty
package ru.scalabook.pdp.services.postgresql

import _root_.doobie.*
import _root_.doobie.implicits.*
import _root_.doobie.util.transactor.Transactor.*
import cats.effect.IO

trait PostgreSQLHealthCheck[F[_]]:
  def status: F[Boolean]

object PostgreSQLHealthCheck:
  def make(xa: Aux[[x] =>> IO[x], Unit]): PostgreSQLHealthCheck[IO] =
    new PostgreSQLHealthCheck[IO]:
      def status: IO[Boolean] =
        sql"""SELECT pid FROM pg_stat_activity;"""
          .query[Int]
          .to[List]
          .transact(xa)
          .map(_.nonEmpty)
```

Для Redis мы просто отправляем ping-запрос на сервер; для Postgres — выполняем простой запрос. 
Оба действия имеют таймаут в одну секунду и выполняются параллельно с использованием функции `parMapN`.

В обоих случаях, если что-то пойдет не так (например, не удается подключиться к серверу), 
мы возвращаем `Unreachable`, используя комбинатор `orElse` из `ApplicativeError`.

## HTTP

В главе 5 мы изучили маршруты и клиенты Http4s, а также другие возможности. 
Теперь нам нужно посмотреть, как тестировать эти компоненты, и я должен признаться, 
что мне нравится писать именно такие тесты из-за универсальности этого фреймворка; он действительно хорошо продуман.

### Маршруты

Я утверждал, что сервер — это функция, и я буквально это имел в виду! 
Нам не нужно запускать сервер для тестирования наших HttpRoutes, поскольку это обычные функции. 
Более того, мы можем использовать возможности тестирования на основе свойств для написания точных тестов.
Вот тест для `BrandRoutes`, который предоставляет единственную GET-конечную точку для получения всех брендов.



---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala
[http4s]: https://http4s.org/
[redis4cats]: https://github.com/profunktor/redis4cats

{%
showPrev = true
prevPage = ../api.html
showNext = true
nextPage = settings_routes.html
%}

# Публичное API без авторизации

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


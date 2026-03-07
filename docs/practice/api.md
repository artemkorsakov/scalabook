{%
showPrev = true
prevPage = authorization.html
showNext = true
nextPage = api/users_routes.html
%}

# Клиентское API

## Проверка работоспособности

Нашему приложению необходимо сообщать о своем состоянии работоспособности,
что обычно включает в себя, помимо прочего, проверку подключения к базе данных.
Мы можем смоделировать это следующим образом.

```dotty  
// pdp/src/main/scala/ru/scalabook/pdp/model/AppStatus.scala
package ru.scalabook.pdp.model

import io.circe.*
import io.circe.generic.semiauto.*
import ru.scalabook.pdp.model.AppStatus.*

final case class AppStatus(redis: RedisStatus, postgres: PostgresStatus)

object AppStatus:
  opaque type RedisStatus = Status

  object RedisStatus:
    def apply(status: Boolean): RedisStatus =
      Status.find(status)

  opaque type PostgresStatus = Status

  object PostgresStatus:
    def apply(status: Boolean): PostgresStatus =
      Status.find(status)

  enum Status(isOk: Boolean):
    case Okay()        extends Status(true)
    case Unreachable() extends Status(false)

  object Status:
    def find(status: Boolean): RedisStatus =
      if status then Status.Okay()
      else Status.Unreachable()

  given encoderStatus: Encoder[Status]       = deriveEncoder[Status]
  given encoderAppStatus: Encoder[AppStatus] = deriveEncoder[AppStatus]
end AppStatus 
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
  val literal = "io.circe" %% "circe-literal" % version
  val parser  = "io.circe" %% "circe-parser"  % version
}

object http4s {
  private val version = "0.23.33"

  val circe  = "org.http4s" %% "http4s-circe"  % version
  val client = "org.http4s" %% "http4s-client" % version
  val core   = "org.http4s" %% "http4s-core"   % version
  val dsl    = "org.http4s" %% "http4s-dsl"    % version
  val server = "org.http4s" %% "http4s-server" % version

  val jwt = "dev.profunktor" %% "http4s-jwt-auth" % "2.0.13"
}

object iron {
  private val version = "3.3.0"
  val circe           = "io.github.iltotore" %% "iron-circe"      % version
  ...
}
```

## Проверка работоспособности

Сервис HealthCheck сообщает о состоянии как Redis, так и PostgreSQL, которые мы
собираемся предоставить через HTTP для удобного доступа.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/routes/HealthRoutes.scala
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





---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala
[http4s]: https://http4s.org/

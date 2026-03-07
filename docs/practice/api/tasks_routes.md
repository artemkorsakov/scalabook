{%
showPrev = true
prevPage = settings_routes.html
showNext = true
nextPage = ../sources.html
%}

# Роуты для задач

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

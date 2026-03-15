{%
showPrev = true
prevPage = settings_routes.html
showNext = true
nextPage = ../sources.html
%}

# Endpoint-ы задач

## Получение списка задач пользователя

Требуются endpoint`ы по работе со списком всех доступных типов задач:

- Получение списка задач пользователя
- Получение списка задач пользователя заданного статуса
- Получение списка задач пользователя заданного типа
- Получение списка задач пользователя заданного раздела

```http
GET /tasks?status=knowledge_base&task_type=podcast&section_id=<sectionId>

Response code:
– 200: Возвращает список отфильтрованных по параметрам задач

Response body on success:

[
  {
    "id": 1,
    "name": "Статья о рекурсии",
    "status": "KnowledgeBase",
    "taskType": "Article"
  },
  ...
]
```

В ответе нам нужны не все данные по каждой из задач, а только такие поля:

- идентификатор задачи
- название задачи
- тип задачи
- раздел, к которому относится задача
- цель задачи
- порядок выполнения
- даты начала и окончания

Создаем тип результата запроса на получении информации:

```dotty
final case class TaskResult(
    id: TaskId,
    name: TaskName,
    taskType: TaskType,
    userTypeId: Option[UserTypeId],
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: Ordinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate]
)
```

Доработаем `TaskService` и добавим метод получения данных обо всем списке задач:

```dotty
sealed trait TaskTypeFilter
case object Book                 extends TaskTypeFilter
case object Article              extends TaskTypeFilter
case object Podcast              extends TaskTypeFilter
case object Video                extends TaskTypeFilter
case object Course               extends TaskTypeFilter
case class Other(id: UserTypeId) extends TaskTypeFilter
  
trait TaskService[F[_]: Monad]:
  def getAll(
      userId: UserId,
      maybeStatus: Option[Status],
      maybeTaskType: Option[TaskTypeFilter],
      maybeSectionId: Option[SectionId]
  ): F[Vector[TaskResult]]
```

Доработаем `TaskLifecycleService` и добавим метод получения данных обо всем списке задач:

```dotty
def getAll(
    userId: UserId,
    maybeStatus: Option[Status],
    maybeTaskType: Option[TaskTypeFilter],
    maybeSectionId: Option[SectionId]
): F[Vector[TaskResult]] =
  taskService.getAll(
    userId = userId,
    maybeStatus = maybeStatus,
    maybeTaskType = maybeTaskType,
    maybeSectionId = maybeSectionId
  )
```

Работа с задачами определенного типа и раздела:

```dotty
final case class TaskRoutes[F[_]: {JsonDecoder, MonadThrow}](
    taskLifecycleService: TaskLifecycleService[F]
) extends Http4sDsl[F]:

  private[routes] val prefixPath = "/tasks"

  object StatusQueryParam
      extends OptionalQueryParamDecoderMatcher[Status]("status")

  object TaskTypeQueryParam
      extends OptionalQueryParamDecoderMatcher[TaskTypeFilter]("task_type")

  object SectionIdQueryParam
      extends OptionalQueryParamDecoderMatcher[SectionId]("section_id")

  private val httpRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of:
      // Получение списка задач пользователя
      case GET -> Root :?
          StatusQueryParam(maybeStatus) +&
          TaskTypeQueryParam(maybeTaskType) +&
          SectionIdQueryParam(maybeSectionId) as user =>
        Ok(
          taskLifecycleService.getAll(
            userId = user.id,
            maybeStatus = maybeStatus,
            maybeTaskType = maybeTaskType,
            maybeSectionId = maybeSectionId
          )
        )
```

Для работы необходимы декодеры:

```dotty
object Status:
  given QueryParamDecoder[Status] =
    QueryParamDecoder[String].emap: str =>
      Status.values
        .find(t => t.toString.equalsIgnoreCase(str))
        .toRight(ParseFailure(s"Неизвестный статус '$str'", ""))
```

```dotty  
object TaskTypeFilter:
  given QueryParamDecoder[TaskTypeFilter] =
    QueryParamDecoder[String].emap:
      case "book"    => BookFilter.asRight[ParseFailure]
      case "article" => ArticleFilter.asRight[ParseFailure]
      case "podcast" => PodcastFilter.asRight[ParseFailure]
      case "video"   => VideoFilter.asRight[ParseFailure]
      case "course"  => CourseFilter.asRight[ParseFailure]
      case str       =>
        str
          .toIntOption
          .flatMap(UserTypeId.option)
          .toRight(ParseFailure(s"Неизвестный фильтр '$str'", ""))
          .map(OtherFilter(_))  
```

```dotty  
type SectionId = SectionId.T
object SectionId extends RefinedType[Int, GreaterEqual[0]]:
  given QueryParamDecoder[SectionId] =
    QueryParamDecoder[Int].emap: id =>
      SectionId.either(id).left.map(error => ParseFailure(error, ""))  
```


## Создание задачи



## Открытие задачи



## Поставить в очередь



## Взять в работу



## Выполнить задачу



## Отложить задачу



## Удаление задачи



---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala

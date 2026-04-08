{%
showPrev = true
prevPage = settings_routes.html
showNext = true
nextPage = ../main.html
%}

# Endpoint-ы задач

## Обзор функциональности

Endpoint-ы задач предоставляют полный CRUD и жизненный цикл управления задачами пользователя. 
Система поддерживает различные типы задач (книги, статьи, подкасты и пользовательские типы), 
статусы (база знаний, в очереди, в работе, выполнено) и гибкую фильтрацию.

## Получение списка задач

### Фильтрация задач

Система предоставляет несколько вариантов получения списка задач с возможностью комбинированной фильтрации:

- **По статусу** - фильтрация по текущему состоянию задачи
- **По типу** - фильтрация по пользовательским типам
- **По разделу** - фильтрация по разделу, к которому относится задача

```http
GET /tasks?status=in_progress&task_type=podcast&section_id=42
Authorization: Bearer <jwt-token>
```

**Параметры запроса:**

| Параметр     | Тип              | Описание                         | Пример                                                          |
|--------------|------------------|----------------------------------|-----------------------------------------------------------------|
| `status`     | `Status`         | Фильтр по статусу задачи         | `in_progress`, `completed`                                      |
| `task_type`  | `TaskTypeFilter` | Фильтр по типу задачи            | `book`, `article`, `podcast`, `video`, `course` или числовой ID |
| `section_id` | `SectionId`      | Фильтр по идентификатору раздела | `42`                                                            |

**Успешный ответ (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Статья о рекурсии",
    "taskType": "Article",
    "userTypeId": null,
    "status": "KnowledgeBase",
    "sectionId": 5,
    "goalId": 12,
    "ordinal": 1,
    "startDate": null,
    "endDate": null
  },
  {
    "id": 2,
    "name": "Подкаст о функциональном программировании",
    "taskType": "Podcast",
    "userTypeId": null,
    "status": "Planned",
    "sectionId": 3,
    "goalId": 8,
    "ordinal": 2,
    "startDate": null,
    "endDate": null
  },
  {
    "id": 3,
    "name": "Пользовательский тип",
    "taskType": "other",
    "userTypeId": 15,
    "status": "InProgress",
    "sectionId": 2,
    "goalId": 15,
    "ordinal": 3,
    "startDate": "2024-01-15",
    "endDate": "2024-01-30"
  }
]
```

### Модель данных для ответа

```dotty
import java.time.LocalDate

final case class TaskResult(
    id: TaskId,                     // Уникальный идентификатор задачи
    name: TaskName,                 // Название задачи
    taskType: TaskType,             // Тип задачи (встроенный или пользовательский)
    userTypeId: Option[UserTypeId], // ID пользовательского типа (если есть)
    status: Status,                 // Текущий статус
    sectionId: SectionId,           // Раздел, к которому относится задача
    goalId: GoalId,                 // Цель, в рамках которой создана задача
    ordinal: Ordinal,               // Порядковый номер в списке
    startDate: Option[LocalDate],   // Дата начала (для задач в работе)
    endDate: Option[LocalDate]      // Плановая дата окончания
)
```

### Типы фильтрации задач

Для гибкой фильтрации по типу задачи используется алгебраический тип данных (ADT):

```dotty
sealed trait TaskTypeFilter:
  def toPair: TaskType | UserTypeId =
    this match
      case BookFilter                  => TaskType.Book
      case ArticleFilter               => TaskType.Article
      case PodcastFilter               => TaskType.Podcast
      case VideoFilter                 => TaskType.Video
      case CourseFilter                => TaskType.Course
      case OtherFilter(id: UserTypeId) => id

/** Встроенные типы задач */
case object BookFilter                 extends TaskTypeFilter
case object ArticleFilter              extends TaskTypeFilter
case object PodcastFilter              extends TaskTypeFilter
case object VideoFilter                extends TaskTypeFilter
case object CourseFilter               extends TaskTypeFilter
/** Пользовательские типы (из настроек) */
case class OtherFilter(id: UserTypeId) extends TaskTypeFilter
```

### Сервисный слой

#### TaskService (низкоуровневый доступ к данным)

```dotty
trait TaskService[F[_]: Monad]:
  /** Получение отфильтрованного списка задач
    * @param userId идентификатор пользователя
    * @param maybeStatus опциональный фильтр по статусу
    * @param maybeTaskType опциональный фильтр по типу задачи
    * @param maybeSectionId опциональный фильтр по разделу
    * @return список задач
    */
  def getAll(
      userId: UserId,
      maybeStatus: Option[Status],
      maybeTaskType: Option[TaskType | UserTypeId],
      maybeSectionId: Option[SectionId]
  ): F[Vector[TaskResult]]
```

#### TaskLifecycleService (оркестрация бизнес-логики)

```dotty
trait TaskLifecycleService[F[_]]:
  /** Получение списка задач с применением всех фильтров
    * Делегирует выполнение TaskService
    */
  def getAll(
      userId: UserId,
      maybeStatus: Option[Status],
      maybeTaskType: Option[TaskType | UserTypeId],
      maybeSectionId: Option[SectionId]
  ): F[Vector[TaskResult]] =
    taskService.getAll(
      userId = userId,
      maybeStatus = maybeStatus,
      maybeTaskType = maybeTaskType,
      maybeSectionId = maybeSectionId
    )

  // ... другие методы lifecycle
```

### HTTP слой с фильтрацией

```dotty
final case class TaskRoutes[F[_]: {JsonDecoder, MonadThrow, Concurrent, Logger}](
    taskLifecycleService: TaskLifecycleService[F]
) extends PdpRoutes[F]:

  private[routes] val prefixPath = "/tasks"

  // Определение QueryParam экстракторов
  private object OptionalStatusQueryParam
      extends OptionalQueryParamDecoderMatcher[Status]("status")

  private object OptionalTaskTypeQueryParam
      extends OptionalQueryParamDecoderMatcher[TaskTypeFilter]("task_type")

  private object OptionalSectionIdQueryParam
      extends OptionalQueryParamDecoderMatcher[SectionId]("section_id")

  private val httpRoutes: AuthedRoutes[UserInfo, F] =
    AuthedRoutes.of:
      // GET /tasks?status=...&task_type=...&section_id=...
      case GET -> Root :?
          OptionalStatusQueryParam(maybeStatus) +&
          OptionalTaskTypeQueryParam(maybeTaskType) +&
          OptionalSectionIdQueryParam(maybeSectionId) as user =>
        Ok(
          taskLifecycleService.getAll(
            userId = user.id,
            maybeStatus = maybeStatus,
            maybeTaskType = maybeTaskType.map(_.toPair),
            maybeSectionId = maybeSectionId
          )
        )

      // ... остальные маршруты

  def routes(authMiddleware: AuthMiddleware[F, UserInfo]): HttpRoutes[F] =
    Router(prefixPath -> authMiddleware(httpRoutes))    
```

### Декодеры для Query параметров

#### Декодер для Status

```dotty
object Status:
  /** Преобразует строковый параметр запроса в Status */
  given QueryParamDecoder[Status] =
    QueryParamDecoder[String].emap: str =>
      Status.values
        .find(t => t.toString.equalsIgnoreCase(str))
        .toRight(ParseFailure(
          sanitized = s"Неизвестный статус '$str'",
          details = s"Допустимые значения: ${Status.values.mkString(", ")}"
        ))     
```

#### Декодер для TaskTypeFilter

```dotty
object TaskTypeFilter:
  /** Преобразует строковый параметр запроса в TaskTypeFilter
    * Поддерживает:
    * - Именованные значения: book, article, podcast, video, course
    * - Числовые ID для пользовательских типов: 42, 15
    */
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
          .map(OtherFilter(_))
          .toRight(ParseFailure(
            sanitized = s"Неизвестный фильтр '$str'",
            details =
              "Ожидается book/article/podcast/video/course или числовой ID пользовательского типа"
          ))          
```

#### Декодер для SectionId

```dotty
type SectionId = SectionId.T
object SectionId extends RefinedType[Int, GreaterEqual[0]]:
  /** Преобразует числовой параметр запроса в SectionId */
  given QueryParamDecoder[SectionId] =
    QueryParamDecoder[Int].emap: id =>
      SectionId.either(id).left.map: error =>
        ParseFailure(
          sanitized = s"Некорректный ID раздела: $id",
          details = error
        )    
```

## Управление задачами

### Создание задачи

Создание новой задачи в статусе "База знаний" (Knowledge Base).

**Запрос:**

```http
POST /tasks
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "Изучить монады",
  "taskType": "article",
  "sectionId": 5,
  "goalId": 12,
  ...
}
```

**Реализация:**

```dotty 
case ar @ POST -> Root / TaskTypeVar(taskType) as user =>
  def create(task: CreateTask): F[Response[F]] =
    taskLifecycleService
      .create(task)
      .flatMap(Created(_)) // Возвращает 201 Created с ID новой задачи
      .recover()

  taskType match
    case TaskType.Book =>
      ar.req.decodeR[NewBookTask]: newTask =>
        create(newTask.toCreateTask(user.id))
    case TaskType.Article =>
      ar.req.decodeR[NewArticleTask]: newTask =>
        create(newTask.toCreateTask(user.id))
    case TaskType.Podcast =>
      ar.req.decodeR[NewPodcastTask]: newTask =>
        create(newTask.toCreateTask(user.id))
    case TaskType.Video =>
      ar.req.decodeR[NewVideoTask]: newTask =>
        create(newTask.toCreateTask(user.id))
    case TaskType.Course =>
      ar.req.decodeR[NewCourseTask]: newTask =>
        create(newTask.toCreateTask(user.id))
    case TaskType.Other =>
      ar.req.decodeR[NewUserTask]: newTask =>
        create(newTask.toCreateTask(user.id))             
```

**Успешный ответ (201 Created):**

```
"aacc1cdd-c54f-4ae1-9551-2bb2c14fd476"
```

### Получение конкретной задачи

**Запрос:**

```http
GET /tasks/task?task_id=123&task_type=article
Authorization: Bearer <jwt-token>
```

**Параметры запроса:**

| Параметр    | Тип              | Описание             |
|-------------|------------------|----------------------|
| `task_id`   | `TaskId`         | Идентификатор задачи |
| `task_type` | `TaskTypeFilter` | Тип задачи           |

**Реализация:**

```dotty
private object TaskIdQueryParam
    extends QueryParamDecoderMatcher[TaskId]("task_id")

private object TaskTypeQueryParam
    extends QueryParamDecoderMatcher[TaskType]("task_type")

case GET -> Root / "task" :?
    TaskIdQueryParam(id) +& TaskTypeQueryParam(taskType) as user =>
  Ok(
    taskLifecycleService.get(
      id = id,
      taskType = taskType, // Конвертация фильтра в тип задачи
      userId = user.id
    )
  )
```

### Постановка задачи в очередь

Перемещение задачи из "базы знаний" в очередь на выполнение.

**Запрос:**

```http
PUT /tasks/enqueue
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "id": 123,
  "taskType": "article",
  "ordinal": 5,
  "plan": [
    "Найти материалы",
    "Изучить основы"
  ]
}              
```

**Модель запроса:**

```dotty
final case class EnqueueTask(
    id: TaskId,
    taskType: TaskType,
    ordinal: Ordinal,            // Порядок в очереди
    plan: NonEmptyList[TaskStep] // План выполнения (минимум 1 шаг)
)
```

**Реализация:**

```dotty
case ar @ PUT -> Root / "enqueue" as user =>
  ar.req.decodeR[EnqueueTask]: enqueueTask =>
    taskLifecycleService
      .enqueue(
        id = enqueueTask.id,
        taskType = enqueueTask.taskType,
        userId = user.id,
        ordinal = enqueueTask.ordinal,
        plan = enqueueTask.plan
      )
      .flatMap(Ok(_))
      .recover()   
```

### Начало работы над задачей

Перевод задачи из очереди в статус "в работе".

**Запрос:**

```http
PUT /tasks/start
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "id": 123,
  "taskType": "article",
  "startDate": "2024-01-15",
  "endDate": "2024-01-30"
}
```

**Модель запроса:**

```dotty
final case class StartTask(
    id: TaskId,
    taskType: TaskType,
    startDate: LocalDate, // Дата начала работы
    endDate: LocalDate    // Плановая дата завершения
)
```

**Реализация:**

```dotty
case ar @ PUT -> Root / "start" as user =>
  ar.req.decodeR[StartTask]: task =>
    taskLifecycleService
      .start(
        id = task.id,
        taskType = task.taskType,
        userId = user.id,
        startDate = task.startDate,
        endDate = task.endDate
      )
      .flatMap(Ok(_))
      .recover()     
```

### Завершение задачи

Отметка задачи как выполненной с оценкой и отзывом.

**Запрос:**

```http
PUT /tasks/complete
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "id": 123,
  "taskType": "article",
  "rating": 5,
  "review": "Отличная статья, много полезной информации",
  "endDate": "2026-01-28",
  "duration": 480
}
```

**Модель запроса:**

```dotty
final case class CompleteTask(
    id: TaskId,
    taskType: TaskType,
    rating: Rating,          // Оценка от 1 до 5
    review: Review,          // Текстовый отзыв
    endDate: LocalDate,      // Фактическая дата завершения
    duration: FiniteDuration // Фактическая длительность в минутах
)
```

**Реализация:**

```dotty
case ar @ PUT -> Root / "complete" as user =>
  ar.req.decodeR[CompleteTask]: task =>
    taskLifecycleService
      .complete(
        id = task.id,
        taskType = task.taskType,
        userId = user.id,
        rating = task.rating,
        review = task.review,
        endDate = task.endDate,
        duration = task.duration
      )
      .flatMap(Ok(_))
      .recover()       
```

### Откладывание задачи

Перевод задачи в статус "отложено" (например, если не хватает времени или ресурсов).

**Запрос:**

```http
PUT /tasks/defer
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "id": 123,
  "taskType": "article"
}
```

**Модель запроса:**

```dotty
final case class TaskInfo(
    id: TaskId,
    taskType: TaskType
)
```

**Реализация:**

```dotty
case ar @ PUT -> Root / "defer" as user =>
  ar.req.decodeR[TaskInfo]: task =>
    taskLifecycleService
      .defer(
        id = task.id,
        taskType = task.taskType,
        userId = user.id
      )
      .flatMap(Ok(_))
      .recover()      
```

### Удаление задачи

Полное удаление задачи из системы.

**Запрос:**

```http
DELETE /tasks
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "id": 123,
  "taskType": "article"
}
```

**Реализация:**

```dotty
case ar @ DELETE -> Root as user =>
  ar.req.decodeR[TaskInfo]: task =>
    taskLifecycleService
      .delete(
        id = task.id,
        taskType = task.taskType,
        userId = user.id
      )
      .flatMap(_ => NoContent())  // 204 No Content при успехе
      .recover()       
```

## Сводная таблица endpoint-ов

| Метод  | Path              | Параметры                           | Тело запроса   | Ответ                | Описание               |
|--------|-------------------|-------------------------------------|----------------|----------------------|------------------------|
| GET    | `/tasks`          | `status`, `task_type`, `section_id` | -              | 200 + `TaskResult[]` | Получение списка задач |
| POST   | `/tasks`          | -                                   | `CreateTask`   | 201 + ID             | Создание задачи        |
| GET    | `/tasks/task`     | `task_id`, `task_type`              | -              | 200 + `TaskResult`   | Получение задачи по ID |
| PUT    | `/tasks/enqueue`  | -                                   | `EnqueueTask`  | 200                  | Постановка в очередь   |
| PUT    | `/tasks/start`    | -                                   | `StartTask`    | 200                  | Начало работы          |
| PUT    | `/tasks/complete` | -                                   | `CompleteTask` | 200                  | Завершение задачи      |
| PUT    | `/tasks/defer`    | -                                   | `TaskInfo`     | 200                  | Откладывание задачи    |
| DELETE | `/tasks`          | -                                   | `TaskInfo`     | 204                  | Удаление задачи        |

## Коды ответов

| Код                           | Описание            | Когда возникает                         |
|-------------------------------|---------------------|-----------------------------------------|
| **200 OK**                    | Успешный запрос     | GET, PUT операции                       |
| **201 Created**               | Ресурс создан       | POST /tasks                             |
| **204 No Content**            | Успешное удаление   | DELETE /tasks                           |
| **400 Bad Request**           | Ошибка валидации    | Некорректные параметры или тело запроса |
| **401 Unauthorized**          | Не аутентифицирован | Отсутствует или невалидный JWT токен    |
| **403 Forbidden**             | Нет прав            | Попытка доступа к чужой задаче          |
| **404 Not Found**             | Ресурс не найден    | Задача с указанным ID не существует     |
| **422 Unprocessable Content** | Ошибка тела запроса | Некорректные параметры или тело запроса |

## Особенности реализации

1. **Типобезопасность** - использование уточненных типов для валидации на уровне компиляции
2. **Комбинируемая фильтрация** - все параметры фильтрации опциональны и могут комбинироваться
3. **Единый lifecycle** - все операции с задачами проходят через `TaskLifecycleService`
4. **Полиморфизм типов** - поддержка как встроенных, так и пользовательских типов задач
5. **Доменные экстракторы** - пользовательские экстракторы для `QueryParam` и `PathParam`

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[pfp]: https://leanpub.com/pfp-scala

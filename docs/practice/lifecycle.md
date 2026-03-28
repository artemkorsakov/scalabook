{%
showPrev = true
prevPage = db/db_test.html 
showNext = true
nextPage = lifecycle/complete.html
%}

# Жизненный цикл задач

После успешного определения алгебр (интерфейсов операций) и их интерпретаторов (реализаций для конкретных эффектов), 
наступает ключевой этап проектирования — создание программ, инкапсулирующих бизнес-логику. 
Эти программы, построенные на абстракциях алгебр, обеспечивают переносимость, тестируемость и чистоту предметной области.

## Сервис управления настройками

В системе персонального планирования и развития одной из фундаментальных подсистем является сервис настройки. 
Он отвечает за управление метаданными и структурой задач: 
пользовательскими типами задач, глобальными целями и разделами.
Без корректно настроенной структуры работа с самими задачами становится невозможной.

### Архитектура и композиция

Сервис реализован как нерасширяемый case-класс, который агрегирует более специализированные сервисы. 
Это демонстрирует принцип композиции и разделения ответственности:

```dotty
final case class SettingsService[F[_]: {MonadThrow as MT}](
    typeService: UserTypeService[F],   // Сервис работы с типами задач
    sectionService: SectionService[F], // Сервис работы с разделами
    goalService: GoalService[F],       // Сервис работы с целями
    taskService: TaskService[F]        // Сервис задач
)
```

Контекстная граница `MonadThrow` наделяет сервис способностью работать с эффектами, 
поддерживающими последовательные вычисления (`Monad`) и 
обработку ошибок через выбрасывание исключений в контексте эффекта (`Throw`). 

### Функциональная ответственность

`SettingsService` предоставляет полный CRUD (Create, Read, Update, Delete) цикл, а также операции изменения порядка для каждой из сущностей:

- **Создание** новых типов, целей и разделов.
- **Чтение/получение** списков существующих сущностей.
- **Обновление** атрибутов существующих сущностей.
- **Изменение порядка** следования сущностей (например, для отображения в UI).
- **Удаление** сущностей.

Рассмотрим каждую операцию детально.

## Создание пользовательского типа задачи

Операция создания гарантирует уникальность имени типа в пределах системы, предотвращая дублирование.

```dotty
def createTaskType(name: UserTypeName): F[UserTypeId] =
  createItem[UserType, UserTypeName, UserTypeId](
    getAll = typeService.getByUser(newItem.userId),
    getName = _.name,
    name = newItem.name,
    create = typeService.create(newItem)
  )
      
private def createItem[A, B, C](
    getAll: F[Vector[A]],
    getName: A => B,
    name: B,
    create: => F[C]
): F[C] =
  getAll.flatMap: items =>
    if items.exists(getName(_) == name) then
      MT.raiseError(
        new IllegalArgumentException(
          s"An item with name '$name' already exists"
        )
      )
    else create 
```

**Принцип работы:**

1. Запрашиваем полный список существующих типов заданного пользователя через `getByUser`.
2. Проверяем, не существует ли уже тип с запрошенным именем (`items.exists(getName(_) == name)`).
3. Если дубликат найден, операция прерывается с ошибкой `IllegalArgumentException`.
4. Если имя уникально, делегирует создание в `typeService.create`, который возвращает идентификатор нового типа (`UserTypeId`).

**Обобщенная приватная логика `createItem`:**
Для избежания дублирования кода, общая логика проверки уникальности и создания вынесена в параметризованный метод `createItem`. 
Он абстрагирован от конкретных типов сущностей (`A`), их имен (`B`) и идентификаторов (`C`), 
принимая соответствующие функции доступа и операции.

## Обновление типов, целей и разделов

Операция обновления должна обеспечить две ключевые предпосылки:

1. Обновляемая сущность должна существовать.
2. Новое имя сущности не должно конфликтовать с именами других сущностей (кроме себя самой).

```dotty
def updateTaskType(userType: UserType): F[Unit] =
  updateItem[UserType, UserTypeId, UserTypeName](
    getAll = typeService.getByUser(userType.userId),
    updated = userType,
    getId = _.id,
    getName = _.name,
    update = typeService.update
  )
  
private def updateItem[A, B, C](
    getAll: F[Vector[A]],
    updated: A,
    getId: A => B,
    getName: A => C,
    update: A => F[Unit]
): F[Unit] =
  getAll.flatMap: types =>
    val id      = getId(updated)
    val newName = getName(updated)

    if types.forall(getId(_) != id) then
      MT.raiseError(
        new IllegalArgumentException(s"An item with id '$id' not found")
      )
    else if types.exists(t => getName(t) == newName && getId(t) != id) then
      MT.raiseError(
        new IllegalArgumentException(
          s"Can't update! Task type with name '$newName' already exists"
        )
      )
    else update(updated)  
```

**Логика валидации в `updateItem`:**

1. **Проверка существования:** `types.forall(getId(_) != id)` — если элемент с идентификатором `id` не найден в базе данных, генерируется ошибка.
2. **Проверка уникальности имени:** `types.exists(t => getName(t) == newName && getId(t) != id)` — ошибка возникает, 
   если имя `newName` уже занято другой сущностью (чей идентификатор не совпадает с обновляемым).
3. Если обе проверки пройдены элемент обновляется.

## Изменение порядка сущностей

Эта операция позволяет пользователю кастомизировать порядок отображения элементов (типов, целей, разделов) в интерфейсе. 
На вход принимается вектор идентификаторов в желаемом порядке.

```dotty
def reorderTaskTypes(ids: NonEmptyVector[UserTypeId], userId: UserId): F[Unit] =
  reorderItems[UserType, UserTypeId](
    getAll = typeService.getByUser(userId),
    ids = ids,
    getId = _.id,
    update = item => ordinal => item.copy(ordinal = ordinal),
    updateAll = typeService.updateAll
  )
  
private def reorderItems[A, B](
    getAll: F[Vector[A]],
    ids: NonEmptyVector[B],
    getId: A => B,
    update: A => Ordinal => A,
    updateAll: NonEmptyVector[A] => F[Unit]
): F[Unit] =
  getAll.flatMap: items =>
    NonEmptyVector.fromVector(items) match
      case None =>
        MT.raiseError(new IllegalArgumentException("Items list is empty"))
      case Some(neItems) =>
        val notFoundIds = ids.filterNot(id => neItems.exists(getId(_) == id))
        if notFoundIds.nonEmpty then
          MT.raiseError(
            new IllegalArgumentException(
              s"Some ids are not found: ${notFoundIds.mkString(", ")}"
            )
          )
        else
          val updated =
            neItems.map: item =>
              val index   = ids.toVector.indexOf(getId(item))
              val ordinal =
                if index >= 0 then Ordinal.option(index + 1) else None
              update(item)(ordinal.getOrElse(Ordinal.Max))
          updateAll(updated)
```

**Алгоритм в `reorderItems`:**

1. **Проверка на существование:** если список `items` пуст, возвращаем ошибку.
2. **Валидация входных данных:** Определяются те идентификаторы из запроса `ids`, которые отсутствуют в базе данных (`notFoundIds`). 
   Если такие идентификаторы найдены, операция завершается с ошибкой.
3. **Сопоставление и расчет порядка:** Для каждой существующей сущности (`item`) вычисляется её новая позиция (`index`) 
   в переданном векторе `ids`. На основе индекса вычисляется порядок `ordinal`, начинающийся с 1. 
   Если сущность не указана в `ids`, ей присваивается максимальный порядок.
4. **Формирование обновленных объектов:** С помощью функции `updateAll` элементы обновляются.

### 4. Получение списка сущностей

Наиболее простая операция, которая делегируется соответствующему специализированному сервису.

```dotty
def getTaskTypes(userId: UserId): F[Vector[UserType]] = typeService.getByUser(userId)
```

Аналогичные методы `getGoals` и `getSections` будут обращаться к `goalService.getByUser(userId)` 
и `sectionService.getByUser(userId)` соответственно.

## Удаление сущностей: обеспечение целостности данных

Операция удаления сущностей (таких как разделы, цели или пользовательские типы задач) 
является критической с точки зрения целостности данных. 
Прежде чем удалить какую-либо сущность, необходимо убедиться, 
что она не используется в других частях системы — в частности, что не существует задач, 
которые ссылаются на эту сущность через внешние ключи. 

Для каждого типа сущности (раздел, цель, пользовательский тип) создается отдельный метод в `TaskService`, 
который проверяет, используется ли эта сущность хотя бы в одной задаче любого типа.

```dotty
private[services] trait TaskService[F[_]: Monad]:
  ...

  /** Проверяет существование задачи с заданным разделом. */
  def existsSection(id: SectionId, userId: UserId): F[Boolean]

  /** Проверяет существование задачи с заданной целью. */
  def existsGoal(id: GoalId, userId: UserId): F[Boolean]

  /** Проверяет существование задачи с заданным типом пользовательских задач. */
  def existsUserType(id: UserTypeId, userId: UserId): F[Boolean]
```

И реализация: 

```dotty
private[services] class PostgreSQLTaskService[F[_]: MonadCancelThrow](
    xa: Aux[[x] =>> F[x], Unit]
) extends TaskService[F]:
  ...
  
  def existsSection(id: SectionId, userId: UserId): F[Boolean] =
    (
      getExistsFragment[BookTask](Task.sectionIdColumn, id.value, userId),
      getExistsFragment[ArticleTask](Task.sectionIdColumn, id.value, userId),
      getExistsFragment[PodcastTask](Task.sectionIdColumn, id.value, userId),
      getExistsFragment[VideoTask](Task.sectionIdColumn, id.value, userId),
      getExistsFragment[CourseTask](Task.sectionIdColumn, id.value, userId),
      getExistsFragment[UserTask](Task.sectionIdColumn, id.value, userId)
    ).mapN(_ || _ || _ || _ || _ || _)

  def existsGoal(id: GoalId, userId: UserId): F[Boolean] =
    (
      getExistsFragment[BookTask](Task.goalIdColumn, id.value, userId),
      getExistsFragment[ArticleTask](Task.goalIdColumn, id.value, userId),
      getExistsFragment[PodcastTask](Task.goalIdColumn, id.value, userId),
      getExistsFragment[VideoTask](Task.goalIdColumn, id.value, userId),
      getExistsFragment[CourseTask](Task.goalIdColumn, id.value, userId),
      getExistsFragment[UserTask](Task.goalIdColumn, id.value, userId)
    ).mapN(_ || _ || _ || _ || _ || _)

  def existsUserType(id: UserTypeId, userId: UserId): F[Boolean] =
    getExistsFragment[UserTask](UserTask.userTypeIdColumn, id.value, userId)

  private def getExistsFragment[A: TableSchema as schema](
      col: ColumnDefinition,
      id: Int,
      userId: UserId
  ): F[Boolean] =
    val sqlFragment =
      Fragment.const(
        s"""SELECT 1 FROM ${schema.tableName.value} WHERE ${col.name.value} = $id AND user_id = '$userId' LIMIT 1;"""
      )

    sqlFragment
      .query[Int]        // Запрос возвращает столбец со значением 1 при наличии записи
      .option            // Преобразует результат в Option[Int] (Some(1) или None)
      .transact(xa)      // Выполняет запрос в контексте транзакции
      .map(_.isDefined)  // Преобразует Option в Boolean: true, если запись найдена
```

#### Интеграция проверок в операции удаления

Методы проверки интегрируются в операции удаления через сервис настроек (`SettingsService`), 
что обеспечивает безопасное удаление только тех сущностей, которые не используются.

```dotty
def deleteTaskType(id: UserTypeId, userId: UserId): F[Unit] =
  // Сначала проверяем наличие зависимостей
  taskService.existsUserType(id, userId).ifM(
    // Если existsUserType вернул true (тип используется) → вызываем ошибку
    MT.raiseError(
      new IllegalArgumentException(
        s"Невозможно удалить тип задачи с ID $id: тип используется в одной или нескольких задачах"
      )
    ),
    // Если existsUserType вернул false (тип не используется) → выполняем удаление
    typeService.delete(id)
  )  
```

Аналогичная логика применяется для удаления разделов и целей.

### Заключение по сервису настроек

`SettingsService` представляет собой согласованный фасад над подсистемами, 
обеспечивающий целостность и непротиворечивость данных в рамках предметной области. 

С проектированием сервиса настройки работа над инфраструктурой системы завершена. 
Следующим шагом является определение **сервиса жизненного цикла задач (`TaskLifecycleService`)**, 
который будет оперировать непосредственно экземплярами задач, 
используя настроенную структуру (типы, цели, разделы) для их организации 
и управления их состоянием (создание, выполнение, завершение, архивация).

## Создание задачи

Процесс создания задачи является фундаментальной операцией в системе управления персональным развитием. 
Ключевой особенностью является возможность инициализации задачи сразу в одном из трех возможных статусов, 
что позволяет гибко планировать и структурировать работу. 
Каждый статус накладывает определенные требования к полноте заполнения данных:

- **База знаний**: Требуется заполнение всех базовых полей (название, описание, тип, раздел, цель), 
  но **не требуется** план выполнения и даты. Это идеальный статус для идей и намерений, которые еще не детализированы.
- **Запланировано**: Необходимо заполнить **все поля, включая подробный план выполнения**, 
  но **без указания дат начала и окончания**. Задача полностью готова к переходу в активную фазу.
- **В работе**: Требуется **полное заполнение всех полей** задачи, включая план выполнения, 
  дату начала и планируемую дату завершения. Это статус для активной, текущей работы.

### Реализация операции создания

Сервис жизненного цикла задач (`TaskLifecycleService`) агрегирует все необходимые сервисы для валидации и сохранения задачи.

```dotty
final case class TaskLifecycleService[F[_]: {MonadThrow as MT}](
    typeService: UserTypeService[F],   // Для проверки пользовательских типов
    sectionService: SectionService[F], // Для проверки раздела
    goalService: GoalService[F],       // Для проверки цели
    taskService: TaskService[F],       // Для непосредственного сохранения задачи
    historyService: HistoryService[F]  // Для хранения истории задач
):
  import TaskLifecycleService.*

  def create(task: CreateTask): F[TaskId] =
    // 1. Сбор и валидация всех зависимых сущностей
    val validationResult =
      for
        sectionOpt  <- sectionService.get(task.sectionId)
        goalOpt     <- goalService.get(task.goalId)
        // Проверка для пользовательского типа
        userTypeVal <- validateUserTaskType(task) 
      yield (
        // Преобразование Option в Validated с ошибкой
        sectionOpt
          .filter(_.userId == task.userId)
          .toValidNel(s"Section with id ${task.sectionId} not found"),
        goalOpt
          .filter(_.userId == task.userId)
          .toValidNel(s"Goal with id ${task.goalId} not found"),
        userTypeVal,
        // Валидация статуса
        validateStatus(task)
      )
        // Комбинирование результатов: успех, только если все Valid
        .mapN { (_, _, _, _) => () } 

    // 2. Исполнение валидации и создание или возврат ошибки
    validationResult.flatMap:
      // Вся валидация пройдена → создаем задачу
      case Validated.Valid(_)        => taskService.create(task)
      case Validated.Invalid(errors) =>
        // Накопленные ошибки объединяются и выбрасываются как исключение
        MT.raiseError(
          new IllegalArgumentException(errors.toVector.mkString(", "))
        )
  end create

  // Приватный метод для валидации пользовательского типа задачи.
  // Проверка требуется только для задач типа `CreateUserTask`.
  private def validateUserTaskType(task: CreateTask)
      : F[ValidatedNel[String, Unit]] =
    task match
      case userTask: CreateUserTask =>
        typeService
          .get(userTask.userTypeId)
          .map:
            case Some(userType) if userType.userId == task.userId =>
              Validated.valid(()) // Тип существует
            case None =>
              Validated.invalidNel(
                s"User type with id ${userTask.userTypeId} not found"
              )
      // Для задач без пользовательского типа проверка не требуется    
      case _ => MT.pure(Validated.valid(()))
end TaskLifecycleService

// Сопутствующий объект, содержит чистые функции валидации, зависящие только от статуса.
object TaskLifecycleService:
  // Основная функция валидации статуса. Определяет, какие дополнительные проверки нужны.
  private[services] def validateStatus(task: CreateTask)
      : ValidatedNel[String, Unit] =
    task.status match
      case Status.InProgress | Status.Completed =>
        // Для статуса "В работе" и "Завершено" обязательны И план, И корректные даты
        (validatePlan(task), validateDates(task)).mapN((_, _) => ())
      case Status.Planned       => 
        // Для "Запланировано" обязателен только план
        validatePlan(task)
      case Status.KnowledgeBase => 
        // Для "Базы знаний" дополнительных проверок нет
        Validated.valid(())

  // Проверка наличия плана выполнения (непустого списка шагов).
  private[services] def validatePlan(task: CreateTask)
      : ValidatedNel[String, Unit] =
    if task.plan.isEmpty then
      Validated.invalidNel("Plan is empty")
    else Validated.valid(())

  // Проверка корректности дат: обе даты должны быть указаны, 
  // а дата окончания — быть после даты начала.
  private[services] def validateDates(task: CreateTask)
      : ValidatedNel[String, Unit] =
    (task.startDate, task.endDate) match
      case (Some(start), Some(end)) if end.isAfter(start) => Validated.valid(())
      case (Some(start), Some(end))                       =>
        Validated.invalidNel(s"Start date $start is after end date $end")
      case _ =>
        Validated.invalidNel("Start or end date is empty")
end TaskLifecycleService
```

## Постановка задачи в очередь

Эта операция переводит задачу из стадии идеи ("База знаний") в стадию готового к выполнению плана ("Запланировано"). 
Ключевые действия:

1. **Смена статуса** с `KnowledgeBase` на `Planned`.
2. **Обязательное предоставление плана выполнения** — задача не может быть запланирована без детального плана.
3. **Назначение порядкового номера**: Система может предлагать первые свободные порядковые номера среди задач того же типа, 
   чтобы облегчить пользователю организацию очереди.

```dotty
def enqueue(
    id: TaskId,
    taskType: TaskType,
    userId: UserId,
    ordinal: Ordinal,
    plan: NonEmptyVector[TaskStep] // Непустой список шагов — гарантия на уровне типов
): F[Unit] =
  // 1. Находим задачу, строго проверяя её текущий статус
  findTask(id, taskType, userId, Status.KnowledgeBase).flatMap: task =>
    // 2. Создаем обновленную версию: меняем статус и привязываем план
    val updated = task.copy(
      status = Status.Planned,
      ordinal = ordinal,
      plan = plan.toVector
    )
    // 3. Сохраняем изменения
    taskService.update(updated) 
  
def get(id: TaskId, taskType: TaskType, userId: UserId): F[Option[Task]] =
  taskService.get(id, taskType).map: task =>
    task.filter(_.userId == userId)
        
// Вспомогательная функция для безопасного поиска задачи 
// с предварительной валидацией статуса.  
private def findTask(
    id: TaskId,
    taskType: TaskType,
    userId: UserId,
    status: Status
): F[Task] =
  get(id, taskType, userId).flatMap:
    case Some(task) if task.status == status =>
      MT.pure(task) // Задача найдена и имеет ожидаемый статус
    case Some(_) =>
      // Задача найдена, но в неправильном статусе (ошибка бизнес-логики)
      MT.raiseError(
        new IllegalArgumentException(
          s"Task with id '$id' is not in '$status' state"
        )
      )
    case None =>
      // Задача не найдена (ошибка данных)
      MT.raiseError(
        new IllegalArgumentException(s"Task with id '$id' not found")
      )     
```

## Начало работы над задачей

Операция переводит задачу в активное состояние. При этом необходимо:

1.  **Установить дату начала работы** (может подставляться текущая дата по умолчанию).
2.  **Установить планируемую дату завершения**, которая должна быть логически корректной (после даты начала).

```dotty
def start(
    id: TaskId,
    taskType: TaskType,
    userId: UserId,
    startDate: LocalDate,
    endDate: LocalDate
): F[Unit] =
  // Предварительная проверка логики дат на уровне сервиса
  if endDate.isBefore(startDate) then
    MT.raiseError(
      new IllegalArgumentException(
        s"End date $endDate is before start date $startDate"
      )
    )
  else
    // Поиск задачи в статусе "Запланировано"
    findTask(id, taskType, userId, Status.Planned).flatMap: task =>
      val updated = task.copy(
        status = Status.InProgress,
        startDate = startDate.some, // Фиксация даты начала
        endDate = endDate.some      // Фиксация дедлайна
      )
      taskService.update(updated)        
```

## Откладывание задачи

Данная операция позволяет временно снять задачу с активного выполнения, не отменяя её полностью. 
Это полезно при смене приоритетов или возникновении блокировок.

1.  **Смена статуса** с `InProgress` обратно на `Planned`.
2.  **Очистка дат**: Даты начала и окончания обнуляются, так как при повторном взятии в работу они, скорее всего, будут другими.

```dotty
def defer(id: TaskId, taskType: TaskType, userId: UserId): F[Unit] =
  findTask(id, taskType, userId, Status.InProgress).flatMap: task =>
    val updated = task.copy(
      status = Status.Planned,
      startDate = none,
      endDate = none   
    )
    taskService.update(updated)   
```

## Удаление задачи

Важная и необратимая операция. 
Сервис жизненного цикла делегирует её напрямую в `TaskService`, который отвечает за постоянное хранение. 

```dotty
def delete(id: TaskId, taskType: TaskType, userId: UserId): F[Unit] =
  get(id, taskType, userId).map:
    case Some(_) => taskService.delete(id, taskType)
    case None    => MT.unit  
```

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

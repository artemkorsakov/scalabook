{%
showPrev = true
prevPage = db/db_test.html 
showNext = true
nextPage = lifecycle/complete.html
%}

# Жизненный цикл задач

## Сервис настройки

Для начала создадим сервис настройки, который будет отвечать за работу с пользовательскими типами, целями и разделами.

```dotty
final case class SettingsService[F[_]: {MonadThrow as MT, Logger as log}](
    typeService: UserService[F],
    sectionService: SectionService[F],
    goalService: GoalService[F]
)
```

Он будет выполнять следующие функции:

- Создание типов, целей и разделов
- Обновление типов, целей и разделов
- Изменение порядка типов, целей и разделов
- Получение списка типов, целей и разделов
- Удаление типов, целей и разделов

### Создание пользовательского типа

```dotty
def createTaskType(name: UserTypeName): F[UserTypeId] =
  createItem[UserType, UserTypeName, UserTypeId](
    getAll = typeService.getAll,
    getName = _.name,
    name = name,
    create = typeService.create
  )
  
private def createItem[A, B, C](
    getAll: F[List[A]],
    getName: A => B,
    name: B,
    create: B => F[C]
): F[C] =
  getAll.flatMap: items =>
    if items.exists(getName(_) == name) then
      MT.raiseError(
        new IllegalArgumentException(
          s"An item with name $name already exists"
        )
      )
    else create(name) 
```

Нельзя создать тип, если он уже существует.

Создание цели и раздела аналогично.

### Обновление типов, целей и разделов

```dotty
def updateTaskType(userType: UserType): F[Unit] =
  updateItem[UserType, UserTypeId, UserTypeName](
    getAll = typeService.getAll,
    updated = userType,
    getId = _.id,
    getName = _.name,
    update = typeService.update
  )

private def updateItem[A, B, C](
    getAll: F[List[A]],
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
        new IllegalArgumentException(s"An item with id $id not found")
      )
    else if types.exists(t => getName(t) == newName && getId(t) != id) then
      MT.raiseError(
        new IllegalArgumentException(
          s"Can't update! Task type with name $newName already exists"
        )
      )
    else update(updated)
```

Обновление цели и раздела аналогично.

### Изменение порядка типов, целей и разделов

```dotty
def reorderTaskTypes(ids: Vector[UserTypeId]): F[Unit] =
  reorderItems[UserType, UserTypeId](
    getAll = typeService.getAll,
    ids = ids,
    getId = _.id,
    update = item => ordinal => item.copy(ordinal = ordinal),
    updateAll = typeService.updateAll
  )

private def reorderItems[A, B](
    getAll: F[List[A]],
    ids: Vector[B],
    getId: A => B,
    update: A => Ordinal => A,
    updateAll: List[A] => F[Unit]
): F[Unit] =
  getAll.flatMap: items =>
    val notFoundIds = ids.filterNot(id => items.exists(getId(_) == id))
    if notFoundIds.nonEmpty then
      MT.raiseError(
        new IllegalArgumentException(s"Some ids are not found: $notFoundIds")
      )
    else
      val updated =
        items.map: item =>
          val index   = ids.indexOf(getId(item))
          val ordinal = if index >= 0 then Ordinal.option(index + 1) else None
          update(item)(ordinal.getOrElse(Ordinal.Max))
      updateAll(updated)
```

Изменение порядка цели и раздела аналогично.

### Получение списка типов, целей и разделов

```dotty
def getTaskTypes: F[List[UserType]] = typeService.getAll
```

Получение списка цели и раздела аналогично.

### Удаление типов, целей и разделов

```dotty
def deleteTaskType(id: UserTypeId): F[Unit] = typeService.delete(id)
```

Удаление цели и раздела аналогично.

## Создание задачи

Задачу можно создать в любом из следующих статусов:

- База знаний (требуется заполнение всех полей, кроме плана выполнения и дат)
- Запланировано (требуется заполнение всех полей, кроме дат)
- В работе (требуется заполнение всех полей)

```dotty
final case class TaskLifecycleService[F[_]: {MonadThrow as MT}](
    typeService: UserService[F],
    sectionService: SectionService[F],
    goalService: GoalService[F],
    taskService: TaskService[F]
):
  import TaskLifecycleService.*

  def createTask(task: CreateTask): F[UUID] =
    val result =
      for
        sec      <- sectionService.get(task.sectionId)
        goal     <- goalService.get(task.goalId)
        usertype <- validateUserTaskType(task)
      yield (
        sec.toRightNel(s"Section with id ${task.sectionId} not found"),
        goal.toRightNel(s"Goal with id ${task.goalId} not found"),
        usertype,
        validateStatus(task)
      )
        .mapN { (_, _, _, _) => () }

    result.flatMap {
      case Right(_)     => taskService.create(task)
      case Left(errors) =>
        MT.raiseError(
          new IllegalArgumentException(errors.toList.mkString(", "))
        )
    }
  end createTask

  private def validateUserTaskType(task: CreateTask)
      : F[EitherNel[String, Unit]] =
    task match
      case userTask: CreateUserTask =>
        typeService
          .get(userTask.userTypeId)
          .map {
            case Some(_) =>
              Right(())
            case None =>
              Left(NonEmptyList.one(
                s"User type with id ${userTask.userTypeId} not found"
              ))
          }
      case _ => MT.pure(Right(()))

end TaskLifecycleService

object TaskLifecycleService:
  private def validateStatus(task: CreateTask): EitherNel[String, Unit] =
    task.status match
      case Status.InProgress =>
        (validatePlan(task), validateDates(task)).mapN((_, _) => ())
      case Status.Planned       => validatePlan(task)
      case Status.KnowledgeBase => Right(())

  private def validatePlan(task: CreateTask): EitherNel[String, Unit] =
    if task.plan.isEmpty then
      Left(NonEmptyList.one("Plan is empty"))
    else Right(())

  private def validateDates(task: CreateTask): EitherNel[String, Unit] =
    (task.startDate, task.endDate) match
      case (Some(start), Some(end)) if end.isAfter(start) => Right(())
      case (Some(start), Some(end))                       =>
        Left(NonEmptyList.one(s"Start date $start is after end date $end"))
      case _ => Left(NonEmptyList.one("Start or end date is empty"))
```

## Поставить в очередь

Из "Базы знаний" в "Запланировано".

- Заполнение порядка выполнения: выводятся первые свободные порядки задач того же типа, что и ставится в очередь.
- Напоминание о необходимости добавления плана выполнения.

```dotty
def enqueue(
    id: UUID,
    taskType: TaskType,
    plan: NonEmptyList[TaskStep]
): F[Unit] =
  findTask(id, taskType, Status.KnowledgeBase).flatMap: task =>
    val updated = task.copy(status = Status.Planned, plan = plan.toList)
    taskService.update(updated)
    
private def findTask(id: UUID, taskType: TaskType, status: Status): F[Task] =
  get(id, taskType).flatMap {
    case Some(task) if task.status == status =>
      MT.pure(task)
    case Some(_) =>
      MT.raiseError(
        new IllegalArgumentException(
          s"Task with id '$id' is not in '$status' state"
        )
      )
    case None =>
      MT.raiseError(
        new IllegalArgumentException(s"Task with id '$id' not found")
      )
  }      
```

## Взять в работу

Из "Запланировано" в "В работе".

Заполняемые поля:

- Дата начала (автоподстановка текущей даты)
- Планируемая дата завершения
  Планируемая дата завершения не может быть раньше даты начала

```dotty
def start(
    id: UUID,
    taskType: TaskType,
    startDate: LocalDate,
    endDate: LocalDate
): F[Unit] =
  if endDate.isBefore(startDate) then
    MT.raiseError(
      new IllegalArgumentException(
        s"End date $endDate is before start date $startDate"
      )
    )
  else
    findTask(id, taskType, Status.Planned).flatMap: task =>
      val updated = task.copy(
        status = Status.InProgress,
        startDate = startDate.some,
        endDate = endDate.some
      )
      taskService.update(updated)
```

## Отложить задачу

Кнопка “Отложить” (только из “В работе”)
Возврат в “Запланировано” с очисткой дат начала и окончания

```dotty
def defer(id: UUID, taskType: TaskType): F[Unit] =
  findTask(id, taskType, Status.InProgress).flatMap: task =>
    val updated = task.copy(
      status = Status.Planned,
      startDate = none,
      endDate = none
    )
    taskService.update(updated)
```

## Удаление задачи

Кнопка “Удаление задачи”
Безвозвратное удаление задачи!

```dotty
def delete(id: UUID, taskType: TaskType): F[Unit] =
  taskService.delete(id, taskType)
```

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

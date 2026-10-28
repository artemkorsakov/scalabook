{%
showPrev = true
prevPage = db/db_test.html 
showNext = true
nextPage = lifecycle/service.html
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

Сервис реализован как нерасширяемый case class, который агрегирует более специализированные сервисы. 
Это демонстрирует принцип композиции и разделения ответственности:

```dotty
final case class SettingsService[F[_]: MonadThrow](
    taskTypeService: TaskTypeService[F], // Сервис работы с типами задач
    sectionService: SectionService[F],   // Сервис работы с разделами
    goalService: GoalService[F],         // Сервис работы с целями
    taskService: TaskService[F],         // Сервис задач
    sourcesService: SourcesService[F],   // Сервис работы с источниками (база знаний)
    periodicStepService: PeriodicStepService[F] // Сервис шагов периодических задач
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
def createTaskType(newItem: CreateTaskType): F[TaskTypeId] =
  createItem[TaskType, TaskTypeName, TaskTypeId](
    getAll = taskTypeService.getByUser(newItem.userId),
    getName = _.name,
    name = newItem.name,
    create =
      taskTypeService.create(newItem).flatTap { taskTypeId =>
        val newSource = CreateSource(
          taskTypeId = taskTypeId,
          userId = newItem.userId,
          information = ""
        )
        sourcesService.create(newSource)
      }
  )

private def createItem[A, B, C](
    getAll: F[Vector[A]],
    getName: A => B,
    name: B,
    create: => F[C]
): F[C] =
  getAll.flatMap: items =>
    if items.exists(getName(_) == name) then
      MonadThrow[F].raiseError(
        new IllegalArgumentException(
          s"An item with name '$name' already exists"
        )
      )
    else create    
```

**Принцип работы:**

1. Запрашиваем полный список существующих типов заданного пользователя через `getByUser`.
2. Проверяем, существует ли уже тип с запрошенным именем (`items.exists(getName(_) == name)`).
3. Если дубликат найден, операция прерывается с ошибкой `IllegalArgumentException`.
4. Если имя уникально, делегирует создание в `typeService.create`, который возвращает идентификатор нового типа (`TaskTypeId`).
5. Дополнительно для каждого созданного типа `flatTap` создаёт связанную запись источника (`Source`) с пустым содержимым — она становится «Базой знаний» для данного типа в дашборде.

**Обобщенная приватная логика `createItem`:**
Для избежания дублирования кода, общая логика проверки уникальности и создания вынесена в параметризованный метод `createItem`. 
Он абстрагирован от конкретных типов сущностей (`A`), их имен (`B`) и идентификаторов (`C`), 
принимая соответствующие функции доступа и операции.

## Обновление типов, целей и разделов

Операция обновления должна обеспечить две ключевые предпосылки:

1. Обновляемая сущность должна существовать.
2. Новое имя сущности не должно конфликтовать с именами других сущностей (кроме себя самой).

```dotty
def updateTaskType(taskType: TaskType): F[Boolean] =
  updateItem[TaskType, TaskTypeId, TaskTypeName](
    getAll = taskTypeService.getByUser(taskType.userId),
    updated = taskType,
    getId = _.id,
    getName = _.name,
    update = taskTypeService.update
  )  
  
private def updateItem[A, B, C](
    getAll: F[Vector[A]],
    updated: A,
    getId: A => B,
    getName: A => C,
    update: A => F[Boolean]
): F[Boolean] =
  getAll.flatMap: types =>
    val id      = getId(updated)
    val newName = getName(updated)

    if types.forall(getId(_) != id) then
      MonadThrow[F].raiseError(
        new IllegalArgumentException(s"An item with id '$id' not found")
      )
    else if types.exists(t => getName(t) == newName && getId(t) != id) then
      MonadThrow[F].raiseError(
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
def reorderTaskTypes(
    ids: NonEmptyVector[TaskTypeId],
    userId: UserId
): F[Boolean] =
  reorderItems[TaskType, TaskTypeId](
    getAll = taskTypeService.getByUser(userId),
    ids = ids,
    getId = _.id,
    update = item => ordinal => item.copy(ordinal = ordinal),
    updateAll = taskTypeService.updateAll
  )
  
private def reorderItems[A, B](
    getAll: F[Vector[A]],
    ids: NonEmptyVector[B],
    getId: A => B,
    update: A => Ordinal => A,
    updateAll: NonEmptyVector[A] => F[Boolean]
): F[Boolean] =
  getAll.flatMap: items =>
    NonEmptyVector.fromVector(items) match
      case None =>
        MonadThrow[F].raiseError(
          new IllegalArgumentException("Items list is empty")
        )
      case Some(neItems) =>
        val notFoundIds = ids.filterNot(id => neItems.exists(getId(_) == id))
        if notFoundIds.nonEmpty then
          MonadThrow[F].raiseError(
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
def getTaskTypes(userId: UserId): F[Vector[TaskType]] = typeService.getByUser(userId)
```

Аналогичные методы `getGoals` и `getSections` будут обращаться к `goalService.getByUser(userId)` 
и `sectionService.getByUser(userId)` соответственно.

## Удаление сущностей: обеспечение целостности данных

Операция удаления сущностей (таких как разделы, цели или типы задач) 
является критической с точки зрения целостности данных. 
Прежде чем удалить какую-либо сущность, необходимо убедиться, 
что она не используется в других частях системы — в частности, что не существует задач, 
которые ссылаются на эту сущность через внешние ключи. 

Для каждого типа сущности (раздел, цель, тип задач) создается отдельный метод в `TaskService`, 
который проверяет, используется ли эта сущность хотя бы в одной задаче любого типа.

```dotty
trait TaskService[F[_]: Monad]:
  ...

  /** Проверяет существование задачи с заданным разделом. */
  def existsSection(id: SectionId, userId: UserId): F[Boolean]

  /** Проверяет существование задачи с заданной целью. */
  def existsGoal(id: GoalId, userId: UserId): F[Boolean]

  /** Проверяет существование задачи с заданным типом пользовательских задач. */
  def existsTaskType(id: TaskTypeId, userId: UserId): F[Boolean]
```

И реализация: 

```dotty
private[pdp] class PostgreSQLTaskService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends TaskService[F]:
  ...
  
  def existsSection(id: SectionId, userId: UserId): F[Boolean] =
    sql"""SELECT 1 FROM tasks WHERE section_id = $id AND user_id = $userId LIMIT 1;"""
      .query[Int]        // Запрос возвращает столбец со значением 1 при наличии записи
      .option            // Преобразует результат в Option[Int] (Some(1) или None)
      .transact(xa)      // Выполняет запрос в контексте транзакции
      .map(_.isDefined)  // Преобразует Option в Boolean: true, если запись найдена

  def existsGoal(id: GoalId, userId: UserId): F[Boolean] =
    sql"""SELECT 1 FROM tasks WHERE goal_id = $id AND user_id = $userId LIMIT 1;"""
      .query[Int]
      .option
      .transact(xa)
      .map(_.isDefined)

  def existsTaskType(id: TaskTypeId, userId: UserId): F[Boolean] =
    sql"""SELECT 1 FROM tasks WHERE task_type_id = $id AND user_id = $userId LIMIT 1;"""
      .query[Int]
      .option
      .transact(xa)
      .map(_.isDefined)
```

#### Интеграция проверок в операции удаления

Методы проверки интегрируются в операции удаления через сервис настроек (`SettingsService`), 
что обеспечивает безопасное удаление только тех сущностей, которые не используются.

```dotty
def deleteTaskType(id: TaskTypeId, userId: UserId): F[Boolean] =
  // Сначала проверяем наличие зависимостей
  taskService.existsTaskType(id, userId).ifM(
    // Если existsTaskType вернул true (тип используется) → вызываем ошибку
    MonadThrow[F].raiseError(
      new IllegalArgumentException(
        s"Невозможно удалить тип задачи с ID $id: тип используется в задачах"
      )
    ),
    // Если existsTaskType вернул false (тип не используется) → выполняем удаление
    taskTypeService.get(id).flatMap:
      case Some(item) if item.userId == userId =>
        sourcesService.delete(id, userId) *>
          taskTypeService.delete(id)
      case _ => false.pure[F]
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

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

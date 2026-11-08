# Завершение задачи

Завершение — это ключевой этап в жизненном цикле задачи, который подводит итог выполненной работы, 
позволяет оценить результаты и пополнить статистику для будущего анализа. 
Этот процесс создает структурированную запись о достижении, обогащенную метаданными и субъективной оценкой.

## Операция "Выполнить задачу"

Кнопка "Выполнить" доступна **только для задач в статусе "В работе"**, 
что обеспечивает корректность бизнес-процесса: нельзя завершить задачу, которая еще не начата или находится только в планах.

При завершении задачи пользователь заполняет следующие поля:

- **Краткое ревью задачи** — текстовое описание проделанной работы, ключевых выводов, возникших сложностей и полученных результатов.
- **Оценка (⭐️ 1-5)** — субъективная оценка полезности, сложности или удовлетворенности от выполнения задачи.
- **Фактическая дата завершения** — реальная дата окончания работы, которая может отличаться от планируемой.

### Сохранение в истории

Для последующего анализа и учета выполненных работ система сохраняет в истории **структурированный снимок завершенной задачи**, включающий:

1.  **Идентификационные данные:**
    - Тип задачи (книга, статья, курс и т.д.)
    - Уникальный идентификатор задачи (TaskId)
    - Наименование задачи
2.  **Контекст выполнения:**
    - Раздел, к которому относится задача
    - Цель, на достижение которой направлена задача
3.  **Фактические метрики выполнения:**
    - Фактическая продолжительность выполнения
    - Дата начала и фактическая дата окончания
4.  **Результаты и оценка:**
    - Оценка пользователя
    - Краткое ревью

## Модель данных завершенной задачи

Модель `CompletedTask` инкапсулирует все данные, необходимые для долгосрочного хранения информации о выполненных задачах.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/CompletedTask.scala
/** Модель выполненной задачи, представляющая собой неизменяемый снимок состояния
  * задачи на момент её завершения. 
  */
final case class CompletedTask(
    taskId: TaskId,
    taskTypeId: TaskTypeId,
    name: TaskName,
    userId: UserId,
    sectionId: SectionId,
    goalId: GoalId,
    duration: FiniteDuration,
    rating: Rating,
    review: Review,
    startDate: LocalDate,
    endDate: LocalDate
)

object CompletedTask:
  /** Перечисление возможных оценок задачи. */
  enum Rating(val r: Byte):
    case One   extends Rating(1)
    case Two   extends Rating(2)
    case Three extends Rating(3)
    case Four  extends Rating(4)
    case Five  extends Rating(5)

  /** Неявный экземпляр Meta[Rating] для Doobie, обеспечивающий преобразование
    * между Scala-перечислением и целочисленным представлением в БД.
    */
  given Meta[Rating] =
    Meta[Int]
      .timap(i =>
        Rating.values.find(_.r.toInt == i).getOrElse(Rating.One)
      )(_.r)

  /** Уточненный тип для текста ревью с гарантиями валидности: непустая строка
    * без ведущих/завершающих пробелов.
    */
  type Review = Review.T
  object Review extends RefinedType[String, MinLength[1] & Trimmed]
```

## Сервис работы с историей задач

Для работы с историей завершенных задач определяется отдельный сервис, следующий принципу единственной ответственности.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/HistoryService.scala
private[services] trait HistoryService[F[_]]:
  def create(task: CompletedTask): F[Int]

  def getByUser(userId: UserId): F[Vector[CompletedTask]]
```

### PostgreSQL-реализация HistoryService

```dotty
private[pdp] class PostgreSQLHistoryService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends HistoryService[F]:

  def create(task: CompletedTask): F[Int] =
    sql"""INSERT INTO completed_tasks (task_id, task_type_id, name, user_id, section_id, goal_id, duration, rating, review, start_date, end_date) 
         VALUES (${task.taskId}, ${task.taskTypeId}, ${task.name}, ${task.userId}, ${task.sectionId}, ${task.goalId}, ${task.duration}, ${task.rating}, ${task.review}, ${task.startDate}, ${task.endDate}) 
         RETURNING id"""
      .query[Int]   // Запрос возвращает ID вставленной записи
      .unique       // Ожидаем ровно один результат
      .transact(xa) // Выполняем в транзакционном контексте

  def getByUser(userId: UserId): F[Vector[CompletedTask]] =
    fr"""SELECT task_id, task_type_id, name, user_id, section_id, goal_id, duration, rating, review, start_date, end_date 
         FROM completed_tasks WHERE user_id = $userId"""
      .query[CompletedTask] // Используем автоматический маппинг через Meta
      .to[Vector]           // Преобразуем поток результатов в список
      .transact(xa)         // Выполняем запрос
```

### Интеграция HistoryService в жизненный цикл задач

Сервис истории добавляется в качестве зависимости в `TaskLifecycleService`, 
что позволяет координировать завершение задачи и сохранение её в архиве.

```dotty
final case class TaskLifecycleService[F[_]: MonadThrow](
    taskTypeService: TaskTypeService[F],
    sectionService: SectionService[F],
    goalService: GoalService[F],
    taskService: TaskService[F],
    historyService: HistoryService[F]  // Новый сервис для работы с историей
):
```

## Команда завершения задачи

Метод `complete` инкапсулирует всю логику завершения задачи: валидацию, создание исторической записи и обновление статуса исходной задачи.

```dotty
/** Завершает задачу, находящуюся в статусе "В работе", сохраняя её результаты
  * в истории и обновляя статус на "Завершено".
  */
def complete(
    id: TaskId,
    userId: UserId,
    rating: Rating,
    review: Review,
    endDate: LocalDate,
    duration: FiniteDuration
): F[Boolean] =
  for
    // Находим и валидируем задачу
    task <- findTask(id, userId, Status.InProgress)
    
    // Если дата начала отсутствует или больше даты окончания,
    // то она устанавливается равной дате окончания
    startDate = task.startDate.filter(_.isBefore(endDate)).getOrElse(endDate)
    
    // Создаем снимок завершенной задачи для истории
    completedTask =
      CompletedTask(
        taskId = task.id,
        taskTypeId = task.taskTypeId,
        name = task.name,
        userId = task.userId,
        sectionId = task.sectionId,
        goalId = task.goalId,
        duration = duration,
        rating = rating,
        review = review,
        startDate = startDate,
        endDate = endDate
      )
      
    // Сохраняем задачу в историю (независимая операция)
    _ <- historyService.create(completedTask)
    
    // Обновляем статус исходной задачи на "Завершено"
    updated = task.copy(
      status = Status.Completed,
      startDate = completedTask.startDate.some,
      endDate = completedTask.endDate.some,
      duration = duration
    )
    updateRes <- taskService.update(updated)
  yield updateRes
end complete
```

## Миграции базы данных

Добавление таблицы `completed_tasks` требует создания соответствующей миграции, аналогично другим таблицам системы. 

```dotty
private[pdp] class PostgreSQLMigrationService[F[_]: MonadCancelThrow](
  xa: Transactor[F]
):
  ...
  
  def initialize(): IO[Boolean] =
    val program =
      for
        ...
        _ <- createCompletedTasksTable.update.run
        _ <- addSectionFKToTable("completed_tasks").update.run
        _ <- addGoalFKToTable("completed_tasks").update.run
        _ <- addUsersFKToTable("completed_tasks").update.run
      yield true
    program.transact(xa)
    
  private val createCompletedTasksTable: Fragment =
    sql"""CREATE TABLE IF NOT EXISTS completed_tasks (
         id SERIAL PRIMARY KEY, 
         task_id UUID NOT NULL, 
         task_type_id INT NOT NULL, 
         name VARCHAR(255) NOT NULL, 
         user_id UUID NOT NULL, 
         section_id INT NOT NULL, 
         goal_id INT NOT NULL, 
         duration BIGINT NOT NULL, 
         rating INT NOT NULL, 
         review TEXT, 
         start_date DATE, 
         end_date DATE);"""  
```

Этот подход к завершению задач обеспечивает не только корректное изменение статуса, 
но и создание ценного архива выполненных работ для последующего анализа и отслеживания прогресса в долгосрочной перспективе.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

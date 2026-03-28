{%
showPrev = true
prevPage = ../lifecycle.html
showNext = true
nextPage = ../authorization.html
%}

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
// pdp/src/main/scala/ru/scalabook/pdp/model/CompletedTask.scala
package ru.scalabook.pdp.model


/** Модель выполненной задачи, представляющая собой неизменяемый снимок состояния
  * задачи на момент её завершения. 
  */
final case class CompletedTask(
    taskId: TaskId,
    taskType: TaskType,
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

  /** Неявная реализация TableSchema для типа CompletedTask. Описывает, как
    * сущность CompletedTask должна быть отображена в таблицу БД.
    */
  given TableSchema[CompletedTask] with
    def tableName: TableName = TableName("completed_tasks")

    def columns: Columns =
      Columns(
        ColumnDefinition(
          ColumnName.Id,
          ColumnType.Serial
        ).asPK(), // Автоинкрементный первичный ключ для таблицы истории
        ColumnDefinition(
          ColumnName("task_id"),
          ColumnType.UUID
        ).asNotNull(), // Ссылка на оригинальную задачу
        ColumnDefinition(
          ColumnName("task_type"),
          ColumnType.Varchar
        ).asNotNull(), // Тип задачи для фильтрации в отчетах
        ColumnDefinition(
          ColumnName("name"),
          ColumnType.Varchar
        ).asNotNull(), // Название задачи
        ColumnDefinition(
          ColumnName("user_id"),
          ColumnType.UUID
        ).asNotNull(), // Идентификатор пользователя
        ColumnDefinition(
          ColumnName("section_id"),
          ColumnType.Int
        ).asNotNull(), // Внешний ключ на таблицу разделов
        ColumnDefinition(
          ColumnName("goal_id"),
          ColumnType.Int
        ).asNotNull(), // Внешний ключ на таблицу целей
        ColumnDefinition(
          ColumnName("duration"),
          ColumnType.BigInt
        ).asNotNull(), // Длительность
        ColumnDefinition(
          ColumnName("rating"),
          ColumnType.Int
        ).asNotNull(), // Числовая оценка 1-5
        ColumnDefinition(
          ColumnName("review"),
          ColumnType.Text
        ), // Текстовое поле для развернутого отзыва
        ColumnDefinition(
          ColumnName("start_date"),
          ColumnType.Date
        ), // Дата начала выполнения
        ColumnDefinition(
          ColumnName("end_date"),
          ColumnType.Date
        ) // Фактическая дата завершения
      )
  end given
end CompletedTask
```

## Сервис работы с историей задач

Для работы с историей завершенных задач определяется отдельный сервис, следующий принципу единственной ответственности.

```dotty
// pdp/src/main/scala/ru/scalook/pdp/services/HistroyServices.scala
package ru.scalabook.pdp.services

import cats.Monad
import ru.scalabook.pdp.model.CompletedTask

/** Алгебра (интерфейс) сервиса истории выполненных задач. Определяет
  * контракты для работы с архивом завершенных задач, независимо от конкретной
  * реализации хранилища.
  */
private[services] trait HistoryService[F[_]: Monad]:
  /** Сохраняет выполненную задачу в историю.
    *
    * @param task
    *   Модель завершенной задачи для сохранения.
    * @return
    *   F[Unit] - эффект, подтверждающий успешное сохранение.
    */
  def create(task: CompletedTask): F[Unit]

  /** Возвращает все выполненные задачи из истории. Предназначен для
    * формирования отчетов, аналитики и отображения достижений пользователя.
    *
    * @return
    *   F[Vector[CompletedTask]] - эффект, возвращающий коллекцию завершенных
    *   задач.
    */
  def getByUser(userId: UserId): F[Vector[CompletedTask]]
```

### PostgreSQL-реализация HistoryService

```dotty
private[services] class PostgreSQLHistoryService[F[_]: MonadCancelThrow](
    xa: Aux[[x] =>> F[x], Unit]
) extends HistoryService[F]:

  def create(task: CompletedTask): F[Int] =
    generateInsertReturningId[CompletedTask, CompletedTask, Int](task)
      .query[Int]    // Запрос возвращает ID вставленной записи
      .unique        // Ожидаем ровно один результат
      .transact(xa)  // Выполняем в транзакционном контексте

  def getByUser(userId: UserId): F[Vector[CompletedTask]] =
    fr"""SELECT task_id, task_type, name, user_id, section_id, goal_id, duration, rating, review, start_date, end_date 
         FROM completed_tasks WHERE user_id = $userId"""
      .query[CompletedTask]  // Используем автоматический маппинг через Meta
      .to[Vector]            // Преобразуем поток результатов в список
      .transact(xa)          // Выполняем запрос          
```

### Интеграция HistoryService в жизненный цикл задач

Сервис истории добавляется в качестве зависимости в `TaskLifecycleService`, 
что позволяет координировать завершение задачи и сохранение её в архиве.

```dotty
final case class TaskLifecycleService[F[_]: {MonadThrow as MT}](
    typeService: UserTypeService[F],
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
  *
  * @param id
  *   UUID идентификатор задачи.
  * @param taskType
  *   Тип задачи (для валидации и поиска в правильной таблице).
  * @param rating
  *   Оценка задачи пользователем.
  * @param review
  *   Текстовый отзыв о выполнении задачи.
  * @param endDate
  *   Фактическая дата завершения задачи.
  * @param duration
  *   Реальная продолжительность выполнения.
  * @return
  *   F[Unit] - эффект, представляющий успешное завершение операции.
  * @throws IllegalArgumentException
  *   если задача не найдена или находится не в статусе "В работе".
  */
def complete(
    id: TaskId,
    taskType: TaskType,
    userId: UserId,
    rating: Rating,
    review: Review,
    endDate: LocalDate,
    duration: FiniteDuration
): F[Unit] =
  for
    // 1. Находим и валидируем задачу
    task <- findTask(id, taskType, userId, Status.InProgress)
    
    // Если дата начала отсутствует или больше даты окончания,
    // то она устанавливается равной дате окончания
    startDate = task.startDate.filter(_.isBefore(endDate)).getOrElse(endDate)
    
    // 2. Создаем снимок завершенной задачи для истории
    completedTask =
      CompletedTask(
        taskId = task.id,
        taskType = task.taskType,
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
      
    // 3. Сохраняем задачу в историю (независимая операция)  
    _ <- historyService.create(completedTask)
    
    // 4. Обновляем статус исходной задачи на "Завершено"
    updated = task.copy(
      status = Status.Completed,
      startDate = completedTask.startDate.some,
      endDate = completedTask.endDate.some
    )
    _ <- taskService.update(updated)
  yield ()
end complete
```

## Миграции базы данных

Добавление таблицы `completed_tasks` требует создания соответствующей миграции, аналогично другим таблицам системы. 

```dotty
private[services] class PostgreSQLMigrationService[F[_]: MonadCancelThrow](
  xa: Aux[[x] =>> F[x], Unit]
):
  ...
  
  def initialize(): IO[Unit] =
    val program =
      for
        ...
        _ <- generateCreateTableFor[CompletedTask].update.run
        _ <- addSectionFKToTable[CompletedTask]().update.run
        _ <- addGoalFKToTable[CompletedTask]().update.run
      yield ()
    program.transact(xa)
```

Этот подход к завершению задач обеспечивает не только корректное изменение статуса, 
но и создание ценного архива выполненных работ для последующего анализа и отслеживания прогресса в долгосрочной перспективе.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

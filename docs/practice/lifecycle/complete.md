{%
showPrev = true
prevPage = ../lifecycle.html
showNext = true
nextPage = authorization.html
%}

# Завершение задачи

## Выполнить задачу

Кнопка “Выполнить” (только из “В работы”)
Заполняемые поля:

- Краткое ревью задачи
- Оценка (⭐️1-5)
- Фактическая дата завершения

В истории сохраняем следующую информацию:

- Тип задачи
- Идентификатор задачи
- Раздел
- Цель задачи
- Реальная продолжительность или количество страниц в книге
- Наименование
- Оценка
- Краткое ревью
- Дата начала и фактическая дата окончания

Определим тип завершенной задачи:


```dotty
package ru.scalabook.pdp.model

import ru.scalabook.pdp.model.Goal.GoalId
import ru.scalabook.pdp.model.Section.SectionId
import io.github.iltotore.iron.*
import _root_.doobie.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.CompletedTask.*

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.duration.FiniteDuration
import ru.scalabook.sql.*
import ru.scalabook.sql.ColumnDefinition.*
import ru.scalabook.sql.TableSchema.*

/** Выполненная задача.
  *
  *   - Идентификатор задачи
  *   - Тип задачи
  *   - Наименование
  *   - Раздел
  *   - Цель задачи
  *   - Реальная продолжительность задачи
  *   - Количество страниц, если задача типа “Книга”
  *   - Оценка
  *   - Краткое ревью
  *   - Дата начала и фактическая дата окончания
  */
final case class CompletedTask(
    taskId: UUID,
    taskType: TaskType,
    name: TaskName,
    sectionId: SectionId,
    goalId: GoalId,
    duration: FiniteDuration,
    rating: Rating,
    review: Review,
    startDate: LocalDate,
    endDate: LocalDate
)

object CompletedTask:
  enum Rating(val r: Byte):
    case One   extends Rating(1)
    case Two   extends Rating(2)
    case Three extends Rating(3)
    case Four  extends Rating(4)
    case Five  extends Rating(5)

  given Meta[Rating] =
    Meta[Int]
      .timap(i =>
        Rating.values.find(_.r.toInt == i).getOrElse(Rating.One)
      )(_.r)

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
        ).asPK(),
        ColumnDefinition(
          ColumnName("task_id"),
          ColumnType.UUID
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("task_type"),
          ColumnType.Varchar
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("name"),
          ColumnType.Varchar
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("section_id"),
          ColumnType.Int
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("goal_id"),
          ColumnType.Int
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("duration"),
          ColumnType.BigInt
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("rating"),
          ColumnType.Int
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("review"),
          ColumnType.Text
        ),
        ColumnDefinition(
          ColumnName("start_date"),
          ColumnType.Date
        ),
        ColumnDefinition(
          ColumnName("end_date"),
          ColumnType.Date
        )
      )
  end given
end CompletedTask
```

Определим сервис, который будет сохранять выполненную задачу.

```dotty
package ru.scalabook.pdp.services

import cats.Monad
import ru.scalabook.pdp.model.CompletedTask

/** Интерфейс сервиса истории выполненных задач.
  */
private[services] trait HistoryService[F[_]: Monad]:
  /** Сохраняет выполненную задачу в историю. */
  def create(task: CompletedTask): F[Unit]

  /** Возвращает все выполненные задачи. */
  def getAll: F[Vector[CompletedTask]]
```

Добавление миграции аналогично другим таблицам.

```dotty
private[services] class PostgreSQLHistoryService(xa: Aux[[x] =>> IO[x], Unit])
    extends HistoryService[IO]:

  def create(task: CompletedTask): IO[Int] =
    generateInsertReturningId[CompletedTask, CompletedTask, Int](task)
      .query[Int]
      .unique
      .transact(xa)

  def getAll: IO[List[CompletedTask]] =
    sql"SELECT task_id, task_type, name, section_id, goal_id, duration, rating, review, start_date, end_date FROM completed_tasks"
      .query[CompletedTask].to[List].transact(xa)
```

Команда для выполнения задачи:

```dotty
def complete(
    id: UUID,
    taskType: TaskType,
    rating: Rating,
    review: Review,
    endDate: LocalDate,
    duration: FiniteDuration
): F[Unit] =
  for
    task <- findTask(id, taskType, Status.InProgress)
    completedTask =
      CompletedTask(
        taskId = task.id,
        taskType = task.taskType,
        name = task.name,
        sectionId = task.sectionId,
        goalId = task.goalId,
        duration = duration,
        rating = rating,
        review = review,
        startDate = task.startDate.getOrElse(endDate),
        endDate = endDate
      )
    _ <- historyService.create(completedTask)
    updated = task.copy(
      status = Status.Completed,
      startDate = task.startDate,
      endDate = endDate.some
    )
    _ <- taskService.update(updated)
  yield ()
end complete
```



---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

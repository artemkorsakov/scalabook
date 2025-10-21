{%
showPrev = true
prevPage = task_types/localization.html
showNext = true
nextPage = api.html
%}

# Работа с БД (CRUD)

Задачи из индивидуального плана развития надо где-то хранить.
Но прежде чем определить, где именно хранить задачи, для начала определимся с тем,
какое именно поведение от взаимодействия с хранилищем мы ожидаем.

Для этого создадим трейт, приватный в разрезе пакета, чтобы контролировать чтобы этот интерфейс
был доступен только внутри проекта.

```dotty
// src/main/scala/ru/scalabook/pdp/db/TaskService.scala
package ru.scalabook.pdp.db

import cats.effect.IO
import ru.scalabook.pdp.model.TaskType.UserType
import ru.scalabook.pdp.model.*

import java.util.UUID

private[db] trait TaskService:
  def createUserType(userType: UserType): IO[Unit]

  def getUserType(id: TaskTypeId): IO[Option[UserType]]

  def getAllUserTypes: IO[List[UserType]]

  def updateUserType(userType: UserType): IO[Unit]

  def deleteUserType(id: TaskTypeId): IO[Unit]

  def createSection(section: Section): IO[Unit]
  
  def getSection(id: SectionId): IO[Option[Section]]

  def getAllSections: IO[List[Section]]

  def updateSection(section: Section): IO[Unit]

  def deleteSection(id: SectionId): IO[Unit]

  def createTask(task: Task): IO[Unit]

  def getTask(id: UUID, taskType: TaskType): IO[Option[Task]]

  def getAllTasks: IO[List[Task]]

  def updateTask(task: Task): IO[Unit]

  def deleteTask(id: UUID, taskType: TaskType): IO[Unit]
end TaskService
```

Это классический CRUD без дополнительной логики.
Мы позволяем создавать задачи - тип `IO[Unit]` говорит о том, что у операции есть побочный эффект,
а результатом будет - `Unit` - задача сохранена, 
получать задачу по заданному идентификатору и типу - `IO[Option[Task]]` - тип говорит, что задачи может и не быть.
Обновлять задачу и удалять задачу.

Данный сервис не должен нести в себе никакой бизнес логики.
Например, если нам нужно перенести задачу из одной категории в другую - например, из статьи в книгу,
то мы это делаем путем удаления старой задачи и создания новой и эта бизнес-логика будет на стороне,
которая за неё ответственна. Но не внутренний сервис `TaskService` - он ответственен только 
за хранение задач.


У этого сервиса может быть несколько реализаций.
Например, мы можем хранить задачи в PostgreSQL или в любой другой БД.

Рассмотрим пример работы с PostgreSQL, а также вынесем в конфиги выбор БД для хранения задач.

Добавляем в Dependencies зависимость от doobie:

```dotty
object Dependencies {
  ...

  object doobie {
    private val version = "1.0.0-RC10"
    val core            = "org.tpolecat" %% "doobie-core"     % version
    val postgres        = "org.tpolecat" %% "doobie-postgres" % version
  }
  
  object iron {
    private val version = "3.2.0"
    val core            = "io.github.iltotore" %% "iron"            % version
    val doobie          = "io.github.iltotore" %% "iron-doobie"     % version
    val pureconfig      = "io.github.iltotore" %% "iron-pureconfig" % version
  }

  ...

  val rootLibs: Seq[ModuleID] =
    Seq(
      cats.catsEffect,
      doobie.core,
      doobie.postgres,
      iron.core,
      iron.doobie,
      iron.pureconfig,
      pureconfig.core
    )

  ...
}
```

## CRUD пользовательских типов

## CRUD секций

## CRUD тасок

Создаем реализацию взаимодействия с БД:

```dotty
package ru.scalabook.pdp.db

import doobie.*
import doobie.implicits.*
import cats.*
import cats.effect.*
import cats.implicits.*
import ru.scalabook.pdp.model.{Task, TaskType}

import java.util.UUID

private[db] class PostgreSQLTaskService(
    url: String,
    user: String,
    password: String
) extends TaskService {
  private val xa = Transactor.fromDriverManager[[x] =>> IO[x]](
    driver = "org.postgresql.Driver",
    url = url,
    user = user,
    password = password,
    logHandler = None
  )

  def createTask(task: Task): IO[Unit] =
    val program = sql"select 42".query[Int].unique
    program.transact(xa).map: i =>
      println(i)

  def getTask(id: UUID, taskType: TaskType): IO[Option[Task]] =
    IO.pure(None)

  def updateTask(task: Task): IO[Unit] =
    IO.unit

  def deleteTask(id: UUID, taskType: TaskType): IO[Unit] =
    IO.unit
}
```

Для проверки корректности взаимодействия с БД добавляем зависимость testcontainers:

```dotty
object testcontainers {
  private val version = "1.20.0"
  val postgres = "org.testcontainers" % "postgresql" % version
}

...
  
val testLibs: Seq[ModuleID] =
Seq(
  testcontainers.postgres,
  weaver.cats
).map(_ % Test)
```

Он позволяет локально разворачивать докер-образ postgreSQL, где мы можем проверить взаимодействие с БД.

Для проверки сохранения задач каждого типа создаем экземпляры задач и добавляем тесты, проверяющие CRUD
для каждого типа задач:

```dotty
```


---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

# Реализация конкретных сервисов работы с PostgreSQL

## Конкретизация абстрактных алгебр: от интерфейса к реализации

Принцип **"Program to an interface, not an implementation"** является фундаментальным в функциональном программировании. 
В нашем контексте это означает:

- **Абстрактный уровень**: Трейт `TaskTypeService[F[_]]` определяет **что** нужно сделать (контракт).
- **Конкретный уровень**: Класс `PostgreSQLTaskTypeService` определяет **как** это сделать для PostgreSQL (реализация).

Такое разделение обеспечивает несколько ключевых преимуществ:

- **Свобода замены реализации**: Возможность перейти с PostgreSQL на другую СУБД (MySQL, SQLite) 
  или даже на in-memory хранилище для тестов, модифицируя только слой реализации.
- **Упрощение тестирования**: Бизнес-логика может тестироваться с mock-реализациями, не требуя развернутой БД.
- **Соблюдение принципа инверсии зависимостей**: Высокоуровневые модули (бизнес-логика) зависят от абстракций, 
  а не от деталей низкоуровневых модулей (работы с БД).

## Структура и реализация PostgreSQLTaskTypeService

Класс `PostgreSQLTaskTypeService` является конкретной реализацией алгебры `TaskTypeService[F]`, использующей PostgreSQL в качестве бэкенда.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLTaskTypeService.scala
private[pdp] class PostgreSQLTaskTypeService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends TaskTypeService[F]:

  /** Создает новый тип задачи.
    *
    * @param newItem
    *   Новый тип задачи.
    * @return
    *   {{{F[TaskTypeId]}}} - эффект, который при выполнении вернет ID созданной
    *   записи.
    */
  def create(newItem: CreateTaskType): F[TaskTypeId] =
    sql"""INSERT INTO task_types (name, user_id, displayed, ordinal) 
          VALUES (${newItem.name}, ${newItem.userId}, ${newItem.displayed}, ${newItem.ordinal}) 
          RETURNING id"""
      .query[TaskTypeId] // Указываем, что запрос возвращает TaskTypeId
      .unique            // Ожидаем ровно одну запись (возвращаемый ID)
      .transact(xa)      // Выполняем в контексте транзакции

  /** Получает тип задачи по его ID.
    *
    * @param id
    *   Идентификатор типа задачи.
    * @return
    *   {{{F[Option[TaskType]]}}} - эффект, возвращающий Optional результат.
    */
  def get(id: TaskTypeId, userId: UserId): F[Option[TaskType]] =
    sql"""SELECT id, name, user_id, displayed, ordinal FROM task_types WHERE id = $id AND user_id = $userId"""
      .query[TaskType] // Указываем тип результата 
      .option          // Может вернуть 0 или 1 запись
      .transact(xa)
      
  /** Получает все типы задач.
    *
    * @return
    *   {{{F[Vector[TaskType]]}}} - эффект, возвращающий список всех типов.
    */
  def getByUser(userId: UserId): F[Vector[TaskType]] =
    sql"""SELECT id, name, user_id, displayed, ordinal FROM task_types WHERE user_id = $userId"""
      .query[TaskType] // Селект всех полей
      .to[Vector]      // Преобразуем поток результатов в Vector
      .transact(xa)  

  /** Обновляет существующий тип задачи.
    *
    * @param taskType
    *   Объект TaskType с обновленными данными (должен содержать существующий
    *   ID).
    * @return
    *   {{{F[Boolean]}}} - эффект, подтверждающий выполнение операции.
    */
  def update(taskType: TaskType): F[Boolean] =
    getUpdateFragment(taskType)
      .update        // Указываем, что это update-запрос
      .run           // Выполняем
      .transact(xa)
      .map(_ > 0)    // Преобразуем F[Int] в F[Boolean]
      
  def updateAll(taskTypes: NonEmptyVector[TaskType]): F[Boolean] =
    // Сворачиваем хвост списка, начиная с первого элемента.
    // Используем оператор *> (sequencing) для последовательного выполнения запросов
    // с игнорированием промежуточных результатов (важен только побочный эффект).
    taskTypes.tail.foldLeft(getUpdateFragment(taskTypes.head).update.run) {
      case (acc, taskType) =>
        acc *> getUpdateFragment(taskType).update.run
    }
      .transact(xa) // Выполняем всю последовательность в одной транзакции
      .map(_ > 0)

  /** Удаляет тип задачи по ID.
    *
    * @param id
    *   Идентификатор типа задачи для удаления.
    * @return
    *   {{{F[Boolean]}}} - эффект, подтверждающий выполнение операции.
    */
  def delete(id: TaskTypeId): F[Boolean] =
    sql"""DELETE FROM task_types WHERE id = $id"""
      .update.run.transact(xa).map(_ > 0)

  private def getUpdateFragment(taskType: TaskType): Fragment =
    sql"""UPDATE task_types 
         SET name = ${taskType.name}, user_id = ${taskType.userId}, displayed = ${taskType.displayed}, ordinal = ${taskType.ordinal} 
         WHERE id = ${taskType.id}"""
end PostgreSQLTaskTypeService
```

При этом для `UserId`, т.к. это непрозрачный тип, необходимо явно определить [`Meta[UserId]`](https://typelevel.org/doobie/docs/12-Custom-Mappings.html):

```dotty
import org.typelevel.doobie.*
import org.typelevel.doobie.postgres.*
import org.typelevel.doobie.postgres.implicits.*

object UserId:
  given Meta[UserId] =
    Meta[UUID].imap[UserId](UserId(_))(identity)
```

## Принцип минимального и точного соответствия контракту

Важнейший аспект реализации — строгое соблюдение контракта, определенного в интерфейсе `TaskTypeService`.

### Что означает "реализовывать только необходимые методы"?

1. **Точное соответствие сигнатурам**: Класс реализует ровно те методы, которые объявлены в трейте, 
   с точно такими же типами параметров и возвращаемых значений.
2. **Отсутствие дополнительных публичных методов**: Класс не добавляет новых публичных методов, 
   которые не были бы частью контракта.
3. **Инкапсуляция деталей реализации**: Все вспомогательные функции, специфичные для PostgreSQL, 
   должны быть приватными методами или функциями в сопутствующем объекте.

**Пример нарушения принципа (антипаттерн):**

```dotty
// ❌ НЕПРАВИЛЬНО: Класс добавляет методы, не объявленные в интерфейсе
class PostgreSQLTaskService extends TaskService[IO]:
  // Методы из интерфейса - правильно
  def create(task: CreateTask): IO[TaskId] = ...
  def get(id: TaskId, userId: UserId): IO[Option[Task]] = ...

  // Лишний публичный метод - НЕПРАВИЛЬНО!
  def internalDatabaseOptimization(): IO[Unit] = ...

  // Метод, меняющий сигнатуру - НЕПРАВИЛЬНО!
  def getByUser(limit: Int): IO[Vector[Task]] = ... // В интерфейсе нет параметра limit!
```

**Пример правильной реализации:**

```dotty
// ✅ ПРАВИЛЬНО: Точное соответствие интерфейсу
class PostgreSQLTaskService extends TaskService[IO]:
  // Только методы из интерфейса
  def create(task: CreateTask): IO[TaskId] = ...
  def get(id: TaskId, userId: UserId): IO[Option[Task]] = ...
  def getByUser(userId: UserId): IO[Vector[Task]] = ... 
  def update(task: Task): IO[Boolean] = ...
  def delete(id: TaskId): IO[Boolean] = ...

  // Приватные вспомогательные методы - допустимо
  private def buildWhereClause(filters: Map[String, String]): String = ...
```

## Механизм преобразования типов: роль неявных параметров (given instances)

Ключевой элемент, обеспечивающий работу кода — это система неявных параметров (given instances) в Scala 3.

### Что происходит при вызове `.query[UserType]`?

```dotty
sql"""SELECT id, name, user_id, displayed, ordinal FROM task_types WHERE id = $id"""
  .query[UserType]  // "Магическое" преобразование!
  .option
  .transact(xa)
```

1. **Doobie ищет неявный `Get[TaskType]`**: Метод `query[T]` требует наличия в неявной области видимости 
   экземпляра типа `Get[T]` — класса типов Doobie, который знает, как преобразовать строку результата SQL в объект типа `T`.

2. **Импорт `iron.doobie.given` решает проблему**:

   ```dotty
   import io.github.iltotore.iron.doobie.given
   ```

   Этот импорт предоставляет автоматические экземпляры `Get` и `Put` для уточненных типов, 
   таких как `TypeTypeName`, `TypeTypeId` и составных case class, содержащих такие поля.

3. **Автоматическая деривация**: Для case class'ов, таких как `TypeType`, 
   Doobie может автоматически сгенерировать `Get[TypeType]`, 
   если для всех его полей существуют соответствующие экземпляры `Get`.

## Правила корректного использования неявных параметров

Неявные параметры — мощный инструмент, но их неправильное использование может сделать код запутанным и сложным для понимания.

### ❌ Типичные ошибки и антипаттерны:

```dotty
// 1. Использование неявных параметров для передачи бизнес-данных
def processOrder()(using order: Order): IO[Unit] =  
  // ❌ АНТИПАТТЕРН!
  // Где взять order? Кто его должен установить?

// 2. Изменение поведения через неявные параметры
def calculatePrice(using discountCalculator: DiscountCalculator): Money =  
  // ❌ Опасно!
  // Теперь результат зависит от скрытого контекста.

// 3. Чрезмерное использование для "удобства"
def getUser(using id: UserId, db: Database, logger: Logger): IO[User] =  
  // ❌ Слишком много!
  // Код становится запутанным, сложно понять зависимости.
```

### ✅ Правильные сценарии использования:

```dotty
// 1. Конфигурация и настройки (неизменяемые)
def initializeService(using config: AppConfig): IO[Service] =  // ✅ Правильно

// 2. Сессии и аутентификация (в пределах запроса)
def getCurrentUser(using session: UserSession): IO[User] =  // ✅ Правильно

// 3. Кодеки и сериализаторы (инфраструктура)
def decodeResponse[T](response: String)(using decoder: Decoder[T]): IO[T] =  // ✅ Правильно

// 4. Логирование и телеметрия
def businessOperation(input: Input)(using logger: Logger): IO[Output] =  // ✅ Правильно
```

## Интеграция с другими сервисами: Sections и Goals

Реализация сервисов для `Section` и `Goal` следует точно такой же схеме, что и для `TypeType`. 
Различия минимальны и касаются только:

1. **Типов доменных моделей**: Вместо `TypeType` используются `Section` и `Goal`.
2. **Типов идентификаторов**: `SectionId` и `GoalId` вместо `TypeTypeId`.
3. **Типов параметров создания**: `CreateSection` и `CreateGoal` вместо `CreateTypeType`.

Ссылка на полные реализации: 
[PostgreSQLSectionService](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLSectionService.scala) 
и [PostgreSQLGoalService](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLGoalService.scala).

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

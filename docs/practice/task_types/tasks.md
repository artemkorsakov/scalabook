{%
showPrev = true
prevPage = statuses.html
showNext = true
nextPage = ../db.html
%}

# Задача

Каждая задача имеет набор полей, 
обеспечивающих единообразие, структурированность и удобство отслеживания прогресса. 

## Список полей

| Поле          | Тип данных          | Описание                                                                            |
|---------------|---------------------|-------------------------------------------------------------------------------------|
| `id`          | `TaskId` (`UUID`)   | Уникальный идентификатор задачи                                                     |
| `name`        | `TaskName`          | Название задачи                                                                     |
| `userId`      | `UserId`            | Идентификатор пользователя, которому принадлежит задача                             |
| `taskTypeId`  | `TaskTypeId`        | Идентификатор типа задачи                                                           |
| `status`      | `Status`            | Текущий статус задачи                                                               |
| `sectionId`   | `SectionId`         | Идентификатор раздела, к которому относится задача                                  |
| `goalId`      | `GoalId`            | Идентификатор цели, которую выполняет задача                                        |
| `ordinal`     | `Ordinal`           | Порядковый номер задачи. Используется для определения последовательности выполнения |
| `startDate`   | `Option[LocalDate]` | Дата, когда задача была взята в работу                                              |
| `endDate`     | `Option[LocalDate]` | Планируемая дата завершения                                                         |
| `information` | `String`            | Дополнительная информация о задаче (например, описание, примечания, источники)      |
| `plan`        | `Plan`              | План выполнения задачи — список шагов или действий, необходимых для её завершения   |
| `duration`    | `FiniteDuration`    | Трудоемкость задачи                                                                 |
| `timeSpent`   | `FiniteDuration`    | Затраченное время                                                                   |

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/Task.scala
final case class Task(
    id: TaskId,
    name: TaskName,
    userId: UserId,
    taskTypeId: TaskTypeId,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: Ordinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: Plan,
    duration: FiniteDuration,
    timeSpent: FiniteDuration
)

object Task:
  opaque type TaskId = UUID
  object TaskId:
    def apply(uuid: UUID): TaskId = uuid
    
    def parse(uuid: String): Validated[String, TaskId] =
      Try(UUID.fromString(uuid)).toValidated.leftMap(_.getMessage)

  type TaskName = TaskName.T
  object TaskName
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]
```

Для описания шагов плана создадим отдельные файлы `Plan.scala` и `PlanStep.scala`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/Plan.scala
final case class Plan(steps: List[PlanStep]):
  lazy val progress: Double =
    val completed = steps.count(_.completed).toDouble
    completed / steps.length

object Plan:
  val Empty: Plan = Plan(List.empty)
  
// pdp/src/main/scala/ru/scalabook/pdp/models/Plan.scala
final case class PlanStep(
    number: StepNumber,
    step: StepName,
    completed: Boolean
)

object PlanStep:
  /** Номер пункта плана в формате: n.n.n */
  type StepNumber = StepNumber.T

  object StepNumber
      extends RefinedType[String, Match["^\\d+(?:\\.\\d+)*$"]]:
    given Ordering[StepNumber] =
      @tailrec
      def loop(list1: List[Int], list2: List[Int]): Boolean =
        (list1, list2) match
          case (h1 :: t1, h2 :: t2) if h1 == h2 => loop(t1, t2)
          case (h1 :: t1, h2 :: t2)             => h1 < h2
          case _                                => list1.isEmpty

      Ordering.fromLessThan { case (step1, step2) =>
        loop(
          list1 = step1.value.split("\\.").flatMap(_.toIntOption).toList,
          list2 = step2.value.split("\\.").flatMap(_.toIntOption).toList
        )
      }

  type StepName = StepName.T
  object StepName
      extends RefinedType[String, MinLength[2] & MaxLength[300] & Trimmed]
end PlanStep  
```

## Разбор каждого поля

### `id: TaskId`

- **Описание**: Уникальный идентификатор задачи.
- **Ограничения**: создаем отдельный [непрозрачный тип](https://docs.scala-lang.org/ru/scala3/book/types-opaque-types.html)
  для безопасности типов, чтобы случайно не перепутать идентификаторы разных объектов 
  (например, идентификатор задачи и идентификатор пользователя)
- **Пример**: `b7a98f3e-5f4d-4c6f-916a-51e05f3c0001`

### `name: TaskName`

- **Описание**: Название задачи.
- **Ограничения**:
    - От 2 до 200 символов без начальных или конечных пробелов
- **Пример**: `"Изучить книгу 'Clean Code'"`

### `userId: UserId`

- **Описание**: Идентификатор владельца задачи.
- **Зачем нужно**: Для реализации многопользовательской модели.

### `taskTypeId: TaskTypeId`

- **Описание**: Идентификатор типа задачи.
- **Пример**: `TaskTypeId(1)`

### `status: Status`

- **Описание**: Текущий статус задачи.
- **Возможные значения**:
    - `Status.InProgress` – задача в работе
    - `Status.Planned` – задача запланирована
    - `Status.KnowledgeBase` – задача в базе знаний
    - `Status.Completed` – задача выполнена

### `sectionId: SectionId`

- **Описание**: Идентификатор раздела, к которому относится задача.
- **Пример**: `SectionId(1)`

### `goalId: GoalId`

- **Описание**: Идентификатор цели, которую выполняет задача.
- **Пример**: `GoalId(1)`

### `ordinal: Ordinal`

- **Описание**: Порядковый номер задачи - её приоритет.
- **Ограничения**: Должно быть положительное число (`Positive`)
- **Пример**: `1`, `2`, `3`, ...

### `startDate: Option[LocalDate]`

- **Описание**: Дата начала работы над задачей.
- **Пример**: `Some(2025-01-10)`

### `endDate: Option[LocalDate]`

- **Описание**: Планируемая дата окончания работы над задачей.
- **Пример**: `Some(2025-01-20)`

### `information: String`

- **Описание**: Дополнительная информация о задаче.
- **Пример**: `"Данное учебное пособие — это классический, проверенный временем труд..."`
- **Зачем нужно**: Хранит описание, примечания и любую другую информацию, которая может быть полезна при выполнении задачи.

### `plan: Plan`

- **Описание**: План выполнения задачи — список шагов или действий, необходимых для её завершения.
- **Структура**: `Plan(steps: List[PlanStep])`, где каждый `PlanStep` содержит номер шага (например, `1.2.3`), описание шага и признак выполнения.
- **Пример**: 
  ```scala
  Plan(List(PlanStep("1", "Прочитать первую главу", completed = true), 
              PlanStep("2", "Сделать конспект", completed = false)))
  ```
- **Зачем нужно**: Позволяет разбить задачу на конкретные шаги, отслеживать прогресс (через `plan.progress`) 
  и использовать его как чек-лист. Наличие плана — обязательное условие для взятия задачи в работу или постановки в очередь.

### `duration: FiniteDuration`

- **Описание**: Трудоемкость задачи — сколько времени планируется на неё потратить.
- **Пример**: `FiniteDuration(120, TimeUnit.MINUTES)`

### `timeSpent: FiniteDuration`

- **Описание**: Затраченное время — сколько времени реально было потрачено на задачу.
- **Пример**: `FiniteDuration(95, TimeUnit.MINUTES)`

## Тестирование кода

Одной из самых важных частей разработки программного обеспечения является тестирование кода.
Хорошо написанные тесты дают уверенность в том, что код работает так, как задумано,
помогают ловить регрессии при рефакторинге и служат живой документацией к проекту.
Давайте разберём, как писать тесты на Scala 3 с использованием
библиотеки **Weaver**.

### Что такое Weaver?

[Weaver](https://typelevel.org/weaver-test/) — это лёгкий фреймворк для тестирования,
созданный специально для проектов, использующих **Cats Effect**. Его главные преимущества:

- **Простой и лаконичный API** — тесты выглядят почти как обычный Scala-код.
- **Полная поддержка эффектов** — `IO`, `Resource` и других типов Cats Effect «из коробки».
- **Параллельный запуск тестов** — тесты выполняются параллельно по умолчанию, что ускоряет сборку.
- **Понятные сообщения об ошибках** — при падении теста сразу видно, что именно пошло не так.

### Пример: сортировка шагов плана

Рассмотрим реальный пример из нашего проекта — тестирование сортировки шагов плана.
Каждый шаг имеет номер вида `1`, `1.1`, `1.2.1`, `2.1` и т.д. Такие номера нужно сортировать
естественным образом: `1.2` должно идти раньше `1.10`, хотя как строки они сортируются наоборот.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/models/PlanStepSuite.scala
object PlanStepSuite extends SimpleIOSuite:
  pureTest("StepNumber ordering should sort correctly") {
    val unsorted = List(
      StepNumber("1.10"),
      StepNumber("1.2"),
      StepNumber("1"),
      StepNumber("1.1"),
      StepNumber("2.1"),
      StepNumber("2"),
      StepNumber("1.2.1"),
      StepNumber("1.1.1")
    )

    val expected = List(
      StepNumber("1"),
      StepNumber("1.1"),
      StepNumber("1.1.1"),
      StepNumber("1.2"),
      StepNumber("1.2.1"),
      StepNumber("1.10"),
      StepNumber("2"),
      StepNumber("2.1")
    )

    expect.same(expected, unsorted.sorted)
  }
```

### Соглашения о написании тестов

Чтобы тесты оставались полезными и читаемыми, придерживайтесь следующих правил:

1. **Один тест — одна проверка.** Тест должен отвечать на один вопрос.
   Если проверок несколько, лучше разбить их на отдельные тесты.

2. **Понятные названия.** Название теста должно описывать поведение, а не реализацию.
   Плохо: `test("parse works")`. Хорошо: `test("parse should reject empty string")`.

3. **Структура Given-When-Then.** Разделяйте тест на три части:
  - **Given** — подготовка данных (например, неотсортированный список).
  - **When** — действие (например, `.sorted`).
  - **Then** — проверка (например, `expect.same`).

4. **Изолированность.** Каждый тест должен работать независимо от других.
   Не полагайтесь на порядок выполнения тестов.

5. **Тестируйте крайние случаи.** Помимо «счастливого пути», проверяйте пустые значения,
   границы диапазонов, некорректный ввод.

6. **Тесты как документация.** Хорошо написанные тесты показывают, как использовать код.
   Если вам нужно понять, как работает функция, — посмотрите на её тесты.

Тесты — это не «дополнительная работа», а **неотъемлемая часть кода**.
Пишите их вместе с реализацией, а не после. Это поможет вам быстрее находить ошибки
и сделает рефакторинг безопасным.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

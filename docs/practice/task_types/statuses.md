{%
showPrev = true
prevPage = ../task_types.html
showNext = true
nextPage = localization.html
%}

# Статусы и типы задач

## Статусы задачи

Каждая задача в персональном плане развития имеет статус, который определяет её текущее состояние и этап выполнения.
Статусы позволяют отслеживать прогресс, планировать нагрузку и анализировать эффективность обучения.

Существует три основных статуса задач:

### **В работе**

**Описание:**  

Задачи, над которыми вы сейчас работаете. Это активные задачи, которые находятся в процессе выполнения.

**Характеристики:**

- У задачи заполнены все поля.
- Проставлена **дата взятия в работу** и **планируемая дата окончания**.
- Есть подробный **план выполнения**, который можно актуализировать по мере необходимости.
- Задачи в этом статусе обычно не превышают количество 3-5 штук, так как важно сохранять фокус.

**Пример использования:** 

Если вы по утрам читаете книгу "Clean Code", днем слушаете подкаст о карьере разработчика,
а вечером смотрите обучающее видео — эти три задачи будут находиться в статусе "В работе".

### **Запланировано**

**Описание:** 

Задачи, которые уже детализированы и ожидают своего часа для взятия в работу. Они находятся в очереди на выполнение.

**Характеристики:**

- Все поля задачи заполнены, кроме **даты взятия в работу** и **даты окончания**.
- Есть детальный **план выполнения**, поэтому задачу можно взять в работу в любой момент.
- Эти задачи могут быть использованы как резерв или дополнительная нагрузка, когда у вас появляется свободное время.

**Пример использования:**  

Вы подготовили список из 10 статей, которые хотите прочесть. 
На данный момент вы читаете одну ("В работе"), а остальные находятся в статусе "Запланировано".

### **База знаний**

**Описание:**  

Это задачи, которые пока не имеют детального плана выполнения. 
Они находятся в вашей "базе" как потенциальные цели, но ещё не готовы к началу работы.

**Характеристики:**

- У задачи **не заполнен план выполнения**.
- Нельзя брать в работу, пока не будет сформирован четкий план.
- Представляет собой список возможностей для будущего развития.

**Пример использования:**  

Вы решили, что хотели бы начать свой блог, но пока не знаете, как это сделать. 
Эта задача находится в "Базе знаний", потому что вы ещё не определились с шагами реализации.

### Реализация статусов в коде

Статусы реализуются в виде [перечисления (`enum`)](https://docs.scala-lang.org/scala3/reference/enums/enums.html).
Перечисление (enum) — это специальный тип данных, позволяющий определить фиксированный набор именованных констант.

Создадим файл `Status.scala` в директории `pdp/src/main/scala/ru/scalabook/pdp/model`.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Status.scala
package ru.scalabook.pdp.model

enum Status:
  case InProgress, Planned, KnowledgeBase
```

Таким образом, каждая задача может иметь один из трёх возможных статусов.

## Типы задач

В системе управления индивидуальным планом развития (ИПР) важно иметь возможность гибко описывать разные форматы задач. 
Для этого в проекте используется перечисление `TaskType`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/TaskType.scala
package ru.scalabook.pdp.model

enum TaskType:
  case Book, Article, Podcast, Video, Course, Other
```

`TaskType` — это **перечисление**, которое описывает возможные категории задач. 
Каждый тип задачи связан с конкретным форматом контента или деятельностью, например:

- Изучение книги
- Чтение статьи
- Слушание подкаста
- Просмотр видео
- Прохождение курса
- Выполнение пользовательской задачи (например, ведение блога, участие в open-source)

Для пользовательских форматов задач создадим отдельный `case class UserType` с заданными ограничениями на типы полей.

Для работы с ограничениями на типы данных (например, проверка длины строки, минимального значения числа и т.д.) 
будем использовать библиотеку [Iron](https://github.com/Iltotore/iron), 
которая позволяет создавать [**уточнённые типы** (refined types)](../../libs/refined.md).

Чтобы использовать Iron, нужно добавить её в список зависимостей проекта в файле `Dependencies.scala`:

```dotty
object Dependencies {
  // ...

  object iron {
    private val version = "3.2.1"
    val core = "io.github.iltotore" %% "iron" % version
  }

  val pdpLibs: Seq[ModuleID] =
    Seq(
      cats.catsEffect,
      iron.core
    )

  // ...
}
```

Теперь мы можем использовать функционал Iron для создания безопасных типов данных.

### `UserType`

Создадим файл `UserType.scala` в директории `pdp/src/main/scala/ru/scalabook/pdp/model`.

```dotty
package ru.scalabook.pdp.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.UserType.*

final case class UserType(id: UserTypeId, name: UserTypeName)

object UserType:
  type UserTypeId = UserTypeId.T
  object UserTypeId extends RefinedType[Int, GreaterEqual[0]]

  type UserTypeName = UserTypeName.T
  object UserTypeName
      extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]
```

Это обычный [**case class**](https://docs.scala-lang.org/ru/tour/case-classes.html), 
который содержит два обязательных поля: `id` и `name`:

- `final` — указывает, что класс не может быть расширен (используется для гарантии иммутабельности и безопасности).
- `case class` автоматически предоставляет:
    - конструктор
    - методы `equals`, `hashCode`, `toString`
    - поддержку сопоставления с шаблоном
- `id` — идентификатор пользовательского типа 
    - должен быть ≥ 0
- `name` — название пользовательского типа
    - От 2 до 20 символов
    - Строка должна быть обрезана (без пробелов в начале и конце)

#### Что такое "уточненные типы" (Refined Types)?

Библиотека Iron позволяет создавать типы с ограничениями. 
Вместо простого `Int` или `String` мы создаем типы, которые гарантируют, что данные соответствуют определенным правилам.

##### Пример без уточненных типов (проблемный):

```dotty
// Проблема: никаких проверок!
case class UserType(id: Int, name: String)

// Можно создать некорректные данные:
val bad1 = UserType(-1, "A")         // ID отрицательный, имя слишком короткое
val bad2 = UserType(1, "   test   ") // Лишние пробелы
```

##### Пример с уточненными типами (безопасный):

```dotty
// Решение: типы с ограничениями
case class UserType(id: UserTypeId, name: UserTypeName)

// Некорректные данные не скомпилируются!
val bad1 = UserType(UserTypeId(-1), UserTypeName("name"))
// -- Constraint Error --------------------------------------------------------
// Could not satisfy a constraint for type scala.Int.
// 
// Value: -1
// Message: Should be greater than or equal to 0
// ----------------------------------------------------------------------------

val bad2 = UserType(UserTypeId(1), UserTypeName("   test   "))
// -- Constraint Error --------------------------------------------------------
// Could not satisfy a constraint for type java.lang.String.
// 
// Value: test
// Message: Should have a minimum length of 2 & 
//          Should have a maximum length of 20 & 
//          Should not have leading or trailing whitespaces
// ----------------------------------------------------------------------------
```

#### Зачем нужны opaque types (запечатанные типы)?

[Создание новых типов](https://iltotore.github.io/iron/docs/reference/newtypes.html) через

```dotty
type A = A.T
object A extends RefinedType...
```

создает [запечатанный (opaque)](https://docs.scala-lang.org/ru/scala3/book/types-opaque-types.html) тип `A`.

Проблема, которую они решают:

Без opaque types:

```dotty
type UserId = Int
type ProductId = Int

def getUser(id: UserId): User = ???
def getProduct(id: ProductId): Product = ???

// Проблема: можно перепутать ID!
val userId: UserId = 123
getProduct(userId) // Ошибка, но компилятор не поймет!
```

С opaque types:

```dotty
type UserId = UserId.T
object UserId extends RefinedType[Int, Positive]

type ProductId = ProductId.T
object ProductId extends RefinedType[Int, Positive]

// Теперь нельзя перепутать!
val userId: UserId = UserId(123)
getProduct(userId) 
// Ошибка компиляции:
// Found: (userId : UserId)
// Required: ProductId
```

#### Практические примеры использования

```dotty
// Корректные примеры:
UserType(UserTypeId(5), UserTypeName("Блог"))
UserType(UserTypeId(6), UserTypeName("Open Source"))

// Некорректные примеры (не скомпилируются):
// Ошибка: -1 < 0
UserType(UserTypeId(-1), UserTypeName("Тест"))
// Ошибка: название не подходит по размерам
UserType(UserTypeId(5), UserTypeName("A"))
UserType(
  UserTypeId(0),
  UserTypeName("Очень длинное название которое не подходит")
)
// Ошибка: есть лишние пробелы
UserType(UserTypeId(5), UserTypeName(" тест "))
```

#### Преимущества такого подхода

1. **Безопасность типов** — компилятор проверяет корректность данных
2. **Ясность кода** — типы документируют свои ограничения
3. **Предотвращение ошибок** — многие ошибки обнаруживаются на этапе компиляции
4. **Легкость рефакторинга** — изменения типов автоматически проверяются компилятором

### Итого

- `TaskType` — это перечисление, описывающее типы задач в системе.
    - Оно включает стандартные типы: `Book`, `Article`, `Podcast`, `Video`, `Course`.
- Также предусмотрен `UserType` для пользовательских форматов задач.
- Для обеспечения безопасности данных используются уточнённые типы.
- Такой подход позволяет создавать структурированный, читаемый и надёжный код.

## Разделы задач

В системе управления ИПР важно не только описывать отдельные задачи, 
но и группировать их по тематическим разделам. 
Это помогает лучше структурировать контент и облегчает навигацию по задачам.

### Зачем нужны разделы?

**Разделы** — это категоризации задач, которые позволяют:

- **Группировать однотипные задачи** (например, все задачи по алгоритмам).
- **Упрощать поиск и фильтрацию** задач в интерфейсе.
- **Создавать логическую последовательность** в обучении (например, сначала изучить основы программирования, потом — продвинутые алгоритмы).
- **Оценивать прогресс** по конкретной теме.

Примеры типичных разделов:

- **Алгоритмы**
- **Функциональное программирование**
- **Soft skills**
- **Базы данных**
- **Web-разработка**
- **Проектирование архитектур**
- **Тестирование**

### Реализация раздела в коде

Чтобы реализовать разделы в проекте, мы создаем класс `Section`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Section.scala
package ru.scalabook.pdp.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.Section.*

final case class Section(id: SectionId, name: SectionName)

object Section:
  type SectionId = SectionId.T
  object SectionId extends RefinedType[Int, GreaterEqual[0]]

  type SectionName = SectionName.T
  object SectionName
      extends RefinedType[String, MinLength[2] & MaxLength[100] & Trimmed]
```

### Пример использования

```dotty
Section(SectionId(1), SectionName("Алгоритмы"))
Section(SectionId(2), SectionName("Soft skills"))
Section(SectionId(3), SectionName("Web-разработка"))
```

### Почему это важно?

Использование разделов дает следующие преимущества:

- **Логическая организация задач.** Вы видите, какая задача относится к какой теме.
- **Удобство фильтрации.** Можно легко найти все задачи по определённому направлению.
- **Поддержка сложных пользовательских интерфейсов.** Например, в веб-приложении можно отображать задачи по разделам в виде карточек.
- **Масштабируемость.** Добавление новых разделов не требует глобальных изменений в коде.

## Глобальные цели

Важно не только иметь задачи, но и определять их цели. 
**Цели** — это мета-описания, которые помогают понять, зачем выполнять ту или иную задачу 
и как она связана с моим профессиональным ростом.

### Зачем нужны цели задач?

**Цели задач** — это описание ожидаемых результатов от выполнения задачи, которые позволяют:

- **Объяснять ценность задачи** — зачем её выполнять, какие навыки развивает.
- **Мотивировать пользователя** — показывать, как задача связана с карьерным ростом.
- **Связывать задачи с компетенциями** — определять, какие именно навыки развивает задача.
- **Оценивать релевантность задачи** — помогает понять, подходит ли задача для текущего уровня развития.

Примеры типичных целей:

- **Научиться писать чистый и поддерживаемый код**
- **Освоить принципы функционального программирования**
- **Развить навыки работы с базами данных**
- **Приобрести опыт проектирования распределённых систем**
- **Улучшить навыки командной работы и коммуникации**
- **Понять основы тестирования ПО**

### Реализация цели в коде

Чтобы реализовать цели в проекте, мы создаем класс `Goal`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Goal.scala
package ru.scalabook.pdp.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.Goal.*

final case class Goal(id: GoalId, name: GoalName)

object Goal:
  type GoalId = GoalId.T
  object GoalId extends RefinedType[Int, GreaterEqual[0]]

  type GoalName = GoalName.T
  object GoalName
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]
```

### Пример использования

```dotty
Goal(GoalId(1), GoalName("Освоить основы алгоритмического мышления"))
Goal(GoalId(2), GoalName("Научиться проектировать REST API"))
Goal(GoalId(3), GoalName("Развить навыки работы с PostgreSQL"))
Goal(GoalId(4), GoalName("Приобрести опыт код-ревью"))
Goal(GoalId(5), GoalName("Понять принципы TDD"))
```

### Связь целей с задачами

На практике цели обычно связываются с задачами через отношения "один-ко-многим":

**Одна цель** может быть связана с **несколькими задачами** 
(например, цель "Освоить функциональное программирование" может включать 
задачи по изучению монад, функторов и чистых функций).

Использование целей задач дает следующие преимущества:

- **Прозрачность обучения** — пользователь понимает, какие навыки он развивает, выполняя задачу.
- **Стратегическое планирование** — можно строить индивидуальные траектории развития, опираясь на цели.
- **Измеримость прогресса** — можно отслеживать прогресс не только по выполненным задачам, но и по достигнутым целям.
- **Гибкость системы** — цели позволяют адаптировать задачи под разные карьерные пути и специализации.
- **Мотивационный аспект** — понимание "зачем" повышает вовлеченность пользователя.

Цели превращают задачи из простых упражнений в осмысленные шаги профессионального развития, 
делая систему ИПР более эффективной и персонализированной.

## Общие поля задачи

Каждая задача имеет набор общих полей, 
обеспечивающих единообразие, структурированность и удобство отслеживания прогресса. 
Эти поля определяют базовую информацию о задаче и могут быть использованы для всех типов задач, 
независимо от их формата (книга, статья, подкаст и т.д.).

### Список общих полей

| Поле          | Тип данных          | Описание                                                                            |
|---------------|---------------------|-------------------------------------------------------------------------------------|
| `id`          | `UUID`              | Уникальный идентификатор задачи                                                     |
| `name`        | `TaskName`          | Название задачи                                                                     |
| `taskType`    | `TaskType`          | Идентификатор типа задачи (из перечисления `TaskType`)                              |
| `status`      | `Status`            | Текущий статус задачи: `InProgress`, `Planned`, `KnowledgeBase`                     |
| `sectionId`   | `SectionId`         | Идентификатор раздела, к которому относится задача                                  |
| `goalId`      | `GoalId`            | Идентификатор цели, которую выполняет задача                                        |
| `ordinal`     | `TaskOrdinal`       | Порядковый номер задачи. Используется для определения последовательности выполнения |
| `startDate`   | `Option[LocalDate]` | Дата, когда задача была взята в работу                                              |
| `endDate`     | `Option[LocalDate]` | Планируемая дата завершения                                                         |
| `information` | `String`            | Дополнительная информация о задаче (например, цели, примечания, источники)          |
| `plan`        | `List[TaskStep]`    | План выполнения задачи — список шагов или действий, необходимых для её завершения   |
| `isArchived`  | `Boolean`           | Флаг завершенности задачи                                                           |

### Реализация через `sealed trait Task`

Для унификации всех типов задач в проекте создаётся 
общий [запечатанный трейт](https://docs.scala-lang.org/ru/tour/pattern-matching.html) `Task`, 
который содержит все общие поля. 
Он определяет интерфейс, который должны реализовывать конкретные типы задач (книги, статьи, подкасты и т.д.).

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
package ru.scalabook.pdp.model

import ru.scalabook.pdp.model.Goal.GoalId
import ru.scalabook.pdp.model.Section.SectionId

import java.time.LocalDate
import java.util.UUID

sealed trait Task:
  val id: UUID
  val name: TaskName
  val taskType: TaskType
  val status: Status
  val sectionId: SectionId
  val goalId: GoalId
  val ordinal: TaskOrdinal
  val startDate: Option[LocalDate]
  val endDate: Option[LocalDate]
  val information: String
  val plan: List[TaskStep]
  val isArchived: Boolean
```

Для описания типов создадим отдельный `package object`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/package.scala
package ru.scalabook.pdp

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

package object model {
  type TaskName = TaskName.T
  object TaskName
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]

  type TaskOrdinal = TaskOrdinal.T
  object TaskOrdinal extends RefinedType[Int, Positive]

  type TaskStep = TaskStep.T
  object TaskStep
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]
}
```

### Разбор каждого поля

#### `id: UUID`

- **Описание**: Уникальный идентификатор задачи.
- **Пример**: `b7a98f3e-5f4d-4c6f-916a-51e05f3c0001`
- **Зачем нужно**: Уникальный ID позволяет однозначно идентифицировать задачу в системе, 
  особенно при работе с базами данных или API.

#### `name: TaskName`

- **Описание**: Название задачи.
- **Ограничения**:
    - От 2 до 200 символов без начальных или конечных пробелов
- **Пример**: `"Изучить книгу 'Clean Code'"`
- **Зачем нужно**: Название помогает быстро понять, о чём задача.

#### `taskType: TaskType`

- **Описание**: Тип задачи.
- **Пример**: `TaskType.Book`, `TaskType.Other`
- **Зачем нужно**: Тип задачи определяет, как она будет обрабатываться, где хранится 
  и какие дополнительные поля требуются для заполнения.

#### `status: Status`

- **Описание**: Текущий статус задачи.
- **Возможные значения**:
    - `Status.InProgress` – задача в работе
    - `Status.Planned` – задача запланирована
    - `Status.KnowledgeBase` – задача в базе знаний
- **Зачем нужно**: Статус помогает отслеживать прогресс и планировать дальнейшие действия.

#### `sectionId: SectionId`

- **Описание**: Идентификатор раздела, к которому относится задача.
- **Пример**: `SectionId(1)`
- **Зачем нужно**: Разделы позволяют логически группировать задачи, что упрощает управление обучением и фильтрацию.

#### `goalId: GoalId`

- **Описание**: Идентификатор цели, которую выполняет задача.
- **Пример**: `GoalId(1)`
- **Зачем нужно**: Цели помогают определить, зачем выполняется каждая задача.

#### `ordinal: TaskOrdinal`

- **Описание**: Порядковый номер задачи - её приоритет.
- **Ограничения**: Должно быть положительное число (`Positive`)
- **Пример**: `1`, `2`, `3`, ...
- **Зачем нужно**: Задает порядок выполнения задач.

#### `startDate: Option[LocalDate]`

- **Описание**: Дата начала работы над задачей.
- **Пример**: `Some(2025-01-10)`
- **Зачем нужно**: Отмечает начало этапа выполнения задачи.

#### `endDate: Option[LocalDate]`

- **Описание**: Планируемая дата окончания работы над задачей.
- **Пример**: `Some(2025-01-20)`
- **Зачем нужно**: Показывает, когда задачу планируется завершить.

#### `information: String`

- **Описание**: Дополнительная информация о задаче.
- **Пример**: `"Данное учебное пособие — это классический, проверенный временем труд..."`
- **Зачем нужно**: Хранит описание, примечания и любую другую информацию, которая может быть полезна при выполнении задачи.

#### `plan: List[TaskStep]`

- **Описание**: Список шагов для выполнения задачи.
- **Пример**:
  ```dotty
  List("Прочитать первую главу", "Сделать заметки", "Выполнить упражнения")
  ```
- **Ограничения**:
    - Каждый шаг должен быть от 2 до 200 символов без начальных и конечных пробелов
- **Зачем нужно**: План помогает структурировать процесс выполнения задачи и контролировать прогресс.

#### `isArchived: Boolean`

- **Описание**: Флаг, указывающий, завершена ли задача.
- **Пример**: `true`, если задача завершена, в ином случае - `false`.
- **Зачем нужно**: Завершенные задачи не отображаются в списке активных задач, 
  но полезны для истории, расчета статистики и прогноза.

### Почему это важно?

Использование общих полей в классе `Task` дает следующие преимущества:

- **Единообразие** — все задачи имеют одинаковый набор обязательных полей, что упрощает их обработку и хранение.
- **Гибкость** — можно легко добавлять новые типы задач, расширяя функционал без изменения базовой структуры.
- **Безопасность данных** — благодаря уточненным типам, мы можем гарантировать, 
  что данные соответствуют требованиям (например, длина строки, минимальное значение числа и т.д.).
- **Удобство анализа** — наличие полей `startDate`, `endDate`, `order`, `section` позволяет строить графики, 
  анализировать прогресс и составлять отчёты.

## Конкретные типы задач

В системе управления персональным планом развития помимо общих полей, 
каждая задача может иметь **дополнительные поля**, специфичные для её формата. 
Это позволяет более точно описать содержание задачи и учитывать особенности её выполнения.

### Книги

Книги — это один из самых популярных источников знаний, особенно при изучении фундаментальных тем. 
Они подходят для долгосрочного изучения и глубокого погружения в тему.

| Поле      | Тип данных | Описание                                                      |
|-----------|------------|---------------------------------------------------------------|
| `authors` | `Authors`  | Автор(-ы) книги                                               |
| `pages`   | `Pages`    | Количество страниц в книге (должно быть положительным числом) |

> 💡 Пример: `"Robert C. Martin"`, `464`

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
final case class BookTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: TaskOrdinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    isArchived: Boolean,
    authors: Authors,
    pages: Pages
) extends Task:
  val taskType: TaskType = TaskType.Book

// pdp/src/main/scala/ru/scalabook/pdp/model/package.scala
package object model {
  ...
  
  type Authors = Authors.T
  object Authors extends RefinedType[String, MinLength[1] & Trimmed]
  
  type Pages = Pages.T
  object Pages extends RefinedType[Int, Positive]
  
  ...
}
```

### Статьи

Статьи позволяют быстро и детально разобраться в конкретной теме. 
Они часто публикуются в блогах, на технических сайтах или в журналах.

| Поле       | Тип данных       | Описание                     |
|------------|------------------|------------------------------|
| `authors`  | `Authors`        | Автор(-ы) статьи             |
| `link`     | `Link`           | Ссылка на статью             |
| `duration` | `FiniteDuration` | Приблизительное время чтения |

> 💡 Пример: `"John Doe"`, `https://example.com/article`, `15 минут`

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
final case class ArticleTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: TaskOrdinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    isArchived: Boolean,
    authors: Authors,
    link: Link,
    duration: FiniteDuration
) extends Task:
  val taskType: TaskType = TaskType.Article
  
// pdp/src/main/scala/ru/scalabook/pdp/model/package.scala
package object model {
  ...
  
  type Link = Link.T
  object Link extends RefinedType[String, MinLength[1] & Trimmed]
  
  ...
}  
```

### Подкасты

Подкасты — отличный способ развивать soft skills, получить мотивацию и узнать о карьерных возможностях. 
Они удобны для прослушивания во время поездки, прогулки или отдыха.

| Поле              | Тип данных       | Описание                        |
|-------------------|------------------|---------------------------------|
| `authors`         | `Authors`        | Имя автора или команды подкаста |
| `link`            | `Link`           | Ссылка на подкаст               |
| `episodes`        | `Episodes`       | Количество эпизодов             |
| `episodeDuration` | `FiniteDuration` | Длительность одного эпизода     |
| `duration`        | `FiniteDuration` | Общая длительность подкаста     |

> 💡 Пример: `"The Coding Podcast Team"`, `https://podcasts.apple.com/episode`, `10`, `30 минут`, `300 минут`

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
final case class PodcastTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: TaskOrdinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    isArchived: Boolean,
    authors: Authors,
    link: Link,
    episodes: Episodes,
    episodeDuration: FiniteDuration
) extends Task:
  val taskType: TaskType = TaskType.Podcast

  def duration: FiniteDuration =
    val ms = episodeDuration.toMillis * episodes.value.toLong
    FiniteDuration(ms, MILLISECONDS)
end PodcastTask

// pdp/src/main/scala/ru/scalabook/pdp/model/package.scala
package object model {
  ...
  
  type Episodes = Episodes.T
  object Episodes extends RefinedType[Int, Positive]
  
  ...
} 
```

> - ✅ Общая длительность подкаста вычисляется на основе количества эпизодов и их продолжительности.

### Видео

Видео объединяет визуальную информацию и живой контекст. 
Это особенно полезно для понимания сложных концепций и участия в онлайн-мероприятиях.

| Поле       | Тип данных       | Описание                                  |
|------------|------------------|-------------------------------------------|
| `authors`  | `Authors`        | Имя докладчика(-ов) или организатора(-ов) |
| `platform` | `Platform`       | Платформа (YouTube, Vimeo и т.д.)         |
| `link`     | `Link`           | Ссылка на видео                           |
| `duration` | `FiniteDuration` | Длительность видео                        |

> 💡 Пример: `"Jane Smith"`, `"YouTube"`, `https://www.youtube.com/watch?v=xyz123`, `45 минут`

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
final case class VideoTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: TaskOrdinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    isArchived: Boolean,
    authors: Authors,
    platform: Platform,
    link: Link,
    duration: FiniteDuration
) extends Task:
  val taskType: TaskType = TaskType.Video
  
// pdp/src/main/scala/ru/scalabook/pdp/model/package.scala
package object model {
  ...
  
  type Platform = Platform.T
  object Platform extends RefinedType[String, MinLength[1] & Trimmed]
  
  ...
}   
```

### Курсы

Курсы — самый эффективный способ развить практические навыки. 
Они обычно включают теорию, упражнения и проекты, что делает обучение более активным и результативным.

| Поле       | Тип данных       | Описание                                           |
|------------|------------------|----------------------------------------------------|
| `authors`  | `Authors`        | Имя преподавателя(-ей) или название платформы      |
| `platform` | `Platform`       | Образовательная платформа (Udemy, Coursera и т.д.) |
| `link`     | `Link`           | Ссылка на курс                                     |
| `duration` | `FiniteDuration` | Общая длительность курса                           |

> 💡 Пример: `"Т Банк"`, `"Stepik"`, `https://stepik.org/course/16243/syllabus`, `720 минут`

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
final case class CourseTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: TaskOrdinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    isArchived: Boolean,
    authors: Authors,
    platform: Platform,
    link: Link,
    duration: FiniteDuration
) extends Task:
  val taskType: TaskType = TaskType.Course
```

### Пользовательский тип

Пользовательский тип задачи (`UserTask`) предназначен для тех форматов, 
которые не могут быть описаны стандартными категориями. 
Например:

- Ведение собственного блога
- Участие в open-source проектах
- Работа над pet-проектом
- Выступление с докладом

| Поле       | Тип данных       | Описание                                        |
|------------|------------------|-------------------------------------------------|
| `duration` | `FiniteDuration` | Предполагаемое или фактическое время выполнения |

> 💡 Пример: `5 часов`

```dotty
final case class UserTask(
    id: UUID,
    name: TaskName,
    userTypeId: UserTypeId,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: TaskOrdinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    isArchived: Boolean,
    duration: FiniteDuration
) extends Task:
  val taskType: TaskType = TaskType.Other
```

> - ✅ `UserTask` сам указывает свой тип задачи `userTypeId`.

## Зачем это нужно?

Использование конкретных типов задач обеспечивает следующие преимущества:

### **Структурированный подход к обучению**

Каждый тип задачи имеет чёткое описание, что делает её выполнение более понятным и управляемым. 
Например, если вы работаете с курсом, вы сразу видите, где он находится, 
сколько времени займёт и какие шаги нужно выполнить.

### **Отслеживание прогресса**

С помощью статусов и дополнительных полей вы можете видеть:

- какие задачи уже выполнены
- какие находятся в работе
- какие ещё только планируются

Это помогает лучше контролировать своё обучение и распределять нагрузку.

### **Гибкость в планировании**

Вы можете легко менять порядок задач, добавлять новые или переносить старые в очередь. 
Например, если у вас появилось свободное время, вы можете взять из "Запланировано" задачу, которая вам интересна.

### **Улучшение мотивации**

Когда вы видите, как продвигаетесь по списку задач, это даёт чувство достижения и желание продолжать развитие. 
Особенно важно это при работе с долгосрочными целями.

### **Анализ эффективности**

Вы можете оценить, какие типы задач приносят больше пользы, а какие требуют доработки. 
Например, если вы замечаете, что курсы дают лучший результат, чем книги, вы можете скорректировать свой план обучения.

## Пример использования

```dotty
val book: BookTask =
  BookTask(
    id = UUID.randomUUID(),
    name = TaskName("Scala Programming"),
    status = Status.InProgress,
    sectionId = SectionId(1),
    goalId = GoalId(1),
    ordinal = TaskOrdinal(1),
    startDate = Some(LocalDate.of(2024, 1, 1)),
    endDate = Some(LocalDate.of(2024, 12, 31)),
    information = "Best practices and advanced features",
    plan = List(TaskStep("Intro"), TaskStep("Basics"), TaskStep("OOP")),
    isArchived = false,
    authors = Authors("John Doe"),
    pages = Pages(500)
  )

val userTask: UserTask = UserTask(
  id = UUID.randomUUID(),
  name = TaskName("Создать презентацию по проекту"),
  userTypeId = UserTypeId(1),
  status = Status.InProgress,
  sectionId = SectionId(2),
  goalId = GoalId(1),
  ordinal = TaskOrdinal(6),
  startDate = Some(LocalDate.of(2023, 10, 1)),
  endDate = Some(LocalDate.of(2023, 10, 15)),
  information = "Создание презентации для внутреннего совещания.",
  plan = List(
    TaskStep("Собрать данные"),
    TaskStep("Создать слайды"),
    TaskStep("Проверить дизайн")
  ),
  isArchived = true,
  duration = FiniteDuration(3, HOURS)
)
```

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

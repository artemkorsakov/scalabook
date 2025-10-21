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
- Задачи в этом статусе обычно не превышают количество 5–7 штук, так как важно сохранять фокус.

**Пример использования:** 

Если вы сейчас читаете книгу "Clean Code", слушаете подкаст о карьере разработчика 
и смотрите обучающее видео — эти три задачи будут находиться в статусе "В работе".

### **Запланировано**

**Описание:** 

Задачи, которые уже подготовлены и ожидают своего времени для начала. Они находятся в очереди на выполнение.

**Характеристики:**

- Все поля задачи заполнены, кроме **даты взятия в работу** и **даты окончания**.
- Есть детальный **план выполнения**, поэтому задачу можно взять в работу в любой момент.
- Эти задачи могут быть использованы как резерв или дополнительная нагрузка, когда у вас появляется свободное время.

**Пример использования:**  

Вы подготовили список из 10 статей, которые хотите прочесть в будущем. 
В данный момент вы занимаетесь только двумя, а остальные находятся в статусе "Запланировано".

### **База знаний**

**Описание:**  

Это задачи, которые пока не имеют детального плана выполнения. 
Они находятся в вашей "базе" как потенциальные цели, но ещё не готовы к началу работы.

**Характеристики:**

- У задачи **не заполнен план выполнения**.
- Нельзя брать в работу, пока не будет сформирован четкий путь выполнения.
- Представляет собой список возможностей для будущего развития.

**Пример использования:**  

Вы решили, что хотели бы начать свой блог, но пока не знаете, как это сделать. 
Эта задача находится в "Базе знаний", потому что вы ещё не определились с шагами реализации.

### Реализация статусов в коде

Статусы реализуются в виде [перечисления (`enum`)](https://docs.scala-lang.org/scala3/reference/enums/enums.html):

```dotty
// src/main/scala/ru/scalabook/pdp/model/Status.scala
package ru.scalabook.pdp.model

enum Status:
  case InProgress, Planned, KnowledgeBase
```

Таким образом, каждая задача может иметь один из трёх возможных статусов.

Перечисление (enum) — это специальный тип данных, позволяющий определить фиксированный набор именованных констант. 

## Типы задач

В системе управления персональным планом развития (ИПР) важно иметь возможность гибко описывать разные форматы задач. 
Для этого в проекте используется перечисление `TaskType`, которое определяет типы задач, доступные по умолчанию, 
а также позволяет добавлять пользовательские типы для нестандартных форматов.

### Что такое `TaskType`?

`TaskType` — это **перечисление**, которое описывает возможные категории задач. 
Каждый тип задачи связан с конкретным форматом контента или деятельностью, например:

- Изучение книги
- Чтение статьи
- Слушание подкаста
- Просмотр видео
- Прохождение курса
- Выполнение пользовательской задачи (например, ведение блога, участие в open-source)

### Использование библиотеки Iron

Для работы с ограничениями на типы данных (например, проверка длины строки, минимального значения числа и т.д.) 
мы используем библиотеку [Iron](https://github.com/Iltotore/iron), 
которая позволяет создавать [**уточнённые типы** (refined types)](../../libs/refined.md).

#### Добавление зависимости

Чтобы использовать Iron, нужно добавить её в список зависимостей проекта. 
Это делается в файле `Dependencies.scala`:

```dotty
object Dependencies {
  // ... другие зависимости ...

  object iron {
    private val version = "3.2.0"
    val core = "io.github.iltotore" %% "iron" % version
  }

  val rootLibs: Seq[ModuleID] =
    Seq(
      cats.catsEffect,
      iron.core
    )

  // ... остальные части файла ...
}
```

Теперь мы можем использовать функционал Iron для создания безопасных типов данных.

### Подробное объяснение перечисления `TaskType`

Создадим файл `TaskType.scala` в директории `src/main/scala/ru/scalabook/pdp/model`.

```dotty
// src/main/scala/ru/scalabook/pdp/model/TaskType.scala
package ru.scalabook.pdp.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.TaskType.*

import scala.language.implicitConversions

enum TaskType(val id: TaskTypeId):
  case Book    extends TaskType(PredefinedTaskTypeId(0))
  case Article extends TaskType(PredefinedTaskTypeId(1))
  case Podcast extends TaskType(PredefinedTaskTypeId(2))
  case Video   extends TaskType(PredefinedTaskTypeId(3))
  case Course  extends TaskType(PredefinedTaskTypeId(4))

  case UserType(override val id: UserTaskTypeId, name: TaskTypeName)
      extends TaskType(id)
end TaskType

object TaskType:
  type PredefinedTaskTypeId = PredefinedTaskTypeId.T
  object PredefinedTaskTypeId
      extends RefinedType[Int, GreaterEqual[0] & Less[5]]

  type UserTaskTypeId = UserTaskTypeId.T
  object UserTaskTypeId extends RefinedType[Int, GreaterEqual[5]]

  type TaskTypeId = PredefinedTaskTypeId | UserTaskTypeId

  type TaskTypeName = TaskTypeName.T
  object TaskTypeName
      extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]
end TaskType
```

#### Объявление перечисления

```dotty
enum TaskType(val id: TaskTypeId):
```

Здесь:

- `enum` — ключевое слово для создания перечисления
- `TaskType` — название нашего перечисления
- `val id: TaskTypeId` — каждый вариант перечисления имеет идентификатор определенного типа

#### Предопределенные типы задач

Это стандартные, заранее определенные типы задач:

```dotty
case Book    extends TaskType(PredefinedTaskTypeId(0)) // Книга
case Article extends TaskType(PredefinedTaskTypeId(1)) // Статья
case Podcast extends TaskType(PredefinedTaskTypeId(2)) // Подкаст
case Video   extends TaskType(PredefinedTaskTypeId(3)) // Видео
case Course  extends TaskType(PredefinedTaskTypeId(4)) // Курс
```

#### Пользовательский тип задачи

```dotty
case UserType(override val id: UserTaskTypeId, name: TaskTypeName)
    extends TaskType(id)
```

Это специальный случай, который позволяет пользователям создавать свои собственные типы задач:

- `id` — идентификатор пользовательского типа (должен быть ≥ 5)
- `name` — название пользовательского типа

#### PredefinedTaskTypeId — ID предопределенных типов

```dotty
type PredefinedTaskTypeId = PredefinedTaskTypeId.T
object PredefinedTaskTypeId
    extends RefinedType[Int, GreaterEqual[0] & Less[5]]
```

**Что это значит:**

- Это целое число (`Int`)
- Допустимые значения: от 0 до 4

#### UserTaskTypeId — ID пользовательских типов

```dotty
type UserTaskTypeId = UserTaskTypeId.T
object UserTaskTypeId extends RefinedType[Int, GreaterEqual[5]]
```

**Что это значит:**

- Это целое число (`Int`)
- Должно быть ≥ 5
- Это гарантирует, что пользовательские ID не пересекаются с предопределенными

#### TaskTypeId — общий тип для ID

```dotty
type TaskTypeId = PredefinedTaskTypeId | UserTaskTypeId
```

[Объединяет оба типа ID](https://docs.scala-lang.org/ru/scala3/book/types-union.html) в один общий тип.

#### TaskTypeName — название типа задачи

```dotty
type TaskTypeName = TaskTypeName.T
object TaskTypeName
    extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]
```

**Ограничения для названия:**

- Минимальная длина: 2 символа
- Максимальная длина: 20 символов
- Строка должна быть обрезана (без пробелов в начале и конце)

#### Что такое "уточненные типы" (Refined Types)?

Библиотека Iron позволяет создавать типы с ограничениями. 
Вместо простого `Int` или `String` мы создаем типы, которые гарантируют, что данные соответствуют определенным правилам.

##### Пример без уточненных типов (проблемный):

```dotty
// Проблема: никаких проверок!
case class TaskType(id: Int, name: String)

// Можно создать некорректные данные:
val bad1 = TaskType(-1, "A")         // ID отрицательный, имя слишком короткое
val bad2 = TaskType(1, "   test   ") // Лишние пробелы
```

##### Пример с уточненными типами (безопасный):

```dotty
// Решение: типы с ограничениями
case class TaskType(id: TaskTypeId, name: TaskTypeName)

// Некорректные данные не скомпилируются!
// val bad1 = TaskType(-1, "A")         // Ошибка компиляции!
// val bad2 = TaskType(1, "   test   ") // Ошибка компиляции!
```

#### Зачем нужны opaque types (запечатанные типы)?

[Создание новых типов](https://iltotore.github.io/iron/docs/reference/newtypes.html) через

```dotty
type A = A.T
object A extends RefinedType...
```

создает [запечатанный (opaque)](https://docs.scala-lang.org/ru/scala3/book/types-opaque-types.html) тип `A`.

##### Проблема, которую они решают:

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
// getProduct(userId) // Ошибка компиляции! Типы разные
```

#### Практические примеры использования

##### Создание предопределенных типов:

```dotty
// Это работает, т.к. значения корректны
val bookType: TaskType = TaskType.Book
val articleType: TaskType = TaskType.Article
```

##### Создание пользовательских типов:

```dotty
// Корректные примеры:
UserType(UserTaskTypeId(5), TaskTypeName("Блог"))
UserType(UserTaskTypeId(6), TaskTypeName("Open Source"))

// Некорректные примеры (не скомпилируются):
// Ошибка: ID=3 < 5
UserType(UserTaskTypeId(3), TaskTypeName("Тест")) 
// Ошибка: название слишком короткое  
UserType(UserTaskTypeId(5), TaskTypeName("A"))      
UserType(UserTaskTypeId(5), TaskTypeName("Очень длинное название которое не подходит"))
// Ошибка: есть лишние пробелы
UserType(UserTaskTypeId(5), TaskTypeName(" тест ")) 
```

#### Пример ошибки при неверном использовании

Если попытаться создать `UserType` с некорректными данными, компилятор выдаст ошибку.

Например, `val invalid = UserType(UserTaskTypeId(-1), TaskTypeName(" blog "))` приведёт к следующим ошибкам:

- `-1` не проходит ограничение `GreaterEqual[0]`, при компиляции выдает:
  "Could not satisfy a constraint for type scala. Int. Value: -1 Message: Should be greater than or equal to 0"
- `" blog "` не проходит ограничения `MinLength[2]`, `MaxLength[20]` и `Trimmed`, при компиляции выдает:
  "Could not satisfy a constraint for type java. lang. String.
  Value: blog Message: Should have a minimum length of 2 & Should have a maximum length of 20
  & Should not have leading or trailing whitespaces"

Компиляция завершится с сообщением об ошибке, что помогает избежать некорректных значений на этапе разработки.

#### Преимущества такого подхода

1. **Безопасность типов** — компилятор проверяет корректность данных
2. **Ясность кода** — типы документируют свои ограничения
3. **Предотвращение ошибок** — многие ошибки обнаруживаются на этапе компиляции
4. **Легкость рефакторинга** — изменения типов автоматически проверяются компилятором

### Итоги

- `TaskType` — это перечисление, описывающее типы задач в системе.
- Оно включает стандартные типы: `Book`, `Article`, `Podcast`, `Video`, `Course`.
- Также предусмотрен `UserType` для пользовательских форматов задач.
- Для обеспечения безопасности данных используются уточнённые типы.
- Такой подход позволяет создавать структурированный, читаемый и надёжный код.

## Разделы задачи

В системе управления ИПР важно не только описывать отдельные задачи, 
но и группировать их по тематическим разделам. 
Это помогает лучше структурировать контент, облегчает навигацию по задачам 
и позволяет видеть прогресс в рамках определённой области знаний.

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
- **Карьерный рост**
- **Проектирование архитектур**
- **Тестирование и CI/CD**

### Реализация раздела в коде

Чтобы реализовать разделы в проекте, мы создаем класс `Section` в файле:

```dotty
// src/main/scala/ru/scalabook/pdp/model/Section.scala
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
end Section
```

Этот класс будет использоваться для хранения информации о каждом разделе. Он состоит из двух полей:

#### `id`: Уникальный идентификатор раздела

```dotty
type SectionId = SectionId.T
object SectionId extends RefinedType[Int, GreaterEqual[0]]
```

- `GreaterEqual[0]` гарантирует, что `id` не может быть меньше 0 (например, `-1`, `null` и т.п.).

#### `name`: Название раздела

```dotty
type SectionName = SectionName.T
object SectionName
    extends RefinedType[String, MinLength[2] & MaxLength[100] & Trimmed]
```

- Строка, которая:
    - имеет **минимум 2 символа**
    - имеет **максимум 100 символов**
    - **не содержит лишних пробелов** в начале или конце

> ✅ Пример допустимого значения: `"Функциональное программирование"`
> 
> ❌ Пример недопустимого значения: `"w"`, `"   Алгоритмы "` (из-за пробелов), 
> `"Это очень длинное название раздела, которое явно превышает 100 символов, потому что здесь 101 символ."`

### Класс `Section`

После определения типов создаём сам класс `Section`:

```dotty
final case class Section(id: SectionId, name: SectionName)
```

- Это обычный [**case class**](https://docs.scala-lang.org/ru/tour/case-classes.html), который содержит два обязательных поля: `id` и `name`.
- `final` — указывает, что класс не может быть расширен (используется для гарантии иммутабельности и безопасности).
- `case class` автоматически предоставляет:
    - конструктор
    - методы `equals`, `hashCode`, `toString`
    - поддержку сопоставления с шаблоном

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

### Итоги

- Разделы (`Section`) — это важная часть системы управления ИПР.
- Они позволяют группировать задачи по темам и упрощают управление обучением.
- Реализуются через `case class` с использованием уточнённых типов.
- Каждый раздел имеет уникальный идентификатор и проверенное имя.

## Общие поля задачи

В системе управления персональным планом развития каждая задача имеет набор **общих полей**, 
которые обеспечивают единообразие, структурированность и удобство отслеживания прогресса. 
Эти поля определяют базовую информацию о задаче и могут быть использованы для всех типов задач, 
независимо от их формата (книга, статья, подкаст и т.д.).

### Список общих полей

| Поле          | Тип данных          | Описание                                                                             |
|---------------|---------------------|--------------------------------------------------------------------------------------|
| `id`          | `UUID`              | Уникальный идентификатор задачи.                                                     |
| `name`        | `TaskName`          | Название задачи.                                                                     |
| `taskTypeId`  | `TaskTypeId`        | Идентификатор типа задачи (из перечисления `TaskType`).                              |
| `status`      | `Status`            | Текущий статус задачи: `InProgress`, `Planned`, `KnowledgeBase`.                     |
| `sectionId`   | `SectionId`         | Идентификатор раздела, к которому относится задача.                                  |
| `order`       | `TaskOrder`         | Порядковый номер задачи. Используется для определения последовательности выполнения. |
| `startDate`   | `Option[LocalDate]` | Дата, когда задача была взята в работу.                                              |
| `endDate`     | `Option[LocalDate]` | Планируемая дата завершения.                                                         |
| `information` | `String`            | Дополнительная информация о задаче (например, цели, примечания, источники).          |
| `plan`        | `List[TaskStep]`    | План выполнения задачи — список шагов или действий, необходимых для её завершения.   |


### Реализация через `sealed trait Task`

Для унификации всех типов задач в проекте создаётся 
общий [запечатанный трейт](https://docs.scala-lang.org/ru/tour/pattern-matching.html) `Task`, 
который содержит все общие поля. 
Он определяет интерфейс, который должны реализовывать конкретные типы задач (книги, статьи, подкасты и т.д.).

#### Код реализации:

```dotty
// src/main/scala/ru/scalabook/pdp/model/Task.scala
package ru.scalabook.pdp.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.BookTask.*
import ru.scalabook.pdp.model.Section.SectionId
import ru.scalabook.pdp.model.Task.*
import ru.scalabook.pdp.model.TaskType.*

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

sealed trait Task:
  val id: UUID
  val name: TaskName
  val taskTypeId: TaskTypeId
  val status: Status
  val sectionId: SectionId
  val order: TaskOrder
  val startDate: Option[LocalDate]
  val endDate: Option[LocalDate]
  val information: String
  val plan: List[TaskStep]

object Task:
  type TaskName = TaskName.T
  object TaskName
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]

  type TaskOrder = TaskOrder.T
  object TaskOrder extends RefinedType[Int, Positive]

  type TaskStep = TaskStep.T
  object TaskStep
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]
end Task
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
    - Минимум 2 символа
    - Максимум 200 символов
    - Не должно содержать начальных или конечных пробелов
- **Пример**: `"Изучить книгу 'Clean Code'"`
- **Зачем нужно**: Название помогает быстро понять, о чём задача.

#### `taskTypeId: TaskTypeId`

- **Описание**: Идентификатор типа задачи.
- **Пример**: `TaskType.Book.id`, `UserTaskTypeId(5)`
- **Зачем нужно**: Тип задачи определяет, как она будет обрабатываться, где хранится 
  и какие дополнительные поля требуются для заполнения.

#### `status: Status`

- **Описание**: Текущий статус задачи.
- **Возможные значения**:
    - `Status.InProgress` – задача в работе
    - `Status.Planned` – задача запланирована
    - `Status.KnowledgeBase` – задача в базе знаний
- **Пример**: `Status.InProgress`
- **Зачем нужно**: Статус помогает отслеживать прогресс и планировать дальнейшие действия.

#### `sectionId: SectionId`

- **Описание**: Идентификатор раздела, к которому относится задача.
- **Пример**: `SectionId(1)`
- **Зачем нужно**: Разделы позволяют логически группировать задачи, что упрощает управление обучением и фильтрацию.

#### `order: TaskOrder`

- **Описание**: Порядковый номер задачи.
- **Ограничения**: Должно быть положительное число (`Positive`)
- **Пример**: `1`, `2`, `3`, ...
- **Зачем нужно**: Позволяет определить, в какой последовательности выполнять задачи.

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
- **Пример**: `"Цель: понять основы чистого кода, чтобы применить на практике."`
- **Зачем нужно**: Хранит описание, цели, примечания и любую другую информацию, которая может быть полезна при выполнении задачи.

#### `plan: List[TaskStep]`

- **Описание**: Список шагов для выполнения задачи.
- **Пример**:
  ```dotty
  List("Прочитать первую главу", "Сделать заметки", "Выполнить упражнения")
  ```
- **Ограничения**:
    - Каждый шаг должен быть не короче 2 символов
    - Не длиннее 200 символов
    - Не содержать лишних пробелов
- **Зачем нужно**: План помогает структурировать процесс выполнения задачи и контролировать прогресс.

### Почему это важно?

Использование общих полей в классе `Task` дает следующие преимущества:

- **Единообразие** — все задачи имеют одинаковый набор обязательных полей, что упрощает их обработку и хранение.
- **Гибкость** — можно легко добавлять новые типы задач, расширяя функционал без изменения базовой структуры.
- **Безопасность данных** — благодаря библиотеке Iron, мы можем гарантировать, 
  что данные соответствуют требованиям (например, длина строки, минимальное значение числа и т.д.).
- **Удобство анализа** — наличие полей `startDate`, `endDate`, `order`, `section` позволяет строить графики, 
  анализировать прогресс и составлять отчёты.

## Конкретные типы задач

В системе управления персональным планом развития помимо общих полей, 
каждая задача может иметь **дополнительные поля**, специфичные для её формата. 
Это позволяет более точно описать содержание задачи и учитывать особенности её выполнения.

Ниже мы рассмотрим реализацию конкретных типов задач:

- **Книги**
- **Статьи**
- **Подкасты**
- **Видео**
- **Курсы**
- **Пользовательский тип**

### Книги

Книги — это один из самых популярных источников знаний, особенно при изучении фундаментальных тем. 
Они подходят для долгосрочного изучения и глубокого погружения в тему.

| Поле      | Тип данных | Описание                                                            |
|-----------|------------|---------------------------------------------------------------------|
| `authors` | `String`   | Имена авторов книги                                                 |
| `pages`   | `Pages`    | Общее количество страниц в книге (должно быть положительным числом) |

> 💡 Пример: `"Robert C. Martin"`, `464`

```dotty
final case class BookTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    order: TaskOrder,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    authors: String,
    pages: Pages
) extends Task:
  val taskTypeId: TaskTypeId = TaskType.Book.id

object BookTask:
  type Pages = Pages.T
  object Pages extends RefinedType[Int, Positive]
```

### Статьи

Статьи позволяют быстро и детально разобраться в конкретной теме. 
Они часто публикуются в блогах, на технических сайтах или в журналах.

| Поле       | Тип данных       | Описание                                          |
|------------|------------------|---------------------------------------------------|
| `author`   | `String`         | Автор статьи                                      |
| `link`     | `String`         | Ссылка на статью                                  |
| `duration` | `FiniteDuration` | Приблизительное время чтения (например, 15 минут) |

> 💡 Пример: `"John Doe"`, `https://example.com/article`, `15 минут`


```dotty
sealed trait TaskWithDuration extends Task:
  val duration: FiniteDuration

final case class ArticleTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    order: TaskOrder,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    author: String,
    link: String,
    duration: FiniteDuration
) extends TaskWithDuration:
  val taskTypeId: TaskTypeId = TaskType.Article.id
```

> ✅ Наследует интерфейс `TaskWithDuration`, чтобы работать с трудозатратностью по времени.

### Подкасты

Подкасты — отличный способ развивать soft skills, получить мотивацию и узнать о карьерных возможностях. 
Они удобны для прослушивания во время поездки, прогулки или отдыха.

| Поле              | Тип данных       | Описание                        |
|-------------------|------------------|---------------------------------|
| `author`          | `String`         | Имя автора или команды подкаста |
| `link`            | `String`         | Ссылка на подкаст               |
| `episodes`        | `Positive`       | Количество эпизодов             |
| `episodeDuration` | `FiniteDuration` | Длительность одного эпизода     |
| `duration`        | `FiniteDuration` | Общая длительность подкаста     |

> 💡 Пример: `"The Coding Podcast Team"`, `https://podcasts.apple.com/episode`, `10`, `30 минут`, `300 минут`

```dotty
final case class PodcastTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    order: TaskOrder,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    author: String,
    link: String,
    episodes: Long :| Positive,
    episodeDuration: FiniteDuration
) extends TaskWithDuration:
  val taskTypeId: TaskTypeId   = TaskType.Podcast.id
  val duration: FiniteDuration =
    val ms = episodeDuration.toMillis * episodes
    FiniteDuration(ms, MILLISECONDS)
```

> - ✅ Наследует интерфейс `TaskWithDuration`, чтобы работать с трудозатратностью по времени.
> - ✅ Автоматически вычисляется общая длительность подкаста на основе количества эпизодов и их продолжительности.

### Видео

Видео объединяет визуальную информацию и живой контекст. 
Это особенно полезно для понимания сложных концепций и участия в онлайн-мероприятиях.

| Поле       | Тип данных       | Описание                          |
|------------|------------------|-----------------------------------|
| `author`   | `String`         | Имя докладчика или организатора   |
| `platform` | `String`         | Платформа (YouTube, Vimeo и т.д.) |
| `link`     | `String`         | Ссылка на видео                   |
| `duration` | `FiniteDuration` | Длительность видео                |

> 💡 Пример: `"Jane Smith"`, `"YouTube"`, `https://www.youtube.com/watch?v=xyz123`, `45 минут`

```dotty
final case class VideoTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    order: TaskOrder,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    author: String,
    platform: String,
    link: String,
    duration: FiniteDuration
) extends TaskWithDuration:
  val taskTypeId: TaskTypeId = TaskType.Video.id
```

> ✅ Наследует интерфейс `TaskWithDuration`, чтобы работать с трудозатратностью по времени.

### Курсы

Курсы — самый эффективный способ развить практические навыки. 
Они обычно включают теорию, упражнения и проекты, что делает обучение более активным и результативным.

| Поле       | Тип данных       | Описание                                           |
|------------|------------------|----------------------------------------------------|
| `author`   | `String`         | Имя преподавателя или название платформы           |
| `platform` | `String`         | Образовательная платформа (Udemy, Coursera и т.д.) |
| `link`     | `String`         | Ссылка на курс                                     |
| `duration` | `FiniteDuration` | Общая длительность курса                           |

> 💡 Пример: `"Т Банк"`, `"Stepik"`, `https://stepik.org/course/16243/syllabus`, `720 минут`

```dotty
final case class CourseTask(
    id: UUID,
    name: TaskName,
    status: Status,
    sectionId: SectionId,
    order: TaskOrder,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    author: String,
    platform: String,
    link: String,
    duration: FiniteDuration
) extends TaskWithDuration:
  val taskTypeId: TaskTypeId = TaskType.Course.id
```

> ✅ Наследует интерфейс `TaskWithDuration`, чтобы работать с трудозатратностью по времени.

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
    taskTypeId: UserTaskTypeId,
    status: Status,
    sectionId: SectionId,
    order: TaskOrder,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[TaskStep],
    duration: FiniteDuration
) extends TaskWithDuration
```

> - ✅ В отличие от других типов, `UserTask` сам указывает свой `taskType`.
> - ✅ Наследует интерфейс `TaskWithDuration`, чтобы работать с трудозатратностью по времени.

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
    sectionId = scala.id,
    order = TaskOrder(1),
    startDate = Some(LocalDate.of(2024, 1, 1)),
    endDate = Some(LocalDate.of(2024, 12, 31)),
    information = "Best practices and advanced features",
    plan = List(TaskStep("Intro"), TaskStep("Basics"), TaskStep("OOP")),
    authors = "John Doe",
    pages = Pages(500)
  )

val userTask: UserTask = UserTask(
  id = UUID.randomUUID(),
  name = TaskName("Создать презентацию по проекту"),
  taskTypeId = myBlog.id,
  status = Status.InProgress,
  sectionId = fp.id,
  order = TaskOrder(6),
  startDate = Some(LocalDate.of(2023, 10, 1)),
  endDate = Some(LocalDate.of(2023, 10, 15)),
  information = "Создание презентации для внутреннего совещания.",
  plan = List(
    TaskStep("Собрать данные"),
    TaskStep("Создать слайды"),
    TaskStep("Проверить дизайн")
  ),
  duration = FiniteDuration(3, HOURS)
)
```

## Вывод

Конкретные типы задач делают систему индивидуального плана развития более гибкой, информативной и удобной для пользователя. Они позволяют:

- точно описывать содержание каждой задачи
- отслеживать прогресс
- анализировать эффективность разных форматов обучения
- сохранять структуру и логику всего плана

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

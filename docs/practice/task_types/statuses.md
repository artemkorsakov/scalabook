# Статусы и типы задач

## Статусы задачи

Каждая задача в индивидуальном плане развития имеет статус, который определяет её текущее состояние и этап выполнения.
Статусы позволяют отслеживать прогресс, планировать нагрузку и анализировать эффективность обучения.

![Statuses](../images/statuses.png)

Рассмотрим следующие статусы задач:

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
- Эти задачи могут быть использованы как ближайший резерв, когда закончатся задачи "В работе".

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

### **Завершено**

**Описание:**

Задачи, которые были полностью выполнены. Этот статус фиксирует завершение работы и позволяет сохранить историю достижений.

**Характеристики:**

- Задача выполнена в полном объеме согласно изначальному или актуализированному плану.
- Проставлена **фактическая дата окончания**.
- Задачи в этом статусе служат архивом вашего прогресса и могут использоваться для анализа пройденного пути.

**Пример использования:**

Вы прочитали книгу "Clean Code", выполнили все практические задания к ней и оформили конспект основных принципов. 
После этого вы завершаете задачу и указываете дату окончания, отмечая это достижение в своем плане.

### Реализация статусов в коде

Статусы реализуются в виде [перечисления (`enum`)](https://docs.scala-lang.org/scala3/reference/enums/enums.html).
Перечисление (enum) — это специальный тип данных, позволяющий определить фиксированный набор именованных констант.

Создадим файл `Status.scala` в директории `pdp/src/main/scala/ru/scalabook/pdp/models`.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/Status.scala
package ru.scalabook.pdp.models

enum Status:
  case InProgress, Planned, KnowledgeBase, Completed
```

Таким образом, каждая задача может иметь один из перечисленных статусов.

## Типы задач

В системе управления индивидуальным планом развития (ИПР) важно иметь возможность гибко описывать разные форматы задач.
Каждый тип задачи связан с конкретным форматом контента или деятельностью, например:

- Изучение книги
- Чтение статьи
- Слушание подкаста
- Просмотр видео
- Прохождение курса

Для типов создадим `case class TaskType` с заданными ограничениями на типы полей.

Для работы с ограничениями на типы данных (например, проверка длины строки, минимального значения числа и т.д.) 
будем использовать библиотеку [Iron](https://github.com/Iltotore/iron), 
которая позволяет создавать [**уточнённые типы** (refined types)](../../libs/refined.md).

Чтобы использовать Iron, нужно добавить её в список зависимостей проекта в файле `Dependencies.scala`:

```dotty
object Dependencies {
  // ...

  object iron {
    private val version = "3.4.1"
    val cats            = "io.github.iltotore" %% "iron-cats"       % version
    val circe           = "io.github.iltotore" %% "iron-circe"      % version
    val core            = "io.github.iltotore" %% "iron"            % version
    val pureconfig      = "io.github.iltotore" %% "iron-pureconfig" % version
  }

  val pdpLibs: Seq[ModuleID] =
    Seq(
      cats.catsEffect,
      iron.cats,
      iron.circe,
      iron.core,
      iron.pureconfig
    )

  // ...
}
```

### `TaskType`

Создадим файл `TaskType.scala` в директории `pdp/src/main/scala/ru/scalabook/pdp/models`.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/TaskType.scala
final case class TaskType(
    id: TaskTypeId,
    name: TaskTypeName,
    userId: UserId,
    displayed: Boolean,
    ordinal: Ordinal
)

object TaskType:
  type TaskTypeId = TaskTypeId.T
  object TaskTypeId extends RefinedType[Int, GreaterEqual[0]]

  type TaskTypeName = TaskTypeName.T
  object TaskTypeName
      extends RefinedType[String, MinLength[2] & MaxLength[30] & Trimmed]
      
// pdp/src/main/scala/ru/scalabook/pdp/models/User.scala      
object User:
  type UserId = UserId.T
  object UserId extends PdpUUID
    
// pdp/src/main/scala/ru/scalabook/pdp/models/src.scala
trait PdpUUID extends RefinedType[String, ValidUUID]:
  inline def apply(uuid: UUID): T = applyUnsafe(uuid.toString)

type Ordinal = Ordinal.T
object Ordinal extends RefinedType[Int, Positive]:
  val Min: T = Ordinal(1)

  val Max: T = Ordinal(1000)

  given Ordering[Ordinal] with
    def compare(x: Ordinal, y: Ordinal): Int =
      x.value - y.value

  extension (ordinal: Ordinal)
    def next: Ordinal =
      Ordinal.applyUnsafe(ordinal.value + 1)          
```

Это обычный [**case class**](https://docs.scala-lang.org/ru/tour/case-classes.html):

- `final` — указывает, что класс не может быть расширен (используется для гарантии иммутабельности и безопасности).
- `case class` автоматически предоставляет:
    - конструктор
    - методы `equals`, `hashCode`, `toString`
    - поддержку сопоставления с шаблоном
- `id` — идентификатор типа задачи
    - должен быть ≥ 0
- `name` — название типа задачи
    - От 2 до 30 символов
    - Строка должна быть обрезана (без пробелов в начале и конце)
- `userId` - идентификатор пользователя
- `displayed` — флаг, указывающий, включен ли тип задачи в пользовательском интерфейсе
- `ordinal` — порядковый номер типа

### Что такое "уточненные типы" (Refined Types)?

Библиотека Iron позволяет создавать типы с ограничениями. 
Вместо простого `Int` или `String` мы создаем типы, которые гарантируют, что данные соответствуют определенным правилам.

##### Пример без уточненных типов:

```dotty
// Проблема: никаких проверок!
case class TaskType(id: Int, name: String)

// Можно создать некорректные данные:
val bad1 = TaskType(-1, "A")         // ID отрицательный, имя слишком короткое
val bad2 = TaskType(1, "   test   ") // Лишние пробелы
```

##### Пример с уточненными типами:

```dotty
// Решение: типы с ограничениями
case class TaskType(id: TaskTypeId, name: TaskTypeName)

// Некорректные данные не скомпилируются!
val bad1 = TaskType(TaskTypeId(-1), TaskTypeName("name"))
// -- Constraint Error --------------------------------------------------------
// Could not satisfy a constraint for type scala.Int.
// 
// Value: -1
// Message: Should be greater than or equal to 0
// ----------------------------------------------------------------------------

val bad2 = TaskType(TaskTypeId(1), TaskTypeName("   test   "))
// -- Constraint Error --------------------------------------------------------
// Could not satisfy a constraint for type java.lang.String.
// 
// Value: test
// Message: Should have a minimum length of 2 & 
//          Should have a maximum length of 30 & 
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

Без запечатанных типов:

```dotty
type UserId = Int
type ProductId = Int

def getUser(id: UserId): User = ???
def getProduct(id: ProductId): Product = ???

// Проблема: можно перепутать ID!
val userId: UserId = 123
getProduct(userId) // Ошибка, но компилятор не поймет!
```

С запечатанными типами:

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
TaskType(TaskTypeId(5), TaskTypeName("Блог"))
TaskType(TaskTypeId(6), TaskTypeName("Open Source"))

// Некорректные примеры (не скомпилируются):
// Ошибка: -1 < 0
TaskType(TaskTypeId(-1), TaskTypeName("Тест"))
// Ошибка: название не подходит по размерам
TaskType(TaskTypeId(5), TaskTypeName("A"))
TaskType(
  TaskTypeId(0),
  TaskTypeName("Очень длинное название, которое не подходит")
)
// Ошибка: есть лишние пробелы
TaskType(TaskTypeId(5), TaskTypeName(" тест "))
```

#### Преимущества такого подхода

1. **Безопасность типов** — компилятор проверяет корректность данных
2. **Ясность кода** — типы документируют свои ограничения
3. **Предотвращение ошибок** — многие ошибки обнаруживаются на этапе компиляции
4. **Легкость рефакторинга** — изменения типов автоматически проверяются компилятором

## Разделы задач

В системе управления ИПР важно не только описывать отдельные задачи, 
но и группировать их по тематическим разделам. 
Это помогает лучше структурировать контент и облегчает навигацию по задачам.

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
// pdp/src/main/scala/ru/scalabook/pdp/models/Section.scala
final case class Section(
    id: SectionId,
    name: SectionName,
    userId: UserId,
    displayed: Boolean,
    ordinal: Ordinal
)

object Section:
  type SectionId = SectionId.T
  object SectionId extends RefinedType[Int, GreaterEqual[0]]

  type SectionName = SectionName.T
  object SectionName
      extends RefinedType[String, MinLength[2] & MaxLength[100] & Trimmed]    
```

### Пример использования

```dotty
Section(
  SectionId(1), 
  SectionName("Алгоритмы"), 
  UserId(UUID.randomUUID()), 
  true, 
  Ordinal(1)
)
```

## Глобальные цели

Важно не только иметь задачи, но и определять их цели. 
**Цели** — это мета-описания, которые помогают понять, зачем выполнять ту или иную задачу 
и как она связана с профессиональным ростом.

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
// pdp/src/main/scala/ru/scalabook/pdp/models/Goal.scala
final case class Goal(
    id: GoalId,
    name: GoalName,
    userId: UserId,
    displayed: Boolean,
    ordinal: Ordinal
)

object Goal:
  type GoalId = GoalId.T
  object GoalId extends RefinedType[Int, GreaterEqual[0]]

  type GoalName = GoalName.T
  object GoalName
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]
```

### Пример использования

```dotty
Goal(
  GoalId(1), 
  GoalName("Освоить основы алгоритмического мышления"), 
  UserId(UUID.randomUUID()), 
  true, 
  Ordinal(1)
)
```

### Связь целей с задачами

На практике цели обычно связываются с задачами через отношения "один-ко-многим":

**Одна цель** может быть связана с **несколькими задачами** 
(например, цель "Освоить функциональное программирование" может включать 
задачи по изучению монад, функторов и чистых функций).

Использование целей задач дает следующие преимущества:

- **Прозрачность обучения** — пользователь понимает, какие навыки он развивает, выполняя задачу.
- **Мотивационный аспект** — понимание "зачем" повышает вовлеченность.

Цели превращают задачи из простых упражнений в осмысленные шаги профессионального развития, 
делая систему ИПР более эффективной и персонализированной.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

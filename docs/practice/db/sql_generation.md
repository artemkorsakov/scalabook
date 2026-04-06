{%
showPrev = true
prevPage = ../db.html
showNext = true
nextPage = db_crud.html
%}

# Автоматическая генерация SQL-запросов

## Работа с PostgreSQL через библиотеку Doobie

После проектирования алгебр и создания мок-реализаций наступает этап разработки продакшен-реализаций, 
которые будут взаимодействовать с реальной системой хранения данных. 
В нашем проекте мы выбрали **PostgreSQL** — зрелую, 
надежную и полнофункциональную реляционную систему управления базами данных (СУБД), 
которая отлично подходит для промышленной эксплуатации благодаря поддержке транзакций, 
целостности данных, сложных типов (ENUM, массивы, JSON) и высокой производительности.

> 💡 **Примечание о переносимости**: Описанные ниже архитектурные принципы и подходы к работе с данными 
> являются универсальными и применимы к большинству реляционных баз данных (MySQL, Oracle, SQL Server и др.). 
> Технические отличия будут заключаться в основном в синтаксисе SQL-диалектов, 
> особенностях драйверов и настройках подключения к конкретной СУБД.

### Выбор библиотеки для доступа к данным: Doobie

Для реализации взаимодействия с PostgreSQL будем использовать библиотеку [Doobie](https://typelevel.org/doobie/index.html).

Doobie — это чисто функциональная JDBC-слой библиотека для работы с реляционными базами данных. 
Ее ключевые преимущества:

- **Строго функциональный подход**: Полная интеграция с экосистемой Cats Effect, использование `IO` 
  и других абстрактных эффектов для описания всех операций с побочными эффектами (ввод-вывод, работа с сетью). 
  Это обеспечивает предсказуемость, тестируемость и композируемость кода.
- **Типобезопасность на уровне компиляции**: Многие распространенные ошибки 
  (несоответствие типов между Scala и SQL, опечатки в именах колонок, неверное количество параметров) 
  выявляются на этапе компиляции, а не во время выполнения, что значительно повышает надежность приложения.
- **Высокая композируемость**: Запросы в Doobie построены на базе примитивов, 
  которые можно комбинировать с помощью монадических операций (`map`, `flatMap`) и аппликативных функторов. 
  Это позволяет строить сложные, динамические SQL-запросы из простых, повторно используемых компонентов.
- **Прозрачность и контролируемость**: Библиотека предоставляет удобные инструменты для логирования запросов и их выполнения.

Для начала работы с Doobie необходимо добавить соответствующие зависимости в конфигурацию сборки проекта (файл `build.sbt`).

Структура зависимостей для модуля, работающего с базой данных:

```dotty
// Файл: project/Dependencies.scala
object Dependencies {
  // ... другие блоки зависимостей (cats, http4s и т.д.) ...

  // Блок зависимостей для библиотеки Doobie
  object doobie {
    // Рекомендуется использовать актуальную стабильную версию.
    // Версия RC10 указана как пример.
    private val version = "1.0.0-RC10"

    // Основные модули Doobie, необходимые для работы:
    val core     = "org.tpolecat" %% "doobie-core"     % version
    val free     = "org.tpolecat" %% "doobie-free"     % version
    val postgres = "org.tpolecat" %% "doobie-postgres" % version
  }

  // Блок зависимостей для библиотеки Iron, используемой для уточненных типов
  object iron {
    private val version = "3.2.1"

    val core       = "io.github.iltotore" %% "iron"            % version
    val doobie     = "io.github.iltotore" %% "iron-doobie"     % version
    val pureconfig = "io.github.iltotore" %% "iron-pureconfig" % version
  }

  // ... другие блоки зависимостей ...

  // Корневой список библиотек для основного модуля приложения (pdp)
  val pdpLibs: Seq[ModuleID] =
    Seq(
      // ... другие библиотеки (cats-effect, http4s, circe) ...

      doobie.core,     
      doobie.free,     
      doobie.postgres, 
      iron.core,       
      iron.doobie,     
      iron.pureconfig, 

      // ... остальные библиотеки ...
    )

  // ... остальная конфигурация зависимостей для других модулей ...
}
```

### Следующие шаги после настройки зависимостей

После успешного подключения необходимых библиотек мы можем приступить к реализации следующих ключевых задач:

1. **Создание интерпретаторов алгебр для PostgreSQL**: Реализация трейтов `UserTypeService[F[_]]`, `TaskService[F[_]]` 
   и других с использованием Doobie's `ConnectionIO` или `Transactor[F]`.
2. **Настройка подключения к базе данных**: Конфигурация пула соединений (`HikariCP` или аналоги), 
   создание `Transactor` — объекта, который преобразует программы Doobie (`ConnectionIO`) в целевой эффект `F[_]` (например, `IO`).
3. **Реализация CRUD-операций**: Написание или генерация SQL-запросов 
   для операций `insert`, `select`, `update`, `delete` для каждой из наших сущностей, 
   используя компиляторы запросов Doobie (`Update`, `Query`).

## Проблематика ручного написания SQL

Рассмотрим задачу подготовки базы данных к работе — создание схемы (таблиц, типов). 
Допустим, нам необходимо сгенерировать DDL (Data Definition Language) скрипты для инициализации БД.

В результате мы должны получить набор SQL-запросов, подобных следующим:

```sql
-- Создание таблиц для основных сущностей
CREATE TABLE IF NOT EXISTS user_types (
  id SERIAL PRIMARY KEY, 
  name VARCHAR(255) NOT NULL,
  user_id UUID NOT NULL,  
  displayed BOOLEAN NOT NULL DEFAULT TRUE, 
  ordinal INT NOT NULL DEFAULT 1000
);

CREATE TABLE IF NOT EXISTS sections (
  id SERIAL PRIMARY KEY, 
  name VARCHAR(255) NOT NULL,
  user_id UUID NOT NULL,
  displayed BOOLEAN NOT NULL DEFAULT TRUE, 
  ordinal INT NOT NULL DEFAULT 1000
); 

CREATE TABLE IF NOT EXISTS goals (
  id SERIAL PRIMARY KEY, 
  name VARCHAR(255) NOT NULL,
  user_id UUID NOT NULL,
  displayed BOOLEAN NOT NULL DEFAULT TRUE, 
  ordinal INT NOT NULL DEFAULT 1000
);

-- Создание пользовательского типа ENUM для статуса задачи
CREATE TYPE StatusEnum AS ENUM ('InProgress', 'Planned', 'KnowledgeBase');

-- Создание таблицы для задач типа "Книга" со специфичными полями
CREATE TABLE IF NOT EXISTS books (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY, 
  name VARCHAR(255) NOT NULL,
  user_id UUID NOT NULL,
  status StatusEnum NOT NULL, 
  section_id INT NOT NULL, 
  goal_id INT NOT NULL, 
  ordinal INT NOT NULL, 
  start_date DATE, 
  end_date DATE, 
  information TEXT, 
  plan VARCHAR[], 
  authors VARCHAR(255) NOT NULL, 
  pages INT NOT NULL
);
-- ... аналогичные CREATE TABLE для articles, podcasts, videos, courses, user_tasks ...
```

**Подсчет объема работы:**

- **3 SQL-запроса** для базовых справочников (`user_types`, `sections`, `goals`).
- **6 SQL-запросов** для таблиц, соответствующих каждому типу задач (`books`, `articles`, ...).
- **Итого: 9 SQL-запросов**, которые необходимо написать и поддерживать.

При внимательном рассмотрении становится очевидно, что структура этих запросов обладает высокой степенью повторяемости. 
У нас есть множество `case class`-ов (доменных моделей), 
для которых SQL-запросы создания таблиц будут иметь идентичную структуру: 
`CREATE TABLE <имя_таблицы> (<поле1> <тип1>, <поле2> <тип2>, ...)`. 
Различия сводятся лишь к имени таблицы (которое часто производно от имени класса) 
и типам SQL-колонок, которые должны соответствовать типам полей Scala-класса.

### Оценка существующих решений (ORM/Query Builders)

В экосистеме Scala существуют библиотеки, которые решают проблему описания схемы и построения запросов, например:

- **[Slick](https://scala-slick.org/)** — "Functional Relational Mapping" библиотека, 
  предоставляет DSL для типобезопасного построения запросов. 
  Однако ее фокус — это построение запросов, а не удобная генерация DDL из case class. 
  Также ее идеология несколько отличается от чисто функционального стека Cats Effect.
- **[ScalaSQL](https://github.com/com-lihaoyi/scalasql)** — библиотека от Li Haoyi, 
  предоставляющая прямое написание SQL с интерполяцией и проверкой на этапе компиляции. 
  Однако ее интеграция с Cats Effect и современным функциональным стеком может требовать дополнительных усилий.

Основная проблема существующих решений в контексте нашего проекта — 
это либо недостаточная поддержка бесшовной интеграции с Cats Effect и стилем `Tagless Final`, 
либо отсутствие удобного механизма **автоматической генерации DDL** напрямую из case class-ов, 
которые у нас уже определены в модуле `model`.

### Решение: создание собственного генератора SQL на макросах

Учитывая специфику проекта (четко определенные case class-ы, желание минимизировать boilerplate, 
потребность в глубокой интеграции с Cats Effect/Doobie), самым прагматичным, хотя и нетривиальным решением, 
является создание собственного **макроса для генерации SQL**.

**Зачем "ещё один ORM для SQL на Scala"?** В данном контексте это оправдано, 
так как позволяет достичь максимального соответствия нашим требованиям: 
генерировать валидный, безопасный SQL прямо из структуры данных, определенной на Scala, избегая дублирования и ручных ошибок.

#### Архитектурное решение: создание отдельного репозитория

Для изоляции логики генерации SQL и обеспечения её повторного использования,
создадим [отдельный репозиторий][sql-gen] — `sql-gen`.

В файле `project/Dependencies.scala` добавляем блок зависимостей для `sql-gen`:

```dotty
// Список зависимостей, специфичных для sql-gen.
val sqlGenLibs: Seq[ModuleID] =
  Seq(
    doobie.core,
    doobie.free,
    iron.core   
  )
```

После настройки структуры проекта создадим стандартные директории для исходного кода:

- `src/main/scala/` — для основной логики макросов и утилит генерации.
- `src/test/scala/` — для модульных тестов генератора SQL.

## Моделирование метаданных таблицы

В первую очередь необходимо создать модель, которая будет описывать структуру одной колонки (поля) таблицы в базе данных. 
Для этого создаем пакет `ru.scalabook.sql` и в нем файл `ColumnDefinition.scala`.

Класс `ColumnDefinition` представляет собой неизменяемую структуру данных, содержащую все необходимые атрибуты, 
которые могут быть у колонки в SQL: имя, тип, флаги ограничений (`NOT NULL`, `PRIMARY KEY`), 
проверки (`CHECK`) и значения по умолчанию (`DEFAULT`). 
Для обеспечения типобезопасности и валидации на этапе компиляции мы используем библиотеку Iron для создания уточненных типов.

```dotty
// src/main/scala/ru/scalabook/sql/ColumnDefinition.scala
/** Модель, описывающая атрибуты одного столбца (колонки) в SQL таблице.
  *
  * @param name
  *   Имя колонки в базе данных. Должно соответствовать правилам именования SQL.
  * @param sqlType
  *   SQL-тип данных колонки (например, VARCHAR(255), INT, TIMESTAMP).
  * @param isPrimaryKey
  *   Флаг, указывающий, является ли колонка частью первичного ключа (PRIMARY
  *   KEY).
  * @param notNull
  *   Флаг ограничения NOT NULL, запрещающий наличие NULL значений в колонке.
  * @param check
  *   Опциональное условие CHECK для дополнительных ограничений значений.
  * @param default
  *   Опциональное выражение DEFAULT для задания значения по умолчанию.
  */
final case class ColumnDefinition(
    name: ColumnName,
    sqlType: ColumnType,
    isPrimaryKey: Boolean,
    notNull: Boolean,
    check: Option[ColumnCheck],
    default: Option[ColumnDefault]
):
  /** Помечает колонку как первичный ключ. Возвращает новый экземпляр
    * `ColumnDefinition` с установленным флагом `isPrimaryKey = true`.
    */
  def asPK(): ColumnDefinition =
    copy(isPrimaryKey = true)

  /** Устанавливает ограничение NOT NULL для колонки. Возвращает новый экземпляр
    * с флагом `notNull = true`.
    */
  def asNotNull(): ColumnDefinition =
    copy(notNull = true)

  /** Добавляет условие CHECK к определению колонки.
    *
    * @param check
    *   Условие валидации в виде SQL-выражения (например, "> 0").
    */
  def check(check: ColumnCheck): ColumnDefinition =
    copy(check = Some(check))

  /** Устанавливает значение по умолчанию (DEFAULT) для колонки.
    *
    * @param default
    *   SQL-выражение для значения по умолчанию (например, "NOW()").
    */
  def default(default: ColumnDefault): ColumnDefinition =
    copy(default = Some(default))

object ColumnDefinition:
  /** Уточненный тип для имени колонки. Ограничения: непустая строка длиной от 1
    * до 63 символов, обрезанная по краям.
    */
  type ColumnName = ColumnName.T

  object ColumnName
      extends RefinedType[String, MinLength[1] & MaxLength[63] & Trimmed]:
    /** Предопределенное имя для колонки с идентификатором. */
    val Id: ColumnName = ColumnName("id")

  type ColumnType = ColumnType.T

  object ColumnType
      extends RefinedType[String, MinLength[1] & MaxLength[100] & Trimmed]:
    /** Предопределенные константы для часто используемых SQL-типов PostgreSQL. */
    val BigInt: ColumnType       = ColumnType("BIGINT")
    val Boolean: ColumnType      = ColumnType("BOOLEAN")
    val Date: ColumnType         = ColumnType("DATE")
    val Int: ColumnType          = ColumnType("INT")
    // Автоинкрементный целочисленный идентификатор
    val Serial: ColumnType       = ColumnType("SERIAL") 
    val Text: ColumnType         = ColumnType("TEXT")
    val Timestamp: ColumnType    = ColumnType("TIMESTAMP")
    val UUID: ColumnType         = ColumnType("UUID")
    val Varchar: ColumnType      = ColumnType("VARCHAR(255)") 
    val VarcharArray: ColumnType = ColumnType("VARCHAR[]")    

  /** Уточненный тип для условия CHECK. Строка, содержащая SQL-выражение. */
  type ColumnCheck = ColumnCheck.T

  object ColumnCheck extends RefinedType[String, MinLength[1] & Trimmed]

  /** Уточненный тип для значения по умолчанию (DEFAULT). 
    * Строка, содержащая SQL-выражение. 
    */
  type ColumnDefault = ColumnDefault.T

  object ColumnDefault extends RefinedType[String, MinLength[1] & Trimmed]:
    val Now: ColumnDefault  = ColumnDefault("NOW()")
    val UUID: ColumnDefault = ColumnDefault("gen_random_uuid()")

  /** Упрощенный конструктор для создания ColumnDefinition без дополнительных
    * ограничений. Создает колонку, которая не является первичным ключом,
    * допускает NULL, не имеет CHECK и DEFAULT.
    *
    * @param name
    *   Имя колонки.
    * @param sqlType
    *   Тип данных колонки.
    * @return
    *   Базовое определение колонки.
    */
  def apply(name: ColumnName, sqlType: ColumnType): ColumnDefinition =
    ColumnDefinition(
      name = name,
      sqlType = sqlType,
      isPrimaryKey = false,
      notNull = false,
      check = None,
      default = None
    )
end ColumnDefinition
```

## Определение схемы таблицы

Следующим шагом создаем абстракцию для описания всей таблицы целиком. Для этого добавляем файл `TableSchema.scala`.

Трейт `TableSchema[T]` является [классом типов (typeclass)](../../typeclass/index.md), 
который предоставляет метаданные для типа `T` (например, `UserType`), необходимые для генерации DDL. 
Ключевая идея в том, что для каждого типа данных, который нужно сохранять в таблице, 
мы определяем отдельную реализацию этого класса типов. 
Каждая реализация содержит имя таблицы и коллекцию определений ее колонок.

```dotty
// src/main/scala/ru/scalabook/sql/TableSchema.scala
/** Класс типов, предоставляющий метаданные для генерации SQL DDL для типа
  * данных `T`.
  *
  * @tparam T
  *   Тип Scala, для которого определяется схема таблицы.
  */
trait TableSchema[T]:
  import TableSchema.*

  /** Имя таблицы в базе данных, соответствующее типу `T`. */
  def tableName: TableName

  /** Упорядоченная коллекция определений колонок, составляющих таблицу. */
  def columns: Columns

  /** Генерирует строку с SQL-запросом CREATE TABLE на основе метаданных. Запрос
    * идемпотентен благодаря конструкции `IF NOT EXISTS`.
    *
    * @return
    *   SQL-строка для создания таблицы.
    */
  final def generateCreateTable: String =
    // Преобразуем каждое определение колонки в строку SQL-синтаксиса.
    val columnDefs = columns.value.map: col =>
      val notNullStr =
        if col.notNull then " NOT NULL" else ""
      val checkStr =
        col.check.map(v => s" CHECK (${col.name} $v)").getOrElse("")
      val defaultStr =
        col.default.map(v => s" DEFAULT $v").getOrElse("")
      val primaryKeyStr =
        if col.isPrimaryKey then " PRIMARY KEY" else ""

      // Собираем итоговое определение одной колонки.
      s"${col.name} ${col.sqlType}$notNullStr$checkStr$defaultStr$primaryKeyStr"

    // Объединяем определения колонок и формируем полный запрос CREATE TABLE.
    s"CREATE TABLE IF NOT EXISTS $tableName (${columnDefs.mkString(", ")});"
end TableSchema

object TableSchema:
  /** Уточненный тип для имени таблицы. Аналогично ColumnName. */
  type TableName = TableName.T

  object TableName
      extends RefinedType[String, MinLength[1] & MaxLength[63] & Trimmed]

  /** Уточненный тип для неизменяемой коллекции определений колонок.
    * Гарантирует, что таблица имеет хотя бы одну колонку.
    */
  type Columns = Columns.T

  object Columns extends RefinedType[Vector[ColumnDefinition], MinLength[1]]:
    /** Вспомогательный конструктор для создания `Columns` из одного или
      * нескольких `ColumnDefinition`.
      *
      * @param head
      *   Первое (обязательное) определение колонки.
      * @param columns
      *   Остальные определения колонок (опционально).
      */
    def apply(head: ColumnDefinition, columns: ColumnDefinition*): Columns =
      Columns.applyUnsafe(head +: columns.toVector)

    /** Операция конкатенации двух коллекций определений колонок. Полезна для
      * построения схем из модульных частей.
      */
    extension (first: Columns)
      def ++(second: Columns): Columns =
        Columns.applyUnsafe(first.value ++ second.value)
```

## Реализация макроса для генерации SQL-фрагментов

Теперь, имея модель метаданных и класс типов, необходимо создать инструмент, 
который будет использовать эти метаданные для генерации готового SQL-кода на этапе компиляции. 
Для этого применяем метапрограммирование Scala 3 (макросы). Создаем файл `SqlMacros.scala`.

Макрос `generateCreateTableFor[T]` — это inline-метод, 
который во время компиляции ищет реализацию `TableSchema[T]` для заданного типа `T`. 
Если реализация найдена, макрос вызывает его метод `generateCreateTable` 
и упаковывает результирующую SQL-строку в тип `Fragment` из библиотеки Doobie. 
`Fragment` — это типобезопасный контейнер Doobie для SQL-выражений, 
который может безопасно комбинироваться с другими фрагментами и параметрами.

```dotty
// src/main/scala/ru/scalabook/sql/SqlMacros.scala
object SqlMacros:
  /** Inline-макрос, который на этапе компиляции генерирует Doobie Fragment с
    * SQL-запросом CREATE TABLE для типа `T`.
    *
    * @tparam T
    *   Тип, для которого должна существовать реализация TableSchema[T].
    * @return
    *   Fragment, содержащий DDL-запрос для создания таблицы.
    */
  inline def generateCreateTableFor[T]: Fragment =
    ${ generateCreateTableForImpl[T] }

    /** Реализация макроса, работающая с цитатами (quotes) и отражением
    * (reflection).
    *
    * @tparam T
    *   Тип, переданный в макрос (извлекается из контекста `Type[T]`).
    * @param quotes
    *   Контекст цитирования, предоставляющий API для метапрограммирования.
    * @return
    *   Выражение (Expr) типа `Fragment`.
    */
  private def generateCreateTableForImpl[T: Type](using
      Quotes
  ): Expr[Fragment] =
    import quotes.reflect.*

    // Пытаемся найти в неявном контексте реализацию TableSchema[T]
    Expr.summon[TableSchema[T]] match
      case Some(tableSchema) =>
        // Если реализация найдена, создаем цитату (quoted code), которая:
        // 1. Получает реализацию схемы (schema).
        // 2. Вызывает её метод generateCreateTable.
        // 3. Оборачивает результат в Fragment.const.
        '{
          val schema = $tableSchema
          Fragment.const(schema.generateCreateTable)
        }
      case None =>
        // Если реализация не найдена, останавливаем компиляцию с понятной ошибкой.
        report.errorAndAbort(
          s"No TableSchema found for type ${Type.show[T]}"
        )
```

## Предоставление реализации `TableSchema` для доменных моделей

Класс типов `TableSchema` становится полезным только тогда, когда для конкретных типов данных определены его реализации. 
Эти реализации обычно размещаются в сопутствующих объектах соответствующих типов, 
чтобы они автоматически попадали в область видимости неявных параметров.

Пример для доменного типа `UserType`:

```dotty
// Файл: pdp/src/main/scala/ru/scalabook/pdp/model/UserType.scala
object UserType:
  // ...

  /** Неявная реализация TableSchema для типа UserType. Описывает, как сущность
    * UserType должна быть отображена в таблицу БД.
    */
  given TableSchema[UserType] with
    def tableName: TableName = TableName("user_types") // Имя таблицы

    def columns: Columns =
      Columns(
        // Колонка 'id' типа SERIAL (автоинкремент), является первичным ключом.
        ColumnDefinition(
          ColumnName.Id,
          ColumnType.Serial
        ).asPK(),
        // Колонка 'name' типа VARCHAR(255), обязательна к заполнению (NOT NULL).
        ColumnDefinition(
          ColumnName("name"),
          ColumnType.Varchar
        ).asNotNull(),
        // Колонка 'user_id' типа UUID, обязательна к заполнению (NOT NULL).
        ColumnDefinition(
          ColumnName("user_id"),
          ColumnType.UUID
        ).asNotNull(),
        // Колонка 'displayed' типа BOOLEAN, по умолчанию TRUE.
        ColumnDefinition(
          ColumnName("displayed"), 
          ColumnType.Boolean
        ).asNotNull()
          .default(ColumnDefault("TRUE")),
        // Колонка 'ordinal' типа INT, по умолчанию 1000.  
        ColumnDefinition(
          ColumnName("ordinal"),
          ColumnType.Int
        ).asNotNull()
          .default(ColumnDefault("1000"))
      )
end UserType
```

## Результат работы системы

Когда в коде, например, в миграциях базы данных или в тестах, вызывается макрос `generateCreateTableFor[UserType]`, 
происходит следующее:

1.  **На этапе компиляции:**

    - Компилятор находит реализацию `given TableSchema[UserType]` в сопутствующем объекте `UserType`.
    - Макрос `generateCreateTableForImpl` получает доступ к этой реализации.
    - Вызывается метод `generateCreateTable` этой реализации, который возвращает строку:
      ```sql
      CREATE TABLE IF NOT EXISTS user_types (
          id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL,
          user_id UUID NOT NULL, displayed BOOLEAN NOT NULL DEFAULT TRUE,
          ordinal INT NOT NULL DEFAULT 1000
      );
      ```
    - Эта строка оборачивается в `Fragment.const(...)`.
    - Код макроса "раскрывается", и в месте вызова оказывается валидное выражение типа `Fragment`.

2.  **На этапе выполнения:**
    - Полученный `Fragment` может быть передан в Doobie для непосредственного выполнения в базе данных.
    - Выполнится именно тот SQL-запрос, который был сгенерирован на этапе компиляции.

**Итоговый сгенерированный SQL:**

```sql
CREATE TABLE IF NOT EXISTS user_types (
  id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, 
  user_id UUID NOT NULL, displayed BOOLEAN NOT NULL DEFAULT TRUE, 
  ordinal INT NOT NULL DEFAULT 1000
);
```

Таким образом, мы построили типобезопасную систему, которая позволяет по типу Scala (например, `UserType`) 
генерировать корректный DDL на SQL, минимизируя ручной труд и возможность синтаксических ошибок. 
Все преобразования контролируются компилятором и используют заранее определенные, проверенные шаблоны.

Проверку корректности DDL [можно проводить в тестах](https://gitverse.ru/scalabook/sql-gen/content/master/src/test/scala/ru/scalabook/sql/SqlMacrosSuite.scala).

Например, тест для проверки генерации SQL для `BookTask`:

```dotty
pureTest("Generate create table"):
  expect.same(
    """CREATE TABLE IF NOT EXISTS books (id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
       name VARCHAR(200) NOT NULL,
       user_id UUID NOT NULL,
       status StatusEnum NOT NULL,
       section_id INT NOT NULL,
       ordinal INT NOT NULL CHECK (ordinal > 0),
       start_date DATE,
       end_date DATE,
       information TEXT,
       plan VARCHAR[],
       authors VARCHAR(255),
       pages INT NOT NULL CHECK (pages > 0));"""
      .replaceAll("\n", " ").replaceAll("\\s+", " "),
    generateCreateTableFor[BookTask].query.sql.trim
  )
```

## Генерация DDL для пользовательских типов (ENUM)

Помимо создания таблиц, в реляционных базах данных часто используются пользовательские типы данных, 
такие как ENUM в PostgreSQL, которые идеально подходят для представления ограниченного набора значений. 
В нашем домене, например, статус задачи (`Status`) - это перечисление: 
`InProgress`, `Planned`, `KnowledgeBase`, `Completed`. 
Для генерации DDL-запросов для таких типов создаем дополнительный макрос в файле `SqlMacros.scala`.

```dotty
// src/main/scala/ru/scalabook/sql/SqlMacros.scala
object SqlMacros:
  // Суффикс, добавляемый к имени Scala enum для получения имени SQL-типа
  private inline val enumSuf = "Enum"

  // Макрос для генерации CREATE TABLE (предыдущая реализация)
  // ...

  /** Inline-макрос для генерации SQL-запроса CREATE TYPE для перечисления
    * (enum). Работает с любым типом, являющимся подтипом `scala.reflect.Enum`.
    *
    * @tparam E
    *   Тип перечисления (например, `Status`).
    * @return
    *   Fragment, содержащий DDL-запрос для создания типа ENUM в PostgreSQL.
    */
  inline def generateCreateEnumType[E <: reflect.Enum]: Fragment =
    ${ generateCreateEnumTypeImpl[E] }

  /** Реализация макроса для генерации CREATE TYPE. Использует reflection API
    * Scala 3 для извлечения информации о перечислении.
    */
  private def generateCreateEnumTypeImpl[E: Type](using
      Quotes
  ): Expr[Fragment] =
    import quotes.reflect.*

    // Получаем представление типа и его символ (информацию о классе/перечислении)
    val tpe    = TypeRepr.of[E]
    val symbol = tpe.typeSymbol

    // Извлекаем имена всех вложенных объектов-вариантов перечисления, удаляя суффикс '$'
    val cases = symbol.children.map(_.name.stripSuffix("$"))

    // Проверяем, что перечисление не пустое
    if cases.isEmpty then
      report.errorAndAbort(s"Enum ${symbol.name} has no cases")

    // Формируем имя SQL-типа (имя Scala-перечисления + суффикс "Enum")
    val typeName = symbol.name.stripSuffix("$")
    // Преобразуем имена вариантов в SQL-синтаксис: 'InProgress', 'Planned', ...
    val values   = cases.map(v => s"'$v'").mkString(", ")
    // Собираем итоговый SQL-запрос
    val sql      = s"CREATE TYPE ${typeName}$enumSuf AS ENUM ($values);"

    '{
      Fragment.const(${ Expr(sql) })
    }
  end generateCreateEnumTypeImpl
end SqlMacros
```

**Результат работы**: При вызове `generateCreateEnumType[Status]` на этапе компиляции будет сгенерирован следующий SQL-запрос:

```sql
CREATE TYPE StatusEnum AS ENUM ('InProgress', 'Planned', 'KnowledgeBase', 'Completed');
```

## Организация определения схем для иерархии типов задач

Для эффективного управления повторяющимися колонками в таблицах различных типов задач 
мы применяем принцип композиции и повторного использования. 
Общие для всех задач колонки вынесены в сопутствующий объект `Task`, 
а специфичные колонки определяются для каждого конкретного типа задачи.

```dotty
// Файл: pdp/src/main/scala/ru/scalabook/pdp/model/Task.scala
object Task:
  // --- Определение общих колонок для всех задач ---
  val idColumn: ColumnDefinition =
    ColumnDefinition(
      ColumnName.Id,
      ColumnType.UUID
    ).asPK().default(ColumnDefault.UUID)

  val nameColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("name"), ColumnType.Varchar).asNotNull()

  val userId: ColumnDefinition = ColumnDefinition(
    ColumnName("user_id"),
    ColumnType.UUID
  ).asNotNull()
  
  // Статус ссылается на созданный нами ENUM-тип
  val statusColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("status"), ColumnType("StatusEnum")).asNotNull()

  val sectionIdColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("section_id"), ColumnType.Int).asNotNull()
  val goalIdColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("goal_id"), ColumnType.Int).asNotNull()
  val ordinalColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("ordinal"), ColumnType.Int).asNotNull()

  // Даты могут быть nullable
  val startColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("start_date"), ColumnType.Date)
  val endColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("end_date"), ColumnType.Date)

  val infoColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("information"), ColumnType.Text)
  val planColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("plan"), ColumnType.VarcharArray)

  // Коллекция всех общих колонок в правильном порядке
  val commonColumns: Columns = Columns(
    idColumn,
    nameColumn,
    userId,
    statusColumn,
    sectionIdColumn,
    goalIdColumn,
    ordinalColumn,
    startColumn,
    endColumn,
    infoColumn,
    planColumn
  )

  // --- Определения специфичных колонок для повторного использования ---
  val authorsColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("authors"), ColumnType.Varchar).asNotNull()
  val platformColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("platform"), ColumnType.Varchar).asNotNull()
  val linkColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("link"), ColumnType.Text).asNotNull()
  val durationColumn: ColumnDefinition =
    ColumnDefinition(ColumnName("duration"), ColumnType.BigInt).asNotNull()
end Task

// Примеры реализаций TableSchema для конкретных типов задач:
object BookTask:
  given TableSchema[BookTask] with
    def tableName: TableName = TableName("books")
    def columns: Columns =
      commonColumns ++ Columns(
        authorsColumn,
        ColumnDefinition(ColumnName("pages"), ColumnType.Int).asNotNull()
      )

object ArticleTask:
  given TableSchema[ArticleTask] with
    def tableName: TableName = TableName("articles")
    def columns: Columns =
      commonColumns ++ Columns(
        authorsColumn,
        linkColumn,
        durationColumn
      )

// ... аналогичные определения для PodcastTask, VideoTask, CourseTask ...

object UserTask:
  given TableSchema[UserTask] with
    def tableName: TableName = TableName("user_tasks")
    // Для пользовательских задач схема отличается, 
    // т.к. есть колонка user_type_id
    def columns: Columns =
      Columns(
        idColumn,
        nameColumn,
        userId,
        ColumnDefinition(ColumnName("user_type_id"), ColumnType.Int).asNotNull(),
        statusColumn,
        sectionIdColumn,
        goalIdColumn,
        ordinalColumn,
        startColumn,
        endColumn,
        infoColumn,
        planColumn,
        durationColumn
      )
  end given
end UserTask
```

**Сгенерированный DDL для `BookTask`**:

```sql
CREATE TABLE IF NOT EXISTS books (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  user_id UUID NOT NULL,
  status StatusEnum NOT NULL,
  section_id INT NOT NULL,
  goal_id INT NOT NULL,
  ordinal INT NOT NULL,
  start_date DATE,
  end_date DATE,
  information TEXT,
  plan VARCHAR[],
  authors VARCHAR(255) NOT NULL,
  pages INT NOT NULL
);
```

## Масштабируемость системы генерации

Возникает резонный вопрос: **"Какова реальная выгода?"** 

Вместо написания 9 SQL-запросов `CREATE TABLE` мы написали примерно такое же количество определений `TableSchema`. 
На первый взгляд, выигрыш не очевиден.

Однако ключевые преимущества проявляются в следующих аспектах:

1. **Исключение синтаксических ошибок и опечаток**: Все SQL-строки генерируются кодом, а не вводятся вручную. 
   Ошибка в `ColumnType` будет обнаружена компилятором, если для нее не предусмотрена константа.
2. **Согласованность именования**: Имена таблиц и колонок извлекаются из единого источника истины — реализации `TableSchema`.
3. **Легкость рефакторинга**: При изменении структуры доменного класса достаточно обновить соответствующий `TableSchema`, 
   и все SQL-запросы автоматически адаптируются.

**Настоящая мощь системы раскрывается при генерации запросов DML (Data Manipulation Language)**. 
Для полноценного CRUD-репозитория каждой сущности необходимо реализовать 5 типов запросов:

- `CREATE` → `INSERT` (1 запрос)
- `READ` (single) → `SELECT ... WHERE id = ?` (1 запрос)
- `READ` (all) → `SELECT ...` (1 запрос)
- `UPDATE` → `UPDATE ... WHERE id = ?` (1 запрос)
- `DELETE` → `DELETE ... WHERE id = ?` (1 запрос)

**Итого: 5 запросов × 9 сущностей = 45 SQL-запросов.**

Ручное написание и поддержка 45 запросов — это значительный объем работы с высокой вероятностью ошибок 
(опечатки в именах колонок, неправильные плейсхолдеры `?`, забытые условия `WHERE`). 
Тестирование каждого запроса также требует усилий.

С системой генерации достаточно реализовать **4 универсальных макроса** 
(для SELECT по ID, SELECT ALL, INSERT, UPDATE, DELETE), 
способные работать для любой сущности, для которой определен `TableSchema`. 
При добавлении новой сущности не нужно писать ни одного нового SQL-запроса — 
достаточно предоставить для нее реализацию `TableSchema`.

## Реализация макросов для DML-операций

### Макросы для операций чтения (SELECT)

```dotty
object SqlMacros:
  // ... предыдущие макросы ...

  /** Генерирует функцию, которая по идентификатору создает Fragment для
    * SELECT-запроса, извлекающего одну запись по первичному ключу. Тип ID может
    * быть любым (Int, UUID, String).
    */
  inline def generateSelectById[T, Id]: Id => Fragment =
    ${ generateSelectByIdImpl[T, Id] }

  private def generateSelectByIdImpl[T: Type, Id: Type](using
      Quotes
  ): Expr[Id => Fragment] =
    import quotes.reflect.*
    
    Expr.summon[TableSchema[T]] match
      case Some(tableSchema) =>
        '{
          (id: Id) =>
            val schema = $tableSchema
            // Собираем список всех колонок для SELECT
            val columnNames = schema.columns.value.map(_.name).mkString(", ")
            Fragment.const(
              s"SELECT $columnNames FROM ${schema.tableName} WHERE id = '$id'"
            )
        }
      case None =>
        report.errorAndAbort(s"No TableSchema found for type ${Type.show[T]}")
      
  /** Генерирует функцию, которая по идентификатору создает Fragment для
    * SELECT-запроса, извлекающего одну запись по заданному полю. Тип Value
    * может быть любым (Int, UUID, String).
    */
  inline def generateSelectByField[T, Value]: (Value, String) => Fragment =
    ${ generateSelectByFieldImpl[T, Value] }

  private def generateSelectByFieldImpl[T: Type, Value: Type](using
      Quotes
  ): Expr[(Value, String) => Fragment] =
    import quotes.reflect.*

    Expr.summon[TableSchema[T]] match
      case Some(tableSchema) =>
        '{
          (id: Value, field: String) =>
            val schema      = $tableSchema
            val columnNames = schema.columns.value.map(_.name).mkString(", ")
            Fragment.const(
              s"SELECT $columnNames FROM ${schema.tableName} WHERE $field = '$id'"
            )
        }
      case None =>
        report.errorAndAbort(s"No TableSchema found for type ${Type.show[T]}")    
        
  /** Генерирует Fragment для SELECT-запроса, извлекающего все записи таблицы.
    */
  inline def generateSelectAll[T]: Fragment =
    ${ generateSelectAllImpl[T] }

  private def generateSelectAllImpl[T: Type](using Quotes): Expr[Fragment] =
    import quotes.reflect.*
    
    Expr.summon[TableSchema[T]] match
      case Some(tableSchema) =>
        '{
          val schema = $tableSchema
          val columnNames = schema.columns.value.map(_.name).mkString(", ")
          Fragment.const(s"SELECT $columnNames FROM ${schema.tableName}")
        }
      case None =>
        report.errorAndAbort(s"No TableSchema found for type ${Type.show[T]}")
```

### Макрос для операции удаления (DELETE)

```dotty
object SqlMacros:
  // ...

  inline def generateDeleteById[T, Id]: Id => Fragment =
    ${ generateDeleteByIdImpl[T, Id] }

  private def generateDeleteByIdImpl[T: Type, Id: Type](using
      Quotes
  ): Expr[Id => Fragment] =
    import quotes.reflect.*
    Expr.summon[TableSchema[T]] match
      case Some(tableSchema) =>
        '{
          (id: Id) =>
            val schema = $tableSchema
            Fragment.const(s"DELETE FROM ${schema.tableName} WHERE id = '$id'")
        }
      case None =>
        report.errorAndAbort(s"No TableSchema found for type ${Type.show[T]}")
```

### Макросы для операций вставки и обновления (INSERT, UPDATE)

Эти макросы сложнее, так как должны учитывать типы полей сущности для корректной подстановки параметров. 
Они используют класс типов `Meta` из Doobie, который содержит информацию о том, 
как читать и писать значения данного типа Scala из/в JDBC.

```dotty
object SqlMacros:
  // ...

  /** Генерирует функцию для INSERT-запроса, которая принимает данные для
    * вставки (тип B), исключает колонку первичного ключа (она генерируется БД)
    * и возвращает Fragment, который при выполнении вернет сгенерированный ID
    * (тип Id). A - тип сущности в БД, B - тип данных для создания (например,
    * CreateBookTask).
    */
  inline def generateInsertReturningId[A, B, Id]: B => Fragment =
    ${ generateInsertReturningIdImpl[A, B, Id] }

  @SuppressWarnings(Array(
    "scalafix:DisableSyntax.asInstanceOf",
    "DisableSyntax.throw"
  ))
  private def generateInsertReturningIdImpl[A: Type, B: Type, Id: Type](using
      Quotes
  ): Expr[B => Fragment] =
    import quotes.reflect.*
    val entityType = TypeRepr.of[B]
    // Проверяем, является ли B case class-ом
    val isProduct = entityType <:< TypeRepr.of[Product] 

    Expr.summon[TableSchema[A]] match
      case Some(tableSchema) =>
        if isProduct then
          // Для case class: получаем метаданные (Meta) для каждого поля
          val fields    = entityType.typeSymbol.caseFields
          val metaExprs = fields.map: field =>
            val fieldType = field.termRef.widenTermRefByName
            fieldType.asType match
              case '[t] => Expr.summon[Meta[t]].getOrElse(
                  report.errorAndAbort(
                    s"Cannot find Meta for field ${field.name}: ${fieldType.show}"
                  )
                )
          val metaList = Expr.ofList(metaExprs)

          '{
            (entity: B) =>
              val schema = $tableSchema
              // Исключаем колонку PK из списка для INSERT
              val columnsWithoutPK = schema.columns.value.filterNot(_.isPrimaryKey)
              val columnNames = columnsWithoutPK.map(_.name.value).mkString(", ")
              // Генерируем плейсхолдеры с учетом типа (особенно для ENUM)
              val placeholders = columnsWithoutPK.map(getPlaceholder).mkString(", ")

              entity match
                case product: Product =>
                  // Извлекаем значения всех полей case class
                  val fieldValues = 
                    (0 until product.productArity)
                      .map(product.productElement).toList
                  // Сопоставляем каждое значение с его Meta 
                  // для создания списка параметров Doobie
                  val elems = $metaList.zip(fieldValues).map {
                    case (meta, value) =>
                      Arg(
                        value, 
                        Put.metaProjectionWrite(using meta.asInstanceOf[Meta[Any]])
                      )
                  }
                  // Создаем Fragment с параметризованным запросом
                  Fragment(
                    s"INSERT INTO ${schema.tableName.value} ($columnNames) " +
                      s"VALUES ($placeholders) RETURNING id",
                    elems
                  )
          }
        else
          // Упрощенная ветка для не-product типов (например, простых значений)
          Expr.summon[Meta[B]] match
            case Some(metaB) =>
              '{ (entity: B) => ... } // Генерация для одного поля
            case None => report.errorAndAbort(s"Cannot find Meta for type ${Type.show[B]}")
      case None => report.errorAndAbort(s"No TableSchema found for type ${Type.show[A]}")

  // Аналогичная по сложности реализация generateUpdateByIdImpl...
  inline def generateUpdateById[A, Id]: A => Fragment =
    ${ generateUpdateByIdImpl[A, Id] }

  private def generateUpdateByIdImpl[A: Type, Id: Type](using Quotes): Expr[A => Fragment] = 
  {
    // Логика похожа на insert, но формирует SET column1 = ?, column2 = ? ... WHERE id = ?
    // и соответствующим образом переставляет параметры.
  }

  /** Вспомогательная функция для определения правильного плейсхолдера в
    * зависимости от типа SQL-колонки. Для ENUM-типов требуется специальный
    * каст: `?::StatusEnum`.
    */
  inline private def getPlaceholder(columnDef: ColumnDefinition) =
    columnDef.sqlType.value match
      case enumType if enumType.endsWith(enumSuf) => s"?::$enumType"
      case _ => "?"
```

## Итог: библиотека для типобезопасной генерации SQL

В результате мы создали не просто набор макросов, а целую [библиотеку для гарантированно корректной генерации SQL][sql-gen], которая:

1.  Исключает целый класс runtime-ошибок, связанных с SQL.
2.  Обеспечивает согласованность между структурой Scala-классов и таблиц БД.
3.  Легко масштабируется при добавлении новых сущностей.
4.  Позволяет сконцентрироваться на бизнес-логике, а не на рутинном написании шаблонного SQL.

**Примеры сгенерированных запросов для `UserType`:**

```dotty
generateCreateTableFor[UserType]
// Результат: 
// CREATE TABLE IF NOT EXISTS user_types 
// (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, 
// user_id UUID NOT NULL, displayed BOOLEAN NOT NULL DEFAULT TRUE, 
// ordinal INT NOT NULL DEFAULT 1000);

generateInsertReturningId[UserType, UserTypeName, UserTypeId](name)
// Результат: INSERT INTO user_types (name, user_id, displayed, ordinal) VALUES (?, ?, ?, ?) RETURNING id

generateSelectById[UserType, UserTypeId](1)
// Результат: SELECT id, name, user_id, displayed, ordinal FROM user_types WHERE id = '1' 

generateSelectByField[UserType, UserId]("ad03ed66-5fee-4d20-ac88-cbba1d5caf9e", "user_id")
// Результат: SELECT id, name, user_id, displayed, ordinal FROM user_types WHERE user_id = 'ad03ed66-5fee-4d20-ac88-cbba1d5caf9e'

generateUpdateById[UserType, Int](userType)
// Результат: UPDATE user_types SET name = ?, user_id = ?, displayed = ?, ordinal = ? WHERE id = ?

generateDeleteById[UserType, UserTypeId](id)
// Результат: DELETE FROM user_types WHERE id = '4'
```

**Примеры сгенерированных запросов для `BookTask`:**

```sql
-- Создание таблицы
CREATE TABLE IF NOT EXISTS books (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY, 
  name VARCHAR(255) NOT NULL, 
  user_id UUID NOT NULL, 
  status StatusEnum NOT NULL, 
  section_id INT NOT NULL, 
  goal_id INT NOT NULL, 
  ordinal INT NOT NULL, 
  start_date DATE, 
  end_date DATE, 
  information TEXT, 
  plan VARCHAR[], 
  authors VARCHAR(255) NOT NULL, 
  pages INT NOT NULL
); 

-- Вставка с возвратом ID
INSERT INTO books (name, user_id, status, section_id, goal_id, ordinal, start_date, 
  end_date, information, plan, authors, pages)
VALUES (?, ?, ?::StatusEnum, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id

-- Выборка по ID
SELECT id, name, user_id, status, section_id, goal_id, ordinal, start_date, 
  end_date, information, plan, authors, pages
FROM books WHERE id = '14c8fa94-4896-4449-82fb-5b1adec2504a'

-- Обновление
UPDATE books SET
  name = ?,
  user_id = ?,
  status = ?::StatusEnum,
  section_id = ?,
  goal_id = ?,
  ordinal = ?,
  start_date = ?,
  end_date = ?,
  information = ?,
  plan = ?,
  authors = ?,
  pages = ?
WHERE id = '14c8fa94-4896-4449-82fb-5b1adec2504a'

-- Удаление
DELETE FROM books WHERE id = '14c8fa94-4896-4449-82fb-5b1adec2504a'
```

Эта библиотека доступна в [Maven Central](https://mvnrepository.com/artifact/ru.scalabook/sql-gen).

Добавляем её в зависимости (`project/Dependencies.scala`):

```sbt
object scalabook {
  val sqlGen = "ru.scalabook" %% "sql-gen" % "0.1.3"
}

... 

val pdpLibs: Seq[ModuleID] =
  Seq(
    ...
    scalabook.sqlGen,
    ...
  )
```

---

**Ссылки:**

- [Ещё один ORM для SQL на Scala][sql-gen]
- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[sql-gen]: https://gitverse.ru/scalabook/sql-gen

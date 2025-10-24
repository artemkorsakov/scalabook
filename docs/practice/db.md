{%
showPrev = true
prevPage = task_types/localization.html
showNext = true
nextPage = db/db_crud.html
%}

# Работа с базой данных (CRUD операции)

## Зачем нужно хранилище данных?

В нашем приложении есть задачи из индивидуального плана развития, которые необходимо где-то сохранять. 
Но прежде чем выбрать конкретное место хранения (базу данных), 
давайте сначала определим, какое поведение мы ожидаем от системы хранения.

### Какие данные нам нужно хранить?

У нас есть три типа данных, которые необходимо сохранять:

1. **Пользовательские типы задач** - категории, которые создают пользователи
2. **Разделы** - группы или категории для организации задач
3. **Сами задачи** - конкретные задания, которые нужно выполнить

### Базовые операции (CRUD)

Для каждого типа данных нам нужны стандартные операции:

- **`create`** - создание новой записи в базе данных
- **`get`** - получение одной конкретной записи
- **`getAll`** - получение всех записей данного типа
- **`update`** - обновление существующей записи
- **`delete`** - удаление записи

### Обработка побочных эффектов

Каждая операция будет возвращать тип `IO` - это [специальный тип из функционального программирования](../fp/external-effects.md), 
который показывает, что при выполнении операции могут возникнуть побочные эффекты. 
Например:

- Пропадет соединение с базой данных
- Возникнут проблемы с сетью
- База данных будет временно недоступна

Использование `IO` помогает нам явно обозначить эти возможные проблемы в типе возвращаемого значения.

### Принцип единой ответственности

Наш сервис работы с базой данных должен следовать принципу единой ответственности - 
он отвечает **только** за взаимодействие с базой данных, без какой-либо бизнес-логики.

#### Пример для понимания:

Что произойдет, если мы попытаемся создать задачу для несуществующего раздела?

**Проблема ли это сервиса базы данных?** Нет!

Потому что:

- Сервис БД отвечает только за хранение данных
- Проверка корректности данных (например, существование раздела) - это бизнес-логика
- Кто-то другой должен убедиться, что все условия выполнены перед сохранением

### Защита целостности данных

Чтобы обеспечить согласованность данных в базе, мы ограничиваем доступ к этому сервису:

- Объявляем сервис **приватным** в пределах пакета
- Внешний код не может напрямую обращаться к сервису БД
- Все операции проходят через слой бизнес-логики, который проверяет корректность

## Объявление сервиса

Создаем интерфейс (трейт) `TaskService`:

```dotty
// src/main/scala/ru/scalabook/pdp/db/TaskService.scala
package ru.scalabook.pdp.db

import cats.effect.IO
import ru.scalabook.pdp.model.TaskType.*
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.model.Section.*

import java.util.UUID

// Сервис доступен только внутри пакета db
private[db] trait TaskService:

  // Операции для пользовательских типов задач
  def createUserType(userType: UserType): IO[Unit]
  def getUserType(id: UserTaskTypeId): IO[Option[UserType]]
  def getAllUserTypes: IO[List[UserType]]
  def updateUserType(userType: UserType): IO[Unit]
  def deleteUserType(id: UserTaskTypeId): IO[Unit]

  // Операции для разделов
  def createSection(section: Section): IO[Unit]
  def getSection(id: SectionId): IO[Option[Section]]
  def getAllSections: IO[List[Section]]
  def updateSection(section: Section): IO[Unit]
  def deleteSection(id: SectionId): IO[Unit]

  // Операции для задач
  def createTask(task: Task): IO[Unit]
  def getTask(id: UUID, taskTypeId: TaskTypeId): IO[Option[Task]]
  def getAllTasks: IO[List[Task]]
  def updateTask(task: Task): IO[Unit]
  def deleteTask(id: UUID, taskTypeId: TaskTypeId): IO[Unit]

end TaskService
```

### Что означают типы возвращаемых значений?

- **`IO[Unit]`** - операция имеет побочный эффект, но не возвращает конкретного значения 
  (просто подтверждение, что операция выполнена)
- **`IO[Option[Task]]`** - операция может вернуть задачу, а может и не найти её (вернуть `None`)
- **`IO[List[Task]]`** - операция возвращает список задач (возможно пустой)

### Важное замечание о бизнес-логике

Если нам нужно выполнить сложную операцию (например, перенести задачу из одной категории в другую), 
это **не** делается напрямую через сервис БД.

Вместо этого:

1. Бизнес-логика удаляет старую задачу
2. Создает новую задачу в нужной категории
3. Сервис БД просто выполняет эти две простые операции хранения

Таким образом, сервис `TaskService` остается простым и отвечает только за хранение данных, 
а вся сложная логика остается на верхних уровнях приложения.

## Реализация сервиса работы с базой данных

### Множественные варианты реализации

У нашего сервиса `TaskService` может быть **несколько различных реализаций** - 
это важный принцип проектирования, который дает нам гибкость.

#### Почему это важно?

- Мы можем начать с простой реализации для тестирования
- Позже перейти на промышленную базу данных без изменения основного кода
- Легко подменять реализации для разных окружений (тестового, продакшн)

#### Примеры возможных реализаций:

- **PostgreSQL** - промышленная реляционная база данных
- **SQLite** - легковесная база для разработки
- **Mock-реализация** - заглушка для unit-тестов

### Выбор PostgreSQL

В нашем примере мы рассмотрим работу с **PostgreSQL** - это надежная, полнофункциональная реляционная база данных, 
которая хорошо подходит для production-окружения.

> 💡 **Важно**: Принципы работы с другими базами данных аналогичны, отличаются только некоторые технические детали (синтаксис SQL, настройки подключения и т.д.)

### Библиотека Doobie

Для работы с базой данных мы будем использовать библиотеку **[Doobie](https://typelevel.org/doobie/index.html)**.

#### Что такое Doobie?

Doobie - это функциональная библиотека для работы с базами данных в Scala. Ее основные преимущества:

- **Функциональный подход** - интеграция с Cats Effect и IO
- **Типобезопасность** - многие ошибки обнаруживаются на этапе компиляции
- **Композируемость** - возможность строить сложные запросы из простых частей
- **Прозрачность** - легко понять, какие SQL-запросы выполняются

### Подключение зависимостей

Чтобы начать работать с Doobie, нам нужно добавить необходимые библиотеки в наш проект.

#### Структура зависимостей:

```dotty
object Dependencies {
  // ... другие зависимости ...

  // Блок для Doobie - библиотеки для работы с БД
  object doobie {
    private val version = "1.0.0-RC9"  // Версия Doobie

    // Основные модули Doobie:
    val core     = "org.tpolecat" %% "doobie-core"     % version  // Ядро библиотеки
    val free     = "org.tpolecat" %% "doobie-free"     % version  // Функциональные примитивы
    val postgres = "org.tpolecat" %% "doobie-postgres" % version  // Драйвер для PostgreSQL
  }

  // Блок для Iron - библиотеки для уточненных типов
  object iron {
    private val version = "3.2.0"

    val core       = "io.github.iltotore" %% "iron"            % version  // Основная библиотека
    val doobie     = "io.github.iltotore" %% "iron-doobie"     % version  // Интеграция с Doobie
    val pureconfig = "io.github.iltotore" %% "iron-pureconfig" % version  // Интеграция с конфигурацией
  }

  // ... другие зависимости ...

  // Список всех библиотек проекта
  val rootLibs: Seq[ModuleID] =
    Seq(
      // ... другие библиотеки ...

      // Подключаем Doobie модули:
      doobie.core,     // Основной функционал
      doobie.free,     // Функциональные операции
      doobie.postgres, // Поддержка PostgreSQL

      // ... остальные библиотеки ...
    )

  // ... остальная конфигурация ...
}
```

### Пояснение по модулям Doobie

#### 1. `doobie-core`

- Содержит основные типы и абстракции
- Предоставляет функциональный API для работы с БД
- Включает систему компиляции SQL-запросов

#### 2. `doobie-free`

- Реализация параметров контекста для построения программ работы с БД
- Позволяет комбинировать операции с БД функциональным способом

#### 3. `doobie-postgres`

- Специфический драйвер для PostgreSQL
- Поддержка PostgreSQL-специфичных типов данных и функций
- Настройки подключения для этой СУБД

### Что дальше?

После подключения зависимостей мы сможем:

1. Создать реализацию `TaskService` для PostgreSQL
2. Настроить подключение к базе данных
3. Реализовать все CRUD-операции с использованием Doobie
4. Обеспечить типобезопасность с помощью Iron

Такой подход позволяет нам легко заменить реализацию в будущем, 
если потребуется перейти на другую базу данных или способ хранения.

## Создание структуры базы данных: Миграции

### Зачем нужен отдельный сервис миграций?

Когда мы работаем с базой данных, нам нужно не только читать и писать данные, 
но и **создавать структуру** для хранения этих данных. 
Для этого мы создаем отдельный сервис миграций.

#### Что делает сервис миграций?

- **Создает таблицы** - структуры для хранения данных
- **Создает типы данных** - специальные типы, которые понимает PostgreSQL
- **Устанавливает связи** - определяет, как таблицы связаны между собой
- **Настраивает ограничения** - правила для обеспечения целостности данных

### Создание сервиса миграций

Создаем класс `PostgreSQLMigrationService`, который будет приватным внутри нашего пакета `db`:

```dotty
// src/main/scala/ru/scalabook/pdp/db/PostgreSQLMigrationService.scala
package ru.scalabook.pdp.db

import _root_.doobie.*
import _root_.doobie.implicits.*
import cats.*
import cats.effect.*

// Сервис доступен только внутри пакета db
private[db] class PostgreSQLMigrationService(
    url: String,      // Адрес базы данных
    user: String,     // Имя пользователя
    password: String  // Пароль
):
  import PostgreSQLMigrationService.*

  // Создаем трансактор - объект для выполнения операций с БД
  private val xa = Transactor.fromDriverManager[[x] =>> IO[x]](
    driver = "org.postgresql.Driver",  // Драйвер PostgreSQL
    url = url,                         // URL базы данных
    user = user,                       // Логин
    password = password,               // Пароль
    logHandler = None                  // Отключаем логирование (можно включить для отладки)
  )

  // Основной метод для инициализации базы данных
  def initialize(): IO[Unit] =
    initializeProgram.transact(xa)  // Выполняем программу инициализации через трансактор
```

### Что такое "сопутствующий объект"?

В Scala есть концепция [**сопутствующего объекта**](https://docs.scala-lang.org/ru/scala3/book/domain-modeling-tools.html#%D1%81%D0%BE%D0%BF%D1%83%D1%82%D1%81%D1%82%D0%B2%D1%83%D1%8E%D1%89%D0%B8%D0%B5-%D0%BE%D0%B1%D1%8A%D0%B5%D0%BA%D1%82%D1%8B) - 
это объект, который имеет то же имя, что и класс, и находится в том же файле.

#### Зачем он нужен?

1. **Хранит элементы, не зависящие от экземпляра класса** - статические методы и константы
2. **Приватные элементы видны в основном классе** - удобно для организации кода
3. **Четкое разделение** - сразу видно, что относится к экземпляру, а что нет

### Реализация SQL-запросов для создания таблиц

В сопутствующем объекте мы создаем SQL-запросы для построения структуры базы:

```dotty
private[db] object PostgreSQLMigrationService:

  // Создание таблицы для пользовательских типов задач
  private val createUserTypesTable: ConnectionIO[Int] =
    sql"""CREATE TABLE IF NOT EXISTS user_types (
          id INT NOT NULL CHECK (id >= 5),        // ID должен быть >= 5
          name VARCHAR(20) NOT NULL,              // Имя не более 20 символов
          PRIMARY KEY (id)                        // ID - первичный ключ
       );""".update.run

  // Создание таблицы для разделов
  private val createSectionsTable: ConnectionIO[Int] =
    sql"""CREATE TABLE IF NOT EXISTS sections (
          id INT NOT NULL CHECK (id >= 0),        // ID должен быть >= 0
          name VARCHAR(100) NOT NULL,             // Имя не более 100 символов
          PRIMARY KEY (id)                        // ID - первичный ключ
       );""".update.run

  // Создание специального типа для статусов
  private val createStatusEnum: ConnectionIO[Int] =
    sql"""CREATE TYPE StatusEnum AS ENUM
          ('InProgress', 'Planned', 'KnowledgeBase');""".update.run

  // Создание таблицы для книг
  private val createBooksTable: ConnectionIO[Int] =
    sql"""CREATE TABLE IF NOT EXISTS books (
          id UUID PRIMARY KEY,                    // Уникальный идентификатор
          name VARCHAR(200) NOT NULL,             // Название книги
          status StatusEnum NOT NULL,             // Статус из нашего ENUM типа
          section_id INT NOT NULL,                // Ссылка на раздел
          order_num INT NOT NULL CHECK (order_num > 0),  // Порядковый номер > 0
          start_date DATE,                        // Дата начала чтения
          end_date DATE,                          // Дата окончания
          information TEXT,                       // Дополнительная информация
          plan VARCHAR[],                         // Массив строк - план чтения
          authors VARCHAR(255),                   // Авторы
          pages INT NOT NULL CHECK (pages > 0)    // Количество страниц > 0
       );""".update.run

  // Создание внешнего ключа для связи с таблицей разделов
  private val addForeignKeysToBooksTable: ConnectionIO[Int] =
    sql"""ALTER TABLE books
          ADD CONSTRAINT fk_section
          FOREIGN KEY (section_id) REFERENCES sections(id);  // Связь с таблицей sections
       """.update.run

  // ... аналогично создаем таблицы для статей, подкастов, видео, курсов и пользовательских задач

  // Основная программа инициализации - выполняет все запросы последовательно
  private val initializeProgram: ConnectionIO[Unit] =
    for
      _ <- createStatusEnum        // Сначала создаем типы
      _ <- createUserTypesTable    // Затем базовые таблицы
      _ <- createSectionsTable
      _ <- createBooksTable        // Потом таблицы с задачами
      _ <- addForeignKeysToBooksTable  // И только потом устанавливаем связи
      _ <- createArticlesTable
      _ <- addForeignKeysToArticlesTable
      _ <- createPodcastsTable
      _ <- addForeignKeysToPodcastsTable
      _ <- createVideosTable
      _ <- addForeignKeysToVideosTable
      _ <- createCoursesTable
      _ <- addForeignKeysToCoursesTable
      _ <- createUserTasksTable
      _ <- addForeignKeysToUserTasksTable
    yield ()  // Возвращаем Unit - программа выполнена
end PostgreSQLMigrationService
```

### Важные особенности нашего подхода

#### 1. **Плоская структура таблиц**

Мы храним каждый тип задачи в отдельной таблице (книги, статьи, подкасты и т.д.) вместо того, 
чтобы использовать одну таблицу со сложными полями или JSON. Это дает:

- **Производительность** - простые запросы работают быстрее
- **Типобезопасность** - каждая таблица имеет строгую структуру
- **Понятность** - легче понимать структуру данных

#### 2. **Ограничения целостности**

Мы используем CHECK-ограничения для обеспечения правильности данных:

- `CHECK (id >= 5)` - ID пользовательских типов должен быть ≥ 5
- `CHECK (pages > 0)` - количество страниц должно быть положительным
- `NOT NULL` - обязательные поля

#### 3. **Внешние ключи**

С помощью `FOREIGN KEY` мы устанавливаем связи между таблицами, что гарантирует:

- Нельзя создать задачу для несуществующего раздела
- При удалении раздела можно настроить каскадное удаление задач

#### 4. **Порядок выполнения**

Важно выполнять операции в правильном порядке:

1. Создать типы данных (ENUM)
2. Создать базовые таблицы
3. Создать таблицы с задачами
4. Установить связи между таблицами

### Как использовать?

Теперь мы можем создать экземпляр `PostgreSQLMigrationService` и выполнить миграцию:

```dotty
val migration = new PostgreSQLMigrationService(
  "jdbc:postgresql://localhost:5432/mydb",
  "username",
  "password"
)

// Выполняем миграцию
migration.initialize().unsafeRunSync()
```

Это создаст всю необходимую структуру базы данных для нашего приложения!

## Реализация сервиса работы с PostgreSQL

### Создание конкретной реализации для PostgreSQL

Мы создаем класс `PostgreSQLTaskService`, который является **конкретной реализацией** нашего абстрактного интерфейса `TaskService`. 
Это следует принципу программирования: "программируйте на уровне интерфейсов, а не реализаций".

#### Что это значит на практике?

- У нас есть абстрактный интерфейс `TaskService`, который определяет **ЧТО** мы хотим делать
- Класс `PostgreSQLTaskService` определяет **КАК** именно мы это делаем для PostgreSQL
- В будущем мы можем создать другие реализации (например, для MySQL или тестовой базы), не меняя основной код

### Структура класса

```dotty
// src/main/scala/ru/scalabook/pdp/db/PostgreSQLTaskService.scala
package ru.scalabook.pdp.db

// Импорты для работы с Doobie (библиотекой для БД)
import _root_.doobie.*
import _root_.doobie.implicits.*
import _root_.doobie.postgres.*
import _root_.doobie.postgres.implicits.*

// Импорты для функционального программирования
import cats.*
import cats.effect.*
import cats.implicits.*

// Импорты для Iron (библиотеки уточненных типов)
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.doobie.given

// Импорты наших моделей данных
import ru.scalabook.pdp.model
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.model.Section.*
import ru.scalabook.pdp.model.Section.SectionName.given
import ru.scalabook.pdp.model.Status.given
import ru.scalabook.pdp.model.TaskType.*
import ru.scalabook.pdp.model.TaskWithDuration.given

import java.util.UUID

// Класс доступен только внутри пакета db
private[db] class PostgreSQLTaskService(
    // Адрес базы данных (например: "jdbc:postgresql://localhost:5432/mydb")
    url: String,      
    user: String,     // Имя пользователя БД
    password: String  // Пароль пользователя БД
) extends TaskService:  // Наследуемся от нашего основного интерфейса

  import PostgreSQLTaskService.*

  // Создаем трансактор - основной объект для работы с базой данных
  private val xa = Transactor.fromDriverManager[[x] =>> IO[x]](
    driver = "org.postgresql.Driver",  // Указываем драйвер PostgreSQL
    url = url,                         // URL подключения к базе
    user = user,                       // Логин
    password = password,               // Пароль
    logHandler = None                  // Отключаем логирование SQL-запросов
  )

  // Здесь будут реализации всех методов из TaskService...
  // def createUserType(userType: UserType): IO[Unit] = ...
  // def getUserType(id: UserTaskTypeId): IO[Option[UserType]] = ...
  // и так далее для всех CRUD операций
```

### Детальное объяснение ключевых моментов

#### 1. **Наследование от TaskService**

```dotty
class PostgreSQLTaskService(...) extends TaskService:
```

Это означает, что наш класс **обязуется реализовать все методы**, объявленные в интерфейсе `TaskService`. 
Компилятор Scala проверит, что мы действительно реализовали все необходимые методы.

#### 2. **Приватность сервиса**

```dotty
private[db] class PostgreSQLTaskService
```

Модификатор `private[db]` означает, что этот класс доступен **только внутри пакета `db`**. Это важная мера безопасности:

- Внешний код не может напрямую создавать экземпляры этого сервиса
- Все взаимодействие происходит через фасады и бизнес-логику
- Мы контролируем, как именно используется доступ к базе данных

#### 3. **Трансактор (Transactor)**

```dotty
private val xa = Transactor.fromDriverManager[[x] =>> IO[x]](
  driver = "org.postgresql.Driver",
  url = url,
  user = user,
  password = password,
  logHandler = None
)
```

**Трансактор** - это центральный объект в Doobie, который:

- Управляет подключениями к базе данных
- Выполняет SQL-запросы
- Обрабатывает транзакции
- Преобразует результаты запросов в Scala-типы

#### 4. **Импорты для Iron**

```dotty
import io.github.iltotore.iron.doobie.given
```

Эти импорты обеспечивают интеграцию между **Iron** (библиотекой уточненных типов) и **Doobie**. Это позволяет:

- Автоматически проверять ограничения типов при чтении из БД
- Гарантировать, что данные из базы соответствуют нашим бизнес-правилам
- Например, если в базе число страниц отрицательное, а у нас тип `PositiveInt`, это будет обработано корректно

### Принцип "только необходимые методы"

Подчеркнём важный принцип:

> "Сервис должен реализовывать только методы `TaskService`, т.е. только то, что ожидает пользователь."

#### Что это означает?

- Класс `PostgreSQLTaskService` **не добавляет новых публичных методов**
- Он реализует **только то, что объявлено в интерфейсе**
- Это следует принципу **минимального интерфейса** - предоставлять ровно столько функциональности, сколько нужно

#### Пример неправильного подхода:

```dotty
// НЕПРАВИЛЬНО - добавляем лишние методы
class PostgreSQLTaskService extends TaskService:
  def createUserType(...): IO[Unit] = ...
  def getUserType(...): IO[Option[UserType]] = ...
  // Лишний метод, которого нет в интерфейсе
  def internalDatabaseOptimization(): IO[Unit] = ...  // ❌ НЕ ДЕЛАЙТЕ ТАК!
```

#### Пример правильного подхода:

```dotty
// ПРАВИЛЬНО - только методы из интерфейса
class PostgreSQLTaskService extends TaskService:
  def createUserType(...): IO[Unit] = ...    // ✅ Из интерфейса
  def getUserType(...): IO[Option[UserType]] = ...  // ✅ Из интерфейса
  // Все остальные методы из TaskService...
```

Если нам нужны внутренние вспомогательные методы, 
мы делаем их **приватными** внутри класса или выносим в сопутствующий объект.

### Преимущества такого подхода

1. **Соблюдение контракта** - мы гарантируем, что реализация соответствует ожиданиям
2. **Возможность замены** - легко подменить PostgreSQL на другую БД
3. **Тестируемость** - можем создать mock-реализацию для тестов
4. **Безопасность** - ограниченный доступ предотвращает неправильное использование

Этот класс становится "рабочей лошадкой", которая знает все детали работы с PostgreSQL, 
но предоставляет только стандартный интерфейс для внешнего мира.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

{%
showPrev = true
prevPage = sql_generation.html
showNext = true
nextPage = db_test.html
%}

# Реализация CRUD операций

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



## Реализация CRUD операций для пользовательских типов задач

### Структура CRUD операций

Давайте подробно разберем, как реализованы базовые операции (Create, Read, Update, Delete) 
для пользовательских типов задач в нашем сервисе.

### Реализация в основном классе

В классе `PostgreSQLTaskService` мы реализуем методы, объявленные в интерфейсе `TaskService`:

```dotty
private[db] class PostgreSQLTaskService(
    url: String,
    user: String,
    password: String
) extends TaskService:
  // ... предыдущий код ...

  // СОЗДАНИЕ - добавляем новый пользовательский тип
  def createUserType(userType: UserType): IO[Unit] =
    insertUserTypeStatement(userType)  // Выполняем SQL-запрос
      .transact(xa)                    // Преобразуем в IO с помощью трансактора
      .void                            // Игнорируем количество обновленных строк

  // ЧТЕНИЕ ОДНОГО - получаем тип по ID
  def getUserType(id: UserTaskTypeId): IO[Option[UserType]] =
    getUserTypeStatement(id)           // SQL-запрос для поиска по ID
      .transact(xa)                    // Выполняем через трансактор

  // ЧТЕНИЕ ВСЕХ - получаем все пользовательские типы
  def getAllUserTypes: IO[List[UserType]] =
    getAllUserTypesStatement           // SQL-запрос без параметров
      .transact(xa)                    // Выполняем и возвращаем список

  // ОБНОВЛЕНИЕ - изменяем существующий тип
  def updateUserType(userType: UserType): IO[Unit] =
    updateUserTypeStatement(userType)  // SQL-запрос обновления
      .transact(xa)
      .void                            // Игнорируем количество обновленных строк

  // УДАЛЕНИЕ - удаляем тип по ID
  def deleteUserType(id: UserTaskTypeId): IO[Unit] =
    deleteUserTypeStatement(id)        // SQL-запрос удаления
      .transact(xa)
      .void                            // Игнорируем количество удаленных строк

  // ... остальные методы ...
end PostgreSQLTaskService
```

### Реализация SQL-запросов в сопутствующем объекте

Все SQL-запросы вынесены в сопутствующий объект для лучшей организации кода
и потому, что они не зависят от экземпляра класса:

```dotty
private[db] object PostgreSQLTaskService:

  // SQL-запрос для ВСТАВКИ нового пользовательского типа
  private val insertUserTypeStatement: UserType => ConnectionIO[Int] =
    userType =>
      sql"""INSERT INTO user_types (id, name)
            VALUES (${userType.id}, ${userType.name})
            ON CONFLICT (id)
            DO NOTHING;"""  // Если запись с таким ID уже есть - ничего не делаем
        .update.run         // Выполняем как UPDATE-запрос, 
                            // возвращаем количество измененных строк

  // SQL-запрос для ПОЛУЧЕНИЯ одного типа по ID
  private val getUserTypeStatement: UserTaskTypeId => ConnectionIO[Option[UserType]] = id =>
    sql"""SELECT id, name FROM user_types WHERE id = $id"""
      .query[UserType]      // Указываем, в какой тип преобразовать результат
      .option               // Возвращаем Option, ведь задачи с заданным id может и не быть

  // SQL-запрос для ПОЛУЧЕНИЯ всех типов
  private val getAllUserTypesStatement: ConnectionIO[List[UserType]] =
    sql"""SELECT id, name FROM user_types"""
      .query[UserType]      // Преобразуем каждую строку в UserType
      .to[List]             // Собираем все результаты в список

  // SQL-запрос для ОБНОВЛЕНИЯ типа
  private val updateUserTypeStatement: UserType => ConnectionIO[Int] = userType =>
    sql"""UPDATE user_types
          SET name = ${userType.name}
          WHERE id = ${userType.id}"""
      .update.run           // Возвращаем количество обновленных строк

  // SQL-запрос для УДАЛЕНИЯ типа
  private val deleteUserTypeStatement: UserTaskTypeId => ConnectionIO[Int] = id =>
    sql"""DELETE FROM user_types WHERE id = $id"""
      .update.run           // Возвращаем количество удаленных строк

  // ... аналогичные запросы для других сущностей ...
end PostgreSQLTaskService
```

### Как работает "магия" преобразования типов?

#### Роль параметров контекста

Ключевую роль играет импорт:

```dotty
import io.github.iltotore.iron.doobie.given
```

Этот импорт подтягивает [**параметры контекста** (given instances)](https://docs.scala-lang.org/ru/scala3/book/ca-context-parameters.html)
для уточненных типов Iron.

#### Что такое параметры контекста?

**Параметры контекста** (given в Scala 3, implicit в Scala 2) - 
это механизм для автоматической передачи контекста между частями программы.

##### Аналогия из реальной жизни:

Представьте, что вы заходите в комнату (вызываете функцию). В комнате уже есть:

- Освещение (контекст логирования)
- Температура воздуха (контекст пользовательской сессии)
- Правила поведения (контекст кодирования типов)

Вам не нужно приносить эти вещи с собой - они уже есть в контексте комнаты.

#### Как это работает в нашем коде?

Когда Doobie видит:

```dotty
.query[UserType]  // Преобразуй результат запроса в тип UserType
```

Он ищет в контексте (scope) **неявный параметр** типа `Get[UserType]` - который знает, как преобразовать данные из базы в наш Scala-тип.

Благодаря импорту `iron.doobie.given`, компилятор автоматически находит эти преобразования для наших уточненных типов.

### Правильное использование параметров контекста

#### ❌ НЕПРАВИЛЬНОЕ использование:

```dotty
// НЕ ДЕЛАЙТЕ ТАК! Это злоупотребление неявными параметрами
def createUserType(using userType: UserType): IO[Unit] =
  // Теперь можно вызывать createUserType() без параметров
  // Но это ОЧЕНЬ ПЛОХАЯ практика!
```

**Почему это плохо?**

- Код становится неявным и сложным для понимания
- Теряется ясность - откуда берется `userType`?
- Сложно тестировать
- Легко допустить ошибку

#### ✅ ПРАВИЛЬНОЕ использование:

```dotty
// Явная передача параметров - ПРАВИЛЬНЫЙ подход
def createUserType(userType: UserType): IO[Unit] =
  // Ясно видно, что метод принимает параметр
  // Код понятен и легко тестируется
```

### Когда использовать параметры контекста правильно?

Неявные параметры следует использовать для **неизменяемого контекста**, например:

#### Правильные примеры контекста:

```dotty
// Конфигурация приложения - не меняется во время работы
def processData(data: Data)(using config: AppConfig): IO[Result]

// Сессия пользователя - контекст для группы операций
def getUserProfile()(using session: UserSession): IO[UserProfile]

// Информация о кодировании типов - как раз наш случай!
def readFromDB[T]()(using encoder: Get[T]): IO[T]

// Логгер - обычно один на все приложение
def businessOperation()(using logger: Logger): IO[Unit]
```

#### Что является хорошим контекстом?

- **Глобальные** настройки
- **Инфраструктурные** компоненты (логгеры, кодеки, конфигурация)
- **Сессии** в пределах определенной области видимости
- **Неизменяемая** конвертация из одной структуры данных в другую

### Итог: принципы хорошего тона

1. **Используйте явные параметры** для бизнес-данных (то, что вводит пользователь)
2. **Используйте неявные параметры** для контекста (то, что не меняется в рамках операции)
3. **Будьте осторожны** - не злоупотребляйте неявными параметрами ради "экономии места"
4. **Код должен быть понятным** - если использование неявных параметров делает код сложным для понимания, лучше использовать явные параметры

Таким образом, в нашем сервисе мы правильно используем неявные параметры для преобразования типов (что является контекстом), 
но передаем бизнес-данные явно через параметры методов.

## Реализация CRUD операций для разделов (Sections)

### Что такое разделы в нашей системе?

**Разделы** - это категории или группы, которые помогают организовать задачи. Например:

- "Рабочие задачи"
- "Личное развитие"
- "Срочные дела"
- "Долгосрочные проекты"

Каждая задача принадлежит к определенному разделу, что позволяет структурировать и группировать задачи.

### Структура CRUD операций для разделов

Давайте подробно разберем каждую операцию для работы с разделами.

В классе `PostgreSQLTaskService` мы реализуем методы для работы с разделами:

```dotty
private[db] class PostgreSQLTaskService(
    url: String,
    user: String,
    password: String
) extends TaskService:
  // ... предыдущий код для пользовательских типов ...

  // СОЗДАНИЕ РАЗДЕЛА - добавляем новый раздел в систему
  def createSection(section: Section): IO[Unit] =
    insertSectionStatement(section)  // Подготавливаем SQL-запрос для вставки
      .transact(xa)                  // Выполняем через трансактор (подключение к БД)
      .void                          // Игнорируем количество обновленных строк

  // ПОЛУЧЕНИЕ РАЗДЕЛА ПО ID - находим конкретный раздел по идентификатору
  def getSection(id: SectionId): IO[Option[Section]] =
    getSectionStatement(id)          // SQL-запрос для поиска по ID
      .transact(xa)                  // Выполняем запрос
      // Возвращаем Option[Section] - либо найденный раздел, либо None если не найден

  // ПОЛУЧЕНИЕ ВСЕХ РАЗДЕЛОВ - получаем полный список всех разделов
  def getAllSections: IO[List[Section]] =
    getAllSectionsStatement          // SQL-запрос без параметров
      .transact(xa)                  // Выполняем и получаем список разделов

  // ОБНОВЛЕНИЕ РАЗДЕЛА - изменяем информацию о существующем разделе
  def updateSection(section: Section): IO[Unit] =
    updateSectionStatement(section)  // SQL-запрос для обновления
      .transact(xa)
      .void                          // Игнорируем количество обновленных строк

  // УДАЛЕНИЕ РАЗДЕЛА - удаляем раздел по идентификатору
  def deleteSection(id: SectionId): IO[Unit] =
    deleteSectionStatement(id)       // SQL-запрос для удаления
      .transact(xa)
      .void                          // Игнорируем количество удаленных строк

  // ... остальные методы для задач и других сущностей ...
end PostgreSQLTaskService
```

### Реализация SQL-запросов в сопутствующем объекте

Все конкретные SQL-запросы вынесены в сопутствующий объект для лучшей организации кода:

```dotty
private[db] object PostgreSQLTaskService:
  // ... запросы для пользовательских типов ...

  // SQL-запрос для ДОБАВЛЕНИЯ нового раздела
  private val insertSectionStatement: Section => ConnectionIO[Int] =
    section =>  // Принимаем объект Section как параметр
      sql"""INSERT INTO sections (id, name)
            VALUES (${section.id}, ${section.name})
            ON CONFLICT (id)
            DO NOTHING;"""
            /*
             * ON CONFLICT (id) DO NOTHING - важная оптимизация:
             * Если раздел с таким ID уже существует в базе,
             * то ничего не делаем (не вызываем ошибку)
             * Это полезно при повторных вызовах или в конкурентных сценариях
             */
        .update.run  // Выполняем как UPDATE-запрос, 
                     // возвращаем количество добавленных строк (0 или 1)

  // SQL-запрос для ПОИСКА РАЗДЕЛА по идентификатору
  private val getSectionStatement: SectionId => ConnectionIO[Option[Section]] =
    id =>  // Принимаем SectionId как параметр
      sql"""SELECT id, name FROM sections WHERE id = $id"""
        /*
         * Простой SELECT запрос:
         * - Выбираем поля id и name из таблицы sections
         * - Фильтруем по переданному id
         */
        .query[Section]  // Указываем Doobie преобразовать результат в тип Section
        .option          // Возвращаем Option - либо Some(section), либо None если не найден

  // SQL-запрос для ПОЛУЧЕНИЯ ВСЕХ РАЗДЕЛОВ
  private val getAllSectionsStatement: ConnectionIO[List[Section]] =
    sql"""SELECT id, name FROM sections"""
      /*
       * Простой SELECT без условий:
       * - Выбираем все записи из таблицы sections
       * - Не используем WHERE условие
       */
      .query[Section]  // Преобразуем каждую строку результата в объект Section
      .to[List]        // Собираем все результаты в Scala List

  // SQL-запрос для ОБНОВЛЕНИЯ информации о разделе
  private val updateSectionStatement: Section => ConnectionIO[Int] =
    section =>  // Принимаем обновленный объект Section
      sql"""UPDATE sections
            SET name = ${section.name}
            WHERE id = ${section.id}"""
            /*
             * UPDATE запрос:
             * - Обновляем поле name для раздела с указанным id
             * - WHERE условие гарантирует, что обновим только нужную запись
             */
        .update.run  // Возвращаем количество обновленных строк (0 или 1)

  // SQL-запрос для УДАЛЕНИЯ раздела
  private val deleteSectionStatement: SectionId => ConnectionIO[Int] =
    id =>  // Принимаем SectionId для удаления
      sql"""DELETE FROM sections WHERE id = $id"""
        /*
         * DELETE запрос:
         * - Удаляем запись из таблицы sections с указанным id
         * - Важно: если на раздел есть ссылки из других таблиц (например, задач),
         *   может возникнуть ошибка внешнего ключа
         */
        .update.run  // Возвращаем количество удаленных строк (0 или 1)

  // ... аналогичные запросы для задач и других сущностей ...
end PostgreSQLTaskService
```

### Детальное объяснение ключевых моментов

#### 1. **Типы возвращаемых значений**

- **`ConnectionIO[Int]`** - операции, которые возвращают количество измененных строк
- **`ConnectionIO[Option[Section]]`** - операции поиска, которые могут вернуть либо раздел, либо ничего
- **`ConnectionIO[List[Section]]`** - операции, возвращающие коллекции
- **`IO[Unit]`** - публичные методы, где нам не важен результат, только факт выполнения

#### 2. **Преобразование `.transact(xa)`**

Метод `.transact(xa)` преобразует `ConnectionIO` в `IO`:

- `ConnectionIO` - описание операции с БД (еще не выполненной)
- `IO` - эффект, который можно выполнить (уже готов к выполнению)

#### 3. **Метод `.void`**

Метод `.void` используется когда:

- Нам не важно возвращаемое значение (количество измененных строк)
- Нас интересует только факт выполнения операции
- Мы хотим явно показать, что игнорируем результат

#### 4. **Обработка конфликтов `ON CONFLICT`**

Конструкция `ON CONFLICT (id) DO NOTHING` полезна для:

- **Идемпотентности** - повторные вызовы с теми же данными не вызывают ошибок
- **Конкурентности** - несколько потоков могут пытаться создать один и тот же раздел
- **Устойчивости** - приложение не падает при попытке создать дубликат

### Важные замечания по безопасности

1. **SQL-инъекции** - Doobie автоматически экранирует параметры, предотвращая SQL-инъекции
2. **Типобезопасность** - компилятор проверяет соответствие типов в запросах
3. **Транзакционность** - каждая операция выполняется в отдельной транзакции
4. **Обработка ошибок** - ошибки БД правильно оборачиваются в `IO` эффекты

Такой подход обеспечивает надежную и безопасную работу с разделами в нашей системе управления задачами.

## Реализация CRUD операций для задач (Tasks)

### Особенности хранения задач

В нашей системе задачи имеют несколько важных особенностей, которые влияют на реализацию:

#### Проблема: разные типы задач с разными полями

У нас есть несколько типов задач, каждый со своей специфической структурой:

- **BookTask** - книги (авторы, страницы)
- **ArticleTask** - статьи (URL, время чтения)
- **PodcastTask** - подкасты (длительность, ссылка)
- **VideoTask** - видео (платформа, длительность)
- **CourseTask** - курсы (платформа, прогресс)
- **UserTask** - пользовательские задачи (шаги, дедлайн)

Каждый тип имеет уникальные поля, которые нужно хранить в базе данных.

### Решение: отдельные таблицы для каждого типа

Мы используем стратегию "Table per Concrete Class" - каждая конкретная задача хранится в своей таблице. Это дает:

- **Производительность** - нет NULL полей
- **Типобезопасность** - каждая таблица имеет строгую схему
- **Понятность** - ясная структура данных

### Специальные преобразования типов

Не для всех типов определены неявные преобразования.
Например, для перечислений `enum Status`, списка задач `List[TaskStep]` и продолжительности `FiniteDuration`
нет автоматического преобразования, поэтому нам необходимо помочь doobie преобразовать эти типы данных,
путем объявления неявной конвертации `given Meta[A]`.

#### 1. Преобразование для перечисления Status

```dotty
// Наше перечисление статусов
enum Status:
  case InProgress, Planned, KnowledgeBase

object Status:
  // Ручное определение преобразования Status <-> String
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**Как это работает:**

- `Meta[String]` - берем базовый преобразователь для строк
- `.timap(f)(g)` - создаем двунаправленное преобразование:
    - `Status.valueOf` - преобразует строку из БД в `Status`
    - `_.toString` - преобразует `Status` в строку для БД

**Безопасность этого подхода:**

- Преобразование безопасно, потому что только наш код пишет в БД
- Мы контролируем, какие статусы попадают в базу
- Поэтому в базе не окажется неизвестный статус и `Status.valueOf` не выбросит исключение

**Риски при изменении статусов:**

Но есть риски: если нужно переименовать статус (например, `InProgress` → `InWork`), потребуется:

1. Обновить `enum` в коде
2. Написать миграцию для обновления типов в БД
3. Обновить статус во всех существующих записях (иначе при чтении вылетит исключение)

Это архитектурное изменение, которое требует планирования!

#### 2. Преобразование для уточненного типа TaskStep

```dotty
// TaskStep - строка с ограничениями длины
type TaskStep = TaskStep.T
object TaskStep
    extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]:

  // Преобразование массива строк в список TaskStep
  given Meta[List[TaskStep]] =
    Meta[Array[String]].timap(
      _.map(TaskStep.applyUnsafe).toList  // Массив -> List[TaskStep]
    )(_.map(_.toString).toArray)          // List[TaskStep] -> массив
```

**Что здесь происходит:**

- PostgreSQL хранит массивы как `Array[String]`
- Мы преобразуем их в `List[TaskStep]` с проверкой ограничений
- `applyUnsafe` используется, потому что мы доверяем данным из БД (они были проверены при сохранении)

#### 3. Преобразование для FiniteDuration

```dotty
sealed trait TaskWithDuration extends Task:
  val duration: FiniteDuration

object TaskWithDuration:
  // Преобразование Long (миллисекунды) в FiniteDuration
  given Meta[FiniteDuration] =
    Meta[Long].timap(
      FiniteDuration(_, MILLISECONDS)  // Long -> FiniteDuration
    )(_.toMillis)                      // FiniteDuration -> Long
```

### Организация неявных экземпляров

Вспомним, что **неявные экземпляры** - это специальные значения, 
которые компилятор Scala автоматически подставляет в код, когда они требуются по контексту.

##### Аналогия из реальной жизни:

Представьте, что вы приходите в ресторан. Вам не нужно приносить с собой:

- 🍽️ Столовые приборы (они уже есть на столе)
- 📋 Меню (официант принесет)
- 💡 Освещение (уже создает атмосферу)

Эти вещи являются **контекстом** - они уже предоставлены заведением и автоматически доступны.

#### Рекомендуемая иерархия расположения

Когда компилятор Scala ищет неявный экземпляр, он следует определенному порядку. 
Мы должны располагать экземпляры в порядке приоритета:

##### 1. 🥇 **Сопутствующий объект** (Наивысший приоритет)

```dotty
// Наш тип данных
enum Status:
  case InProgress, Planned, KnowledgeBase

// СОПУТСТВУЮЩИЙ ОБЪЕКТ - ЛУЧШИЙ ВАРИАНТ
object Status:
  // Неявный экземпляр находится ВНУТРИ объекта Status
  given Meta[Status] =
    Meta[String].timap(Status.valueOf)(_.toString)
```

**Почему это лучше всего:**

- ✅ **Автоматическое обнаружение** - компилятор всегда найдет этот экземпляр
- ✅ **Избегание дубликатов** - может быть только один такой экземпляр
- ✅ **Легкость поиска** - разработчики знают где искать в первую очередь
- ✅ **Предотвращение конфликтов** - нет неоднозначностей

##### 2. 🥈 **Класс типов** (Type class companion)

```dotty
// Если не можем изменить исходный тип (Status),
// помещаем в сопутствующий объект класса типов

object Meta:  // Сопутствующий объект класса типов Meta
  given Meta[Status] =
    Meta[String].timap(Status.valueOf)(_.toString)
```

**Когда использовать:**

- Когда мы не контролируем исходный тип `Status` (например, он из внешней библиотеки)
- Когда хотим группировать связанные экземпляры

##### 3. 🥉 **Специализированный объект** (Fallback вариант)

```dotty
// Создаем специальный объект для наших экземпляров
object DatabaseInstances:
  given Meta[Status] =
    Meta[String].timap(Status.valueOf)(_.toString)

// И явно импортируем там, где нужно
import DatabaseInstances.given
```

**Когда использовать:**

- Для организации экземпляров по функциональным областям
- Когда нужно явно контролировать импорты

#### ОПАСНОСТЬ: Сиротствующие экземпляры (Orphan Instances)

##### Что это такое?

**Сиротствующий экземпляр** - это неявный экземпляр, который объявлен:

- НЕ в сопутствующем объекте типа данных
- НЕ в сопутствующем объекте класса типов
- А в каком-то случайном месте

Подробнее о ["сиротствующих экземплярах" (orphan-instances)](https://pjrt.medium.com/orphan-instances-in-scala-322caa78e382)

##### ❌ ПЛОХОЙ ПРИМЕР:

```dotty
// Файл: src/main/scala/utils/RandomFile.scala
package utils

// СИРОТСТВУЮЩИЙ ЭКЗЕМПЛЯР - ОПАСНО!
given Meta[Status] =
  Meta[String].timap(Status.valueOf)(_.toString)

// ПРОБЛЕМА: этот экземпляр объявлен в случайном месте,
// не связанном ни со Status, ни с Meta
```

##### Проблемы сиротствующих экземпляров:

###### 1. 🕵️ **Трудно найти**

```dotty
// Другой разработчик пытается понять, почему код компилируется:
val status: Status = ...
val result = sql"SELECT status FROM tasks".query[Status]  // ⚡ Магия! Но КАК?

// Приходится искать по всему проекту где объявлен given Meta[Status]
```

###### 2. ⚔️ **Конфликты и неоднозначности**

```dotty
// Файл A.scala
given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)

// Файл B.scala
given Meta[Status] = Meta[Int].timap(i => Status.values(i))(_.ordinal)

// При импорте обоих файлов - КОНФЛИКТ!
// Компилятор не знает какой экземпляр использовать
```

###### 3. 🎭 **Неявное поведение**

```dotty
// Код работает "магически", непонятно почему
def saveTask(task: Task): ConnectionIO[Unit] =
  sql"INSERT INTO tasks (status) VALUES ($task)".update.run
  // Где объявлено преобразование Task -> SQL параметр? Неясно!
```

###### 4. 🔧 **Сложность отладки**

```dotty
// Ошибка компиляции непонятна:
// "could not find implicit value for parameter meta: Meta[Status]"

// Почему? Может быть:
// - Не тот импорт?
// - Конфликт экземпляров?
// - Экземпляр вообще существует?
```

#### Практические рекомендации

##### ✅ ДЕЛАЙТЕ ТАК:

```dotty
// 1. Сначала пробуем в сопутствующем объекте
object MyDataType:
  given MyTypeClass[MyDataType] = ...

// 2. Если не получается - в сопутствующем объекте класса типов
object MyTypeClass:
  given MyTypeClass[MyDataType] = ...

// 3. И только потом - в специализированном объекте
object MyProjectInstances:
  given MyTypeClass[MyDataType] = ...
```

##### ❌ НЕ ДЕЛАЙТЕ ТАК:

```dotty
// НЕ создавайте сиротствующие экземпляры!
package com.example.utils

given TypeClass[SomeType] = ...  // СЛУЧАЙНОЕ МЕСТО!

package com.example.service

given TypeClass[AnotherType] = ...  // ЕЩЕ ОДНО СЛУЧАЙНОЕ МЕСТО!
```

#### Итог

Правильная организация неявных экземпляров - это важная практика, которая делает код:

- 🔍 **Понятным** - легко найти преобразования
- 🛡️ **Надежным** - избегаем конфликтов
- 🔧 **Поддерживаемым** - ясная структура
- 🚀 **Производительным** - компилятор быстрее находит экземпляры

### Реализация CRUD операций в сервисе

#### Создание задачи (Create)

```dotty
def createTask(task: Task): IO[Unit] =
  task match
    case book: BookTask =>
      insertBookTaskStatement(book).transact(xa).void
    case article: ArticleTask =>
      insertArticleTaskStatement(article).transact(xa).void
    case podcast: PodcastTask =>
      insertPodcastTaskStatement(podcast).transact(xa).void
    case video: VideoTask =>
      insertVideoTaskStatement(video).transact(xa).void
    case course: CourseTask =>
      insertCourseTaskStatement(course).transact(xa).void
    case userTask: UserTask =>
      insertUserTaskStatement(userTask).transact(xa).void
```

Сопоставление с шаблоном позволяет:

- Определить конкретный тип задачи
- Вызвать соответствующий SQL-запрос
- Обеспечить типобезопасность

#### Получение всех задач (Read All)

```dotty
def getAllTasks: IO[List[Task]] =
  // Параллельно запрашиваем все типы задач
  (
    getAllBookTasksStatement.transact(xa),      // книги
    getAllArticleTasksStatement.transact(xa),   // статьи
    getAllPodcastTasksStatement.transact(xa),   // подкасты
    getAllVideoTasksStatement.transact(xa),     // видео
    getAllCourseTasksStatement.transact(xa),    // курсы
    getAllUserTasksStatement.transact(xa)       // пользовательские
  ).mapN {
    case (books, articles, podcasts, videos, courses, userTasks) =>
      // Объединяем все в один список
      books ++ articles ++ podcasts ++ videos ++ courses ++ userTasks
  }
```

**Особенности:**

- `mapN` - выполняет запросы параллельно
- `++` - объединяет результаты
- Возвращает `List[Task]` - полиморфный список

#### Получение одной задачи (Read One)

```dotty
def getTask(id: UUID, taskTypeId: TaskTypeId): IO[Option[Task]] =
  taskTypeId match
    case TaskTypeId.Book     => getBookTaskStatement(id).transact(xa)
    case TaskTypeId.Article  => getArticleTaskStatement(id).transact(xa)
    case TaskTypeId.Podcast  => getPodcastTaskStatement(id).transact(xa)
    case TaskTypeId.Video    => getVideoTaskStatement(id).transact(xa)
    case TaskTypeId.Course   => getCourseTaskStatement(id).transact(xa)
    case TaskTypeId.UserTask => getUserTaskStatement(id).transact(xa)
```

**Зачем нужен `taskTypeId`?**

- Помогает определить, в какой таблице искать
- Ускоряет поиск (не нужно проверять все таблицы)

### Детали SQL-запросов для книг

#### Вставка книги:

```sql
INSERT INTO books (
  id, name, status, section_id, order_num, start_date, end_date,
  information, plan, authors, pages
) VALUES (
  ${book.id}, ${book.name}, ${book.status}::StatusEnum, ${book.sectionId},
  ${book.order}, ${book.startDate}, ${book.endDate},
  ${book.information}, ${book.plan},
  ${book.authors}, ${book.pages}
) ON CONFLICT (id) DO NOTHING;
```

**Важные моменты:**

- `::StatusEnum` - приведение к нашему кастомному типу
- `ON CONFLICT` - идемпотентность при повторных вставках
- Все поля соответствуют структуре `BookTask`

#### Получение книги:

```sql
SELECT
  id, name, status, section_id, order_num, start_date, end_date,
  information, plan, authors, pages
FROM books WHERE id = $id
```

**Автоматическое преобразование:**

- Doobie использует наши given instances
- `Status` преобразуется из строки в enum
- `List[TaskStep]` преобразуется из массива

#### Обновление книги:

```sql
UPDATE books SET
  name = ${book.name},
  status = ${book.status}::StatusEnum,
  section_id = ${book.sectionId},
  order_num = ${book.order},
  start_date = ${book.startDate},
  end_date = ${book.endDate},
  information = ${book.information},
  plan = ${book.plan},
  authors = ${book.authors},
  pages = ${book.pages}
WHERE id = ${book.id}
```

### Преимущества такого подхода

1. **Типобезопасность** - компилятор проверяет соответствие типов
2. **Производительность** - каждая таблица оптимизирована под свой тип
3. **Расширяемость** - легко добавить новый тип задачи
4. **Поддержка** - ясная структура облегчает отладку

Этот подход обеспечивает надежное и эффективное хранение разнородных задач в нашей системе.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

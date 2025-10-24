{%
showPrev = true
prevPage = db_crud.html
showNext = true
nextPage = ../api.html
%}

# Интеграционное тестирование с базой данных

## Проблема тестирования работы с БД

Когда мы пишем код для работы с базой данных, возникает важный вопрос: **как его тестировать?**

### Традиционные подходы и их недостатки:

- **Mock-объекты** - не проверяют реальное взаимодействие с БД
- **In-memory базы** - могут вести себя иначе, чем реальная PostgreSQL
- **Разделенная тестовая БД** - сложно поддерживать, могут быть конфликты между тестами

## Решение: Testcontainers

[**Testcontainers**](https://testcontainers.com/getting-started/) - это библиотека, 
которая позволяет запускать реальные сервисы (как Docker-контейнеры) для тестирования.

### Как это работает:

1. Запускает PostgreSQL в Docker-контейнере
2. Создает изолированную базу данных для тестов
3. После тестов автоматически останавливает и удаляет контейнер

## Подключение зависимостей

Добавляем Testcontainers в наш проект:

```dotty
object Dependencies {
  // ... другие зависимости ...

  // Блок для Testcontainers - библиотеки для тестирования с контейнерами
  object testcontainers {
    private val version = "1.21.3"  // Версия Testcontainers

    // Специфичный модуль для PostgreSQL
    val postgres = "org.testcontainers" % "postgresql" % version
  }

  // ... другие зависимости ...

  // Список библиотек для ТЕСТИРОВАНИЯ (только в scope Test)
  val testLibs: Seq[ModuleID] =
    Seq(
      testcontainers.postgres,  // Testcontainers для PostgreSQL
      weaver.cats               // Фреймворк для тестирования
    ).map(_ % Test)  // Важно: только для тестов!

  // ... остальная конфигурация ...
}
```

**Важно:** Зависимости помечены `% Test` - это значит они доступны только в тестах, не попадают в production-сборку.

## Создание тестового сервиса

Создаем специальный объект `TestTaskService` для управления тестовой базой данных:

```dotty
// src/test/scala/ru/scalabook/pdp/db/TestTaskService.scala
package ru.scalabook.pdp.db

import cats.effect.*
import org.testcontainers.containers.PostgreSQLContainer

object TestTaskService:
  // Resource - специальный тип для управления ресурсами с автоматическим освобождением
  def resource: Resource[IO, TaskService] =
    Resource.make {
      // БЛОК СОЗДАНИЯ: запускаем контейнер при начале тестов
      IO {
        val postgres = new PostgreSQLContainer("postgres:18")  // PostgreSQL версии 18
        postgres.start()  // Запускаем контейнер
        postgres          // Возвращаем запущенный контейнер
      }
    } { postgres =>
      // БЛОК ОСВОБОЖДЕНИЯ: останавливаем контейнер после тестов
      IO(postgres.stop())  // Автоматически вызывается после завершения тестов
    }.evalMap { container =>  // Преобразуем контейнер в наш сервис
      // Сначала создаем сервис миграций для настройки базы
      val migrationService = new PostgreSQLMigrationService(
        url = container.getJdbcUrl,       // URL из запущенного контейнера
        user = container.getUsername,     // Стандартный пользователь
        password = container.getPassword  // Стандартный пароль
      )

      // Выполняем миграцию и создаем основной сервис
      migrationService.initialize().as:  // Создаем таблицы в базе
        new PostgreSQLTaskService(
          url = container.getJdbcUrl,
          user = container.getUsername,
          password = container.getPassword
        )
    }
end TestTaskService
```

### Что происходит в этом коде:

1. **`Resource.make`** - создает ресурс с жизненным циклом:
    - **Создание** - запускает Docker-контейнер
    - **Использование** - предоставляет сервис для тестов
    - **Освобождение** - останавливает контейнер

2. **`PostgreSQLContainer("postgres:18")`** - создает контейнер с PostgreSQL 18 версии

3. **Миграция базы** - перед использованием создает все необходимые таблицы

## Написание тестов

Используем фреймворк Weaver для написания тестов:

```dotty
// src/test/scala/ru/scalabook/pdp/db/PostgreSQLTaskServiceSuite.scala
object PostgreSQLTaskServiceSuite extends IOSuite:
  // Определяем тип ресурса, который будет использоваться в тестах
  override type Res = TaskService

  // Общий ресурс для всех тестов (переиспользуется)
  override def sharedResource: Resource[IO, Res] =
    TestTaskService.resource  // Наш тестовый сервис

  // Тест для создания пользовательского типа
  test("PostgreSQLTaskService.createUserType"): service =>
    // Подготавливаем тестовые данные
    val userType: UserType =
      UserType(
        UserTaskTypeId(10),
        TaskTypeName("Check createUserType")
      )

    // Выполняем тестируемые операции
    for
      // ДЕЙСТВИЕ: создаем пользовательский тип
      _ <- service.createUserType(userType)

      // ПРОВЕРКА: пытаемся получить созданный тип
      maybeOpenSource <- service.getUserType(userType.id)
    yield whenSuccess(maybeOpenSource): actual =>
      // Утверждения (assertions) - проверяем корректность
      expect.all(
        actual.id == userType.id,     // ID должен совпадать
        actual.name == userType.name  // Имя должно совпадать
      )

  // ... другие тесты для всех CRUD операций и типов задач
```

## Детальное объяснение процесса тестирования

### Жизненный цикл тестов:

1. **Перед всеми тестами**:
    - Запускается Docker-контейнер с PostgreSQL
    - Выполняются миграции (создаются таблицы)
    - Создается экземпляр `PostgreSQLTaskService`

2. **Во время каждого теста**:
    - Тест получает готовый к работе `TaskService`
    - Выполняются операции с базой данных
    - Проверяются результаты

3. **После всех тестов**:
    - Автоматически останавливается Docker-контейнер
    - Все ресурсы освобождаются

### Преимущества такого подхода:

#### 1. **Изоляция тестов**

```dotty
// Каждый тест работает с чистой базой благодаря миграциям
test("test 1"): service =>
  service.createUserType(type1)  // Не влияет на другие тесты

test("test 2"): service =>
  service.createUserType(type2)  // Начинает с чистой базы
```

#### 2. **Реальное поведение**

- Используется настоящая PostgreSQL, а не эмуляция
- Проверяются реальные SQL-запросы
- Тестируется работа с типами данных PostgreSQL

#### 3. **Автоматизация**

- Не нужно вручную настраивать тестовую базу
- CI/CD система может легко запускать тесты
- Воспроизводимая среда

## Важные особенности

[Полный список Expectations (assertions)](https://typelevel.org/weaver-test/features/expectations.html)

### 1. **whenSuccess макрос**

```dotty
whenSuccess(maybeOpenSource): actual =>
  // Этот блок выполняется только если maybeOpenSource содержит значение
  // Если None - тест автоматически падает
```

### 2. **expect.assertions**

```dotty
expect.all(
  condition1,
  condition2,
  condition3
) // Все условия должны быть true
```

### 3. **Resource переиспользование**

```dotty
override def sharedResource: Resource[IO, Res]
// Один экземпляр сервиса для всех тестов в классе
// Эффективно - не перезапускает контейнер для каждого теста
```

## Заключение

Такой подход к интеграционному тестированию обеспечивает:

- **Надежность** - тестируем на реальной PostgreSQL
- **Изоляцию** - каждый тестовый прогон независим
- **Автоматизацию** - не требует ручной настройки
- **Производительность** - быстрые и стабильные тесты

Это позволяет уверенно вносить изменения в код работы с базой данных, зная что все основные сценарии покрыты тестами.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

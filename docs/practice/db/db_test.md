{%
showPrev = true
prevPage = db_crud.html
showNext = true
nextPage = ../lifecycle.html
%}

# Интеграционное тестирование с базой данных

## Проблема тестирования кода, взаимодействующего с базой данных

При разработке приложений, интенсивно работающих с базами данных, возникает критически важный вопрос: 
**как эффективно и надежно тестировать такой код?** 
Традиционные подходы к тестированию слоя доступа к данным имеют существенные ограничения, 
которые могут привести к ложному чувству уверенности в корректности реализации.

Рассмотрим распространенные методы и их фундаментальные недостатки:

- **Mock-объекты (заглушки)** - хотя и позволяют изолировать тестируемый модуль, 
  они полностью абстрагируются от реального взаимодействия с СУБД. 
  Такой подход не проверяет корректность SQL-запросов, работу транзакций, обработку ошибок базы данных или соответствие типов данных. 
  Тесты проходят с "идеализированной" версией базы, что снижает их практическую ценность.

- **In-memory базы данных (например, H2)** - хотя и обеспечивают более реальное взаимодействие, чем моки, 
  они часто имеют различия в поведении, синтаксисе SQL и поддерживаемых функциях по сравнению с production-СУБД (например, PostgreSQL). 
  Это может привести к ситуации, когда код успешно проходит тесты, но ломается при развертывании на реальной инфраструктуре.

- **Разделенная тестовая база данных, обслуживаемая вручную** - требует значительных усилий по поддержанию согласованного состояния, 
  очистке данных между тестовыми прогонами и предотвращению конфликтов между параллельно выполняющимися тестами. 
  Такой подход сложно автоматизировать и воспроизводить в средах CI/CD.

##### Современное решение: Testcontainers

Библиотека [**Testcontainers**](https://testcontainers.com/getting-started/) предлагает элегантный компромисс между реализмом и управляемостью. 
Она позволяет запускать реальные сервисы (такие как PostgreSQL, Redis, Kafka) внутри Docker-контейнеров,
специально предназначенных для выполнения тестов. 
Это обеспечивает тестирование против настоящей production-подобной среды, сохраняя при этом автоматизацию и изоляцию.

##### Принцип работы Testcontainers:

1. **Динамический запуск** - библиотека программно запускает Docker-контейнер с требуемой СУБД 
   (например, PostgreSQL указанной версии) непосредственно перед выполнением тестов.
2. **Полная изоляция** - для каждого тестового прогона создается свежий экземпляр базы данных, 
   что гарантирует отсутствие побочных эффектов между тестами.
3. **Автоматическое управление жизненным циклом** - после завершения всех тестов контейнер автоматически останавливается 
   и удаляется, освобождая ресурсы системы. Это устраняет необходимость ручной очистки.

## Подключение необходимых зависимостей в проект

Для интеграции Testcontainers в проект необходимо корректно настроить зависимости. 
Важно разделить зависимости для production-кода и для тестов, 
чтобы избежать случайного включения тестовых библиотек в итоговый артефакт.

```dotty
object Dependencies {
  // ... другие зависимости ...

  // Конфигурация библиотек для логирования
  object log4cats {
    private val version = "2.7.1"
    
    val slf4j   = "org.typelevel" %% "log4cats-slf4j"  % version 
    val logback = "ch.qos.logback" % "logback-classic" % "1.5.22"
  }

  // Блок для зависимостей Testcontainers
  object testcontainers {
    private val version = "1.21.4" 

    // Специфичный модуль для PostgreSQL, 
    // содержащий предварительно настроенные конфигурации контейнеров
    val postgres = "org.testcontainers" % "postgresql" % version
  }

  // ... другие зависимости ...

  // Сводный список библиотек, необходимых исключительно для тестирования
  val testLibs: Seq[ModuleID] =
    Seq(
      log4cats.logback,       
      log4cats.slf4j,         
      doobie.postgres,        
      testcontainers.postgres,
      weaver.cats             
    )
    // модификатор `% Test` гарантирует, что эти библиотеки не будут включены 
    // в production-сборку и доступны только при компиляции и выполнении тестов
    .map(_ % Test) 
                    
  // ... остальная конфигурация проекта ...
}
```

## Создание централизованного сервиса для управления тестовой базой данных

Для эффективного повторного использования ресурсов между тестовыми наборами 
и минимизации накладных расходов на запуск контейнеров создается специализированный объект `SharedResources`. 
Этот объект реализует паттерн `GlobalResource` из [фреймворка Weaver][weaver], 
что позволяет инициализировать общие ресурсы один раз перед всеми тестами и корректно освободить их после завершения.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/services/postgresql/SharedResources.scala
package ru.scalabook.pdp.services.postgresql

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import doobie.*
import doobie.util.log.LogEvent
import doobie.util.transactor.Transactor.Aux
import org.testcontainers.containers.PostgreSQLContainer
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{Logger, LoggerFactory}
import ru.scalabook.pdp.services.postgresql.{
  PostgreSQLSectionService,
  PostgreSQLTaskService,
  PostgreSQLUserTypeService
}
import weaver.*

object SharedResources extends GlobalResource:
  // Настройка системы логирования для тестовой среды
  private given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  private given log: Logger[IO]                  = loggerFactory.getLogger

  // Ключи для доступа к общим ресурсам (сервисам) из глобального хранилища Weaver
  val UserTypeServiceKey: Option[String] = "userTypeService".some
  val SectionServiceKey: Option[String]  = "sectionService".some
  val GoalServiceKey: Option[String]     = "goalService".some
  val TaskServiceKey: Option[String]     = "taskService".some

  // Кастомный обработчик логирования SQL-запросов, выполняемых Doobie.
  // Позволяет видеть все выполняемые запросы в логах тестов, что упрощает отладку.
  private val sqlLogHandler: LogHandler[IO] = (logEvent: LogEvent) =>
    log.info(s"${logEvent.sql}")

  // Основной метод, определяющий общие ресурсы для всех тестов.
  // Weaver вызывает этот метод один раз перед запуском всех тестовых наборов.
  def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for
      // 1. Запуск контейнера PostgreSQL.
      container <- postgresResource()
      // 2. Создание Transactor'a (абстракции соединения с БД) Doobie.
      transactor = getTransactor(container)
      // 3. Выполнение миграций базы данных (создание схемы, таблиц, индексов).
      _ <- Resource.eval(migration(transactor))
      // 4. Создание экземпляров сервисов, инъектированных созданным Transactor'ом.
      userService = new PostgreSQLUserTypeService(transactor)
      // 5. Регистрация каждого сервиса в глобальном хранилище.
      _ <- global.putR(userService, UserTypeServiceKey)
      sectionService = new PostgreSQLSectionService(transactor)
      _ <- global.putR(sectionService, SectionServiceKey)
      goalService = new PostgreSQLGoalService(transactor)
      _ <- global.putR(goalService, GoalServiceKey)
      taskService = new PostgreSQLTaskService(transactor)
      _ <- global.putR(taskService, TaskServiceKey)
    yield () // Ресурс возвращает Unit, так как сервисы помещены в глобальное хранилище.

  private def postgresResource(): Resource[IO, PostgreSQLContainer[Nothing]] =
    Resource.make {
      // Действие по приобретению ресурса: создание и запуск контейнера.
      IO {
        // Используется официальный образ PostgreSQL 18
        val postgres = new PostgreSQLContainer("postgres:18") 
        // Блокирующий вызов, запускающий контейнер
        postgres.start() 
        postgres
      }
    } { postgres =>
      // Действие по освобождению ресурса: остановка контейнера.
      // Гарантированно выполняется даже в случае неудачи тестов.
      IO(postgres.stop())
    }

  private def getTransactor(container: PostgreSQLContainer[Nothing])
      : Aux[[x] =>> IO[x], Unit] =
    Transactor.fromDriverManager[[x] =>> IO[x]](
      driver = "org.postgresql.Driver", // JDBC-драйвер PostgreSQL
      url = container.getJdbcUrl,       // Динамический URL, сгенерированный Testcontainers
      user = container.getUsername,     // Имя пользователя по умолчанию (обычно "test")
      password = container.getPassword, // Пароль по умолчанию (обычно "test")
      logHandler = Some(sqlLogHandler)  // Подключение логгера SQL-запросов
    )

  private def migration(transactor: Aux[[x] =>> IO[x], Unit]): IO[Unit] =
    new PostgreSQLMigrationService(transactor).initialize()
end SharedResources
```

## Структура и написание тестов

[Фреймворк Weaver][weaver], интегрированный с Cats Effect, 
используется для организации тестовых сценариев. 
Тестовый класс наследуется от `IOSuite` и использует механизм общих ресурсов (shared resources).

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLUserTypeServiceSuite.scala
package ru.scalabook.pdp.services.postgresql

import cats.effect.*
import ru.scalabook.pdp.model.*
import ru.scalabook.pdp.model.UserType.{UserTypeId, UserTypeName}
import ru.scalabook.pdp.services.postgresql.PostgreSQLUserTypeService
import ru.scalabook.pdp.services.postgresql.SharedResources.UserTypeServiceKey
import weaver.*

// Класс тестового набора для PostgreSQLUserTypeService.
// Параметр `global: GlobalRead` предоставляется Weaver 
// и дает доступ к ранее зарегистрированным общим ресурсам.
class PostgreSQLUserTypeServiceSuite(global: GlobalRead) extends IOSuite:
  // Тип Res определяет, какой именно ресурс будет использоваться 
  // всеми тестами этого набора.
  override type Res = PostgreSQLUserTypeService

  // Метод, который Weaver использует для получения общего ресурса.
  // Ресурс инициализируется один раз (перед первым тестом) и используется повторно.
  override def sharedResource: Resource[IO, Res] =
    global.getOrFailR[PostgreSQLUserTypeService](UserTypeServiceKey)

  // Пример теста на создание сущности.
  // Каждый тест - это функция, которая получает экземпляр `service` (общий ресурс).
  test("PostgreSQLTaskService.create"): service =>
    val name = UserTypeName("Check create")
    for
      // Выполнение операции создания и получение сгенерированного ID.
      id            <- service.create(name)
      // Попытка получения только что созданной сущности по ID.
      maybeUserType <- service.get(id)
    // Комбинатор `yield` возвращает assertion (проверку).
    yield 
      // `whenSuccess` - безопасное извлечение Option
      whenSuccess(maybeUserType): actual => 
        // Проверка корректности сохраненных данных.
        expect.all(
          actual.id == id,              // ID должен совпадать
          actual.name == name,          // Имя должно совпадать
          actual.displayed == true,     // Значение по умолчанию
          actual.ordinal == Ordinal.Max // Значение по умолчанию
        )

  // Тест на обработку случая, когда сущность не найдена.
  test("PostgreSQLTaskService.get - not found"): service =>
    val nonExistentId = UserTypeId(9999) // Заведомо несуществующий ID
    service.get(nonExistentId).map: result =>
      // Ожидается, что результат будет `None`.
      expect(result.isEmpty)

  // Тест на получение всех сущностей.
  test("PostgreSQLTaskService.getAll"): service =>
    val opensource = UserTypeName("Open Source")
    val myBlog     = UserTypeName("Мой блог")
    for
      osId  <- service.create(opensource)
      mbId  <- service.create(myBlog)
      types <- service.getAll // Получение полного списка
    yield expect.all(
      types.length >= 2, // Как минимум две созданные нами сущности
      types.contains(UserType(osId, opensource)), // Проверка наличия первой
      types.contains(UserType(mbId, myBlog))      // Проверка наличия второй
    )

  // Тест на обновление существующей сущности.
  test("PostgreSQLTaskService.update"): service =>
    val exercises = UserTypeName("Exercises")
    val newName   = UserTypeName("Exercises (updated)")
    for
      id <- service.create(exercises)
      // Новая версия сущности с тем же ID
      updated = UserType(
        id = id,
        name = newName,
        displayed = false,
        ordinal = Ordinal(10)
      ) 
      _            <- service.update(updated) // Операция обновления
      maybeUpdated <- service.get(updated.id) // Получение для проверки
    yield whenSuccess(maybeUpdated): actual =>
      expect.all(
        actual.id == updated.id,
        actual.name == newName,       // Имя должно измениться
        actual.displayed == false,    // Статус отображения должен измениться
        actual.ordinal == Ordinal(10) // Порядок должен измениться
      )

  // Тест на удаление сущности.
  test("PostgreSQLTaskService.delete"): service =>
    val name = UserTypeName("Deprecated")
    for
      id        <- service.create(name)
      initial   <- service.getAll     // Состояние до удаления
      _         <- service.delete(id) // Операция удаления
      finalList <- service.getAll     // Состояние после удаления
    yield expect.all(
      initial.exists(_.id == id),  // Сущность должна присутствовать в начальном списке
      finalList.forall(_.id != id) // Сущность должна отсутствовать в конечном списке
    )

end PostgreSQLUserTypeServiceSuite
```

[Полный список тестов][tests]

## Детальное объяснение жизненного цикла и процесса тестирования

### Полный жизненный цикл выполнения тестов с Testcontainers и Weaver:

1. **Фаза инициализации (перед всеми тестами)**:
   - Фреймворк Weaver вызывает метод `sharedResources` объекта `SharedResources`.
   - Запускается новый Docker-контейнер с чистым экземпляром PostgreSQL.
   - На основе параметров запущенного контейнера (URL, логин, пароль) создается 
     `Transactor` Doobie - основная абстракция для выполнения запросов.
   - Выполняются миграции базы данных (DDL-скрипты), которые создают необходимые таблицы, индексы 
     и, возможно, заполняют справочные данные.
   - Создаются экземпляры всех сервисов (`PostgreSQLUserTypeService`, `PostgreSQLSectionService` и т.д.), 
     которым инъектируется общий `Transactor`.
   - Созданные сервисы регистрируются в глобальном хранилище Weaver под уникальными ключами.

2. **Фаза выполнения каждого тестового набора (suite)**:
   - При создании экземпляра тестового класса (например, `PostgreSQLUserTypeServiceSuite`) 
     Weaver через метод `sharedResource` извлекает из глобального хранилища соответствующий сервис (`PostgreSQLUserTypeService`).
   - Этот экземпляр сервиса предоставляется каждому тесту в наборе как параметр функции-теста.
   - Тест выполняет бизнес-операции (create, get, update, delete) через сервис, 
     который, в свою очередь, выполняет реальные SQL-запросы к PostgreSQL в контейнере.
   - После выполнения всех тестов в наборе, Weaver может (в зависимости от конфигурации) выполнить откат транзакции 
     или очистку базы, чтобы изолировать следующий набор тестов. 

3. **Фаза финализации (после всех тестов)**:
   - После выполнения всех тестовых наборов Weaver корректно освобождает все общие ресурсы.
   - Вызывается действие освобождения для ресурса контейнера, которое останавливает и удаляет Docker-контейнер с PostgreSQL.
   - Все выделенные системные ресурсы (память, сетевые порты) освобождаются.

## Важные особенности и идиомы, используемые в тестах

### 1. **Макрос `whenSuccess` для безопасной работы с `Option` и `Either`**

```dotty
whenSuccess(maybeUserType): actual =>
  // Этот блок выполняется только если `maybeUserType` содержит значение (`Some`).
  // Если значение отсутствует (`None`), тест автоматически отмечается как неудачный
  // с понятным сообщением об ошибке.
  // `actual` автоматически имеет тип `UserType` (извлечен из `Option[UserType]`).
  expect(actual.name == expectedName)
```

### 2. **Комбинатор `expect.all` для групповых проверок**

```dotty
expect.all(
  condition1, // Проверка 1
  condition2, // Проверка 2
  condition3  // Проверка 3
) // Тест считается успешным, только если все условия возвращают `true`.
  // При неудаче выводится информация о том, какое именно условие не выполнено.
```

### 3. **Эффективное переиспользование ресурсов через механизм `Resource`**

```dotty
override def sharedResource: Resource[IO, Res]
// Weaver гарантирует, что ресурс (в данном случае `PostgreSQLUserTypeService`)
// будет инициализирован один раз для всего тестового набора и предоставлен каждому тесту.
// Это значительно эффективнее, чем перезапуск контейнера для каждого отдельного теста.
```

### 4. **Полный набор проверок (Expectations) Weaver**

[Официальная документация](https://typelevel.org/weaver-test/features/expectations.html) 
содержит полный список доступных assertions, включая проверки на исключения, асинхронные проверки и многое другое.

## Заключение

Представленный подход к интеграционному тестированию с использованием Testcontainers и Weaver обеспечивает 
комплексное решение, сочетающее в себе несколько критически важных качеств:

- **Высокая надежность и достоверность** - тестирование выполняется на **реальной PostgreSQL**, 
  что гарантирует, что код, успешно прошедший тесты, будет корректно работать в production-среде. 
- **Полная изоляция тестовых прогонов** - каждый набор тестов выполняется в чистом, предсказуемом окружении. 
  Отсутствуют скрытые зависимости между тестами, что упрощает отладку и делает результаты стабильными.
- **Полная автоматизация процесса** - от разработчика не требуется ручных действий по подготовке окружения. 
  Тесты могут быть легко запущены как на локальной машине разработчика, так и в любой CI/CD системе, поддерживающей Docker.
- **Оптимальная производительность** - несмотря на использование реальной СУБД в контейнере, 
  подход обеспечивает достаточно высокую скорость выполнения тестов за счет переиспользования контейнера между тестами 
  и эффективного управления жизненным циклом ресурсов.
- **Эффективная отладка** - благодаря интеграции логирования SQL-запросов 
  и детализированным сообщениям об ошибках от Weaver, процесс выявления и исправления дефектов становится значительно проще.

Такой подход к тестированию слоя доступа к данным позволяет командам разработки **уверенно вносить изменения** 
в логику работы с базой данных, рефакторить запросы и оптимизировать схемы, 
будучи защищенными комплексной сетью интеграционных тестов, которые быстро выявят регрессии и побочные эффекты. 
Это существенно повышает качество и устойчивость приложения в долгосрочной перспективе.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]
- [Полный список тестов][tests]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
[tests]: https://gitverse.ru/scalabook/pdp/content/master/pdp/src/test/scala/ru/scalabook/pdp/services/postgresql
[weaver]: https://typelevel.org/weaver-test/

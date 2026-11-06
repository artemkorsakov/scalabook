{%
showPrev = true
prevPage = db_tasks.html
showNext = true
nextPage = ../lifecycle.html
%}

# Интеграционное тестирование с базой данных

## Проблема тестирования кода, взаимодействующего с базой данных

При разработке приложений, интенсивно работающих с базами данных, возникает вопрос: 
**как эффективно и надежно тестировать такой код?**

Рассмотрим распространенные методы и их недостатки:

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

Библиотека [**Testcontainers**](https://testcontainers.com/getting-started/) предлагает элегантный компромисс между реализмом и управляемостью. 
Она позволяет запускать реальные сервисы (такие как PostgreSQL, Redis, Kafka) внутри Docker-контейнеров,
специально предназначенных для выполнения тестов. 
Это обеспечивает тестирование против настоящей production-подобной среды, сохраняя при этом автоматизацию и изоляцию.

#### Принцип работы Testcontainers:

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
    private val version = "2.8.0"
    
    val slf4j   = "org.typelevel" %% "log4cats-slf4j"  % version 
    val logback = "ch.qos.logback" % "logback-classic" % "1.6.6"
  }

  // Блок для зависимостей Testcontainers (testcontainers-scala)
  object testcontainers {
    private val version = "0.44.1" 

    val postgres = "com.dimafeng" %% "testcontainers-scala-postgresql" % version
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

Для повторного использования ресурсов между тестовыми наборами создается специализированный объект `SharedResources`. 
Этот объект реализует паттерн `GlobalResource` из [фреймворка Weaver][weaver], 
что позволяет инициализировать общие ресурсы один раз перед всеми тестами и корректно освободить их после завершения.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/services/SharedResources.scala
package ru.scalabook.pdp.services

...

object SharedResources extends GlobalResource:
  // Набор сервисов доступа к данным, которые предоставляются тестам.
  // Каждый из них работает с одним и тем же Transactor'ом.
  final case class PDPServices(
      taskTypeService: PostgreSQLTaskTypeService[IO],
      taskService: PostgreSQLTaskService[IO]
  )

  // Настройка системы логирования для тестовой среды
  private given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  private given log: Logger[IO]                  = loggerFactory.getLogger

  // Ключ для доступа к общим ресурсам из глобального хранилища Weaver
  val TasksServiceKey: Option[String] = "tasksServices".some

  // Кастомный обработчик логирования SQL-запросов, выполняемых Doobie.
  private val sqlLogHandler: LogHandler[IO] = (logEvent: LogEvent) =>
    log.info(s"${logEvent.sql}")

  // Основной метод, определяющий общие ресурсы для всех тестов.
  def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for
      // 1. Запуск контейнера PostgreSQL.
      postgresContainer <- postgresResource()
      // 2. Создание Transactor'a (абстракции соединения с БД) Doobie.
      transactor = getTransactor(postgresContainer)
      // 3. Выполнение миграций базы данных.
      _ <- Resource.eval(migration(transactor))
      // 4. Создание сервисов доступа к данным, инъектированных Transactor'ом.
      taskTypeService = new PostgreSQLTaskTypeService[IO](transactor)
      taskService     = new PostgreSQLTaskService[IO](transactor)
      // 5. Регистрация сервисов в глобальном хранилище.
      _ <- global.putR(
        PDPServices(
          taskTypeService = taskTypeService,
          taskService = taskService
        ),
        TasksServiceKey
      )
    yield () // Ресурс возвращает Unit, так как сервисы помещены в глобальное хранилище.
  end for
  end sharedResources

  private def postgresResource(): Resource[IO, PostgreSQLContainer] =
    Resource.make {
      IO {
        val container = PostgreSQLContainer(dockerImageNameOverride =
          DockerImageName.parse("postgres:18")
        )
        container.start()
        container
      }
    }(container => IO(container.stop()))

  private def getTransactor(container: PostgreSQLContainer)
      : Aux[[x] =>> IO[x], Unit] =
    Transactor.fromDriverManager[[x] =>> IO[x]](
      driver = "org.postgresql.Driver",
      url = container.jdbcUrl,
      user = container.username,
      password = container.password,
      logHandler = Some(sqlLogHandler)
    )

  private def migration(transactor: Aux[[x] =>> IO[x], Unit]): IO[Boolean] =
    new PostgreSQLMigrationService(transactor).initialize()
end SharedResources
```

## Структура и написание тестов

[Фреймворк Weaver][weaver], интегрированный с Cats Effect, 
используется для организации тестовых сценариев. 
Тестовый класс наследуется от `IOSuite` и использует механизм общих ресурсов (shared resources).

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLTaskTypeServiceSuite.scala
package ru.scalabook.pdp.services.postgresql

...

// Класс тестового набора для PostgreSQLTaskTypeService.
// Параметр `global: GlobalRead` предоставляется Weaver 
// и дает доступ к ранее зарегистрированным общим ресурсам.
class PostgreSQLTaskTypeServiceSuite(global: GlobalRead) extends IOSuite:
  // Тип Res — кортеж из сервиса и идентификатора пользователя,
  // от лица которого выполняются операции.
  override type Res =
    (
        service: PostgreSQLTaskTypeService[IO],
        userId: UserId
    )

  // Weaver вызывает этот метод один раз (перед первым тестом).
  // Извлекает сервис из глобального хранилища и задаёт идентификатор
  // пользователя-владельца создаваемых сущностей.
  override def sharedResource: Resource[IO, Res] =
    for
      pdpServices <- global.getOrFailR[PDPServices](TasksServiceKey)
      // Произвольный идентификатор пользователя-владельца создаваемых сущностей.
      userId      <- Resource.eval(IO(UserId(UUID.randomUUID())))
    yield (pdpServices.taskTypeService, userId)

  // Пример теста на создание сущности.
  test("PostgreSQLTaskTypeService.create"): resource =>
    val newType = CreateTaskType(
      name = TaskTypeName("Check create"),
      userId = resource.userId
    )
    for
      id            <- resource.service.create(newType)
      maybeTaskType <- resource.service.get(id, resource.userId)
    yield whenSuccess(maybeTaskType): actual =>
      expect.same(id, actual.id) &&
        expect.same(newType.name, actual.name) &&
        expect.same(newType.userId, actual.userId) &&
        expect.same(newType.displayed, actual.displayed) &&
        expect.same(newType.ordinal, actual.ordinal)

  // Тест на обработку случая, когда сущность не найдена.
  test("PostgreSQLTaskTypeService.get - not found"): resource =>
    val nonExistentId = TaskTypeId(9999)
    resource.service.get(nonExistentId, resource.userId).map: result =>
      expect(result.isEmpty)

  // Тест на получение всех сущностей.
  test("PostgreSQLTaskTypeService.getByUser"): resource =>
    val opensource = CreateTaskType(
      name = TaskTypeName("Open Source"),
      userId = resource.userId
    )
    val myBlog = CreateTaskType(
      name = TaskTypeName("Мой блог"),
      userId = resource.userId
    )
    for
      osId  <- resource.service.create(opensource)
      mbId  <- resource.service.create(myBlog)
      types <- resource.service.getByUser(resource.userId)
    yield expect(types.nonEmpty) &&
      expect(types.exists(_.id == osId)) &&
      expect(types.exists(_.id == mbId))

  // Тест на обновление существующей сущности.
  test("PostgreSQLTaskTypeService.update"): resource =>
    val userId    = resource.userId
    val exercises = CreateTaskType(
      name = TaskTypeName("Exercises"),
      userId = userId
    )
    val newName = TaskTypeName("Exercises (updated)")
    for
      id <- resource.service.create(exercises)
      updated = TaskType(
        id = id,
        name = newName,
        userId = userId,
        displayed = !exercises.displayed,
        ordinal = Ordinal(10)
      )
      _            <- resource.service.update(updated)
      maybeUpdated <- resource.service.get(updated.id, updated.userId)
    yield whenSuccess(maybeUpdated): actual =>
      expect.same(updated.id, actual.id) &&
        expect.same(updated.name, actual.name) &&
        expect.same(updated.userId, actual.userId) &&
        expect.same(updated.displayed, actual.displayed) &&
        expect.same(updated.ordinal, actual.ordinal)

  // Тест на удаление сущности.
  test("PostgreSQLTaskTypeService.delete"): resource =>
    val newType = CreateTaskType(
      name = TaskTypeName("Deprecated"),
      userId = resource.userId
    )
    for
      id        <- resource.service.create(newType)
      initial   <- resource.service.getByUser(newType.userId)
      _         <- resource.service.delete(id)
      finalList <- resource.service.getByUser(newType.userId)
    yield expect(initial.exists(_.id == id)) &&
      expect(finalList.forall(_.id != id))

end PostgreSQLTaskTypeServiceSuite
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
- Создаются экземпляры сервисов доступа к данным (`PostgreSQLTaskTypeService`, `PostgreSQLTaskService`), 
      которым инъектируется общий `Transactor`.
   - Созданные сервисы регистрируются в глобальном хранилище Weaver под уникальными ключами.

2. **Фаза выполнения каждого тестового набора (suite)**:
   - При создании экземпляра тестового класса (например, `PostgreSQLTaskTypeServiceSuite`) 
     Weaver через метод `sharedResource` извлекает из глобального хранилища соответствующий сервис (`PostgreSQLTaskTypeService`).
   - Этот экземпляр сервиса предоставляется каждому тесту в наборе как параметр функции-теста.
   - Тест выполняет бизнес-операции (create, get, update, delete) через сервис, 
     который, в свою очередь, выполняет реальные SQL-запросы к PostgreSQL в контейнере.
   - После выполнения всех тестов в наборе, Weaver может (в зависимости от конфигурации) выполнить откат транзакции 
     или очистку базы, чтобы изолировать следующий набор тестов. 

3. **Фаза финализации (после всех тестов)**:
   - После выполнения всех тестовых наборов Weaver корректно освобождает все общие ресурсы.
   - Вызывается действие освобождения для ресурса контейнера, которое останавливает и удаляет Docker-контейнер с PostgreSQL.
   - Все выделенные системные ресурсы (память, сетевые порты) освобождаются.

## Заключение

Представленный подход к интеграционному тестированию с использованием Testcontainers и Weaver обеспечивает 
комплексное решение, сочетающее в себе несколько критически важных качеств:

- **Высокая надежность и достоверность** - тестирование выполняется на **реальной PostgreSQL**, 
  что гарантирует, что код, успешно прошедший тесты, будет корректно работать в production-среде. 
- **Полная автоматизация процесса** - от разработчика не требуется ручных действий по подготовке окружения. 
  Тесты могут быть легко запущены как на локальной машине разработчика, так и в любой CI/CD системе, поддерживающей Docker.
- **Оптимальная производительность** - несмотря на использование реальной СУБД в контейнере, 
  подход обеспечивает достаточно высокую скорость выполнения тестов за счет переиспользования контейнера между тестами 
  и эффективного управления жизненным циклом ресурсов.
- **Эффективная отладка** - благодаря интеграции логирования SQL-запросов 
  и детализированным сообщениям об ошибках от Weaver, процесс выявления и исправления дефектов становится значительно проще.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]
- [Полный список тестов][tests]

[repo]: https://gitverse.ru/scalabook/pdp
[tests]: https://gitverse.ru/scalabook/pdp/content/master/pdp/src/test/scala/ru/scalabook/pdp/services/postgresql
[weaver]: https://typelevel.org/weaver-test/

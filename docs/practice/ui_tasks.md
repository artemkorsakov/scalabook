{%
showPrev = true
prevPage = ui_settings/ui_settings_task_type.html
showNext = true
nextPage = ui_tasks/ui_task_open.html
%}

# Создание задачи

Создание задачи — центральная операция всего приложения. Пользователь может создать задачу
с любой из трёх вкладок главной страницы («В работе», «Запланировано», «База знаний»),
при этом набор обязательных полей зависит от выбранной вкладки. Например, для задач
«В работе» обязательны даты начала и окончания, а для «Базы знаний» они опциональны.

В отличие от страницы настроек, где CRUD-операции происходят прямо в таблице, создание
задачи реализовано через **модальное окно**: пользователь кликает «+ Создать задачу»,
открывается всплывающее окно с формой, после заполнения которого задача создаётся,
а пользователь возвращается на ту же вкладку, откуда пришёл.

![Create task modal](images/create_task_modal.png)

## Отображение модального окна

### Маршрут

```dotty
val getTaskCreate = Route(
  Request.get(Path.root / "task" / "create" / Param.string)
    .extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

**Разбор маршрута:**

- **`Request.get(Path.root / "task" / "create" / Param.string)`** — GET-запрос на
  `/task/create/{tab}`, где `{tab}` — имя вкладки (`"in-progress"`, `"planned"`,
  `"knowledge-base"`). Параметр `tab` определяет, какие поля будут обязательными.
- **`.extractHeader[Authorization]`** — токен передаётся в заголовке `Authorization`.
- **`Response.ok(Entity.html).orNotFound`** — успешный ответ (200 OK) с HTML-телом
  модального окна.

### Хэндлер `CreateTaskModalHandler`

```dotty
final case class CreateTaskModalHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    settingsService: SettingsService[IO]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  val handler: Handler =
    Routes.getTaskCreate.handleIO:
      (tab: String, authorization: Authorization) =>
        handleUserIO: (user, token) =>
          getViewCreateTaskModal(
            isOpen = true,
            settingsService = settingsService,
            userId = user.id,
            token = token,
            tab = tab,
            errorRequest = none,
            errorMessage = none
          )
        .apply(authorization)
end CreateTaskModalHandler
```

**Пошаговый разбор:**

1. **`handleUserIO: (user, token) => ...`** — стандартная проверка токена через
   `AuthorizationHandler`. После успешной проверки получаем `user` и `token`.

2. **`getViewCreateTaskModal(...)`** — формирует HTML модального окна, передавая:
  - `isOpen = true` — окно открыто (для ветвления в шаблоне).
  - `errorRequest = none` — нет данных из предыдущей неудачной попытки.
  - `errorMessage = none` — нет сообщения об ошибке.

### Формирование модального окна

```dotty
def getViewCreateTaskModal(
    settingsService: SettingsService[IO],
    isOpen: Boolean,
    userId: UserId,
    token: JwtToken,
    tab: String,
    errorRequest: Option[CreateTaskRequest],
    errorMessage: Option[String]
): IO[String] =
  for
    taskTypes <- settingsService.getTaskTypes(userId)
    sections  <- settingsService.getSections(userId)
    goals     <- settingsService.getGoals(userId)
  yield View.createTask(
    isOpen = isOpen,
    req = errorRequest.getOrElse(
      CreateTaskRequest.empty(token = token.value, tab = tab)
    ),
    taskTypes = taskTypes,
    sections = sections,
    goals = goals,
    errorMessage = errorMessage
  ).toString
```

**Что происходит:**

1. **Загрузка справочников** — три запроса к `settingsService`:
  - `taskTypes` — типы задач пользователя.
  - `sections` — разделы пользователя.
  - `goals` — цели пользователя.

2. **Формирование `CreateTaskRequest`:**
  - Если есть `errorRequest` (пользователь уже заполнял форму, но получил ошибку) —
    используем его.
  - Иначе — создаём «пустой» запрос через `CreateTaskRequest.empty(token, tab)`.
    Это гарантирует, что форма будет заполнена значениями по умолчанию:
    `ordinal = 1000`, `durationValue = 30`, `durationUnit = MINUTES`,
    а `startDate` и `endDate` — `None`.

**Почему `errorRequest` опционален?**

Когда пользователь впервые открывает форму — все поля пустые (или со значениями
по умолчанию). Но если он отправил форму с ошибкой (например, забыл указать название),
сервер возвращает ту же форму с **сохранёнными значениями** и сообщением об ошибке.
Так пользователю не нужно вводить всё заново.

## Модель `CreateTaskRequest`

`CreateTaskRequest` — это DTO (Data Transfer Object), в который десериализуется тело
формы. Он отличается от доменного `CreateTask` тем, что содержит **только те поля,
которые приходят с формы**, и использует простые типы (`Int`, `Long`, `String`),
а не уточнённые (`TaskId`, `Ordinal`, `FiniteDuration`).

### Определение

```dotty
final case class CreateTaskRequest(
    token: String,
    tab: String,
    name: String,
    taskTypeId: Int,
    sectionId: Int,
    goalId: Int,
    ordinal: Int,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    durationValue: Long,
    durationUnit: TimeUnit
)
```

**Поля:**

| Поле            | Назначение                                                                       |
| --------------- | -------------------------------------------------------------------------------- |
| `token`         | JWT-токен из скрытого поля формы                                                 |
| `tab`           | Вкладка, с которой создаётся задача (`in-progress`, `planned`, `knowledge-base`) |
| `name`          | Название задачи                                                                  |
| `taskTypeId`    | ID типа задачи                                                                   |
| `sectionId`     | ID раздела                                                                       |
| `goalId`        | ID цели                                                                          |
| `ordinal`       | Порядковый номер                                                                 |
| `startDate`     | Дата начала (опционально)                                                        |
| `endDate`       | Дата окончания (опционально)                                                     |
| `information`   | Описание                                                                         |
| `durationValue` | Числовое значение продолжительности                                              |
| `durationUnit`  | Единица измерения (`MINUTES`, `HOURS`, `DAYS`)                                   |

**Почему `durationValue` и `durationUnit` — отдельные поля?**

Потому что форма содержит **два** `<input>`/`<select>` для продолжительности.
Пользователь вводит число и выбирает единицу измерения. 
Только на уровне `CreateTask.parse` эти два поля объединяются в `FiniteDuration`.

### `FormCodec[CreateTaskRequest]`

`FormCodec` — это тип Krop для двустороннего преобразования между `UrlForm` и Scala-объектом. 
Он определяет два метода: `decode` (из формы в объект) и `encode` (из объекта в форму).

**Декодирование (форма → объект):**

```dotty
decode = { (urlForm: UrlForm) =>
  (
    urlForm.getValue("token").toValidatedNec,
    urlForm.getValue("tab").toValidatedNec,
    urlForm.getValue("name").toValidatedNec,
    urlForm.parseInt("taskTypeId").toValidatedNec,
    urlForm.parseInt("sectionId").toValidatedNec,
    urlForm.parseInt("goalId").toValidatedNec,
    urlForm.parseInt("ordinal").toValidatedNec,
    urlForm.parseLocalDate("startDate").toValidatedNec,
    urlForm.parseLocalDate("endDate").toValidatedNec,
    urlForm.parseLong("durationValue").toValidatedNec,
    urlForm.parseTimeUnit("durationUnit").toValidatedNec
  ).mapN { (...) => CreateTaskRequest(...) }
    .toEither.leftMap(_.toChain)
}
```

**Ключевые детали:**

1. **`ValidatedNec`** — каждое поле валидируется независимо, ошибки **аккумулируются**,
   а не останавливают процесс на первой. Это удобно: пользователь видит сразу
   все проблемы, а не правит форму по одной.

2. **`mapN`** — объединяет результаты. Если все поля валидны — создаётся `CreateTaskRequest`.
   Если хотя бы одно невалидно — аккумулированные ошибки сохраняются.

3. **`.toEither.leftMap(_.toChain)`** — Krop ожидает `Either[Chain[String], A]`,
   а мы работаем с `ValidatedNec`. Преобразование стандартное: `Validated` → `Either`,
   `NonEmptyChain` → `Chain`.

4. **`urlForm.getValue("information")`** — здесь используется другой подход:
   поле `information` не проверяется на пустоту, потому что оно опционально.
   Вместо `getValue` используется `getFirstOrElse("information", "")`,
   что даёт пустую строку, если поля нет.

**Кодирование (объект → форма):**

```dotty
encode = { (request: CreateTaskRequest) =>
  UrlForm(
    Map(
      "token"      -> Chain(request.token),
      "tab"        -> Chain(request.tab),
      "name"       -> Chain(request.name),
      ...
      "durationValue" -> Chain(request.durationValue.toString),
      "durationUnit"  -> Chain(request.durationUnit.toString.toLowerCase)
    )
  )
}
```

Используется реже — например, при формировании формы из существующего объекта.
Обратите внимание: `durationUnit` кодируется в нижнем регистре (`minutes`, `hours`,
`days`), чтобы совпадать со значениями `<option>` в HTML.

### Метод `empty`

```dotty
def empty(token: String, tab: String): CreateTaskRequest = CreateTaskRequest(
  token = token,
  tab = tab,
  name = "",
  taskTypeId = 0,
  sectionId = 0,
  goalId = 0,
  ordinal = 1000,
  startDate = none,
  endDate = none,
  information = "",
  durationValue = 30,
  durationUnit = TimeUnit.MINUTES
)
```

Возвращает «пустой» запрос со значениями по умолчанию. 
Используется при первом открытии формы, когда `errorRequest` отсутствует. 
Значение `ordinal = 1000` специально большое — новые задачи попадают в конец списка.

## Шаблон `createTask.scala.html`

Шаблон состоит из двух веток: закрытое модальное окно и открытое. 
Первая используется после успешного создания задачи для «мягкого» закрытия окна.

### Ветка «Модальное окно закрыто»

```html
@if(!isOpen) {
<div id="task-modal" class="uk-modal uk-open" style="display:block;">
  <div class="uk-modal-dialog uk-modal-body">
    <button
      class="add-btn"
      hx-get="/home/@req.tab"
      hx-target="#app"
      hx-swap="outerHTML"
      onclick="closeCreateTaskModal()"
    >
      Обновить список задач
    </button>
  </div>
</div>
}
```

![Обновить список задач](images/reload_tasks.png)

**Зачем нужна эта ветка?**

После успешного создания задачи сервер возвращает этот фрагмент HTML вместо формы.
Он вставляется в `#modal-container` и показывает пользователю кнопку «Обновить список
задач». Клик по ней перезагружает главную страницу с той же вкладки, где была
создана задача — и пользователь видит свою новую задачу в списке.

**Почему не сделать редирект сразу?**

Потому что после успешного создания задачи htmx заменяет **только** `#modal-container`,
а не `#app`. Это оставляет главную страницу на месте, но скрывает форму. Кнопка
«Обновить список задач» даёт пользователю контроль над моментом обновления —
он может сначала прочитать сообщение, затем кликнуть.

### Ветка «Модальное окно открыто»

```html
@if(isOpen) {
<div id="task-modal" class="uk-modal uk-open" style="display:block;">
  <div class="uk-modal-dialog uk-modal-body">
    <h2 class="uk-modal-title">Создание задачи</h2>

    <form hx-post="/tasks" hx-target="#modal-container" hx-swap="innerHTML">
      <input type="hidden" name="token" value="@req.token" />
      <input type="hidden" name="tab" value="@req.tab" />
      ...
    </form>
  </div>
</div>
}
```

**Ключевые моменты формы:**

1. **`hx-post="/tasks"`** — POST-запрос на создание задачи.
2. **`hx-target="#modal-container"`** — ответ сервера вставляется в контейнер модальных
   окон, а не в `#app`. Это важно: после успешного создания задача не должна
   «перекидывать» пользователя на другую страницу — он остаётся на текущей вкладке.
3. **`hx-swap="innerHTML"`** — заменяем содержимое `#modal-container`, а не его самого.
4. **Скрытые поля `token` и `tab`** — передаются в теле запроса. `tab` нужен, чтобы
   сервер знал, на какую вкладку вернуть пользователя после создания задачи.

### Поля формы

![Поля формы](images/create_task_fields.png)

**Название задачи:**

```html
<div class="form-group">
  <label for="taskName">Название задачи:</label>
  <input
    id="taskName"
    name="name"
    type="text"
    required
    placeholder="Введите название"
    value="@req.name"
  />
</div>
```

Простое текстовое поле. Значение берётся из `req.name`, что позволяет сохранить
введённые данные при ошибке валидации.

**Тип задачи, раздел, цель:**

```html
<div class="form-group">
  <label for="taskTypeId">Тип задачи:</label>
  <select id="taskTypeId" name="taskTypeId" required>
    <option value="">Выберите тип...</option>
    @for(taskType <- taskTypes.filter(_.displayed).sortBy(_.ordinal.value)) {
    <option
      value="@taskType.id.value"
      @if(req.taskTypeId=""
      ="taskType.id.value){selected}"
    >
      @taskType.name.value
    </option>
    }
  </select>
</div>
```

Три аналогичных выпадающих списка. Особенности:

- **`.filter(_.displayed)`** — показываем только включённые элементы. Скрытые
  (архивные) не должны быть доступны для выбора.
- **`.sortBy(_.ordinal.value)`** — сортируем по порядку, заданному пользователем
  в настройках.
- **`@if(req.taskTypeId == taskType.id.value){selected}`** — выделяем текущий
  выбранный вариант при повторном рендере после ошибки.

**Порядок:**

```html
<div class="form-group">
  <label for="ordinal">Порядок:</label>
  <input
    id="ordinal"
    name="ordinal"
    type="number"
    min="1"
    value="@req.ordinal"
    required
  />
</div>
```

Порядковый номер задачи. По умолчанию `1000`, чтобы новые задачи попадали в конец
списка, если пользователь не задаст другое значение.

**Даты (с условной обязательностью):**

```html
<div class="form-group">
    <label for="startDate">Дата начала:</label>
    <input id="startDate" name="startDate" type="date"
           value="@req.startDate.map(_.toString).getOrElse("")"
           @if(req.tab == "in-progress") { required }/>
</div>
```

Здесь ключевая деталь: атрибут `required` **условный**. Для вкладки `"in-progress"`
даты обязательны (задача уже в работе, значит, начало зафиксировано). Для
`"planned"` и `"knowledge-base"` — опциональны.

**Информация:**

```html
<div class="form-group">
  <label for="information">Информация:</label>
  <textarea id="information" name="information" rows="10">
@req.information</textarea>
</div>
```

Многострочное текстовое поле для описания задачи, ссылок, заметок.

![](images/create_task_duration.png)

**Продолжительность (двухсоставное поле):**

```html
<div class="form-group">
  <label for="durationValue">Продолжительность:</label>
  <div class="duration-group">
    <input
      id="durationValue"
      name="durationValue"
      type="number"
      min="1"
      value="@req.durationValue"
      required
      class="duration-input"
      placeholder="Значение"
    />
    <select
      id="durationUnit"
      name="durationUnit"
      class="duration-select"
      required
    >
      <option
        value="minutes"
        @if(req.durationUnit=""
        ="TimeUnit.MINUTES){selected}"
      >
        в минутах
      </option>
      <option
        value="hours"
        @if(req.durationUnit=""
        ="TimeUnit.HOURS){selected}"
      >
        в часах
      </option>
      <option value="days" @if(req.durationUnit="" ="TimeUnit.DAYS){selected}">
        в днях
      </option>
    </select>
  </div>
</div>
```

**Почему две части, а не одно поле?**

Пользователю удобнее вводить продолжительность в разных единицах измерения: 
для быстрых задач это минуты, для крупных проектов — дни. 
Если бы мы жёстко задали минуты, пользователю пришлось бы вручную считать: 3 дня = 4320 минут. 
Двухсоставное поле решает эту проблему: пользователь вводит значение и выбирает единицу измерения.

При отправке формы сервер получает `durationValue` и `durationUnit` отдельно и конвертирует в `FiniteDuration`.

**Кнопки:**

```html
<div class="form-actions">
  <button type="submit" class="save-btn">Создать задачу</button>
  <button type="button" class="cancel-btn" onclick="closeCreateTaskModal()">
    Отмена
  </button>
</div>
```

- **«Создать задачу»** — `type="submit"`, отправляет форму через htmx.
- **«Отмена»** — `type="button"`, вызывает JavaScript-функцию `closeCreateTaskModal()`,
  которая закрывает модальное окно без создания задачи.

**Сообщение об ошибке:**

```html
<div class="messageBlock">
  @errorMessage.map { msg =>
  <div class="error">@msg</div>
  }
</div>
```

Если есть сообщение об ошибке — отображается красным блоком. Обычно это ошибки
валидации («Название не может быть пустым») или бизнес-логики («Дата начала
позже даты окончания»).

## Хэндлер создания задачи

После того как пользователь заполнил форму и нажал «Создать задачу», htmx отправляет
POST-запрос на `/tasks`. Здесь происходит вся логика создания.

### Хэндлер `CreateTaskHandler`

```dotty
final case class CreateTaskHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    settingsService: SettingsService[IO],
    taskLifecycleService: TaskLifecycleService[IO]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  val handler: Handler =
    Routes.taskCreate.handleIO: (request: CreateTaskRequest) =>
      val create: ((UserInfo, JwtToken)) => IO[String] =
        (user, jwtToken) =>
          CreateTask.parse(request, user.id) match
            case Validated.Valid(task) =>
              taskLifecycleService
                .create(task)
                .flatMap: _ =>
                  getViewCreateTaskModal(
                    isOpen = false,
                    settingsService = settingsService,
                    userId = user.id,
                    token = jwtToken,
                    tab = request.tab,
                    errorRequest = none,
                    errorMessage = none
                  )
                .recoverWith:
                  case ex: IllegalArgumentException =>
                    getViewCreateTaskModal(
                      isOpen = true,
                      settingsService = settingsService,
                      userId = user.id,
                      token = jwtToken,
                      tab = request.tab,
                      errorRequest = request.some,
                      errorMessage = ex.getMessage.some
                    )
            case Validated.Invalid(errors) =>
              getViewCreateTaskModal(
                isOpen = true,
                settingsService = settingsService,
                userId = user.id,
                token = jwtToken,
                tab = request.tab,
                errorRequest = request.some,
                errorMessage = errors.toChain.toVector.mkString("; ").some
              )
      handleUserIOByToken(create)(request.token)
end CreateTaskHandler
```

**Пошаговый разбор:**

1. **`handleIO: (request: CreateTaskRequest) => ...`** — обработчик принимает
   `CreateTaskRequest`, который был десериализован из формы через `FormCodec`.

2. **`CreateTask.parse(request, user.id)`** — валидация и преобразование запроса
   в доменный объект `CreateTask`. Возвращает `ValidatedNec[String, CreateTask]`:
    - **`Valid`** — все поля корректны, задача готова к созданию.
    - **`Invalid`** — есть ошибки валидации (пустое имя, отрицательный ordinal и т.д.).

3. **Ветка `Valid`:**
    - Вызываем `taskLifecycleService.create(task)` — это метод, который выполняет
      **дополнительную валидацию** (проверка существования типа, раздела, цели,
      проверка дат для статуса) и сохраняет задачу в БД.
    - **Успех** → возвращаем `getViewCreateTaskModal(isOpen = false, ...)`. Это
      «закрытое» модальное окно с кнопкой «Обновить список задач».
    - **Ошибка** (`IllegalArgumentException`) → возвращаем то же модальное окно
      с сохранёнными данными и сообщением об ошибке.

4. **Ветка `Invalid`:** возвращаем модальное окно с ошибками валидации, объединёнными
   через `"; "`.

5. **`handleUserIOByToken(create)(request.token)`** — извлекает токен из тела
   запроса (не из заголовка!), проверяет его через middleware и передаёт
   `(user, jwtToken)` в лямбду `create`.

**Почему валидация двухуровневая?**

`CreateTask.parse` проверяет **структурные** инварианты:

- Некорректный UUID задачи.
- Пустое имя.
- Отрицательный ordinal.

```dotty
object CreateTask:
  def parse(
      request: CreateTaskRequest,
      userId: UserId
  ): ValidatedNec[String, CreateTask] =
    (
      TaskName.either(request.name).toValidatedNec,
      TaskTypeId.either(request.taskTypeId).toValidatedNec,
      Status.find(request.tab).toValidNec(s"Status not found: ${request.tab}"),
      SectionId.either(request.sectionId).toValidatedNec,
      GoalId.either(request.goalId).toValidatedNec,
      Ordinal.either(request.ordinal).toValidatedNec
    ).mapN {
      case (name, taskTypeId, status, sectionId, goalId, ordinal) =>
        CreateTask(
          name = name,
          userId = userId,
          taskTypeId = taskTypeId,
          status = status,
          sectionId = sectionId,
          goalId = goalId,
          ordinal = ordinal,
          startDate = request.startDate,
          endDate = request.endDate,
          information = request.information,
          plan = Plan.Empty,
          duration =
            FiniteDuration(request.durationValue, request.durationUnit),
          timeSpent = FiniteDuration(0, TimeUnit.MINUTES)
        )
    }
```

`taskLifecycleService.create` проверяет **бизнес-инварианты**:

- Существует ли тип задачи с таким ID и принадлежит ли он пользователю.
- Существует ли раздел.
- Существует ли цель.
- Корректны ли даты для выбранного статуса (например, дата окончания не раньше
  даты начала).

Такое разделение позволяет переиспользовать `CreateTask.parse` в других контекстах
(например, в API), а бизнес-валидацию оставить в сервисе.

## Возврат на домашнюю страницу

После создания задачи пользователь может кликнуть «Обновить список задач»
в закрытом модальном окне. Это вызовет GET-запрос на `/home/{tab}`.

### Маршрут

```dotty
val home =
  Route(
    Request.get(Path.root / "home" / Param.string).extractHeader[Cookie],
    Response.ok(Entity.html)
  )
```

### Хэндлер `HomeHandler`

```dotty
final case class HomeHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    taskLifecycleService: TaskLifecycleService[IO]
)(using Logger[IO]):
  private val defaultPage = View.login(None).toString

  val handler: Handler =
    Routes.home.handleIO: (tab: String, cookie: Cookie) =>
      cookie.getToken match
        case Some(token) =>
          middleware.fromToken.run(token)
            .flatMap: (user, token) =>
              getViewWelcome(
                taskLifecycleService = taskLifecycleService,
                user = user,
                jwtToken = token,
                tab = tab
              )
            .recoverWith:
              case ex: AuthorizationException =>
                Logger[IO].error(ex)(
                  s"Ошибка при парсинге токена: ${ex.getMessage}"
                ).as(defaultPage)
              case ex =>
                Logger[IO].error(ex)(s"Ошибка: ${ex.getMessage}")
                  .as(defaultPage)
        case None =>
          defaultPage.pure[IO]
```

**Пошаговый разбор:**

1. **Обработка ошибок:**
    - `AuthorizationException` → логируем и возвращаем форму логина.
    - Любая другая ошибка → логируем и возвращаем форму логина.
2. **Если токена нет** — сразу возвращаем форму логина.

**Почему `HomeHandler` отличается от `InitialHandler`?**

| Аспект          | `InitialHandler`    | `HomeHandler` |
| --------------- | ------------------- | ------------- |
| Путь            | `/`                 | `/home/{tab}` |
| Параметры       | нет                 | `tab: String` |
| Вкладка         | Всегда `DefaultTab` | Любая из URL  |
| Источник токена | Cookie              | Cookie        |
| Возвращает      | Всю страницу        | Только `#app` |

`InitialHandler` используется, когда пользователь впервые заходит на сайт:
он не знает, какую вкладку хочет открыть, поэтому показывается `DefaultTab`.
`HomeHandler` используется при навигации внутри приложения: пользователь уже знает,
куда хочет попасть, и передаёт `tab` в URL.

**Почему возвращается только `#app`, а не вся страница?**

Потому что `hx-target="#app"` и `hx-swap="outerHTML"`. Это позволяет обновлять
**только центральную часть** страницы, не перезагружая шапку, скрипты и стили.
Такой подход экономит трафик и ускоряет взаимодействие.

## Полный поток создания задачи

Рассмотрим сценарий:

1. **Пользователь на вкладке «Запланировано»** кликает «+ Создать задачу».
2. **htmx отправляет GET** на `/task/create/planned` с токеном в заголовке.
3. **`CreateTaskModalHandler`** проверяет токен, загружает справочники,
   возвращает HTML модального окна.
4. **htmx вставляет HTML** в `#modal-container`. Пользователь видит форму.
5. **Пользователь заполняет форму** и нажимает «Создать задачу».
6. **htmx отправляет POST** на `/tasks` с данными формы.
7. **`CreateTaskHandler`** валидирует запрос, вызывает `taskLifecycleService.create`,
   создаёт задачу в БД.
8. **При успехе** возвращается «закрытое» модальное окно с кнопкой
   «Обновить список задач».
9. **Пользователь кликает** на эту кнопку → GET на `/home/planned`.
10. **`HomeHandler`** возвращает обновлённую страницу welcome с новой задачей в таблице.

**Если на шаге 7 ошибка** (например, дата окончания раньше даты начала):

- Возвращается модальное окно с **сохранёнными данными** и сообщением об ошибке.
- Пользователь исправляет данные и отправляет форму снова.

## Итог

Мы реализовали полный цикл создания задачи:

1. **`CreateTaskModalHandler`** — открывает модальное окно с формой.
2. **`createTask.scala.html`** — рендерит форму с условно обязательными полями
   (даты обязательны только для «В работе»).
3. **`CreateTaskHandler`** — валидирует данные, создаёт задачу, обрабатывает
   ошибки.
4. **`HomeHandler`** — возвращает пользователя на его вкладку с обновлённым списком.

**Ключевые архитектурные решения:**

- **Сохранение введённых данных** — `errorRequest` позволяет не терять данные
  при ошибке.
- **Условная обязательность полей** — атрибут `required` выставляется в зависимости
  от вкладки.
- **Мягкое закрытие модального окна** — после успеха пользователь видит кнопку
  «Обновить список задач», что даёт контроль над моментом перехода.

Эта архитектура делает создание задачи быстрым и удобным: пользователь не теряет
контекст, может исправить ошибки, а его данные не пропадают при неудачной попытке.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/

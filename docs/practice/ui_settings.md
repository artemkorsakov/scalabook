# Страница настроек

Страница настроек — это центр управления справочниками приложения. 
Здесь пользователь настраивает **типы задач**, **разделы**, **цели** и **периодические задачи** — четыре справочника,
которые затем используются при создании и фильтрации задач. 

Страница настроек устроена по единому шаблону для каждой из четырёх секций:

1. **Таблица** со списком существующих записей.
2. **Форма добавления** новой записи (скрыта по умолчанию).
3. **Сообщение об ошибке** (если предыдущая операция не удалась).

Каждая строка таблицы поддерживает три действия:

- **Редактирование** — открывает форму редактирования прямо в строке.
- **Удаление** — удаляет запись с подтверждением.
- **Индикация** — показывает порядок и признак «включён».

![Settings](images/settings.png)

## Маршрут

Маршрут объявлен приватно внутри `SettingsHandler`:

```dotty
private val route = Route(
  Request.get(Path.root / "settings").extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

**Разбор маршрута:**

- **`Request.get(Path.root / "settings")`** — GET-запрос на `/settings`.
  Это защищённый эндпоинт, доступный только авторизованным пользователям.
- **`.extractHeader[Authorization]`** — извлекает заголовок `Authorization`, содержащий
  JWT-токен в формате `Bearer <token>`. Именно через этот заголовок мы узнаём,
  кто именно запрашивает настройки.
- **`Response.ok(Entity.html).orNotFound`** — успешный ответ (200 OK) с HTML-телом.
  `.orNotFound` — запасной вариант (404), хотя на практике он редко срабатывает,
  потому что ошибки обрабатываются внутри хэндлера.

**Почему здесь используется заголовок `Authorization`, а не куки?**

Страница настроек вызывается через htmx-запрос из рендер-метода `Views.welcome`. 
htmx передаёт токен через заголовок `Authorization`, а не через куки — так удобнее, 
потому что клиентский JavaScript может явно указать токен в `hx-headers`. 
Это соответствует общей архитектуре приложения: все защищённые эндпоинты принимают токен в заголовке.

## Хэндлер `SettingsHandler`

```dotty
final case class SettingsHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    settingsService: SettingsService[IO]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  private val route = Route(
    Request.get(Path.root / "settings").extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )

  val handler: Handler =
    route.handleIO: (authorization: Authorization) =>
      withUserFromHeader(authorization): (user, jwtToken) =>
        SettingsViews.getViewSettings(
          settingsService = settingsService,
          userId = user.id,
          jwtToken = jwtToken
        )
```

**Пошаговый разбор:**

1. **Зависимости:**
    - `middleware: JwtAuthMiddleware[IO, UserInfo]` — middleware для проверки токена.
      Умеет превращать строку токена в объект `UserInfo` (id, username).
    - `settingsService: SettingsService[IO]` — сервис для работы со справочниками.
      Умеет загружать типы задач, разделы и цели пользователя.
    - `using Logger[IO]` — логгер.

2. **`extends AuthorizationHandler(middleware)`** — наследуемся от базового класса,
   который инкапсулирует логику проверки токена. Это избавляет от необходимости
   вручную извлекать заголовок `Authorization` и валидировать его.

3. **`withUserFromHeader(authorization): (user, token) => ...`** — специальный метод, который
   принимает два аргумента: `authorization` (извлечённый `handleIO` из заголовка) и
   каррированную лямбду `(user, token)`. Метод:
    - Проверяет токен через middleware.
    - Если токен валиден, передаёт в лямбду `user` (информация о пользователе)
      и `token` (сам JWT-токен).
    - Если невалиден — автоматически возвращает `AuthorizationException`,
      которую обрабатывает `AuthorizationHandler`.

4. **`SettingsViews.getViewSettings(...)`** — вспомогательная view-функция из `SettingsViews`
   формирует HTML-страницу настроек, загружая все справочники пользователя.

## Формирование страницы настроек

Логика загрузки справочников вынесена в объект `SettingsViews` в виде двух перегруженных
функций `getViewSettings`. Первая — упрощённая, без сообщений об ошибках. Вторая — полная,
принимает четыре опциональных сообщения (по одному на каждый справочник).

### Упрощённая версия

```dotty
def getViewSettings(
    settingsService: SettingsService[IO],
    userId: UserId,
    jwtToken: SignedJwt
): IO[String] =
  getViewSettings(
    settingsService = settingsService,
    userId = userId,
    jwtToken = jwtToken,
    errorTaskTypeMessage = none,
    errorSectionMessage = none,
    errorGoalMessage = none,
    errorPeriodicStepMessage = none
  )
```

Эта версия используется, когда страница открывается «с нуля» (например, при клике на кнопку «Настройки»). 
Никаких ошибок нет, поэтому все четыре сообщения — `none`.

### Полная версия

```dotty
def getViewSettings(
    settingsService: SettingsService[IO],
    userId: UserId,
    jwtToken: SignedJwt,
    errorTaskTypeMessage: Option[String],
    errorSectionMessage: Option[String],
    errorGoalMessage: Option[String],
    errorPeriodicStepMessage: Option[String]
): IO[String] =
  for
    taskTypes     <- settingsService.getTaskTypes(userId)
    sections      <- settingsService.getSections(userId)
    goals         <- settingsService.getGoals(userId)
    periodicSteps <- settingsService.getSteps(userId)
  yield
    val sortedSteps: Vector[PeriodicStep] =
      periodicSteps.sortBy(step => (step.periodicType.ordinal, step.number))

    Views.settings(
      token = jwtToken,
      taskTypes = taskTypes,
      sections = sections,
      goals = goals,
      periodicSteps = sortedSteps,
      errorTaskTypeMessage = errorTaskTypeMessage,
      errorSectionMessage = errorSectionMessage,
      errorGoalMessage = errorGoalMessage,
      errorPeriodicStepMessage = errorPeriodicStepMessage
    ).render
```

**Что происходит:**

1. **Загружаем типы задач, разделы, цели и периодические шаги пользователя.**
   Шаги сортируются по типу периодичности и номеру (`step.periodicType.ordinal`, `step.number`).
2. **Рендер представления `Views.settings(...)`** — передаём в scalatags-метод:
   - `token` — JWT-токен для htmx-заголовков.
   - `taskTypes`, `sections`, `goals`, `periodicSteps` — загруженные справочники.
   - Четыре опциональных сообщения об ошибках.

**Почему четыре отдельных сообщения об ошибках?**

Каждый справочник — это независимая сущность. 
Пользователь может ошибиться при добавлении типа задачи (например, ввести пустое имя), 
но при этом успешно добавить раздел. 
Четыре отдельных сообщения позволяют показывать ошибку **точно под** соответствующим разделом.

## Рендер представления `Views.settings`

Scalatags-метод в `Views.scala`:

```dotty
def settings(
    token: SignedJwt,
    taskTypes: Vector[TaskType],
    sections: Vector[Section],
    goals: Vector[Goal],
    periodicSteps: Vector[PeriodicStep],
    errorTaskTypeMessage: Option[String],
    errorSectionMessage: Option[String],
    errorGoalMessage: Option[String],
    errorPeriodicStepMessage: Option[String]
): Frag
```

**Параметры метода:**

| Параметр                 | Тип                | Назначение                                 |
| ------------------------ | ------------------ | ------------------------------------------ |
| `token`                  | `SignedJwt`         | JWT-токен для htmx-заголовков              |
| `taskTypes`              | `Vector[TaskType]` | Список типов задач пользователя            |
| `sections`               | `Vector[Section]`  | Список разделов пользователя               |
| `goals`                  | `Vector[Goal]`     | Список целей пользователя                  |
| `periodicSteps`          | `Vector[PeriodicStep]` | Список периодических шагов пользователя |
| `errorTaskTypeMessage`   | `Option[String]`   | Сообщение об ошибке для секции типов задач |
| `errorSectionMessage`    | `Option[String]`   | Сообщение об ошибке для секции разделов    |
| `errorGoalMessage`       | `Option[String]`   | Сообщение об ошибке для секции целей       |
| `errorPeriodicStepMessage` | `Option[String]` | Сообщение об ошибке для секции периодических шагов |

### Общая структура

```dotty
frag(
  div(
    `id` := "app",
    cls  := "app-container",
    div(
      `id` := "welcomeBlock",
      cls  := "welcome-container",
      div(cls := "welcome-header")(
        div(cls := "welcome-user")(
          h2("⚙️ Настройки"),
          ...
```

**Ключевые моменты:**

- **`#app`** — контейнер, который htmx заменяет при переходах.
- **`#welcomeBlock`** — блок с заголовком и секциями.

### Секция «Типы задач»

Три справочника (типы задач, разделы, цели) рендерятся единой scalatags-функцией `settingsTable(tableClass, rows, emptyMessage)`, которая генерирует `<table>` с `<thead>` и `<tbody hx-target="closest tr" hx-swap="outerHTML">`. Каждая строка строится локальной функцией `row(...)`.

Вот пример структуры `<thead>` столбцов:

```dotty
def settingsTable(
    tableClass: String,
    rows: Vector[Tag],
    emptyMessage: String
): Tag =
  if rows.nonEmpty then
    div(cls := "settings-list")(
      table(cls := tableClass)(
        thead(tr(th("Название"), th("Порядок"), th("Включен?"), th(""))),
        tbody(
          attr("hx-target") := "closest tr",
          attr("hx-swap")   := "outerHTML"
        )(rows*)
      )
    )
  else p(cls := "empty-message")(emptyMessage)
```

Атрибуты `<tbody>` — `hx-target="closest tr"`, `hx-swap="outerHTML"` — позволяют при удалении или редактировании строки заменить только `<tr>` (а не всю таблицу).

**Разбор таблицы:**

Каждая строка генерируется scalatags-хелпером `row(...)`. `<tbody>` состоит из `tr` для каждого элемента, где значения подставляются из карточки объекта.

- **Сортировка** — `taskTypes.sortBy(_.ordinal.value)` сортирует записи по порядку.
  Пользователь видит их в том порядке, который задал сам.
- **`data-task-type-id`** — атрибут с ID записи. Используется для отладки и потенциальной
  фильтрации на клиенте.
- **Колонка «Включен?»** — показывает `✓`, если `displayed = true`, и `✗` в противном случае.
  Это позволяет пользователю видеть, какие записи активны, а какие скрыты (например,
  архивные).
- **Кнопка «Редактировать»** — использует `hx-trigger="edit"` вместо `click`. Это значит,
  что запрос отправляется **только** когда JavaScript-функция `handleEditClick()`
  программно вызовет событие `edit`. Такой подход позволяет реализовать **одновременное
  редактирование только одной строки**: если пользователь попытается редактировать
  вторую строку, `handleEditClick` покажет предупреждение и предложит сначала
  закрыть текущую.
- **Кнопка «Удалить»** — использует `hx-delete` и `hx-confirm`. При клике браузер
  показывает диалог подтверждения с указанием имени записи. Если пользователь
  соглашается — htmx отправляет DELETE-запрос.
- **`hx-target="#app"`** для удаления — после удаления сервер возвращает обновлённую
  страницу настроек целиком, и htmx заменяет `#app`.

![Settings Task Type](images/settings_task_types.png)

**Форма добавления типа задачи:**

```dotty
CreateItemRequest.render(
  "addTaskTypeForm",
  "taskTypeForm",
  "task_types",
  token,
  "taskTypeName",
  "типа задачи",
  "hideAddTaskTypeForm()"
),
button(
  cls             := "add-btn",
  attr("onclick") := "showAddTaskTypeForm()"
)(
  "+ Добавить тип задачи"
),

object CreateItemRequest:
  def render(
      divId: String,
      formId: String,
      link: String,
      token: SignedJwt,
      nameId: String,
      entityLabel: String,
      onClick: String
  ): Tag =
    div(
      attr("id") := divId,
      cls        := "add-form",
      style      := "display: none;",
      form(
        attr("id")      := formId,
        attr("hx-post") := s"/settings/$link",
        Htmx.appTarget,
        Htmx.appSwap,
        Htmx.hiddenToken(token.show),
        div(
          cls := "form-group",
          label(`for` := nameId)(s"Название $entityLabel:"),
          input(
            attr("id") := nameId,
            name       := "name",
            `type`     := "text",
            required,
            placeholder := s"Введите название $entityLabel"
          )
        ),
        div(
          cls := "form-actions",
          button(`type` := "submit", cls := "add-btn")("Сохранить"),
          button(
            `type`  := "button",
            cls     := "danger-btn",
            onclick := onClick
          )("Отмена")
        )
      )
    )
```

![Settings, add a new type](images/settings_add_new_task_type.png)

**Ключевые моменты:**

- **Форма скрыта по умолчанию** (`style="display: none;"`). Она показывается
  JavaScript-функцией `showAddTaskTypeForm()` при клике на кнопку «+ Добавить».
- **`hx-post="/settings/task_types"`** — POST-запрос на создание нового типа задачи.
- **`hx-target="#app"`** — после создания сервер возвращает обновлённую страницу,
  htmx заменяет `#app`.
- **`<input type="hidden" name="token" value="{token.show}" />`** — токен передаётся
  в теле формы из-за проблемы [в фреймворке Krop](https://github.com/creativescala/krop/issues/55).
  Значение берётся из JWT-токена (параметр `token: SignedJwt`).
- **Кнопка «Отмена»** — вызывает `hideAddTaskTypeForm()`, которая скрывает форму
  и очищает её поля.

**Ключевые моменты:**

- Каждый справочник имеет **своё** сообщение об ошибке (`errorXxxMessage`),
  которое рендерится под соответствующей секцией. Это позволяет показывать ошибку
  **точно под** соответствующим разделом.
- Если соответствующее сообщение `None` — блок не отображается.

![Settings, task type error](images/settings_task_type_error.png)

### Секция «Разделы» и «Цели»

Секция разделов и целей устроена аналогично секции типов задач. Отличия
только в:

- **Названии таблицы** — `sections-table` вместо `task-types-table`.
- **Классе строки** — `section-row` вместо `task-type-row`.
- **Атрибуте** — `data-section-id` вместо `data-task-type-id`.
- **URL-ах** — `/settings/sections` вместо `/settings/task_types`.
- **Сообщении об ошибке** — `errorSectionMessage`.

Такое единообразие — осознанное решение. Копирование кода для каждого справочника
**лучше**, чем преждевременная абстракция: если в будущем потребуется
специализировать поведение одного из справочников, это легко сделать, не ломая
остальные.

![Settings, sections](images/settings_sections.png)

### Секция «Периодические задачи»

В `Views.settings` есть четвёртая секция — периодические задачи. В отличие от трёх
справочников выше, здесь используется отдельная структура:

- Периодические шаги группируются по типу периодичности (`PeriodicType`).
- Для каждого типа генерируется `div.periodic-type-section` с заголовком `h4.periodic-type-title`.
- Внутри таблицы с классом `periodic-steps-table` столбцы: **«Номер»**, **«Наименование»**.
- Каждая строка — `tr.periodic-step-row[data-periodic-step-id="{id}"]`.
  Первый столбец — номер (`step.number`), второй — наименование с markdown-контентом
  (`step.name`, рендерится через `data-markdown`).
- Кнопки: **✏️ Редактировать** (`hx-get="/settings/periodic_steps/{id}/edit"`, `hx-trigger="edit"`,
  `onclick="handleEditClick(this)"`) и **🗑️ Удалить** (`hx-delete="/settings/periodic_steps/{id}"`,
  `hx-target="#app"`, `hx-swap="outerHTML"`, `hx-confirm="Вы уверены, что хотите удалить периодическую задачу «...»?"`).

Секция формируется в `Views.settings` через параметр `periodicSteps: Vector[PeriodicStep]`.
Порядок определяется сортировкой в `SettingsViews.getViewSettings`.

Детали работы с периодическими шагами разобраны в разделе о периодических задачах.

## Поток работы со страницей настроек

Рассмотрим типичный сценарий:

1. **Пользователь кликает «⚙️ Настройки»** на главной странице → htmx отправляет
   GET на `/settings` с заголовком `Authorization: Bearer <token>`.
2. **`SettingsHandler`** проверяет токен и загружает справочники пользователя
   (типы задач, разделы, цели, периодические шаги).
3. **Сервер возвращает HTML** с таблицами справочников. htmx заменяет
   содержимое `#app`.
4. **Пользователь кликает «+ Добавить тип задачи»** → JavaScript показывает скрытую
   форму. Форма становится видимой.
5. **Пользователь вводит название и кликает «Сохранить»** → htmx отправляет POST
   на `/settings/task_types` с телом формы (включая токен).
6. **Сервер валидирует данные**, создаёт запись и возвращает обновлённую страницу
   настроек. htmx заменяет `#app` — пользователь видит новую строку в таблице.
7. **Если была ошибка** (например, пустое имя) — сервер возвращает ту же страницу,
   но с `errorTaskTypeMessage = Some("...")`. Пользователь видит красное сообщение
   под таблицей.

## Итог

Мы реализовали страницу настроек — центр управления четырьмя справочниками приложения:

1. **Типы задач** — классификация задач по формату (книга, статья, видео).
2. **Разделы** — тематическая группировка задач (Программирование, Психология).
3. **Цели** — стратегические ориентиры, к которым привязаны задачи.
4. **Периодические шаги** — задачи, повторяющиеся по расписанию, сгруппированные по типу периодичности.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

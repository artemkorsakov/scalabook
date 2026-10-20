{%
showPrev = true
prevPage = ../ui_authorization.html
showNext = true
nextPage = ui_login_logout.html
%}

# Шаблон `welcome.scala.html`

`welcome.scala.html` — [центральный шаблон всего приложения](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/twirl/ru/scalabook/pdp/views/welcome.scala.html). 
Это рабочий стол пользователя, куда он попадает после авторизации. 
Шаблон отображает три вкладки с задачами в разных статусах,
кнопки управления (настройки и выход), а также служит «хостом» для всех модальных окон.

Основная задача шаблона — быть **единым экраном** для работы с задачами: пользователь видит
свой прогресс, может переключаться между вкладками, фильтровать задачи по типу и разделу,
создавать новые задачи, открывать существующие и управлять их статусами. Всё это происходит
без перезагрузки страницы благодаря htmx.

## Импорты и параметры

```html
@import ru.scalabook.pdp.frontend.TableTaskCard
@import java.time.format.DateTimeFormatter

@(username: String, token: String, tab: String, 
  inProgressTasks: Vector[TableTaskCard], 
  plannedTasks: Vector[TableTaskCard], 
  knowledgeBaseTasks: Vector[TableTaskCard], 
  plannedTaskTypes: Vector[String], plannedSections: Vector[String], 
  knowledgeBaseTaskTypes: Vector[String], knowledgeBaseSections: Vector[String])
```

**Импорты:**

- **`TableTaskCard`** — доменная модель для отображения задачи в таблице или карточке.
  Содержит уже «обогащённые» данные: имя типа, раздела и цели вместо их id.
- **`DateTimeFormatter`** — для форматирования дат в вид `dd.MM.yyyy` (например, `25.07.2026`).

**Параметры шаблона:**

| Параметр                 | Тип                     | Назначение                                                                                                                |
| ------------------------ | ----------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| `username`               | `String`                | Имя пользователя для приветствия. Отображается в шапке: «Добро пожаловать, @username!»                                    |
| `token`                  | `String`                | JWT-токен для отправки в htmx-заголовках. Используется кнопкой «Выход» и всеми защищёнными запросами                      |
| `tab`                    | `String`                | Текущая активная вкладка. Одно из значений: `"in-progress"`, `"planned"`, `"knowledge-base"`                              |
| `inProgressTasks`        | `Vector[TableTaskCard]` | Задачи в статусе «В работе». Отображаются карточками с прогресс-баром                                                     |
| `plannedTasks`           | `Vector[TableTaskCard]` | Задачи в статусе «Запланировано». Отображаются таблицей с фильтрами                                                       |
| `knowledgeBaseTasks`     | `Vector[TableTaskCard]` | Задачи в статусе «База знаний». Отображаются таблицей с фильтрами                                                         |
| `plannedTaskTypes`       | `Vector[String]`        | Список типов задач для фильтра на вкладке «Запланировано». Содержит только те типы, которые реально есть в `plannedTasks` |
| `plannedSections`        | `Vector[String]`        | Список разделов для фильтра на вкладке «Запланировано»                                                                    |
| `knowledgeBaseTaskTypes` | `Vector[String]`        | Список типов задач для фильтра на вкладке «База знаний»                                                                   |
| `knowledgeBaseSections`  | `Vector[String]`        | Список разделов для фильтра на вкладке «База знаний»                                                                      |

**Почему фильтры передаются отдельными параметрами?**

Можно было бы вычислять список типов и разделов прямо в шаблоне, «пробегаясь» по задачам.
Но это плохая практика по двум причинам:

1. **Производительность** — при большом количестве задач шаблон выполнял бы O(n) операций
   на каждый рендер. Лучше сделать это один раз в сервисе.
2. **Разделение ответственности** — шаблон должен заниматься **отображением**,
   а логика (какие типы показывать в фильтре) — это ответственность сервиса.
   Шаблон получает уже готовые списки.

## Структура разметки

Разметка организована вокруг контейнера `#app`. Это **ключевой элемент** для htmx:
когда сервер возвращает новую страницу или её фрагмент, htmx заменяет именно содержимое `#app`.

```html
<div id="app" class="app-container">
  <iframe onload="saveUserCookies('@token')" style="display:none;"></iframe>
  <div id="welcomeBlock" class="welcome-container">...</div>
  <div id="modal-container"></div>
</div>
```

**Разбор структуры:**

- **`#app`** — главный контейнер. Класс `app-container` задаёт широкую ширину
  (на всю страницу), в отличие от `app-container-narrow`, который используется
  на страницах логина и регистрации.
- **`<iframe>`** — технический трюк: невидимый iframe с `onload`, который сохраняет
  токен в `localStorage` и куки при загрузке страницы. Атрибут `style="display:none"`
  скрывает его от пользователя.
- **`#welcomeBlock`** — блок с приветствием, кнопками и вкладками.
- **`#modal-container`** — пустой контейнер, в который htmx будет подгружать модальные окна
  (создание задачи, планирование, старт, завершение и т.д.).

**Почему `#modal-container` вынесен наружу?**

Модальные окна — это оверлеи, которые должны перекрывать **всю** страницу, а не только
её центральную часть. Если бы `#modal-container` был внутри `#welcomeBlock`, то при замене
содержимого `#app` модалки бы «слетали». Вынесение наружу делает их независимыми
от содержимого `#app`.

### Сохранение токена в куки

```html
<iframe onload="saveUserCookies('@token')" style="display:none;"></iframe>
```

**Зачем это нужно:**

Когда пользователь логинится, сервер устанавливает куку `token` через HTTP-заголовок
`Set-Cookie`. Однако клиентский JavaScript **не всегда** может прочитать эту куку, если
она помечена как `HttpOnly`. Кроме того, при некоторых сценариях (например, при работе
с htmx и замене HTML) куки могут не обновиться.

Этот iframe решает проблему: при загрузке страницы `welcome` он вызывает
`saveUserCookies('@token')`, который:

1. Сохраняет токен в `localStorage` (доступен из JavaScript).
2. Дублирует токен в обычную куку (не `HttpOnly`), доступную из JS.

Так JavaScript-функции (например, `openTask`, `filterPlannedTasks`) могут использовать токен
для запросов.

## Верхняя панель

```html
<div class="welcome-header">
  <div class="welcome-user">
    <h2>Добро пожаловать, @username!</h2>
  </div>
  <div class="welcome-actions">
    <button ... class="settings-btn">⚙️ Настройки</button>
    <button ... class="danger-btn">Выход</button>
  </div>
</div>
```

**Структура:**

- **`.welcome-user`** — блок с приветствием. Отображает имя пользователя из `@username`.
- **`.welcome-actions`** — блок с кнопками действий.

**Кнопка «Настройки»:**

```html
<button
  type="button"
  onclick="htmx.process(this); this.click();"
  hx-get="/settings"
  hx-target="#app"
  hx-swap="outerHTML"
  class="settings-btn"
>
  ⚙️ Настройки
</button>
```

- **`hx-get="/settings"`** — GET-запрос на страницу настроек.
- **`hx-target="#app"`** — заменяем содержимое `#app` на страницу настроек.
- **`hx-swap="outerHTML"`** — заменяем весь блок `#app`, а не только его содержимое.
- **`onclick="htmx.process(this); this.click();"`** — важно: после того как HTML был вставлен
  через `outerHTML`, htmx не всегда автоматически обрабатывает новые элементы.
  `htmx.process(this)` «навешивает» обработчики на элемент, а `this.click()` программно
  вызывает клик — теперь htmx отправляет запрос.

**Кнопка «Выход»:**

```html
<button
  type="button"
  onclick="clearUserCookies(); htmx.process(this); this.click();"
  hx-post="/auth/logout"
  hx-target="#app"
  hx-swap="outerHTML"
  hx-headers='{"Authorization": "Bearer @token"}'
  class="danger-btn"
>
  Выход
</button>
```

- **`onclick="clearUserCookies(); htmx.process(this); this.click();"`** — сначала очищаем
  куки (чтобы даже если запрос не пройдёт, пользователь вышел локально), затем
  обрабатываем и вызываем клик.
- **`hx-post="/auth/logout"`** — POST-запрос на выход.
- **`hx-headers='{"Authorization": "Bearer @token"}'`** — передаём токен в заголовке.
  Это важно, потому что после `clearUserCookies()` токена в куке уже нет, а на сервере
  нужно проверить его валидность, чтобы удалить из чёрного списка.

## Вкладки

Вкладки — это механизм навигации между тремя представлениями задач. Каждая вкладка
имеет свою логику отображения: «В работе» показывает карточки с прогресс-барами,
«Запланировано» и «База знаний» — таблицы с фильтрами.

```html
<div class="tabs-container">
    <div class="tabs-header">
        <button class="tab-btn @if(tab == "in-progress"){active}"
                data-tab="in-progress"
                onclick="switchTab('in-progress')">
            🛠️ В работе
            <span title="...">ℹ️</span>
        </button>
        ...
    </div>
    <div id="tab-in-progress" class="tab-content @if(tab == "in-progress"){active}">
        ...
    </div>
    ...
</div>
```

**Как работает переключение вкладок:**

1. **Каждая кнопка** имеет атрибут `data-tab` с именем вкладки и `onclick="switchTab('...')"`.
2. **Функция `switchTab`** ([в pdp.js](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/resources/assets/js/pdp.js)):
   - Убирает класс `active` у всех кнопок и всех блоков `.tab-content`.
   - Добавляет `active` к нажатой кнопке и соответствующему блоку.
3. **При первой загрузке** активная вкладка определяется параметром `tab`:
   `@if(tab == "in-progress"){active}`.

**Зачем нужен `<span title="...">ℹ️</span>`?**

Это иконка-подсказка. При наведении на неё браузер показывает всплывающий текст,
объясняющий, что означает вкладка. Например, для «В работе»:
«Текущие задачи в работе, отсортированные по планируемой дате завершения».

Это улучшает UX: пользователь, впервые попавший на страницу, понимает, что где лежит.

![in-progress-hint](../images/in-progress-hint.png)

## Вкладка «В работе»

```html
<div id="tab-in-progress" class="tab-content @if(tab == "in-progress"){active}">
    <div class="tab-actions">
        <button class="add-btn"
                hx-get="/task/create/in-progress"
                hx-target="#modal-container"
                hx-swap="innerHTML"
                onclick="openCreateTaskModal()">
            + Создать задачу
        </button>
    </div>
    <div class="tasks-grid">
        @for(task <- inProgressTasks) {
        <div id="@task.id" class="task-card" onclick="openTask('@task.id')">
            ...
        </div>
        }
    </div>
</div>
```

**Особенности вкладки:**

- **Карточки, а не таблица** — задачи «В работе» отображаются карточками, потому что
  для них важны прогресс-бар и дедлайн, а не табличные метрики.
- **Клик по карточке** — открывает страницу задачи в новой вкладке браузера
  (`onclick="openTask('@task.id')"`). Функция `openTask` формирует URL `/task/{id}`
  и открывает его в новом окне.
- **Кнопка «Создать задачу»** — открывает модальное окно с формой создания задачи.
  Запрос идёт на `/task/create/in-progress`, а результат вставляется в `#modal-container`.

**Структура карточки:**

```html
<div class="task-title">@task.name</div>

<div class="task-meta-grid">
  <div class="task-meta-item">
    <span class="meta-icon">📅</span>
    <span class="meta-label">Начало:</span>
    <span class="meta-value">@task.startDate.map(...).getOrElse("—")</span>
  </div>
  <div class="task-meta-item">
    <span class="meta-icon">⏰</span>
    <span class="meta-label">Дедлайн:</span>
    <span class="meta-value">@task.endDate.map(...).getOrElse("—")</span>
  </div>
</div>

<div class="task-progress">
  <div class="progress-label">
    <span>Прогресс</span>
    <span class="progress-percent">@{(task.progress * 100).toInt}%</span>
  </div>
  <div class="progress-bar">
    <div
      class="progress-fill"
      style="width: @{(task.progress * 100).toInt}%"
    ></div>
  </div>
</div>
```

**Разбор:**

- **Название задачи** — заголовок карточки.
- **Мета-данные** — даты начала и дедлайна. Форматируются через `DateTimeFormatter.ofPattern("dd.MM.yyyy")`.
  Если даты нет (например, задача не взята в работу) — отображается `—`.
- **Прогресс-бар** — визуализация прогресса. `task.progress` — число от 0.0 до 1.0,
  умножаем на 100 и округляем до целого. Ширина `.progress-fill` задаётся inline-стилем.

![in-progress-tasks](../images/in-progress-tasks.png)

## Вкладка «Запланировано»

```html
<div id="tab-planned" class="tab-content @if(tab == "planned"){active}">
    <div class="tab-actions">
        <button class="add-btn"
                hx-get="/task/create/planned"
                hx-target="#modal-container"
                hx-swap="innerHTML"
                onclick="openCreateTaskModal()">
            + Создать задачу
        </button>
    </div>

    <div class="filter-container">
        <div class="filter-group">
            <label for="plannedTypeFilter">Тип:</label>
            <select id="plannedTypeFilter" class="filter-select" onchange="filterPlannedTasks()">
                <option value="all">Все типы</option>
                @for(taskType <- plannedTaskTypes) {
                <option value="@taskType">@taskType</option>
                }
            </select>
        </div>
        <div class="filter-group">
            <label for="plannedSectionFilter">Раздел:</label>
            <select id="plannedSectionFilter" class="filter-select" onchange="filterPlannedTasks()">
                <option value="all">Все разделы</option>
                @for(section <- plannedSections) {
                <option value="@section">@section</option>
                }
            </select>
        </div>
    </div>

    <div class="tasks-table-container">
        <table class="tasks-table" id="plannedTasksTable">
            ...
        </table>
    </div>
</div>
```

**Особенности вкладки:**

- **Таблица, а не карточки** — запланированные задачи сравниваются по типу, разделу
  и порядковому номеру, поэтому таблица удобнее.
- **Два фильтра** — по типу и разделу. Списки опций (`plannedTaskTypes`, `plannedSections`)
  приходят из сервиса и содержат только те типы/разделы, которые реально есть
  в `plannedTasks`. Это важно: пользователь не видит бесполезных опций.
- **Фильтрация на клиенте** — JavaScript-функция `filterPlannedTasks()` скрывает строки,
  которые не соответствуют выбранным фильтрам. Это быстро (не требует запроса к серверу)
  и удобно: пользователь может комбинировать фильтры в реальном времени.

**Структура таблицы:**

```html
<table class="tasks-table" id="plannedTasksTable">
  <thead>
    <tr>
      <th>Название</th>
      <th>Тип</th>
      <th>Раздел</th>
      <th>Порядок</th>
    </tr>
  </thead>
  <tbody>
    @for(task <- plannedTasks) {
    <tr
      class="task-row"
      data-task-type="@task.taskType"
      data-task-section="@task.section"
      onclick="openTask('@task.id')"
    >
      <td>@task.name</td>
      <td>@task.taskType</td>
      <td>@task.section</td>
      <td>@task.ordinal.value</td>
    </tr>
    }
  </tbody>
</table>
```

**Ключевые моменты:**

- **`data-task-type` и `data-task-section`** — атрибуты, по которым JavaScript-функция
  `filterPlannedTasks()` понимает, показывать строку или скрывать.
- **Клик по строке** — открывает задачу в новой вкладке. `onclick="openTask('@task.id')"`.
- **Колонка «Порядок»** — показывает `task.ordinal.value`, по которому задачи сортируются.

![planned-tasks](../images/planned-tasks.png)

## Вкладка «База знаний»

```html
<div id="tab-knowledge-base" class="tab-content @if(tab == "knowledge-base"){active}">
    <div class="tab-actions">
        <button class="add-btn"
                hx-get="/task/create/knowledge-base"
                hx-target="#modal-container"
                hx-swap="innerHTML"
                onclick="openCreateTaskModal()">
            + Создать задачу
        </button>
    </div>

    <div class="filter-container">
        ... (аналогично вкладке «Запланировано»)
    </div>

    <div class="tasks-table-container">
        <table class="tasks-table" id="knowledgeTasksTable">
            <thead>
            <tr>
                <th>Название</th>
                <th>Тип</th>
                <th>Раздел</th>
                <th>Продолжительность</th>
            </tr>
            </thead>
            ...
        </table>
    </div>
</div>
```

**Отличия от «Запланировано»:**

- **Последняя колонка** — «Продолжительность» вместо «Порядок». Для задач в базе знаний
  важна оценка трудоёмкости, т.к. задача ещё не запланирована и очередность у задач одинаковая.
- **Другой набор фильтров** — `knowledgeBaseTaskTypes` и `knowledgeBaseSections`.
- **Функция `filterKnowledgeTasks()`** — аналогична `filterPlannedTasks()`, но работает
  с другой таблицей.

![knowledge-base-tasks](../images/knowledge-base-tasks.png)

## Контейнер для модальных окон

```html
<div id="modal-container"></div>
```

Модальные окна — это отдельные HTML-фрагменты, которые подгружаются с сервера при клике
на кнопку «Создать задачу», «Взять в работу», «Завершить задачу» и т.д. (Эти кнопки будут разобраны в разделе "Страница задачи").
htmx вставляет их в `#modal-container` через `hx-target="#modal-container"` и `hx-swap="innerHTML"`.

**Типы модальных окон:**

| Модальное окно  | Триггер                                         | Эндпоинт              |
| --------------- | ----------------------------------------------- | --------------------- |
| Создание задачи | Кнопка «+ Создать задачу» на любой вкладке      | `/task/create/{tab}`  |
| Планирование    | Кнопка «📋 В очередь» на странице задачи        | `/task/{id}/queue`    |
| Начало работы   | Кнопка «🚀 Взять в работу» на странице задачи   | `/task/{id}/start`    |
| Завершение      | Кнопка «✅ Завершить задачу» на странице задачи | `/task/{id}/complete` |

## Как всё работает вместе

Рассмотрим полный сценарий:

1. **Пользователь логинится** → сервер возвращает `welcome.scala.html` с тремя вкладками.
   По умолчанию открыта «В работе».
2. **Пользователь кликает на вкладку «Запланировано»** → JavaScript-функция `switchTab`
   меняет активный класс. **Запроса к серверу нет** — все три вкладки уже отрисованы.
3. **Пользователь нажимает «+ Создать задачу»** → htmx отправляет GET на
   `/task/create/planned`. Сервер возвращает HTML модального окна. htmx вставляет
   его в `#modal-container`.
4. **Пользователь заполняет форму и нажимает «Создать»** → htmx отправляет POST
   на `/tasks`. Сервер создаёт задачу и возвращает обновлённый `welcome.scala.html`.
   htmx заменяет содержимое `#app` — пользователь видит новую задачу в таблице.
5. **Пользователь кликает на строку задачи** → JavaScript-функция `openTask` открывает
   новую вкладку браузера с URL `/task/{id}`. Там отображается детальная страница задачи.
6. **Пользователь нажимает «Выход»** → `clearUserCookies()` очищает куки, затем htmx
   отправляет POST на `/auth/logout`. Сервер инвалидирует токен и возвращает форму логина.
   htmx заменяет `#app` — пользователь видит форму входа.

## Итог

`welcome.scala.html` — это **рабочий стол** пользователя, который:

1. **Показывает задачи в трёх представлениях**: карточки для активных, таблицы
   для запланированных и базы знаний.
2. **Позволяет фильтровать задачи** по типу и разделу прямо на клиенте.
3. **Интегрируется с htmx** для загрузки модальных окон и отправки форм без перезагрузки.
4. **Хранит JWT-токен** в куках и `localStorage` для последующих запросов.
5. **Даёт быстрый доступ** к настройкам и выходу через кнопки в шапке.

Шаблон построен так, чтобы **минимизировать количество запросов** к серверу:
переключение вкладок и фильтрация происходят на клиенте, а сервер используется только
для операций, изменяющих состояние (создание, обновление, удаление задач).

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

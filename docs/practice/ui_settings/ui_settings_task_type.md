# Настройка типов задач, разделов и целей

После того как мы реализовали страницу настроек, нужно добавить функциональность для управления
справочниками: **создание**, **редактирование**, **удаление** и **отображение** типов задач,
разделов, целей и периодических задач. Каждая из этих сущностей работает по одному паттерну, поэтому мы подробно
разберём типы задач, а для разделов и целей отметим только отличия.

## Общий паттерн CRUD-операций

Для каждой сущности реализуется **один класс хэндлеров** (с несколькими `val`-хэндлерами).
Рендерные методы для строк таблиц находятся в `Views` (метод `getTaskTypeRow`),
а редактируемая строка рендерится через `EditItemRequest.render`:

Все операции одной сущности собраны в **один класс** `TaskTypeHandlers`. Каждая операция — это
отдельный публичный `val`-хэндлер, декларативно связанный со своим приватным маршрутом.

Такое разделение — следствие использования htmx: каждое действие требует **конкретного
фрагмента HTML** в ответ. Мы не можем «просто обновить строку» — сервер должен вернуть
готовую разметку, которую htmx вставит в DOM.

## Создание типа задачи

### `TaskTypeHandlers` — класс

Все операции (создание, удаление, редактирование, отображение строк) собраны в один класс
`TaskTypeHandlers`. Общий путь с типизированным параметром `taskTypeIdParam` выделен в `path`,
а пять операций декларативно связаны со своими приватными маршрутами:

```dotty
final case class TaskTypeHandlers(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    settingsService: SettingsService[IO]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  private val path = Path.root / "settings" / "task_types" / taskTypeIdParam
  private val createRoute = Route(
    Request.post(Path.root / "settings" / "task_types")
      .withEntity(Entity.formOf[CreateItemRequest]),
    Response.ok(Entity.html).orNotFound
  )
  private val editRoute = Route(
    Request.put(path).withEntity(Entity.formOf[EditItemRequest]),
    Response.ok(Entity.html).orNotFound
  )
  private val editRowRoute = Route(
    Request.get(path / "edit").extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )
  private val getRowRoute = Route(
    Request.get(path).extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )
  private val deleteRoute = Route(
    Request.delete(path).extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )

  val createHandler: Handler = ...
  val editHandler: Handler = ...
  val editRowHandler: Handler = ...
  val getRowHandler: Handler = ...
  val deleteHandler: Handler = ...
end TaskTypeHandlers
```

`taskTypeIdParam` — это типизированный параметр пути (`Param[TaskTypeId]`), поэтому в хэндлеры
ID записи приходит уже как `TaskTypeId`, а не `Int`, и ручной вызов `TaskTypeId.either` не нужен.

### `createHandler` — создание

```dotty
val createHandler: Handler =
  createRoute.handleIO: (request: CreateItemRequest) =>
    val create: ((UserInfo, JwtToken)) => IO[String] =
      (user, jwtToken) =>
        TaskTypeName.either(request.name) match
          case Right(name) =>
            val newItem = CreateTaskType(name, user.id)
            settingsService
              .createTaskType(newItem)
              .flatMap: _ =>
                getViewSettings(
                  settingsService = settingsService,
                  userId = user.id,
                  jwtToken = jwtToken
                )
              .recoverWith:
                case ex: IllegalArgumentException =>
                  getViewSettings(
                    settingsService = settingsService,
                    userId = user.id,
                    jwtToken = jwtToken,
                    errorTaskTypeMessage = ex.getMessage.some,
                    errorSectionMessage = none,
                    errorGoalMessage = none,
                    errorPeriodicStepMessage = none
                  )
          case Left(error) =>
            getViewSettings(
              settingsService = settingsService,
              userId = user.id,
              jwtToken = jwtToken,
              errorTaskTypeMessage = error.some,
              errorSectionMessage = none,
              errorGoalMessage = none,
              errorPeriodicStepMessage = none
            )

    // ToDo: https://github.com/creativescala/krop/pull/57
    val authorization: Authorization =
      Authorization(Token(AuthScheme.Bearer, request.token))
    withUserFromHeader(authorization)(create)
```

**Пошаговый разбор:**

1. **`createRoute.handleIO: (request: CreateItemRequest) => ...`** — регистрируем обработчик
   на маршрут `createRoute`, принимающий `CreateItemRequest` (содержит `name` и `token`).
   ```dotty
   final case class CreateItemRequest(token: String, name: String) derives FormCodec
   ```
2. **`val create: ((UserInfo, JwtToken)) => IO[String] = ...`** — определяем лямбду,
   которая принимает пару `(user, jwtToken)`.
3. **`TaskTypeName.either(request.name)`** — валидация названия. Возвращает
   `Either[String, TaskTypeName]`:
    - **`Right(name)`** — имя валидно (2–200 символов, без крайних пробелов).
    - **`Left(error)`** — имя невалидно, содержит сообщение об ошибке.
4. **При успешной валидации:**
    - Создаём `CreateTaskType(name, user.id)`.
    - Вызываем `settingsService.createTaskType(newItem)`.
    - При успехе загружаем обновлённую страницу настроек через `getViewSettings`.
    - При ошибке (например, дубликат имени) — перехватываем `IllegalArgumentException`
      и загружаем страницу с сообщением об ошибке.
5. **При ошибке валидации** — сразу загружаем страницу настроек с сообщением
   об ошибке.
6. **`withUserFromHeader(authorization)(create)`** — токен приходит в теле запроса, а не в заголовке.
   Поэтому вручную собираем заголовок `Authorization` из `request.token`:
   ```dotty
   val authorization: Authorization =
     Authorization(Token(AuthScheme.Bearer, request.token))
   ```
   После чего `withUserFromHeader` проверяет его через middleware и передаёт
   `(user, jwtToken)` в лямбду `create`.

   > Примечание: пока это временное решение (см. `// ToDo` — ждём поддержку токена из формы
   > в krop, https://github.com/creativescala/krop/pull/57).

### Маршрут

```dotty
private val createRoute = Route(
  Request.post(Path.root / "settings" / "task_types")
    .withEntity(Entity.formOf[CreateItemRequest]),
  Response.ok(Entity.html).orNotFound
)
```

- POST-запрос на `/settings/task_types`.
- Тело — форма (`application/x-www-form-urlencoded`), десериализуется в `CreateItemRequest`.
- Ответ: 200 OK с HTML (перезагруженная страница настроек) или 404 Not Found при непредусмотренных ошибках.

## Удаление типа задачи

### Хэндлер `deleteHandler`

```dotty
val deleteHandler: Handler =
  deleteRoute.handleIO:
    (taskTypeId: TaskTypeId, authorization: Authorization) =>
      withUserFromHeader(
        authorization
      ): (user: UserInfo, jwtToken: JwtToken) =>
        for
          _    <- settingsService.deleteTaskType(taskTypeId, user.id)
          view <- getViewSettings(
            settingsService = settingsService,
            userId = user.id,
            jwtToken = jwtToken
          )
        yield view
```

**Пошаговый разбор:**

1. **`(taskTypeId: TaskTypeId, authorization: Authorization)`** — обработчик принимает
   типизированный ID записи (из пути через `taskTypeIdParam`) и заголовок `Authorization`.
2. **`withUserFromHeader(authorization)(...)`** — стандартная проверка токена через
   `AuthorizationHandler`. В отличие от создания, здесь токен приходит **в заголовке**:
   `authorization` передаётся первым аргументом, а лямбда `(user, jwtToken)` — вторым.
3. **`settingsService.deleteTaskType(taskTypeId, user.id)`** — удаление записи. Типизированный
   `TaskTypeId` гарантирует валидность ID на уровне типов, поэтому ветки `Right`/`Left` не нужны.
4. **`getViewSettings(...)`** — загрузка обновлённой страницы настроек.

   > Синтаксис `withUserFromHeader(authorization): (user, jwtToken) => ...` — это
   > каррированный вызов: `authorization`/`token` первым аргументом, лямбда,
   > обрабатывающая `(user, jwtToken)`, — вторым. Знак `:` вводит тип/тело лямбды.

### Маршрут

```dotty
private val deleteRoute = Route(
  Request.delete(path).extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

где `path = Path.root / "settings" / "task_types" / taskTypeIdParam`.

- DELETE-запрос на `/settings/task_types/{id}`.
- ID извлекается из пути типизированным параметром `taskTypeIdParam`.
- Токен извлекается из заголовка `Authorization`.

## Отображение строки для редактирования

### Хэндлер `editRowHandler`

```dotty
val editRowHandler: Handler =
  editRowRoute.handleIO:
    (taskTypeId: TaskTypeId, authorization: Authorization) =>
      withUserFromHeader(
        authorization
      ): (user: UserInfo, jwtToken: JwtToken) =>
          getTaskType(taskTypeId, user.id).map: taskType =>
            EditItemRequest.render(taskType, jwtToken, none).toString
```

**Пошаговый разбор:**

1. **`taskTypeId: TaskTypeId`** — ID приходит типизированным из пути через `taskTypeIdParam`.
2. **`getTaskType(taskTypeId, user.id)`** — приватный вспомогательный метод, который
    загружает все типы и находит нужный:
    ```dotty
    private def getTaskType(id: TaskTypeId, userId: UserId): IO[TaskType] =
      for
        taskTypes <- settingsService.getTaskTypes(userId)
        taskType  <- IO.fromOption(
          taskTypes.find(_.id == id)
        )(new IllegalArgumentException("Задача не найдена"))
      yield taskType
    ```
3. **`EditItemRequest.render(...)`** — рендерим scalatags-карточку `EditItemRequest`
    с обычным состоянием (безд ошибки).

**Зачем загружать все типы, а не один?**

В текущей реализации `SettingsService` не имеет метода `getTaskType(id)`.
Вместо этого мы загружаем все типы и ищем нужный в памяти. Это работает, потому
что типов задач у одного пользователя обычно немного (5–20). Если бы их было
тысячи, стоило бы добавить отдельный метод.

### Маршрут

```dotty
private val editRowRoute = Route(
  Request.get(path / "edit").extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

где `path = Path.root / "settings" / "task_types" / taskTypeIdParam`.

- GET-запрос на `/settings/task_types/{id}/edit`.
- Возвращает HTML-строку таблицы в режиме редактирования.

![Settings, edit task type](../images/settings_edit_task_type.png)

## Рендер редактируемой строки

Редактируемая строка рендерится через scalatags-метод `EditItemRequest.render`:

```dotty
object EditItemRequest:
  def render(
      item: TaskType,
      token: JwtToken,
      maybeError: Option[String]
  ): String
```

Метод генерирует `<tr class="task-type-row editing" hx-trigger="cancel" hx-get="{path}">`:

- Первая `<td>`: `<input.edit-input>` с `name="name"`, `autofocus`, `value="{name}"`.
- Вторая `<td>`: `<input.edit-input>` с `name="ordinal"`, `type="number"`, `min="0"`,
  плюс text "Выровнять порядок?" и `<input.edit-checkbox>` с `name="ordinalShift"`.
- Третья `<td>`: `<input.edit-checkbox>` с `name="displayed"`, `type="checkbox"` — `checked` если `displayed`.
- Четвёртая `<td>`:
  - `<input type="hidden" name="token" value="{token.value}"/>` — токен для PUT-запроса,
    генерируется через `Htmx.hiddenToken(token.value)`.
  - Кнопка «❌ Отмена» — `hx-get="{path}"`.
  - Кнопка «💾 Сохранить» — `hx-put="{path}"`, `hx-include="closest tr"`.
  - Ошибка: `maybeError.map(msg => div.err(msg))`.

**Ключевые особенности:**

1. **`class="task-type-row editing"`** — дополнительный класс `editing` используется
    для реализации «одновременного редактирования только одной строки». Функция
    `handleEditClick` ищет `.editing` и предлагает закрыть предыдущую строку.

2. **`hx-trigger="cancel"`** — строка реагирует на событие `cancel`. Когда пользователь
    открывает другую строку, `handleEditClick` отправляет событие `cancel` на текущую,
    что вызывает GET-запрос на `/settings/task_types/{id}` (получить обычную строку).

3. **`autofocus`** на поле `name` — курсор автоматически встаёт в поле редактирования.

4. **`hx-include="closest tr"`** — при отправке формы PUT-запрос включает **все поля
    из ближайшей строки** (`tr`). Это избавляет от необходимости вручную собирать
    данные из полей. Включаются: `name`, `ordinal`, `ordinalShift`, `displayed`, `token`.

5. **`<input type="hidden" name="token">`** — токен передаётся в теле PUT-запроса.
    Генерируется через `Htmx.hiddenToken(token.value)`.

6. **Кнопка «Отмена»** — GET-запрос на `/settings/task_types/{id}`,
    что возвращает обычную строку.

7. **Кнопка «Сохранить»** — PUT-запрос с данными из строки.

8. **Сообщение об ошибке** — если `maybeError` не `None`, отображается красным блоком.

![Settings, edit error](../images/settings_edit_error.png)

## Сохранение изменений

### Хэндлер `editHandler`

Это самый сложный из всех хэндлеров — он поддерживает **переупорядочивание** записей.

```dotty
val editHandler: Handler =
  editRoute.handleIO: (taskTypeId: TaskTypeId, request: EditItemRequest) =>
    val edit: ((UserInfo, JwtToken)) => IO[String] =
      (user, jwtToken) =>
        val rearrange = request.ordinalShift.contains("on")
        (
          TaskTypeName.either(request.name).toValidatedNec,
          Ordinal.either(request.ordinal).toValidatedNec
        ).mapN { case (name, ordinal) =>
          val updated =
            TaskType(
              id = taskTypeId,
              name = name,
              userId = user.id,
              displayed = request.displayed.contains("on"),
              ordinal = ordinal
            )

          val run =
            if rearrange then
              for
                items <- settingsService.getTaskTypes(user.id)
                _     <- settingsService.updateTaskType(updated)
                (beforeUpdated, afterUpdated) =
                  items
                    .filterNot(_.id == updated.id)
                    .partition(_.ordinal < updated.ordinal)
                ids = NonEmptyVector.one(updated.id)
                  .prependVector(
                    beforeUpdated.sortBy(_.ordinal).map(_.id)
                  )
                  .appendVector(afterUpdated.sortBy(_.ordinal).map(_.id))
                _ <- settingsService.reorderTaskTypes(ids, user.id)
              yield ()
            else
              settingsService.updateTaskType(updated).void

          run.map: _ =>
            View.getTaskTypeRow(
              updated
            ).toString
          .recoverWith:
            case ex: IllegalArgumentException =>
              editError(
                id = taskTypeId,
                userId = user.id,
                jwtToken = jwtToken,
                error = ex.getMessage
              )
        }.valueOr(error =>
          editError(
            id = taskTypeId,
            userId = user.id,
            jwtToken = jwtToken,
            error = error.mkString_(";")
          )
        )
    // ToDo: https://github.com/creativescala/krop/pull/57
    val authorization: Authorization =
      Authorization(Token(AuthScheme.Bearer, request.token))
    withUserFromHeader(authorization)(edit)
```

**Пошаговый разбор:**

1. **`(taskTypeId: TaskTypeId, request: EditItemRequest)`** — обработчик принимает
   типизированный ID из пути и тело формы с новыми данными.
   ```dotty
   final case class EditItemRequest(
      token: String,
      name: String,
      ordinal: Int,
      ordinalShift: Option[String],
      displayed: Option[String]
   ) derives FormCodec
   ```

2. **`val rearrange = request.ordinalShift.contains("on")`** — проверяем,
   установлен ли чекбокс «Выровнять порядок?». Если да — после обновления записи
   выполняется переиндексация порядковых номеров остальных записей.

3. **Валидация через `ValidatedNec`:**
    - `TaskTypeName.either(request.name)` — валидация имени.
    - `Ordinal.either(request.ordinal)` — валидация порядкового номера.

   Идентификатор уже типизирован (`TaskTypeId`), поэтому его валидация не требуется.
   `.mapN { ... }` собирает результаты: если **все** валидны — выполняется лямбда;
   если есть ошибки — они аккумулируются в `ValidatedNec` и обрабатываются через
   `.valueOr`.

4. **Создание обновлённого `TaskType`:**
    - `id = taskTypeId` — типизированный ID из пути.
    - `displayed = request.displayed.contains("on")` — чекбокс отмечен → `true`.
    - `ordinal = ordinal` — новое значение порядка.

5. **Два сценария выполнения:**
    - **С переупорядочиванием:**
        - Загружаем все типы пользователя.
        - Обновляем текущий тип.
        - Разделяем остальные типы на две группы: те, что идут **до** нового порядкового номера,
          и те, что **после**.
        - Формируем новый порядок: `[before] + [updated] + [after]`.
        - Вызываем `reorderTaskTypes` для сохранения нового порядка.
    - **Без переупорядочивания:** просто `updateTaskType(updated)`.

6. **Успех:** возвращаем обновлённую строку через `View.getTaskTypeRow`.

 7. **Ошибка бизнес-логики:** перехватываем `IllegalArgumentException` и вызываем
    `editError`, который рендерит строку в режиме редактирования с сообщением.

 8. **`editError`** — приватный вспомогательный метод, который:
     - Загружает оригинальный `TaskType` (по ID) через `getTaskType`.
     - Рендерит `EditItemRequest.render(taskType, jwtToken, error)` с сообщением.

**Зачем нужен `ordinalShift`?**

Представим, что у пользователя есть записи с порядковыми номерами `1, 2, 3, 4`.
Пользователь хочет изменить номер записи `4` на `2`. 
Если просто обновить её, получится `1, 2, 2, 3` — две записи с одинаковым номером. 
В результате придется обновлять порядок на +1 у двух записей.
Чтобы этого избежать, включается переупорядочивание: система пересчитывает номера так, чтобы они
остались уникальными (`1, 2, 3, 4` с новой позицией обновлённой записи).

### Маршрут

```dotty
private val editRoute = Route(
  Request.put(path).withEntity(Entity.formOf[EditItemRequest]),
  Response.ok(Entity.html).orNotFound
)
```

где `path = Path.root / "settings" / "task_types" / taskTypeIdParam`.

- PUT-запрос на `/settings/task_types/{id}`.
- Тело — форма с новыми данными.
- Возвращает обновлённую строку таблицы.

## Отображение обычной строки

### Хэндлер `getRowHandler`

```dotty
val getRowHandler: Handler =
  getRowRoute.handleIO:
    (taskTypeId: TaskTypeId, authorization: Authorization) =>
      withUserFromHeader(
        authorization
      ): (user: UserInfo, jwtToken: JwtToken) =>
        getTaskType(taskTypeId, user.id).map: taskType =>
          View.getTaskTypeRow(
            taskType
          ).toString
```

**Назначение:** возвращает строку таблицы в обычном (нередактируемом) режиме.
Используется когда:

- Пользователь нажал «Отмена» в режиме редактирования.
- Пользователь отменил редактирование через `cancel`-событие.
- После сохранения.

### Маршрут

```dotty
private val getRowRoute = Route(
  Request.get(path).extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

где `path = Path.root / "settings" / "task_types" / taskTypeIdParam`.

## Рендер обычной строки

От обычную строку рендерится scalatags-методом `getTaskTypeRow` в `Views`:

```dotty
def getTaskTypeRow(taskType: TaskType): String
```

Метод генерирует `<tr class="task-type-row" data-task-type-id="{id}">`:

- Первая `<td>`: кнопка «🔄 Обновить» (`hx-get="/settings"`, `hx-target="#app"`,
  `hx-swap="outerHTML"`, `onclick="htmx.process(this); this.click();"`)
  и значение `name` (отображается как plain text).
- Вторая `<td>`: значение `ordinal` (plain text).
- Третья `<td>`: `✓` или `✗` в зависимости от `displayed`.
- Четвёртая `<td>`:
  - Кнопка «✏️ Редактировать» — `hx-get="/settings/task_types/{id}/edit"`,
    `hx-trigger="edit"`, `onclick="handleEditClick(this)"`.
  - Кнопка «🗑️ Удалить» — `hx-delete="/settings/task_types/{id}"`,
    `hx-target="#app"`, `hx-swap="outerHTML"`, `hx-confirm="Вы уверены, что хотите удалить тип задачи «{name}»?"`.

**Особенности:**

- **`data-task-type-id`** — атрибут с ID для отладки.
- **Кнопка «Обновить»** — перезагружает всю страницу настроек.
  Кнопка необходима, потому что после редактирования обновляется только редактируемая строка,
  хотя в остальных строках мог измениться порядок — чтобы это отобразить нужно обновить страницу целиком по кнопке.
- **Кнопка «Редактировать»** — `hx-trigger="edit"` — запрос отправляется **только**
  когда функция `handleEditClick` программно вызовет событие `edit`. Это позволяет
  закрывать ранее открытые строки редактирования.
- **Кнопка «Удалить»** — стандартное подтверждение и удаление.

## Разделы и цели

Хэндлеры и шаблоны для разделов и целей **абсолютно аналогичны**. Отличия только в:

| Аспект              | Типы задач             | Разделы               | Цели               |
| ------------------- | ---------------------- | --------------------- | ------------------ |
| Тип ID              | `TaskTypeId`           | `SectionId`           | `GoalId`           |
| Тип имени           | `TaskTypeName`         | `SectionName`         | `GoalName`         |
| URL-префикс         | `/settings/task_types` | `/settings/sections`  | `/settings/goals`  |
| Обычная строка      | `getTaskTypeRow`       | `getSectionRow`       | `getGoalRow`       |
| Редактируемая строка | `EditItemRequest.render` | `EditItemRequest.render` | `EditItemRequest.render` |
| Сообщение об ошибке | `errorTaskTypeMessage` | `errorSectionMessage` | `errorGoalMessage` |

Такое копирование — осознанное решение. Оно позволяет в будущем **специализировать**
поведение одной из сущностей (например, добавить в цели поле «дедлайн»),
не затрагивая остальные.

## Поток работы с CRUD-операциями

Рассмотрим типичный сценарий редактирования:

1. **Пользователь кликает «✏️ Редактировать»** → JavaScript-функция `handleEditClick`:
    - Проверяет, есть ли уже редактируемая строка (`.editing`).
    - Если есть — предлагает закрыть её.
    - Отправляет событие `edit` на текущей кнопке.
2. **htmx отправляет GET** на `/settings/task_types/{id}/edit`.
3. **Сервер возвращает HTML** с полями ввода. htmx заменяет `<tr>` на редактируемую строку.
4. **Пользователь изменяет поля и кликает «💾 Сохранить»** → htmx отправляет PUT
   на `/settings/task_types/{id}` с данными из строки (`hx-include="closest tr"`).
5. **Сервер валидирует данные и обновляет запись** → возвращает обычную строку
   (или строку с ошибкой, если валидация не прошла).
6. **htmx заменяет `<tr>`** на новую версию.

## Итог

Мы реализовали полный CRUD для четырёх справочников, собрав все операции каждой сущности
в единый класс `TaskTypeHandlers`:

1. **Создание** (`TaskTypeHandlers.createHandler`) — добавляет новую запись и перезагружает
   страницу настроек.
2. **Удаление** (`TaskTypeHandlers.deleteHandler`) — удаляет запись и перезагружает страницу.
3. **Редактирование** (`TaskTypeHandlers.editRowHandler` + `TaskTypeHandlers.editHandler`) —
   открывает форму редактирования прямо в строке таблицы и сохраняет изменения.
4. **Отмена** (`TaskTypeHandlers.getRowHandler`) — возвращает строку в обычный вид.

**Ключевые архитектурные решения:**

- **`ValidatedNec` для валидации** — аккумулирует все ошибки, а не останавливается
  на первой.
- **Опция «Выровнять порядок»** — позволяет переупорядочивать записи, сохраняя
  уникальность порядковых номеров.
- **Единообразие для четырёх справочников** — упрощает поддержку и понимание кода.
- **Единый подход к рендеру** — обычные строки используют scalatags-методы `Views`,
  а редактируемые — `EditItemRequest.render`, что работает для всех сущностей.

После реализации этой функциональности пользователь может полностью управлять
своими справочниками через UI: создавать типы задач, разделы, цели и периодические шаги,
редактировать их названия, порядок и признак отображения, а также удалять ненужные записи.
Всё это — без единой перезагрузки страницы.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

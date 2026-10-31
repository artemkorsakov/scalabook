{%
showPrev = true
prevPage = ui_task_actions.html
showNext = true
nextPage = ../ci_cd.html
%}

# Таймер в задаче

Таймер — это инструмент, который позволяет пользователю фиксировать **фактическое время**,
потраченное на задачу. Это критически важная функция для управления персональным развитием:
она даёт объективные данные о том, сколько времени реально уходит на задачи, помогает
планировать будущие задачи точнее и выявлять, где возникают «пожиратели времени».

В отличие от кнопки «Отложить задачу» или «Завершить задачу», таймер работает **на клиенте**
в реальном времени, а на сервер отправляет только **итоговое значение** при остановке.
Такое решение обусловлено производительностью: если бы мы отправляли запрос на сервер
каждую секунду, то за час работы получилось бы 3600 запросов — это неприемлемо.

## Архитектура таймера

Таймер состоит из трёх компонентов:

1. **Клиентский JavaScript** — обновляет отображение каждую секунду, не обращаясь к серверу.
2. **HTML-разметка** — три кнопки (Старт, Стоп, Сброс) и счётчик.
3. **Серверный хэндлер `AddTimeSpentHandler`** — принимает итоговое время и сохраняет его
   в БД, прибавляя к уже накопленному значению `timeSpent`.

**Ключевое решение:** таймер **не хранит состояние на сервере**. Когда пользователь
запускает таймер, сервер об этом не знает. Только при нажатии «Стоп» клиент отправляет
запрос с количеством прошедших минут. Это упрощает серверную часть и избавляет от
проблем с синхронизацией состояния (например, когда пользователь закрывает вкладку).

## Маршрут

```dotty
val addTimeSpentTask = Route(
  Request.put(Path.root / "task" / uuidParam / "spent")
    .withEntity(Entity.formOf[AddTimeSpentRequest]),
  Response.ok(Entity.html).orNotFound
)
```

**Разбор маршрута:**

- **`Request.put(Path.root / "task" / uuidParam / "spent")`** — PUT-запрос на
  `/task/{uuid}/spent`, где `{uuid}` — идентификатор задачи. Вместо `Param.string`
  используется **`uuidParam`** — это `Param[UUID]`, который сразу декодирует путь
  в `UUID`, поэтому в хэндлере парсинг вручную не нужен.
- **`.withEntity(Entity.formOf[AddTimeSpentRequest])`** — тело запроса в формате
  `application/x-www-form-urlencoded`, десериализуется в `AddTimeSpentRequest`.
- **`Response.ok(Entity.html).orNotFound`** — успешный ответ (200 OK) с HTML-телом
  или 404.

**Почему PUT, а не POST?**

PUT семантически означает «обновить ресурс». Мы обновляем поле `timeSpent`
у существующей задачи. POST обычно используется для создания новых ресурсов,
поэтому PUT — более точный выбор.

## Хэндлер `AddTimeSpentHandler`

```dotty
final case class AddTimeSpentHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    taskLifecycleService: TaskLifecycleService[IO]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  val handler: Handler =
    Routes.addTimeSpentTask.handleIO:
      (uuid: UUID, request: AddTimeSpentRequest) =>
        val run: ((UserInfo, JwtToken)) => IO[Option[String]] =
          (user, jwtToken) =>
            request.timeSpent.refineEither[Positive].toValidatedNec
              .map { timeSpent =>
                val taskId = TaskId(uuid)
                for
                  _ <- taskLifecycleService.addTimeSpent(
                    id = taskId,
                    userId = user.id,
                    timeSpent = timeSpent
                  )
                  maybeView <- getViewTask(
                    taskLifecycleService = taskLifecycleService,
                    userId = user.id,
                    taskId = taskId,
                    jwtToken = jwtToken,
                    errorMessage = none
                  )
                yield maybeView.map(_.toString)
              }.valueOr { errors =>
                Logger[IO].error(errors.mkString_(";")).as(none)
              }
        // ToDo: https://github.com/creativescala/krop/pull/57
        val authorization: Authorization =
          Authorization(Token(AuthScheme.Bearer, request.token))
        handleUserIOOpt(authorization)(run)
end AddTimeSpentHandler
```

**Пошаговый разбор:**

1. **`handleIO: (uuid: UUID, request: AddTimeSpentRequest) => ...`** — обработчик
   принимает UUID задачи из пути (уже декодированный в `UUID` через `uuidParam`)
   и тело запроса.

2. **`val run: ((UserInfo, JwtToken)) => IO[Option[String]] = ...`** — каррированная
   лямбда, которая получает пользователя и токен после проверки.

3. **Валидация через `ValidatedNec`:**
    - `request.timeSpent.refineEither[Positive].toValidatedNec` — валидируем
      количество минут как положительное число.

4. **`.map { timeSpent => ... }`** — если значение валидно, выполняем бизнес-логику:
    - `taskLifecycleService.addTimeSpent(id, userId, timeSpent)` — прибавляет
      `timeSpent` минут к текущему `timeSpent` задачи.
    - `getViewTask(...)` — загружает обновлённую страницу задачи, где поле
      «Затраченное время» уже обновлено.

5. **`.valueOr { errors => ... }`** — если есть ошибки валидации (например,
   отрицательное число минут), логируем их и возвращаем
   `None`. Это приведёт к 404. В реальном приложении можно было бы показать
   страницу с сообщением об ошибке.

6. **`handleUserIOOpt(authorization)(run)`** — токен приходит в теле запроса, а не в заголовке.
   Поэтому вручную собираем заголовок `Authorization` из `request.token`:
   ```dotty
   val authorization: Authorization =
     Authorization(Token(AuthScheme.Bearer, request.token))
   ```
   после чего `handleUserIOOpt` проверяет его через middleware и передаёт
   `(user, jwtToken)` в лямбду `run`.

### Что делает `addTimeSpent`

Метод `taskLifecycleService.addTimeSpent(id, userId, timeSpent)` выполняет
следующую работу:

1. **Проверяет владение** — задача должна принадлежать пользователю.
2. **Загружает текущее состояние** задачи.
3. **Прибавляет** `timeSpent` минут к существующему `timeSpent`.
4. **Сохраняет** обновлённую задачу.

**Важно:** время **прибавляется**, а не заменяется. Это позволяет пользователю
запускать таймер несколько раз за день, и все сессии аккумулируются.

### Модель `AddTimeSpentRequest`

```dotty
final case class AddTimeSpentRequest(
    token: String,
    timeSpent: Int
) derives FormCodec
```

Минимальный DTO: только токен и количество минут.

## HTML-разметка таймера

```html
<div class="task-detail-section full-width">
  <h3>⏱️ Таймер</h3>
  <div class="timer-container">
    <div class="timer-display" id="timerDisplay">00:00:00</div>
    <div class="timer-actions">
      <button
        class="timer-btn start-btn"
        id="startTimerBtn"
        onclick="startTimer('@task.id')"
      >
        ▶️ Старт
      </button>
      <button
        class="timer-btn stop-btn"
        id="stopTimerBtn"
        onclick="stopTimer('@task.id', '@token')"
        style="display: none;"
      >
        ⏹️ Стоп
      </button>
      <button
        class="timer-btn reset-btn"
        id="resetTimerBtn"
        onclick="resetTimer()"
      >
        🔄 Сброс
      </button>
    </div>
  </div>
</div>
```

**Структура:**

1. **`#timerDisplay`** — счётчик в формате `HH:MM:SS`. Начальное значение — `00:00:00`.

2. **Кнопка «Старт»** (`#startTimerBtn`) — запускает таймер через
   `startTimer('@task.id')`. По умолчанию видима.

3. **Кнопка «Стоп»** (`#stopTimerBtn`) — останавливает таймер через
   `stopTimer('@task.id', '@token')`. По умолчанию **скрыта** (`style="display: none;"`).

4. **Кнопка «Сброс»** (`#resetTimerBtn`) — сбрасывает таймер на ноль без
   сохранения. Всегда видима.

## JavaScript-логика

Таймер реализован на JavaScript в файле `pdp.js`. 
Ключевое отличие от простой реализации — **состояние таймера хранится в `localStorage`**, 
что позволяет ему переживать закрытие вкладки, перезагрузку страницы и переключение на другие вкладки браузера.

Состояние хранится в `localStorage` под ключом `pdp_timer_state` в формате:

```json
{ "taskId": "uuid-задачи", "startTime": 1234567890123 }
```

Где `startTime` — время запуска таймера в миллисекундах (получено через `Date.now()`). 
Зная `startTime`, мы в любой момент можем вычислить прошедшее время как `Date.now() - startTime`, 
а не полагаться на инкремент счётчика — это устойчиво к пропущенным тикам `setInterval` при неактивной вкладке.

### Утилиты для работы с состоянием

```javascript
const TIMER_STORAGE_KEY = "pdp_timer_state";

function getTimerState() {
  const raw = localStorage.getItem(TIMER_STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (e) {
    console.error("Failed to parse timer state:", e);
    localStorage.removeItem(TIMER_STORAGE_KEY);
    return null;
  }
}

function saveTimerState(taskId) {
  const state = {
    taskId: taskId,
    startTime: Date.now(),
  };
  localStorage.setItem(TIMER_STORAGE_KEY, JSON.stringify(state));
}

function clearTimerState() {
  localStorage.removeItem(TIMER_STORAGE_KEY);
}

function getElapsedSeconds() {
  const state = getTimerState();
  if (!state) return 0;
  return Math.floor((Date.now() - state.startTime) / 1000);
}
```

**Разбор:**

- **`getTimerState`** — читает состояние из `localStorage` и парсит JSON. 
  Если данные повреждены, ключ удаляется, возвращается `null`. Это защита от порчи данных в `localStorage`.
- **`saveTimerState`** — сохраняет `taskId` и текущее время запуска. Вызывается при клике «Старт».
- **`clearTimerState`** — удаляет ключ из `localStorage`. Вызывается при «Стоп» и «Сброс».
- **`getElapsedSeconds`** — вычисляет прошедшие секунды как разницу между текущим временем и временем старта. 
  Именно эта функция определяет, что показывает счётчик.

### `startTimer(taskId)`

```javascript
function startTimer(taskId) {
  const currentState = getTimerState();
  if (currentState && currentState.taskId === taskId) return;

  if (currentState && currentState.taskId !== taskId) {
    const confirmSwitch = confirm(
      "Таймер уже запущен для другой задачи. Остановить его и запустить новый?",
    );
    if (!confirmSwitch) return;
    clearTimerState();
  }

  saveTimerState(taskId);
  setButtonsRunning(true);
  startTimerInterval();
}
```

**Что происходит:**

1. **Проверка состояния** — если таймер уже запущен **для этой же задачи**, ничего не делаем (защита от двойного клика).
2. **Проверка конфликта** — если таймер запущен **для другой задачи**, показываем `confirm`. 
   Если пользователь отказывается — выходим. Если соглашается — сбрасываем предыдущий таймер (без сохранения).
3. **Сохранение** — `saveTimerState(taskId)` пишет время старта в `localStorage`.
4. **Переключение кнопок** — `setButtonsRunning(true)` скрывает «Старт», показывает «Стоп».
5. **Запуск интервала** — `startTimerInterval()` запускает обновление счётчика.

**Почему `confirm` при переключении задач?** Если пользователь случайно открыл другую задачу и нажал «Старт», 
он может потерять данные о времени, потраченном на предыдущую задачу. `confirm` — простая защита от случайных действий.

### `startTimerInterval()`

```javascript
let timerInterval = null;

function startTimerInterval() {
  if (timerInterval) return;

  timerInterval = setInterval(function () {
    updateTimerDisplay();
  }, 1000);

  updateTimerDisplay();
}
```

**Что происходит:**

1. **Защита от дублирования** — если интервал уже запущен, выходим.
2. **Запуск `setInterval`** — каждую секунду вызываем `updateTimerDisplay`.
3. **Немедленное обновление** — чтобы не ждать первую секунду, сразу показываем актуальное значение.

### `updateTimerDisplay()`

```javascript
function updateTimerDisplay() {
  const display = document.getElementById("timerDisplay");
  if (!display) return;

  const totalSeconds = getElapsedSeconds();
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  display.textContent =
    String(hours).padStart(2, "0") +
    ":" +
    String(minutes).padStart(2, "0") +
    ":" +
    String(seconds).padStart(2, "0");
}
```

**Что происходит:**

1. **Защита от отсутствия элемента** — если `#timerDisplay` нет на странице, выходим.
2. **Вычисление времени** — `getElapsedSeconds()` даёт актуальное значение на основе `startTime` из `localStorage`.
3. **Форматирование** — разбиваем секунды на часы, минуты, секунды с ведущими нулями.
4. **Запись в DOM** — обновляем `textContent` элемента.

**Почему пересчёт, а не инкремент?** Если бы мы делали `timerSeconds++`, 
то при неактивной вкладке `setInterval` мог бы «отставать» или пропускать тики. 
Пересчёт от `startTime` всегда даёт точное значение.

### `stopTimer(taskId, token)`

```javascript
function stopTimer(taskId, token) {
  const state = getTimerState();
  if (!state || state.taskId !== taskId) return;

  const elapsedSeconds = Math.floor((Date.now() - state.startTime) / 1000);
  const minutes = Math.round(elapsedSeconds / 60);

  stopTimerInterval();
  clearTimerState();
  setButtonsRunning(false);

  const display = document.getElementById("timerDisplay");
  if (display) display.textContent = "00:00:00";

  if (minutes > 0) {
    htmx.ajax("PUT", "/task/" + taskId + "/spent", {
      target: "#app",
      swap: "outerHTML",
      values: {
        token: token,
        timeSpent: minutes,
      },
    });
  }
}
```

**Что происходит:**

1. **Проверка состояния** — если таймер запущен для другой задачи, выходим.
2. **Вычисление минут** — `Math.round(elapsedSeconds / 60)`. Округляем до целого: 90 секунд → 2 минуты.
3. **Остановка** — очищаем интервал, удаляем состояние из `localStorage`, переключаем кнопки.
4. **Обнуление отображения** — `00:00:00`.
5. **Отправка на сервер** — если прошло больше нуля минут, отправляем PUT-запрос через `htmx.ajax`. Сервер прибавит время к `task.timeSpent`.
6. **Замена `#app`** — htmx заменяет контейнер на обновлённую страницу задачи, где поле «Затраченное время» уже актуально.

### `resetTimer()`

```javascript
function resetTimer() {
  stopTimerInterval();
  clearTimerState();
  setButtonsRunning(false);

  const display = document.getElementById("timerDisplay");
  if (display) display.textContent = "00:00:00";
}
```

**Что происходит:** сбрасывает таймер **без отправки на сервер**. 
Используется, когда пользователь передумал или нажал «Старт» случайно. 
Состояние из `localStorage` удаляется, счётчик обнуляется, кнопки возвращаются в исходное состояние.

### `restoreTimer(taskId)` — восстановление при загрузке

```javascript
function restoreTimer(taskId) {
  const state = getTimerState();

  if (state && state.taskId === taskId) {
    setButtonsRunning(true);
    startTimerInterval();
  } else {
    setButtonsRunning(false);
    const display = document.getElementById("timerDisplay");
    if (display) display.textContent = "00:00:00";
  }
}
```

**Что происходит:**

1. **Чтение состояния** из `localStorage`.
2. **Если таймер запущен для этой задачи** — показываем кнопку «Стоп», запускаем интервал. 
   Значение счётчика подхватится автоматически, потому что `updateTimerDisplay` пересчитывает от `startTime`.
3. **Если таймер не запущен** (или запущен для другой задачи) — показываем «Старт», счётчик `00:00:00`.

### Автоматический вызов `restoreTimer`

```javascript
document.addEventListener("DOMContentLoaded", function () {
  const container = document.querySelector("[data-task-id]");
  if (container) {
    const taskId = container.getAttribute("data-task-id");
    restoreTimer(taskId);
  }
});

document.addEventListener("htmx:afterSwap", function (event) {
  const container = document.querySelector("[data-task-id]");
  if (container && event.target.id === "app") {
    const taskId = container.getAttribute("data-task-id");
    restoreTimer(taskId);
  }
});
```

**Зачем два обработчика?**

- **`DOMContentLoaded`** — срабатывает при первой загрузке страницы задачи (когда пользователь открыл URL в новой вкладке).
- **`htmx:afterSwap`** — срабатывает после того, как htmx заменил `#app` (например, после завершения задачи или после `stopTimer`, 
  когда сервер вернул обновлённую страницу).

Оба обработчика вызывают `restoreTimer`, который проверяет `localStorage` и восстанавливает состояние. 
Это гарантирует, что таймер правильно отображается **в любой момент**, когда пользователь видит страницу задачи.

### Полный поток работы с сохранением состояния

**Сценарий 1: Пользователь запускает таймер и закрывает вкладку**

1. Клик «Старт» → `localStorage` сохраняет `{ taskId: "abc", startTime: 1234567890 }`.
2. Пользователь закрывает вкладку.
3. Через час пользователь открывает ту же задачу.
4. `DOMContentLoaded` вызывает `restoreTimer("abc")`.
5. JavaScript видит, что таймер запущен, показывает `01:00:00`, кнопка «Стоп» активна.
6. Пользователь кликает «Стоп» → 60 минут отправляются на сервер.

**Сценарий 2: Пользователь переключается на другую вкладку и работает там**

1. Клик «Старт» → `localStorage` = `{ taskId: "abc", startTime: T1 }`.
2. Пользователь переключается в другую вкладку, читает книгу.
3. `setInterval` продолжает работать в фоне, счётчик тикает.
4. Через 30 минут пользователь возвращается — счётчик показывает `00:30:00`.
5. Клик «Стоп» → 30 минут отправляются на сервер.

**Сценарий 3: Пользователь открывает задачу B, таймер запущен для A**

1. Таймер запущен для A, `localStorage` = `{ taskId: "A", startTime: T1 }`.
2. Пользователь открывает задачу B.
3. `restoreTimer("B")` видит, что `taskId` в `localStorage` — `"A"`, а не `"B"`.
4. Показывается кнопка «Старт» со счётчиком `00:00:00`.
5. Пользователь кликает «Старт» → `confirm` предупреждает, что таймер для A будет сброшен.
6. Если соглашается — таймер A **сбрасывается без сохранения**, таймер B запускается.

**Сценарий 4: Пользователь перезагружает страницу**

1. Таймер запущен, `localStorage` = `{ taskId: "abc", startTime: T1 }`.
2. Пользователь нажимает F5.
3. `DOMContentLoaded` → `restoreTimer("abc")` восстанавливает таймер с правильным значением.

### Ограничения

1. **`localStorage` привязан к браузеру.** Если пользователь откроет сайт с другого устройства, таймер там не восстановится.
2. **Одна задача одновременно.** Нельзя запустить таймер для двух задач в разных вкладках — `confirm` предупредит.
3. **`localStorage` может быть очищен** пользователем или браузером (в приватном режиме). Таймер потеряется.
4. **Не сохраняется на сервере.** Если пользователь запустит таймер и не вернётся, время не будет учтено. 
   Это осознанный компромисс: мы не хотим, чтобы сервер хранил «висящие» таймеры.

Таймер с сохранением в `localStorage` решает ключевую задачу: **пользователь может спокойно переключаться на другие вкладки,
читать книгу по задаче или даже закрыть вкладку** — при возвращении таймер продолжит отсчёт с правильного значения. 
Это делает измерение времени естественной частью рабочего процесса, а не хрупкой функцией, требующей постоянного внимания.

## Итог

Таймер — это инструмент для точного измерения фактического времени, потраченного
на задачу. Его реализация — компромисс между точностью и простотой:

**Ключевые архитектурные решения:**

- **Обновление на клиенте** — счётчик тикает каждую секунду в браузере,
  не нагружая сервер.
- **Один запрос при остановке** — на сервер отправляется только итоговое
  количество минут.
- **Прибавление, а не замена** — `taskLifecycleService.addTimeSpent` прибавляет
  время к накопленному, что позволяет запускать таймер несколько раз.
- **Округление до минут** — пользователь вводит целые минуты, что соответствует
  точности измерения.

Для задач управления персональным развитием такой таймер — достаточный
инструмент: он даёт пользователю объективную картину того, сколько времени
уходит на задачи, и помогает планировать будущую нагрузку.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp

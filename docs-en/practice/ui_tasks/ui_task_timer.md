# Timer in a Task

The timer is a tool that allows the user to track the **actual time**
spent on a task. This is critically important for personal development management:
it provides objective data on how much time tasks actually take, helps
plan future tasks more accurately, and identifies where "time eaters" occur.

Unlike the "Postpone Task" or "Complete Task" buttons, the timer works **on the client**
in real time, and sends only the **final value** to the server upon stopping.
This solution is driven by performance: if we sent a request to the server
every second, a one-hour session would produce 3,600 requests — which is unacceptable.

## Timer Architecture

The timer consists of three components:

1. **Client JavaScript** — updates the display every second without contacting the server.
2. **HTML markup** — three buttons (Start, Stop, Reset) and the counter.
3. **Server handler `addTimeSpentHandler`** — accepts the final time and saves it
   to the DB, adding to the accumulated `timeSpent` value.

**Key decision:** the timer **does not store state on the server**. When a user
starts the timer, the server doesn't know about it. Only when "Stop" is pressed does the client send
a request with the number of elapsed minutes. This simplifies the server side and avoids
state synchronization issues (e.g., when the user closes the tab).

## Route

The route is declared privately inside `TaskHandlers` based on the common path `taskPath`
(`Path.root / "task" / uuidParam`):

```dotty
private val addTimeSpentRoute = Route(
  Request.put(taskPath / "spent")
    .withEntity(Entity.formOf[AddTimeSpentRequest]),
  Response.ok(Entity.html).orNotFound
)
```

**Route breakdown:**

- **`Request.put(taskPath / "spent")`** — PUT request to
  `/task/{uuid}/spent`, where `{uuid}` is the task identifier. Uses **`uuidParam`**
  — this is a `Param[UUID]` that decodes the path directly to `UUID`, so manual
  parsing in the handler is not needed.
- **`.withEntity(Entity.formOf[AddTimeSpentRequest])`** — request body in
  `application/x-www-form-urlencoded` format, deserialized into `AddTimeSpentRequest`.
- **`Response.ok(Entity.html).orNotFound`** — successful response (200 OK) with HTML body
  or 404.

**Why PUT, not POST?**

PUT semantically means "update a resource". We are updating the `timeSpent` field
of an existing task. POST is typically used for creating new resources,
so PUT is the more accurate choice.

## `addTimeSpentHandler` Handler (`TaskHandlers` class)

```dotty
val addTimeSpentHandler: Handler =
  addTimeSpentRoute.handleIO:
    (uuid: UUID, request: AddTimeSpentRequest) =>
      val run: ((UserInfo, SignedJwt)) => IO[Option[String]] =
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
              yield maybeView
            }.valueOr { errors =>
              Logger[IO].error(errors.mkString_(";")).as(none)
            }
    // ToDo: https://github.com/creativescala/krop/pull/57
    val authorization: Authorization =
      Authorization(Token(AuthScheme.Bearer, request.token))
    withUserFromHeaderOpt(authorization)(run)
```

**Step-by-step breakdown:**

1. **`handleIO: (uuid: UUID, request: AddTimeSpentRequest) => ...`** — the handler
   accepts the task UUID from the path (already decoded to `UUID` via `uuidParam`)
   and the request body.

2. **`val run: ((UserInfo, SignedJwt)) => IO[Option[String]] = ...`** — a curried
   lambda that receives the user and token after validation.

3. **Validation via `ValidatedNec`:**
    - `request.timeSpent.refineEither[Positive].toValidatedNec` — validates
      the number of minutes as a positive number.

4. **`.map { timeSpent => ... }`** — if the value is valid, we execute the business logic:
    - `taskLifecycleService.addTimeSpent(id, userId, timeSpent)` — adds
      `timeSpent` minutes to the current `timeSpent` of the task.
    - `getViewTask(...)` — loads the updated task page, where the
      "Time Spent" field is already updated.

5. **`.valueOr { errors => ... }`** — if there are validation errors (e.g.,
   a negative number of minutes), we log them and return
   `None`. This leads to 404. In a real application, one could show
   a page with an error message.

### What `addTimeSpent` Does

The method `taskLifecycleService.addTimeSpent(id, userId, timeSpent)` performs
the following work:

1. **Checks ownership** — the task must belong to the user.
2. **Loads the current state** of the task.
3. **Adds** `timeSpent` minutes to the existing `timeSpent`.
4. **Saves** the updated task.

**Important:** the time is **added**, not replaced. This allows the user to
start the timer multiple times a day, and all sessions accumulate.

### `AddTimeSpentRequest` Model

```dotty
final case class AddTimeSpentRequest(
    token: String,
    timeSpent: Int
) derives FormCodec
```

Minimal DTO: only the token and the number of minutes.

## Timer HTML Markup

The timer section is rendered by `Views.task` (the task page). In scalatags,
the task ID and token are injected via string literals
(`s"startTimer('${task.id.value}')"`), and in the resulting HTML — this is:

```dotty
div(cls := "task-detail-section full-width")(
  h3("⏱️ Timer"),
  div(
    cls                  := "timer-container",
    attr("data-task-id") := task.id.value.toString
  )(
    div(cls := "timer-display", `id` := "timerDisplay")("00:00:00"),
    div(cls := "timer-actions")(
      button(
        cls             := "timer-btn start-btn",
        `id`            := "startTimerBtn",
        attr("onclick") := s"startTimer('${task.id.value}')"
      )("▶️ Start"),
      button(
        cls             := "timer-btn stop-btn",
        `id`            := "stopTimerBtn",
        attr("onclick") :=
          s"stopTimer('${task.id.value}', '${token.show}')",
        style := "display: none;"
      )("⏹️ Record"),
      button(
        cls             := "timer-btn reset-btn",
        `id`            := "resetTimerBtn",
        attr("onclick") := "resetTimer()"
      )("🔄 Reset")
    )
  )
)
```

**Structure:**

1. **`#timerDisplay`** — counter in `HH:MM:SS` format. Initial value — `00:00:00`.

2. **"Start" button** (`#startTimerBtn`) — starts the timer via
    `startTimer('{task.id}')`. Visible by default.

3. **"Stop/Record" button** (`#stopTimerBtn`) — stops the timer via
    `stopTimer('{task.id}', '{token}')`. By default **hidden** (`style="display: none;"`).

4. **"Reset" button** (`#resetTimerBtn`) — resets the timer to zero without
    saving. Always visible.

## JavaScript Logic

The timer is implemented in JavaScript in the file `pdp.js`.
The key difference from a simple implementation — the **timer state is stored in `localStorage`**,
which allows it to survive tab closures, page reloads, and switching between browser tabs.

State is stored in `localStorage` under the key `pdp_timer_state` in the format:

```json
{ "taskId": "task-uuid", "startTime": 1234567890123 }
```

Where `startTime` is the timer start time in milliseconds (obtained via `Date.now()`).
Knowing `startTime`, we can compute elapsed time at any moment as `Date.now() - startTime`,
rather than relying on counter increments — this is resilient to missed `setInterval` ticks in inactive tabs.

### State Utilities

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

**Breakdown:**

- **`getTimerState`** — reads state from `localStorage` and parses JSON.
  If data is corrupted, the key is deleted and `null` is returned. This protects against data corruption in `localStorage`.
- **`saveTimerState`** — saves `taskId` and the current start time. Called on "Start" click.
- **`clearTimerState`** — removes the key from `localStorage`. Called on "Stop" and "Reset".
- **`getElapsedSeconds`** — computes elapsed seconds as the difference between
  the current time and the start time from `localStorage`.
  This is the function that determines what the counter displays.

### `startTimer(taskId)`

```javascript
function startTimer(taskId) {
  const currentState = getTimerState();
  if (currentState && currentState.taskId === taskId) return;

  if (currentState) {
    const confirmSwitch = confirm(
      "A timer is already running for another task. Stop it and start a new one?",
    );
    if (!confirmSwitch) return;
    clearTimerState();
  }

  saveTimerState(taskId);
  setButtonsRunning(true);
  startTimerInterval();
}
```

**What happens:**

1. **State check** — if the timer is already running **for this same task**, we do nothing (protection against double-click).
2. **Conflict check** — if the timer is running **for a different task**, we show a `confirm` dialog.
   If the user declines — we exit. If they agree — we reset the previous timer (without saving).
3. **Save** — `saveTimerState(taskId)` writes the start time to `localStorage`.
4. **Switch buttons** — `setButtonsRunning(true)` hides "Start", shows "Stop".
5. **Start interval** — `startTimerInterval()` starts the counter update.

**Why `confirm` when switching tasks?** If the user accidentally opened another task and clicked "Start",
they could lose time data for the previous task. `confirm` is a simple protection against accidental actions.

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

**What happens:**

1. **Duplicate protection** — if an interval is already running, we exit.
2. **Starting `setInterval`** — every second we call `updateTimerDisplay`.
3. **Immediate update** — so we don't wait the first second, we immediately show the
   current value.

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

**What happens:**

1. **Element absence protection** — if `#timerDisplay` is not on the page, we exit.
2. **Time computation** — `getElapsedSeconds()` gives the current value based on `startTime` from `localStorage`.
3. **Formatting** — we break seconds into hours, minutes, and seconds with leading zeros.
4. **Writing to DOM** — we update the element's `textContent`.

**Why recompute instead of increment?** If we did `timerSeconds++`,
then in an inactive tab `setInterval` could "drift" or miss ticks.
Recomputing from `startTime` always gives the correct value.

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

**What happens:**

1. **State check** — if the timer is running for a different task, we exit.
2. **Computing minutes** — `Math.round(elapsedSeconds / 60)`. Round to nearest integer: 90 seconds → 2 minutes.
3. **Stopping** — we clear the interval, remove state from `localStorage`, and switch buttons.
4. **Resetting display** — `00:00:00`.
5. **Sending to server** — if more than zero minutes have passed, we send a PUT request via `htmx.ajax`. The server will add the time to `task.timeSpent`.
6. **Replacing `#app`** — htmx replaces the container with the updated task page, where the "Time Spent" field is already current.

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

**What happens:** resets the timer **without sending to the server**.
Used when the user changes their mind or clicked "Start" by accident.
State is removed from `localStorage`, the counter is reset, and buttons revert to their initial state.

### `restoreTimer(taskId)` — Restoration on Load

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

**What happens:**

1. **Reading state** from `localStorage`.
2. **If timer is running for this task** — we show the "Stop" button and start the interval.
   Counter value will pick up automatically because `updateTimerDisplay` recomputes from `startTime`.
3. **If timer is not running** (or is running for a different task) — we show "Start", counter reads `00:00:00`.

### Automatic `restoreTimer` Invocation

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

**Why two event handlers?**

- **`DOMContentLoaded`** — fires on the first task page load (when the user opens a URL in a new tab).
- **`htmx:afterSwap`** — fires after htmx replaces `#app` (e.g., after completing a task or after `stopTimer`,
  when the server returns an updated page).

Both handlers call `restoreTimer`, which checks `localStorage` and restores the state.
This guarantees the timer is displayed correctly **at any moment** when the user sees the task page.

### Full State-Preserving Workflow

**Scenario 1: User starts the timer and closes the tab**

1. Click "Start" → `localStorage` saves `{ taskId: "abc", startTime: 1234567890 }`.
2. User closes the tab.
3. An hour later, the user opens the same task.
4. `DOMContentLoaded` calls `restoreTimer("abc")`.
5. JavaScript sees the timer is running, shows `01:00:00`, "Stop" button is active.
6. User clicks "Stop" → 60 minutes are sent to the server.

**Scenario 2: User switches to another tab and works there**

1. Click "Start" → `localStorage` = `{ taskId: "abc", startTime: T1 }`.
2. User switches to another tab, reads a book.
3. `setInterval` continues working in the background, the counter ticks.
4. After 30 minutes the user returns — the counter shows `00:30:00`.
5. Click "Stop" → 30 minutes are sent to the server.

**Scenario 3: User opens task B while timer is running for A**

1. Timer is running for A, `localStorage` = `{ taskId: "A", startTime: T1 }`.
2. User opens task B.
3. `restoreTimer("B")` sees that `taskId` in `localStorage` is `"A"`, not `"B"`.
4. "Start" button is shown with counter `00:00:00`.
5. User clicks "Start" → `confirm` warns that timer A will be reset.
6. If they agree — timer A is **reset without saving**, timer B starts.

**Scenario 4: User reloads the page**

1. Timer is running, `localStorage` = `{ taskId: "abc", startTime: T1 }`.
2. User presses F5.
3. `DOMContentLoaded` → `restoreTimer("abc")` restores the timer with the correct value.

### Limitations

1. **`localStorage` is browser-bound.** If the user opens the site from another device, the timer won't restore there.
2. **One task at a time.** You can't start a timer for two tasks in different tabs — `confirm` will warn.
3. **`localStorage` can be cleared** by the user or the browser (in private mode). The timer will be lost.
4. **Not saved on the server.** If the user starts a timer and never returns, the time won't be accounted for.
   This is an intentional compromise: we don't want the server to keep "hanging" timers.

A timer with `localStorage` persistence solves the key task: **the user can comfortably switch between tabs,
read a book on the task, or even close the tab** — upon return, the timer resumes from the correct value.
This makes time measurement a natural part of the workflow, not a fragile feature requiring constant attention.

## Summary

The timer is a tool for accurately measuring the actual time spent
on a task. Its implementation is a compromise between accuracy and simplicity:

**Key architectural decisions:**

- **Client-side updates** — the counter ticks every second in the browser,
  without burdening the server.
- **One request on stop** — only the final
  number of minutes is sent to the server.
- **Add, don't replace** — `taskLifecycleService.addTimeSpent` adds
  time to the accumulated total, allowing the timer to be started multiple times.
- **Rounding to minutes** — the user enters whole minutes, which matches
  the measurement accuracy.

For personal development management tasks, such a timer is a sufficient
tool: it gives the user an objective picture of how much time
tasks take and helps plan future workload.

---

**Links:**

- [Repository with source code][repo]

[repo]: https://gitverse.ru/scalabook/pdp

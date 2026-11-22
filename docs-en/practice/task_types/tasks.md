# Task

Each task has a set of fields
that ensure uniformity, structure, and convenient progress tracking.

## Field List

| Field         | Data Type           | Description                                                                         |
|---------------|---------------------|-------------------------------------------------------------------------------------|
| `id`          | `TaskId` (`UUID`)   | Unique task identifier                                                              |
| `name`        | `TaskName`          | Task name                                                                           |
| `userId`      | `UserId`            | Identifier of the user who owns the task                                            |
| `taskTypeId`  | `TaskTypeId`        | Identifier of the task type                                                         |
| `status`      | `Status`            | Current task status                                                                 |
| `sectionId`   | `SectionId`         | Identifier of the section the task belongs to                                       |
| `goalId`      | `GoalId`            | Identifier of the goal the task is fulfilling                                       |
| `ordinal`     | `Ordinal`           | Ordinal number of the task. Used to determine execution order                       |
| `startDate`   | `Option[LocalDate]` | Date when the task was taken into work                                              |
| `endDate`     | `Option[LocalDate]` | Planned completion date                                                             |
| `information` | `String`            | Additional information about the task (e.g., description, notes, sources)           |
| `plan`        | `Plan`              | Task execution plan — a list of steps or actions required for completion            |
| `duration`    | `FiniteDuration`    | Task effort estimate                                                                |
| `timeSpent`   | `FiniteDuration`    | Time spent                                                                          |

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/Task.scala
final case class Task(
    id: TaskId,
    name: TaskName,
    userId: UserId,
    taskTypeId: TaskTypeId,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: Ordinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: Plan,
    duration: FiniteDuration,
    timeSpent: FiniteDuration
)

object Task:
  type TaskId = TaskId.T
  object TaskId extends PdpUUID

  type TaskName = TaskName.T
  object TaskName
      extends RefinedType[String, MinLength[2] & MaxLength[200] & Trimmed]
```

To describe plan steps, we create separate files `Plan.scala` and `PlanStep.scala`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/Plan.scala
final case class Plan(steps: List[PlanStep]):
  lazy val progress: Double =
    val completed = steps.count(_.completed).toDouble
    completed / steps.length

object Plan:
  val Empty: Plan = Plan(List.empty)
  
// pdp/src/main/scala/ru/scalabook/pdp/models/PlanStep.scala
final case class PlanStep(
    number: StepNumber,
    step: StepName,
    completed: Boolean
) derives Decoder, Encoder

object PlanStep:
  /** Plan step number in the format: n.n.n */
  type StepNumber = StepNumber.T

  val ZeroStepNumber: StepNumber  = StepNumber("0")
  val FirstStepNumber: StepNumber = StepNumber("1")

  object StepNumber
      extends RefinedType[String, Match["^\\d+(?:\\.\\d+)*$"]]:
    given Ordering[StepNumber] =
      @tailrec
      def loop(list1: List[Int], list2: List[Int]): Boolean =
        (list1, list2) match
          case (h1 :: t1, h2 :: t2) if h1 == h2 => loop(t1, t2)
          case (h1 :: t1, h2 :: t2)             => h1 < h2
          case _                                =>
            list1.isEmpty && list2.nonEmpty

      Ordering.fromLessThan { case (step1, step2) =>
        loop(
          list1 = step1.toPoints,
          list2 = step2.toPoints
        )
      }

  extension (stepNumber: StepNumber)
    private def toPoints: List[Int] =
      stepNumber.value.split("\\.").flatMap(_.toIntOption).toList

    def level: Int = stepNumber.value.count(_ == '.')

  type StepName = StepName.T
  object StepName
      extends RefinedType[String, MinLength[2] & Trimmed]
```

## Review of Each Field

### `id: TaskId`

- **Description**: Unique task identifier.
- **Constraints**: We create a separate [opaque type](https://docs.scala-lang.org/ru/scala3/book/types-opaque-types.html)
  for type safety, to avoid accidentally confusing identifiers of different objects
  (e.g., task ID and user ID).
- **Example**: `b7a98f3e-5f4d-4c6f-916a-51e05f3c0001`

### `name: TaskName`

- **Description**: Task name.
- **Constraints**:
    - 2 to 200 characters, without leading or trailing spaces.
- **Example**: `"Study the 'Clean Code' book"`

### `userId: UserId`

- **Description**: Task owner identifier.
- **Why needed**: For implementing a multi-user model.

### `taskTypeId: TaskTypeId`

- **Description**: Task type identifier.
- **Example**: `TaskTypeId(1)`

### `status: Status`

- **Description**: Current task status.
- **Possible values**:
    - `Status.InProgress` – task in progress
    - `Status.Planned` – task planned
    - `Status.KnowledgeBase` – task in knowledge base
    - `Status.Completed` – task completed

### `sectionId: SectionId`

- **Description**: Identifier of the section the task belongs to.
- **Example**: `SectionId(1)`

### `goalId: GoalId`

- **Description**: Identifier of the goal the task is fulfilling.
- **Example**: `GoalId(1)`

### `ordinal: Ordinal`

- **Description**: Ordinal number of the task — its priority.
- **Constraints**: Must be a positive number (`Positive`).
- **Example**: `1`, `2`, `3`, ...

### `startDate: Option[LocalDate]`

- **Description**: Start date of work on the task.
- **Example**: `Some(2025-01-10)`

### `endDate: Option[LocalDate]`

- **Description**: Planned end date of work on the task.
- **Example**: `Some(2025-01-20)`

### `information: String`

- **Description**: Additional information about the task.
- **Example**: `"This textbook is a classic, time-tested work..."`
- **Why needed**: Stores description, notes, and any other information that may be useful when performing the task.

### `plan: Plan`

- **Description**: Task execution plan — a list of steps or actions required to complete it.
- **Structure**: `Plan(steps: List[PlanStep])`, where each `PlanStep` contains a step number (e.g., `1.2.3`), step description, and a completion flag.
- **Example**: 
  ```dotty
  Plan(List(PlanStep("1", "Read the first chapter", completed = true), 
              PlanStep("2", "Take notes", completed = false)))
  ```
- **Why needed**: Allows breaking the task into specific steps, tracking progress (via `plan.progress`),
  and using it as a checklist. Having a plan is a mandatory condition for taking a task into work or enqueueing it.

### `duration: FiniteDuration`

- **Description**: Task effort estimate — how much time is planned to be spent on it.
- **Example**: `FiniteDuration(120, TimeUnit.MINUTES)`

### `timeSpent: FiniteDuration`

- **Description**: Time spent — how much time was actually spent on the task.
- **Example**: `FiniteDuration(95, TimeUnit.MINUTES)`

## Testing Code

One of the most important parts of software development is testing code.
Well-written tests give confidence that the code works as intended,
help catch regressions during refactoring, and serve as living documentation for the project.
Let's examine how to write tests in Scala 3 using the
**Weaver** library.

### What Is Weaver?

[Weaver](https://typelevel.org/weaver-test/) is a lightweight testing framework
designed specifically for projects using **Cats Effect**. Its key advantages:

- **Simple and concise API** — tests look almost like regular Scala code.
- **Full support for effects** — `IO`, `Resource`, and other Cats Effect types "out of the box".
- **Parallel test execution** — tests run in parallel by default, speeding up builds.
- **Clear error messages** — when a test fails, it immediately shows what went wrong.

### Example: Sorting Plan Steps

Consider a real example from our project — testing plan step sorting.
Each step has a number like `1`, `1.1`, `1.2.1`, `2.1`, etc. These numbers need to be sorted
naturally: `1.2` should come before `1.10`, even though as strings they sort the opposite way.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/models/PlanStepSuite.scala
object PlanStepSuite extends SimpleIOSuite:
  pureTest("StepNumber ordering should sort correctly") {
    val unsorted = List(
      StepNumber("1.10"),
      StepNumber("1.2"),
      StepNumber("1"),
      StepNumber("1.1"),
      StepNumber("2.1"),
      StepNumber("2"),
      StepNumber("1.2.1"),
      StepNumber("1.1.1")
    )

    val expected = List(
      StepNumber("1"),
      StepNumber("1.1"),
      StepNumber("1.1.1"),
      StepNumber("1.2"),
      StepNumber("1.2.1"),
      StepNumber("1.10"),
      StepNumber("2"),
      StepNumber("2.1")
    )

    expect.same(expected, unsorted.sorted)
  }
```

### Testing Conventions

To keep tests useful and readable, follow these rules:

1. **One test — one check.** A test should answer one question.
   If there are multiple checks, it is better to split them into separate tests.

2. **Clear names.** A test name should describe behavior, not implementation.
   Bad: `test("parse works")`. Good: `test("parse should reject empty string")`.

3. **Given-When-Then structure.** Split the test into three parts:
   - **Given** — data preparation (e.g., an unsorted list).
   - **When** — the action (e.g., `.sorted`).
   - **Then** — the check (e.g., `expect.same`).

4. **Isolation.** Each test should work independently of others.
   Do not rely on test execution order.

5. **Test edge cases.** Besides the "happy path", check empty values,
   range boundaries, and invalid input.

6. **Tests as documentation.** Well-written tests show how to use the code.
   If you need to understand how a function works — look at its tests.

Tests are not "extra work"; they are an **integral part of the code**.
Write them alongside the implementation, not after. This helps you find errors faster
and makes refactoring safe.

## Calculating the Next Plan Step Number

One of the key operations when working with a plan is computing the next step number.
This is needed when the user adds a new item to the plan via the markdown editor
or through the API. The next step number depends on the **nesting level**: if the current
step is at the top level, the next one gets a number like `N+1`; if inside
some item — the number `N.M+1`; if inside a sub-item — `N.M.K+1`, and so on.

### Numbering Logic

Plan step numbers look like a sequence of numbers separated by dots:
`1`, `1.1`, `1.1.1`, `1.2`, `2`, `2.1`, etc. Each number denotes the position
of the step at its nesting level:

- **`1`** — first step at the top level.
- **`1.1`** — first sub-step of the first step.
- **`1.1.1`** — first sub-item of the first sub-step of the first step.
- **`1.2`** — second sub-step of the first step.

When adding a new step after an existing one, we must **increment the
last counter** at the specified level. For example, if the current step is `3.2.4`, then:

- Next step at the **top level** (level 0) — `4`.
- Next step at the **first level** (level 1) — `3.3`.
- Next step at the **second level** (level 2) — `3.2.5`.
- Next step at the **third level** (level 3) — `3.2.4.1`.

### Implementation

```dotty
object PlanStep:
  extension (stepNumber: StepNumber)
    def next(level: Int): StepNumber = {
      val nextLevel    = math.max(0, level) + 1
      val next: String =
        toPoints.take(nextLevel).toVector match
          // Degenerate case — this should not happen
          case points if points.isEmpty =>
            "0"
          case points if points.length < nextLevel =>
            s"${points.mkString(".")}.1"
          case points if points.length == 1 =>
            (points.last + 1).toString
          case points =>
            s"${points.init.mkString(".")}.${points.last + 1}"

      StepNumber.option(next).getOrElse(ZeroStepNumber)
    }
  end extension
```

### Algorithm Review

**1. Level normalization:**

```dotty
val nextLevel = math.max(0, level) + 1
```

- **`math.max(0, level)`** — protection against negative values. If the user
  passes `-1`, the level is normalized to `0`.
- **`+ 1`** — transition from zero-based index to the number of dots. Level `0`
  corresponds to one number in the step number (`1`), level `1` — two numbers (`1.1`),
  and so on.

**2. Extracting step number points:**

```dotty
toPoints.take(nextLevel).toVector
```

- **`toPoints`** — an extension that splits `"3.2.4"` into `List(3, 2, 4)`.
- **`.take(nextLevel)`** — takes only the first `nextLevel` numbers. This is the key
  step: if the current step is `3.2.4` and we want the next step at level 1,
  we strip everything extra and get `List(3, 2)`. This guarantees that the step
  number at the next level corresponds to the specified level, regardless
  of the depth of the current step number.

**3. Computing the next number:**

Four cases:

- **Empty list (`points.isEmpty`).** Return `"0"` — a degenerate case that
  should not occur in normal operation. For example, if `StepNumber("")`
  was somehow created (although validation shouldn't allow this).

- **List shorter than `nextLevel` (`points.length < nextLevel`).** This means
  the current step is **shallower** than the requested level. For example, the current step is `9`,
  and the level is 2 — we want the next step two nesting levels deep. The logical answer is
  `9.1`: the first sub-step under the current one, not `9.1.1`. The formula `s"${points.mkString(".")}.1"`
  appends `.1` to the existing step number.

- **One number in the list (`points.length == 1`).** The current step is at the top level.
  The next one at the same level is simply an increment: `3` → `4`.

- **Multiple numbers (`points`).** The general case. We increment the last number
  and append it to the beginning: `List(3, 2)` → `3.3`; `List(3, 2, 4)` → `3.2.5`.

**4. Safe `StepNumber` creation:**

```dotty
StepNumber.option(next).getOrElse(ZeroStepNumber)
```

- **`StepNumber.option`** — validates the string via `RefinedType` and returns
  `Option[StepNumber]`.
- **`.getOrElse(ZeroStepNumber)`** — if for some reason the string failed
  validation, return the zero step. This is a crash prevention: better to return `"0"`
  than to throw an exception.

### Examples

Consider how `next` works for step `3.2.4` at different levels:

| Level | `nextLevel` | `take(nextLevel)` | Result  | Explanation                                                    |
| ----- | ----------- | ----------------- | ------- | -------------------------------------------------------------- |
| `-1`  | `1`         | `[3]`             | `4`     | Negative level normalized to 0, next is `4`                    |
| `0`   | `1`         | `[3]`             | `4`     | Top level, increment the first number                          |
| `1`   | `2`         | `[3, 2]`          | `3.3`   | Second level, increment the second number                      |
| `2`   | `3`         | `[3, 2, 4]`       | `3.2.5` | Third level, increment the third number                        |
| `3`   | `4`         | `[3, 2, 4]`       | `3.2.4.1` | Fourth level, add a new number                            |
| `4`   | `5`         | `[3, 2, 4]`       | `3.2.4.1` | Same case — only one level added at a time                   |

### Tests

```dotty
pureTest("StepNumber.next should return next number with the given level"):
  val stepNumber = StepNumber("3.2.4")

  expect.same(StepNumber("4"), stepNumber.next(-1)) &&
  expect.same(StepNumber("4"), stepNumber.next(0)) &&
  expect.same(StepNumber("3.3"), stepNumber.next(1)) &&
  expect.same(StepNumber("3.2.5"), stepNumber.next(2)) &&
  expect.same(StepNumber("3.2.4.1"), stepNumber.next(3)) &&
  expect.same(StepNumber("3.2.4.1"), stepNumber.next(4)) &&
  expect.same(StepNumber("1"), ZeroStepNumber.next(0)) &&
  expect.same(StepNumber("2"), StepNumber("1").next(-1)) &&
  expect.same(StepNumber("2"), StepNumber("1").next(0)) &&
  expect.same(StepNumber("1.1"), StepNumber("1").next(1))
```

**What the tests cover:**

- **Negative level** (`next(-1)`) — protection against invalid input.
- **Level 0** — increment the first number.
- **Level 1** — increment the second number when the current step is longer.
- **Level 2** — increment the third number.
- **Level 3** — going beyond the step number length, adding a new sub-step.
- **Level 4** — same as level 3 (cannot jump two levels deep at once).
- **`ZeroStepNumber`** — degenerate case, the zero step.
- **Simple numbers** (`"1"`) — correct operation with single-level numbers.

---

**References:**

- [Source repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp

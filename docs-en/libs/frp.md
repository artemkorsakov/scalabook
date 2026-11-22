# Functional Reactive Programming

As part of implementing the [Individual Development Plan](../practice/index.md), I encountered the following problem:

I have a list of tasks and want to filter this list: by task type and by section.
When a filter is selected, tasks are filtered accordingly (apologies for the tautology).
I select the "Books" filter, the "Scala" section, and get the result — looks great, but there is a catch:
when I filter by books, I want the section filter
to show only sections that are present in the filtered tasks.

For example, suppose there is a book about Scala and an article about Functional Programming.
Initially, the type filter has three options: all, books, and articles.
The section filter also has three options: all, Scala, and functional programming.
There are two tasks in the list.
So, when filtering tasks by the "Books" type, I want to see not only one task in the list,
but also reduce the section filter to two options only: all and Scala.
Why do I need a "Functional Programming" section filter if it will yield an empty task list?!

How to do this in Scala?

In 1997, at the [Second International Conference on Functional Programming (ICFP '97)](https://dl.acm.org/doi/proceedings/10.1145/258948),
Conal Elliott and Paul Hudak presented the paper ["Functional Reactive Animation"][fran],
which gave rise to an entire paradigm: [Functional Reactive Programming (FRP)][wiki].

Here is how the authors describe it:

> Fran is a collection of types and functions intended for creating rich interactive multimedia animations.  
> At the foundation of Fran are the concepts of "behaviors" and "events".  
> Behaviors represent reactive values that change over time,  
> while events are collections of conditions of arbitrary complexity that can carry diverse information.

Applied to my problem: events are the selected filter values (by task type and by section).
Behaviors are the filtered task list and the filter values for type and section.

Here is the animation: the user sets filter values, and gets back a new list of filter options and a list of tasks.

## How to Implement This?

In Scala, there are several libraries for functional reactive programming:

- [Reactify][reactify]: Marketed as "the one and only true FRP framework for Scala"
- [Scala.rx][scala.rx]: A popular library for reactive variables. It enables creating "smart" variables (Var)
  and computed values (Rx) that automatically recalculate when dependencies change.
- [Monix][monix]: A modern library for asynchronous and reactive programming.
- [SynapseGrid][SynapseGrid]: An original approach to FRP based on Petri nets. 
  Enables building dynamic data processing systems using a DSL for composing multi-input, multi-output functions.

### Preparation

Create the domain models needed for the task:

```dotty
enum TaskType:
  case Book, Article, Video
```

```dotty
enum Section:
  case Scala, FP, Algorithms
```

```dotty
case class Task(name: String, taskType: TaskType, section: Section)
```

Initial data for the task:

```dotty
val tasks: List[Task] = List(
  Task("Scala basics: syntax and types", Book, Scala),
  Task("Functional programming on Scala", Book, FP),
  Task("Sorting algorithms: theory and practice", Book, Algorithms),
  Task("Article: type classes in Scala 3", Article, Scala),
  Task("Article: monads in simple words", Article, FP),
  Task("Article: dynamic programming", Article, Algorithms),
  Task("Video: pattern matching in Scala", Video, Scala),
  Task("Video: introduction to Category Theory", Video, FP),
  Task("Video: graph algorithms (BFS/DFS)", Video, Algorithms),
  Task("Practice: solve 20 LeetCode problems", Article, Algorithms)
)
```

### Reactify

Now let us try Reactify:

```dotty
// Create "events"
val filterByType    = Var[Option[TaskType]](None)
val filterBySection = Var[Option[Section]](None)

// Behaviors are built from events:
val filteredTasks = Val[List[Task]] {
  tasks
    .filter(task =>
      filterByType.forall(_ == task.taskType) &&
        filterBySection.forall(_ == task.section)
    )
}
val types    = Val[Set[TaskType]](filteredTasks().map(_.taskType).toSet)
val sections = Val[Set[Section]](filteredTasks().map(_.section).toSet)
```

We have events — our filters — and behaviors: filtered tasks and a list of values for the filter.

```dotty
println(s"Tasks = $filteredTasks")
// All 10 tasks
println(s"Types = $types")
// Types = Var(Success(Set(Book, Article, Video)))
println(s"Sections = $sections")
// Sections = Var(Success(Set(Scala, FP, Algorithms)))

// The event changed, causing the behavior to change
filterByType := Some(Book)

println(s"Tasks = $filteredTasks")
// Tasks = Var(Success(List(
//   Task(Scala basics: syntax and types, Book, Scala), 
//   Task(Functional programming on Scala, Book, FP), 
//   Task(Sorting algorithms: theory and practice, Book, Algorithms)
// )))
println(s"Types = $types")
// Types = Var(Success(Set(Book)))
println(s"Sections = $sections")
// Sections = Var(Success(Set(Scala, FP, Algorithms)))

// Another event changed
filterBySection := Some(Algorithms)

println(s"Tasks = $filteredTasks")
// Tasks = Var(Success(List(Task(Sorting algorithms: theory and practice, Book, Algorithms))))
println(s"Types = $types")
// Types = Var(Success(Set(Book)))
println(s"Sections = $sections")
// Sections = Var(Success(Set(Algorithms)))
```

As seen above, behaviors change when events occur.
`filteredTasks`, `types`, `sections` have essentially become a computation description
that needs to be executed when certain events happen.
This reduces cognitive load on developers by allowing behavior to be described once.

---

**References:**

- [Functional reactive programming: Wikipedia][wiki]
- [Elliott - Functional images](https://conal.net/papers/functional-images/)
- [Elliott - Push-Pull Functional Reactive Programming](https://conal.net/papers/push-pull-frp/)
- [Elliott - Simply efficient functional reactivity](https://conal.net/papers/simply-reactive/)
- [Elliott, Hudak - Functional Reactive Animation][fran]
- [Hudak, Courtney, Nilsson, Peterson - Arrows, Robots, and Functional Reactive Programming](https://www.cs.yale.edu/homes/hudak/CS429F04/AFPLectureNotes.pdf)
- [Wan, Hudak - Functional reactive programming from first principles](https://dl.acm.org/doi/10.1145/349299.349331)


[fran]: https://conal.net/papers/icfp97/
[reactify]: https://github.com/outr/reactify
[scala.rx]: https://github.com/lihaoyi/scala.rx
[monix]: https://github.com/monix/monix
[SynapseGrid]: https://github.com/Primetalk/SynapseGrid
[wiki]: https://en.wikipedia.org/wiki/Functional_reactive_programming

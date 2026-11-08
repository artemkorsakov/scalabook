# Функциональное реактивное программирование

В рамках реализации ["Индивидуального плана развития"](../practice/index.md) я столкнулся со следующей проблемой:

У меня есть список задач и я хочу сделать фильтрацию по этому списку: по типу задачи, по разделу.
При выборе фильтра происходит фильтрация задач (извините за тавтологию). 
Выбрал фильтр "Книги", раздел "Scala" и получил результат - выглядит шикарно, но есть одно "но": 
когда я фильтрую по книгам, то хочу, чтобы в фильтре по разделам
остались только те разделы, которые присутствуют в отфильтрованных задачах.

Например, есть книга по Scala и статья по Функциональному программированию. 
Изначально в фильтре по типам есть три пункта: все, книги, статьи. 
В фильтре по разделам тоже три пункта: все, Scala, функциональное программирование.
В списке задач две задачки.
Так вот, фильтруя по типу задач "Книги", я хочу не только увидеть одну задачку в списке, 
но и в фильтре по разделам оставить только два пункта: все, Scala.
Зачем мне фильтр по разделу "Функциональное программирование", если он приведет к пустому списку задач?!

Как это сделать на Scala?

В далеком 97-м году на [Второй международной конференции по функциональному программированию (ICFP ’97)](https://dl.acm.org/doi/proceedings/10.1145/258948)
Конан Эллиот (Conal Elliott) и Пол Худак (Paul Hudak) представили статью ["Functional Reactive Animation"][fran],
которая породила целую парадигму [Functional Reactive Programming (FRP)][wiki].

Вот как авторы описывают работу:

> Fran — это набор типов данных и функций, предназначенных для создания насыщенных интерактивных мультимедийных анимаций. 
> В основе Fran лежат понятия «поведений» (behaviors) и «событий» (events). 
> Поведения представляют собой реактивные значения, изменяющиеся во времени, 
> а события — это совокупности условий произвольной сложности, которые могут нести в себе разнообразную информацию. 

Применительно к моей проблеме, события - это выбранные значения фильтра по типу задачи и по разделу.
А поведения: это список отфильтрованных задач и значения фильтров по типу задачи и фильтру по разделу.

Вот такая вот анимация: пользователь задает значения фильтров, а ему в ответ новый список фильтров и список задач.

## Как это реализовать? 

В Scala существует несколько библиотек для функционального реактивного программирования:

- [Reactify][reactify]: Позиционируется как «первый и единственный истинный FRP-фреймворк для Scala»
- [Scala.rx][scala.rx]: Популярная библиотека для реактивных переменных. Она позволяет создавать «умные» переменные (Var) 
  и вычисляемые значения (Rx), которые автоматически пересчитываются при изменении зависимостей.
- [Monix][monix]: Современная библиотека для асинхронного и реактивного программирования.
- [SynapseGrid][SynapseGrid]: Оригинальный подход к FRP, основанный на сетях Петри. 
  Позволяет строить динамические системы обработки данных с помощью DSL для композиции функций с несколькими входами и выходами

### Подготовка

Создаем необходимые для задачи модели:

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

Исходные данные для выполнения задачи:

```dotty
val tasks: List[Task] = List(
  Task("Основы Scala: синтаксис и типы", Book, Scala),
  Task("Функциональное программирование на Scala", Book, FP),
  Task("Алгоритмы сортировки: теория и практика", Book, Algorithms),
  Task("Статья: type classes в Scala 3", Article, Scala),
  Task("Статья: монады простыми словами", Article, FP),
  Task("Статья: динамическое программирование", Article, Algorithms),
  Task("Видео: pattern matching в Scala", Video, Scala),
  Task("Видео: введение в Category Theory", Video, FP),
  Task("Видео: графовые алгоритмы (BFS/DFS)", Video, Algorithms),
  Task("Практика: решить 20 задач на LeetCode", Article, Algorithms)
)
```

### Reactify

Теперь можно попробовать Reactify:

```dotty
// Создаем "события"
val filterByType    = Var[Option[TaskType]](None)
val filterBySection = Var[Option[Section]](None)

// На основе событий формируется поведение:
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

У нас есть события - наши фильтры и некоторое поведение: отфильтрованные задачи и список значений для фильтра.

```dotty
println(s"Tasks = $filteredTasks")
// Все 10 задач
println(s"Types = $types")
// Types = Var(Success(Set(Book, Article, Video)))
println(s"Sections = $sections")
// Sections = Var(Success(Set(Scala, FP, Algorithms)))

// Событие изменилось, что привело к изменению поведения
filterByType := Some(Book)

println(s"Tasks = $filteredTasks")
// Tasks = Var(Success(List(
//   Task(Основы Scala: синтаксис и типы,Book,Scala), 
//   Task(Функциональное программирование на Scala,Book,FP), 
//   Task(Алгоритмы сортировки: теория и практика,Book,Algorithms)
// )))
println(s"Types = $types")
// Types = Var(Success(Set(Book)))
println(s"Sections = $sections")
// Sections = Var(Success(Set(Scala, FP, Algorithms)))

// Ещё одно событие изменилось
filterBySection := Some(Algorithms)

println(s"Tasks = $filteredTasks")
// Tasks = Var(Success(List(Task(Алгоритмы сортировки: теория и практика,Book,Algorithms))))
println(s"Types = $types")
// Types = Var(Success(Set(Book)))
println(s"Sections = $sections")
// Sections = Var(Success(Set(Algorithms)))
```

Как видно выше поведение меняется при наступлении событий.
`filteredTasks`, `types`, `sections` по сути стали описанием вычисления, 
которое нужно выполнить при наступлении определенных событий.
Это позволяет снизить когнитивную нагрузку на разработчиков, позволяя оформить поведение один раз.

---

**Ссылки:**

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

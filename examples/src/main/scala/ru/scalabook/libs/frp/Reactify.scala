package ru.scalabook.libs.frp

import TaskType.*
import Section.*
import reactify.*

object Reactify:
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

  val filterByType    = Var[Option[TaskType]](None)
  val filterBySection = Var[Option[Section]](None)

  val filteredTasks = Val[List[Task]] {
    tasks
      .filter(task =>
        filterByType.forall(_ == task.taskType) &&
          filterBySection.forall(_ == task.section)
      )
  }
  val types    = Val[Set[TaskType]](filteredTasks().map(_.taskType).toSet)
  val sections = Val[Set[Section]](filteredTasks().map(_.section).toSet)

end Reactify

@main def runReactify(): Unit =
  import Reactify.*

  println(s"Tasks = $filteredTasks")
  println(s"Types = $types")
  println(s"Sections = $sections")

  filterByType := Some(Book)

  println(s"Tasks = $filteredTasks")
  println(s"Types = $types")
  println(s"Sections = $sections")

  filterByType    := None
  filterBySection := Some(Algorithms)

  println(s"Tasks = $filteredTasks")
  println(s"Types = $types")
  println(s"Sections = $sections")

  filterByType    := Some(Book)
  filterBySection := Some(Algorithms)

  println(s"Tasks = $filteredTasks")
  println(s"Types = $types")
  println(s"Sections = $sections")
end runReactify

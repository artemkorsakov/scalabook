{%
showPrev = true
prevPage = reduction.html
showNext = true
nextPage = currying.html
%}

# Функции высшего порядка

## Определение и основные характеристики

[Функция высшего порядка (Higher-Order Function, HOF)](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%B8_%D0%BF%D0%B5%D1%80%D0%B2%D0%BE%D0%B3%D0%BE_%D0%BA%D0%BB%D0%B0%D1%81%D1%81%D0%B0) — 
это фундаментальное понятие в функциональном программировании, описывающее функции, которые:

1. **Принимают другие функции в качестве параметров** — могут получать одну или несколько функций для дальнейшей обработки
2. **Возвращают функции как результат** — могут генерировать и возвращать новые функции

Эти свойства делают HOF мощным инструментом для:

- Абстрагирования общих шаблонов обработки
- Создания гибких и переиспользуемых компонентов
- Реализации отложенных вычислений
- Построения композиций операций

## Практический пример: универсальный форматировщик результатов

Рассмотрим реализацию функции `formatResult`, которая демонстрирует работу с HOF:

```dotty
// Вычисление абсолютного значения числа
def abs(n: Int): Int =
  if n < 0 then -n
  else n

// Вычисление факториала (хвостовая рекурсивная версия)
def factorial(n: Int): Int =
  @annotation.tailrec
  def go(n: Int, acc: Int): Int =
    if n <= 0 then acc
    else go(n - 1, n * acc)

  go(n, 1)

// Функция высшего порядка для форматирования результатов
def formatResult(name: String, n: Int, f: Int => Int): String =
  val msg = "The %s of %d is %d."
  msg.format(name, n, f(n))
```

### Анализ реализации

1. **Сигнатура HOF**:
    - `formatResult` принимает функцию `f` типа `Int => Int`
    - Это означает, что `f` должна принимать `Int` и возвращать `Int`
    - Такой подход позволяет абстрагироваться от конкретной операции
2. **Соответствие типов**:
    - Функции `abs` и `factorial` полностью соответствуют типу `Int => Int`
    - Это позволяет передавать их в `formatResult`

**Примеры использования**:

```dotty
formatResult("absolute value", -42, abs)
// Результат: "The absolute value of -42 is 42."

formatResult("factorial", 7, factorial)
// Результат: "The factorial of 7 is 5040."
```

## Преимущества подхода

1. **Универсальность**:
    - Один форматировщик может работать с любыми функциями подходящего типа
    - Не требуется писать отдельные функции для каждого типа вычислений
2. **Гибкость**:
    - Легко добавлять новые операции без изменения `formatResult`
    - Можно комбинировать функции для создания сложных поведений
3. **Читаемость**:
    - Явное указание типов делает код более понятным
    - Логика форматирования отделена от логики вычислений

## Упражнение 1.4.1: Практическое применение HOF

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача**: Реализуйте функцию высшего порядка, которая:

1. Принимает функцию преобразования `A => B` и список элементов типа `A`
2. Возвращает новый список, где каждый элемент преобразован с помощью переданной функции
3. Сохраняет порядок элементов исходного списка

```dotty
def map[A, B](as: List[A])(f: A => B): List[B] = ???
```

**Требования**:

- Использовать параметрические типы (generics)
- Поддерживать работу с разными типами данных
- Реализовать рекурсивную версию

@:note(🔍 Показать решение)

```dotty
def map[A, B](as: List[A])(f: A => B): List[B] =
  as match
    case Nil => Nil
    case h :: t => f(h) :: map(t)(f)
```

**Примеры использования**:

```dotty
map(List(1, 2, 3))(_ * 2) // List(2, 4, 6)
map(List("a", "b"))(_.toUpperCase) // List("A", "B")
```

Это упражнение демонстрирует классическую функцию высшего порядка **map**, которая является основой функционального программирования.

@:@

## Упражнение 1.4.2: Фабрика функций-валидаторов

**Задача**: Реализуйте функцию высшего порядка, которая:

1. Принимает предикат (функцию проверки) типа `A => Boolean`
2. Возвращает новую функцию-валидатор типа `A => Either[String, A]`
3. Возвращаемая функция должна:
   - Принимать значение типа `A`
   - Возвращать `Right(value)` если предикат возвращает `true`
   - Возвращать `Left(errorMessage)` если предикат возвращает `false`

```dotty
def validatorFactory[A](predicate: A => Boolean, errorMessage: String): A => Either[String, A] = ???
```

**Требования**:

- Использовать параметрические типы (generics)
- Поддерживать кастомные сообщения об ошибках
- Сохранять исходное значение при успешной проверке
- Возвращать функцию (не вычислять результат сразу)

@:note(🔍 Показать решение)

```dotty
def validatorFactory[A](predicate: A => Boolean, errorMessage: String): A => Either[String, A] =
  (value: A) =>
    if predicate(value) then Right(value)
    else Left(errorMessage)
```

**Примеры использования**:

```dotty
// Создаем валидаторы
val ageValidator = validatorFactory[Int](_ >= 18, "Age must be >= 18")
val emailValidator = validatorFactory[String](_.contains("@"), "Invalid email format")

// Применяем валидаторы
ageValidator(25)    // Right(25)
ageValidator(16)    // Left("Age must be >= 18")
emailValidator("test@example.com") // Right("test@example.com")
emailValidator("invalid")          // Left("Invalid email format")
```

@:@

## Анонимные функции

[Анонимные функции](https://ru.wikipedia.org/wiki/%D0%90%D0%BD%D0%BE%D0%BD%D0%B8%D0%BC%D0%BD%D0%B0%D1%8F_%D1%84%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F) 
(также известные как функциональные литералы или лямбда-выражения) - это функции, которые:

- Не имеют имени (не связаны с идентификатором)
- Могут быть определены в месте использования
- Часто передаются как аргументы в функции высшего порядка

**Преимущества анонимных функций:**

1. **Локальность** - определение функции непосредственно в месте использования
2. **Лаконичность** - сокращение кода за счет отказа от именования
3. **Гибкость** - возможность быстрого создания специализированных функций

**Пример использования**

Рассмотрим функцию поиска первого элемента в массиве, удовлетворяющего условию:

```dotty
def findFirst[A](as: Array[A], predicate: A => Boolean): Int =
  @annotation.tailrec
  def loop(index: Int): Int =
    if index >= as.length then -1
    else if predicate(as(index)) then index
    else loop(index + 1)

  loop(0) // Начинаем поиск с 0 индекса
```

Применение с анонимной функцией:

```dotty
// Поиск числа 9 в массиве
findFirst(Array(7, 9, 13), (x: Int) => x == 9)
// Результат: 1 (индекс найденного элемента)
```

**Синтаксис анонимных функций в Scala**

Выражение `(x: Int) => x == 9` состоит из:

- Списка параметров `(x: Int)`
- Стрелки `=>` (лямбда-символ)
- Тела функции `x == 9`

**Дополнительные примеры**

Простое преобразование:

```dotty
val square = (x: Int) => x * x
square(5) // 25
```

Фильтрация списка:

```dotty
List(1, 2, 3).filter(x => x % 2 == 1) // List(1, 3)
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample

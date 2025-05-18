{%
showPrev = true
prevPage = currying.html
showNext = true
nextPage = ../data-structures.html
%}

# Композиция функций

Композиция функций — фундаментальная концепция функционального программирования, 
позволяющая создавать сложные операции путем объединения простых функций. 
Это аналогично математической композиции, где результат одной функции становится входом для другой.

**Ключевые преимущества:**

- 🧩 Модульность — разбиение сложной логики на простые компоненты
- 🧠 Улучшенная читаемость — последовательность операций выражается явно
- 🔄 Повторное использование — компоненты могут комбинироваться по-разному
- 🧪 Тестируемость — простые функции легче тестировать изолированно

## Математические основы

В математике композиция функций f и g записывается как g∘f и означает применение f, затем g: `(g∘f)(x) = g(f(x))`

В функциональном программировании эта концепция реализуется через:

- Оператор композиции (∘ в некоторых языках)
- Методы `compose` и `andThen`
- Комбинаторы функций

## Сравнение compose и andThen

В Scala есть два основных способа композиции:

```dotty
val f: A => B = ???
val g: B => C = ???

// 1. compose: g(f(x))
val h1 = g compose f  // "g после f"

// 2. andThen: f andThen g означает сначала f, затем g
val h2 = f andThen g   // "f затем g"
```

**Разница между ними:**

- `compose` применяет функции справа налево (математическая нотация)
- `andThen` применяет функции слева направо (более интуитивно для последовательных операций)

## Подробные примеры

### Базовый пример

```dotty
val toUpperCase: String => String = _.toUpperCase
val addExclamation: String => String = _ + "!"

// Композиция 1: математический стиль
val emphasize1 = addExclamation compose toUpperCase

// Композиция 2: последовательный стиль
val emphasize2 = toUpperCase andThen addExclamation

emphasize1("hello") // "HELLO!"
emphasize2("hello") // "HELLO!"
```

### Работа с разными типами

```dotty
case class User(name: String, age: Int)

val parseUser: String => User = json => /* парсинг JSON */ 
val isAdult: User => Boolean = _.age >= 18
val toResult: Boolean => String = if (_) "Доступ разрешен" else "Доступ запрещен"

val checkAccess = parseUser andThen isAdult andThen toResult

checkAccess("""{"name":"Alice","age":20}""") // "Доступ разрешен"
```

## Свойства композиции

а) **Ассоциативность**:

```dotty
(f andThen g) andThen h == f andThen (g andThen h)
```

Порядок группировки не влияет на результат.

б) **Некоммутативность**:

```dotty
f andThen g != g andThen f  // В общем случае
```

Порядок функций важен!

в) **Существование нейтрального элемента**:

```dotty
f andThen identity == f == identity andThen f
```

Где `identity` — тождественная функция.

## Практическое применение

### Конвейеры обработки данных

```dotty
val cleanInput: String => String = _.trim
val parseNumber: String => Option[Int] = s => Try(s.toInt).toOption
val validate: Int => Option[Int] = n => if (n > 0) Some(n) else None
val double: Int => Int = _ * 2

val process = cleanInput andThen parseNumber andThen (_.flatMap(validate)) andThen (_.map(double))

process(" 10 ") // Some(20)
process("-5")   // None
```

### Построение DSL

```dotty
case class Query(filters: Map[String, String], limit: Int)

val addFilter: (String, String) => Query => Query =
  (key, value) => query => query.copy(filters = query.filters + (key -> value))

val setLimit: Int => Query => Query =
  limit => query => query.copy(limit = limit)

// Композиция преобразователей запроса
val buildQuery = (addFilter("status", "active")(_))
  .andThen(setLimit(10))

val initial = Query(Map.empty, 0)
buildQuery(initial) // Query(Map("status" -> "active"), 10)
```

## Упражнение 1.6.1: Базовые операции

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Реализуйте цепочку преобразований**:

1. Преобразуйте строку в целое число (используйте `.toIntOption` со значением по умолчанию `0`)
2. Увеличьте число на `10`
3. Преобразуйте результат в строку (используйте `.toString`)

```dotty
val solution: String => String = ???
```

**Требования**:

- Используйте композицию функций (`andThen` или `compose`)
- Обрабатывайте базовый случай (пустая строка не должна приводить к ошибке)

**Примеры работы**:

```scala
solution("5")    // "15"
solution("-3")   // "7"
solution("")     // "10"
```

@:note(🔍 Показать решение)

```dotty
val toNumber: String => Int =
  _.toIntOption.getOrElse(0)

val addTen: Int => Int = _ + 10

val solution: String => String =
  toNumber andThen addTen andThen (_.toString)
```

@:@

## Упражнение 1.6.2: Безопасная композиция

**Задача**: Создайте композицию, которая:

1. Парсит строку в `Double` (при некорректном вводе используйте значение по умолчанию - `0.0`)
2. Вычисляет квадратный корень (учитывайте отрицательные числа, вместо них используйте значение по умолчанию - `0.0`)
3. Преобразует результат в строку с 2 знаками после запятой

```dotty
val solution2: String => String = ???
```

**Требования**:

- Каждый шаг должен быть отдельной функцией
- Формат результата: `"Результат: X.XX"`
- При ошибке используйте значение по умолчанию - `0.0`

**Примеры работы**:

```scala
solution2("16.0")  // "Результат: 4,00"
solution2("-4.0")  // "Результат: 0,00"
solution2("abc")   // "Результат: 0,00"
```

@:note(🔍 Показать решение)

```dotty
val parseDouble: String => Double =
  _.toDoubleOption.getOrElse(0.0)

val sqrtSafe: Double => Double =
  x => if x >= 0 then math.sqrt(x) else 0.0

val formatResult: Double => String =
  x => f"Результат: $x%.2f"

val solution2: String => String =
  parseDouble andThen
    sqrtSafe andThen
    formatResult
```

@:@

## Заключение

Композиция функций — мощный инструмент, позволяющий:

- Строить сложную логику из простых компонентов
- Создавать читаемый и выразительный код
- Легко модифицировать и переиспользовать компоненты

**Рекомендации по использованию:**

1. Разбивайте сложные операции на простые функции
2. Используйте `andThen` для последовательных преобразований
3. Соблюдайте принцип единственной ответственности для компонуемых функций

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample

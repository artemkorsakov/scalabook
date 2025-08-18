{%
showPrev = true
prevPage = higher_order_function.html
showNext = true
nextPage = composition.html
%}

# Каррирование

[Каррирование](https://ru.wikipedia.org/wiki/%D0%9A%D0%B0%D1%80%D1%80%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5) —
это фундаментальная техника в функциональном программировании, названная в честь математика Хаскелла Карри.
Эта концепция позволяет преобразовывать функции с несколькими аргументами в цепочку функций с одним аргументом.

Хотя концепция названа в честь [Хаскелла Карри](https://ru.wikipedia.org/wiki/%D0%9A%D0%B0%D1%80%D1%80%D0%B8,_%D0%A5%D0%B0%D1%81%D0%BA%D0%B5%D0%BB%D0%BB),
она была первоначально предложена [Моисеем Шейнфинкелем](https://ru.wikipedia.org/wiki/%D0%A8%D0%B5%D0%B9%D0%BD%D1%84%D0%B8%D0%BD%D0%BA%D0%B5%D0%BB%D1%8C,_%D0%9C%D0%BE%D0%B8%D1%81%D0%B5%D0%B9_%D0%AD%D0%BB%D1%8C%D0%B5%D0%B2%D0%B8%D1%87) в 1924 году.
Каррирование стало важной частью лямбда-исчисления и функциональных языков программирования.

## Основная идея

Каррирование преобразует функцию типа `(A, B) => C` в функцию типа `A => (B => C)`. Это означает:

- Вместо функции, принимающей два аргумента сразу, мы получаем функцию, которая:
    - Принимает первый аргумент (`A`)
    - Возвращает новую функцию, которая ожидает второй аргумент (`B`)
    - После получения второго аргумента возвращает результат (`C`)

## Сравнение каррированной и обычной функций

Рассмотрим две реализации функции поиска минимума:

```dotty
// Каррированная версия
def min(x: Int)(y: Int): Int = if x < y then x else y

// Обычная версия
def min2(x: Int, y: Int): Int = if x < y then x else y
```

### Ключевые различия

| Характеристика           | Каррированная (`min`)       | Обычная (`min2`)              |
|--------------------------|-----------------------------|-------------------------------|
| **Тип функции**          | `Int => (Int => Int)`       | `(Int, Int) => Int`           |
| **Способ вызова**        | `min(5)(10)` или `min(5)`   | `min2(5, 10)`                 |
| **Частичное применение** | Поддерживается              | Требует явного преобразования |
| **Гибкость**             | Выше                        | Ниже                          |

### Анализ типов

**Каррированная версия**:

```dotty
min: Int => (Int => Int)
min(5): Int => Int  // промежуточная функция
min(5)(10): Int     // конечный результат
```

**Обычная версия**:

```dotty
min2: (Int, Int) => Int
min2(5, 10): Int    // сразу конечный результат
```

## Преимущества каррирования

**Частичное применение** — возможность фиксировать часть аргументов:

```dotty
val positiveMin = min(0)  // функция, ищущая минимум между 0 и другим числом
positiveMin(-5)  // -5
positiveMin(10)  // 0
```

**Композиция функций** — удобство создания цепочек преобразований:

```dotty
List(1, 2, 3).map(min(2))  // [1, 2, 2]
```

**Ленивые вычисления** — отложенное выполнение до получения всех аргументов:

```dotty
val expensiveComputation: Int => Int = x => { /* долгие вычисления */ }
val curried = (x: Int) => (y: Int) => expensiveComputation(x) + y
val stage1 = curried(42)  // вычисления не выполняются
```

**Модульность** — возможность разделения сложной логики на простые шаги:

```dotty
def process(data: Data)(filter: Filter)(transformer: Transformer): Result = ???

val dataProcessor = process(data) _    // фиксируем данные
val withFilter = dataProcessor(filter) // добавляем фильтр
val result = withFilter(transformer)   // применяем преобразование
```

## Расширенный пример: каррированное сложение

```dotty
def add(x: Int)(y: Int): Int = x + y
```

### Практическое применение:

```dotty
// Создание специализированных функций
val increment = add(1)      // y => 1 + y
val decrement = add(-1)     // y => -1 + y
val identity = add(0)       // y => y

// Использование в комбинациях
List(1, 2, 3).map(increment)  // [2, 3, 4]
List(5, 6, 7).map(add(10))    // [15, 16, 17]
```

## Функции каррирования и декаррирования

### Базовые преобразования

```dotty
// Преобразование обычной функции в каррированную
def curry[A, B, C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)

// Обратное преобразование
def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)
```

### Таблица преобразований типов

| Исходный тип        | Каррированный тип         | Пример использования              |
|---------------------|---------------------------|-----------------------------------|
| `(A,B) => C`        | `A => B => C`             | `curry((a,b) => a + b)`           |
| `(A,B,C) => D`      | `A => B => C => D`        | `curry3((a,b,c) => a + b + c)`    |
| `(A1,...,An) => R`  | `A1 => ... => An => R`    | Обобщённое преобразование         |

## Инвариант каррирования

Преобразования `curry` и `uncurry` являются взаимно обратными:

```dotty
uncurry(curry(f)) == f  // Для любой f: (A,B) => C
curry(uncurry(g)) == g  // Для любой g: A => B => C
```

Это означает, что мы можем свободно преобразовывать функции между каррированной и некаррированной формами без потери информации.

## Практическое применение в Scala

### Особенности реализации в Scala

**Синтаксический сахар**:

```dotty
// Эквивалентные записи
def add(x: Int)(y: Int) = x + y
def add = (x: Int) => (y: Int) => x + y
```

**Автоматическое каррирование**:

```dotty
val sum = (x: Int, y: Int) => x + y
val curriedSum = sum.curried  // стандартный метод Scala
```

**Частичное применение**:

```dotty
List(1, 2, 3).map(_ + 1)     // с использованием placeholder syntax
List(1, 2, 3).map(add(1))    // с каррированной функцией
```

### Типичные сценарии использования

**Конфигурирование функций**:

```dotty
def connect(dbUrl: String)(credentials: Credentials): Connection = ???

val prodConnection = connect("jdbc:prod://db") _
val devConnection = connect("jdbc:dev://localhost") _
```

**Создание DSL**:

```dotty
def html(tag: String)(content: String): String = s"<$tag>$content</$tag>"
val div = html("div") _
val p = html("p") _

div(p("Hello world"))  // <div><p>Hello world</p></div>
```

**Инъекция зависимостей**:

```dotty
def service(userRepo: UserRepository)(auth: AuthService): BusinessLogic = ???

val prodLogic = service(prodUserRepo)(prodAuth)
val testLogic = service(testUserRepo)(mockAuth)
```

**Конвейеры обработки данных**:

```dotty
def pipeline(validate: Validator)(transform: Transformer)(save: Saver): Data => Result = 
  data => save(transform(validate(data)))

val dataPipeline = pipeline(jsonValidator)(jsonToModel)(dbSaver)
```

## Упражнение 1.6.1: Практика каррирования и частичного применения

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача**: Реализуйте и протестируйте каррированные версии функций, следуя требованиям:

1. Реализуйте функцию `multiply`, которая принимает три аргумента по одному (`x`, `y`, `z`) и возвращает их произведение
2. Создайте каррированную версию функции `concatStrings`, добавляющую разделитель между строками
3. Реализуйте обобщённые функции `curry` и `uncurry` для трёх аргументов

```dotty
// 1. Каррированное умножение (трехаргументное)
def multiply(x: Int)(y: Int)(z: Int): Int = ???

// 2. Каррированная конкатенация строк с разделителем
def concatStrings(delimiter: String)(s1: String)(s2: String): String = ???

// 3. Обобщённые функции преобразования
def curry3[A,B,C,D](f: (A,B,C) => D): A => B => C => D = ???
def uncurry3[A,B,C,D](f: A => B => C => D): (A,B,C) => D = ???
```

**Требования**:

- Все функции должны быть чистыми (без побочных эффектов)
- Использовать параметрический полиморфизм для `curry3`/`uncurry3`
- Поддерживать произвольные типы аргументов

**Примеры использования**:

```dotty
// 1. Умножение
val tripleMultiply = multiply(2)(3)(_) // Частично применённая
tripleMultiply(4) // 24

// 2. Конкатенация
val joinWithComma = concatStrings(", ")("Hello")(_) 
joinWithComma("World") // "Hello, World"

// 3. Преобразования
val sum3 = (a: Int, b: Int, c: Int) => a + b + c
val curriedSum = curry3(sum3)
curriedSum(1)(2)(3)  // 6

val uncurried = uncurry3(curriedSum)
uncurried(1, 2, 3)  // 6
```

@:note(🔍 Показать решение)

```dotty
def multiply(x: Int)(y: Int)(z: Int): Int = x * y * z

def concatStrings(delimiter: String)(s1: String)(s2: String): String =
  s"$s1$delimiter$s2"

def curry3[A, B, C, D](f: (A, B, C) => D): A => B => C => D =
  a => b => c => f(a, b, c)

def uncurry3[A, B, C, D](f: A => B => C => D): (A, B, C) => D =
  (a, b, c) => f(a)(b)(c)
```

@:@

## Заключение

Каррирование — мощный инструмент в функциональном программировании, который:

✓ Позволяет создавать более гибкие и композируемые функции  
✓ Поддерживает частичное применение и создание специализированных функций  
✓ Упрощает инъекцию зависимостей и конфигурирование  
✓ Является основой для многих техник функционального программирования

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [Currying in Scala - Baeldung](https://www.baeldung.com/scala/currying)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/scalabook/fp

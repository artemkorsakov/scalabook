{%
showNext = true
nextPage = fp/pure_function.html
%}

# Функциональное программирование

## Введение в функциональную парадигму

[Функциональное программирование (ФП, FP)][wiki] - это декларативная парадигма программирования,
основанная на математических функциях, где вычисления моделируются как последовательность
преобразований данных через применение функций. В отличие от императивного подхода,
ФП избегает изменения состояния (_state_) и работы с изменяемыми (_mutable_) данными,
делая акцент на:

- Чистоту функций (отсутствие побочных эффектов)
- Неизменяемость (immutability) данных
- Композицию функций как основной способ построения программ
- Выражения вместо инструкций

### Ключевые преимущества функционального подхода

1. **Повышенная читаемость и поддерживаемость кода** - благодаря малому размеру функций,
   их атомарности и отсутствию скрытых зависимостей
2. **Упрощенная отладка** - поскольку чистые функции зависят только от своих аргументов
   и не имеют скрытого состояния, их поведение полностью предсказуемо
3. **Естественная параллелизация** - отсутствие изменяемого состояния и побочных эффектов
   устраняет проблемы гонки данных (_race conditions_) при многопоточном выполнении
4. **Модульность и переиспользуемость** - функции как строительные блоки легко комбинируются
   в более сложные конструкции

В данном разделе мы детально изучим фундаментальные концепции ФП, включая:

- Чистые функции (_pure functions_)
- Рекурсию и хвостовую оптимизацию
- Функции высшего порядка (_higher-order functions_)
- Ленивые вычисления (_lazy evaluation_)
- Алгебраические типы данных и сопоставление с шаблоном (_pattern matching_)

## Сравнение императивного и функционального стилей

Рассмотрим ключевое отличие [императивного программирования (IP)](https://ru.wikipedia.org/wiki/%D0%98%D0%BC%D0%BF%D0%B5%D1%80%D0%B0%D1%82%D0%B8%D0%B2%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5)
от функционального на практическом примере вычисления суммы элементов списка чисел.

### Императивный подход

Характеризуется:

- Явным управлением состоянием через переменные
- Использованием циклов для итераций
- Последовательностью изменяющих состояние команд

```dotty
def sum(ints: List[Int]): Int =
  var sum = 0                         // Изменяемое состояние
  var i = 0                           // Еще одно изменяемое состояние
  while i < ints.length do            // Императивный цикл
    sum += ints(i)                    // Изменение состояния
    i += 1                            // Изменение состояния
  sum                                 // Возврат результата

sum(List(1, 2, 3))                    // 6
```

### Функциональный подход

Отличается:

- Отсутствием изменяемого состояния
- Использованием рекурсии вместо циклов
- Неизменяемостью данных

```dotty
def sum(xs: List[Int]): Int =
  @tailrec                            // Аннотация для хвостовой рекурсии
  def loop(xs: List[Int], acc: Int): Int =  // Вспомогательная функция
    xs match                          // Сопоставление с шаблоном
      case Nil       => acc           // Базовый случай рекурсии
      case x :: tail => loop(tail, acc + x) // Рекурсивный случай

  loop(xs, 0)                         // Начальный вызов с аккумулятором = 0

sum(List(1, 2, 3))                    // 6
```

### Детальное объяснение функционального решения

1. **Структура**:
    - Основная функция `sum` инициализирует процесс, вызывая вспомогательную функцию `loop`
      с начальным аккумулятором `0`
    - Функция `loop` реализует хвостовую рекурсию для накопления результата
2. **Pattern matching**:
    - `Nil` - терминальный случай для пустого списка
    - `x :: tail` - декомпозиция списка на голову (`x`) и хвост (`tail`)
3. **Рекурсивный процесс**:
    - На каждом шаге к аккумулятору `acc` прибавляется текущий элемент `x`
    - Рекурсивный вызов происходит с хвостом списка и новым значением аккумулятора
    - При достижении пустого списка возвращается накопленное значение
4. **Иммутабельность**:
    - Все параметры (`xs`, `acc`) неизменяемы
    - Новые значения передаются через аргументы рекурсивных вызовов

## Основные правила функционального стиля

1. **Избегание null** - использование `Option[T]` для представления возможного отсутствия значения
2. **Чистые функции** - гарантия, что функция при одинаковых входных данных всегда возвращает
   одинаковый результат без побочных эффектов
3. **Неизменяемость** - предпочтение `val` перед `var`, неизменяемых структур данных
4. **Отказ от ООП-классов** - использование case-классов для данных и traits для поведения
5. **Выражения вместо операторов** - `if` должно возвращать значение, а не выполнять действия
6. **Композиция** - построение сложной логики через комбинацию простых функций

## Упражнение 1.1.1: Реализация факториала

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Ваша задача - завершить реализацию функции вычисления факториала,
заменив пропущенные части (`???`) корректным кодом:

```dotty
@tailrec
def factorial(n: Int, acc: Long = 1): Long =
  if (n <= 1) ???
  else factorial(n - 1, ???)
```

**Подсказки**:

1. Функция использует хвостовую рекурсию (обозначено аннотацией `@tailrec`)
2. Параметр `acc` служит аккумулятором для накопления результата
3. При `n ≤ 1` рекурсия должна завершиться, вернув накопленное значение
4. На каждом шаге нужно обновить аккумулятор

@:note(🔎 Детальный разбор решения 🔍)

Принцип работы функции:

1. **Базовый случай**: когда n достигает 1 или меньше, возвращаем аккумулятор,
   содержащий произведение всех чисел от n до 1
2. **Рекурсивный случай**: уменьшаем n на 1 и умножаем аккумулятор на текущее n
3. **Начальное значение**: по умолчанию acc = 1, что корректно для factorial(0) и factorial(1)

```dotty
@tailrec
def factorial(n: Int, acc: Long = 1): Long =
  // Терминальное условие
  if (n <= 1) acc  
  // Рекурсивный вызов                    
  else factorial(n - 1, n * acc)       
```

**Примеры вызовов**:

```dotty
factorial(5)    // 120 = 5 * 4 * 3 * 2 * 1
factorial(0)    // 1    (по определению)
factorial(10)   // 3628800
```

@:@

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Не использовать `null`](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#nothing-and-null)
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [A bite of functional programming (in Scala)](https://scalac.io/blog/functional-programming-in-scala/)
- [Business benefits of functional programming and how to make it work for your company](https://www.youtube.com/watch?v=EcGZdzECmIk)
- [FP vs. OO List Processing](https://blog.cleancoder.com/uncle-bob/2018/12/17/FPvsOO-List-processing.html)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [Unleashing the Power: The Advantages of Functional Programming in the Digital Age](https://scalac.io/blog/unleashing-the-power-the-advantages-of-functional-programming-in-the-digital-age/)
- [What's Functional Programming All About?](https://www.lihaoyi.com/post/WhatsFunctionalProgrammingAllAbout.html)
- [What is Functional Programming? - Alexandru Nedelcu](https://alexn.org/blog/2017/10/15/functional-programming/)
- [What is functional programming and why it matters for your business?](https://scalac.io/blog/benefits-of-functional-programming/)
- [What makes a function pure?](https://blog.kubukoz.com/what-makes-a-function-pure/)
- [Wikipedia][wiki]

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample
[wiki]: https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5

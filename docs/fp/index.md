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
  // Вспомогательная функция
  def loop(xs: List[Int], acc: Int): Int =  
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
    - Функция `loop` реализует рекурсию для накопления результата
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
def factorial(n: Int, acc: Long = 1): Long =
  if (n <= 1) ???
  else factorial(n - 1, ???)
```

**Подсказки**:

1. Функция использует рекурсию
2. Параметр `acc` служит аккумулятором для накопления результата
3. При `n ≤ 1` рекурсия должна завершиться, вернув накопленное значение
4. На каждом шаге нужно обновить аккумулятор

@:note(🔍 Показать решение)

Принцип работы функции:

1. Базовый случай: когда `n` достигает `1` или меньше, возвращаем аккумулятор,
   содержащий произведение всех чисел от `n` до `1`
2. Рекурсивный случай: уменьшаем `n` на `1` и умножаем аккумулятор на текущее `n`
3. Начальное значение: по умолчанию `acc = 1`, что корректно для `factorial(0)` и `factorial(1)`

```dotty
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
- [Десять причин не использовать статически типизированный функциональный язык программирования](https://habr.com/ru/articles/190492/)
- [Жаргон функционального программирования](https://habr.com/ru/articles/310172/)
- [Не использовать `null`](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#nothing-and-null)
- [Не морочьте мне голову со своим функциональным программированием](https://habr.com/ru/companies/oleg-bunin/articles/462505/)
- [От Аристотеля до Тьюринга: что такое функциональное программирование и как оно облегчает жизнь](https://habr.com/ru/companies/avito/articles/926544/)
- [Почему функциональное программирование такое сложное?](https://habr.com/ru/articles/505928/)
    - [Глубже в дебри ФП](https://habr.com/ru/articles/506782/) 
- [Функциональное программирование: в Java и C# слишком много церемоний](https://habr.com/ru/companies/jugru/articles/328042/)
- [Функциональное программирование: то, что вам (наверно) рассказывали. Если вы слушали](https://habr.com/ru/articles/492460/)
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [A Beginner-Friendly Tour through Functional Programming in Scala](https://degoes.net/articles/easy-monads)
- [A bite of functional programming (in Scala)](https://scalac.io/blog/functional-programming-in-scala/)
- [A Critique of Impure Reason](https://blog.higher-order.com/blog/2009/04/27/a-critique-of-impure-reason/)
- [Business benefits of functional programming and how to make it work for your company](https://www.youtube.com/watch?v=EcGZdzECmIk)
- [Error Handling in FP](https://gvolpe.com/blog/error-handling-scala3/)
- [FP vs. OO List Processing](https://blog.cleancoder.com/uncle-bob/2018/12/17/FPvsOO-List-processing.html)
- [FP Anti-Patterns](https://scalajobs.com/blog/functional-programming-anti-patterns-in-scala)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming for Beginners](https://apocalisp.wordpress.com/2011/01/10/functional-programming-for-beginners/)
- [Functional Programming Group](https://www.youtube.com/watch?app=desktop&v=s2ay9nEW3ak&list=PLNo5uJesLLQjRmM9lASwVeL0fFtJuKU0U)
- [Functional Programming in Scala](https://www.youtube.com/playlist?list=PLJGDHERh23x8AD0LFiS9iPg_tKLS1i1wZ)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Functional Programming Isn't the Answer](https://degoes.net/articles/fp-is-not-the-answer)
- [Functional Scala](https://www.youtube.com/playlist?list=PLjtl9Y7VJMWtZz3g0LjMhO54_KBL8Tfwl)
- [Imperative vs Functional Programming](https://apocalisp.wordpress.com/2011/05/30/imperative-vs-functional-programming/)
- [In Defense of OOFP](https://alexn.org/blog/2018/02/12/in-defense-oofp/)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Martians vs Monads: Null Considered Harmful](https://james-iry.blogspot.com/2007/08/martians-vs-monads-null-considered.html)
- [Noumenal Null](https://apocalisp.wordpress.com/2008/05/03/null-vs-pure-reason/)
- [Objects, Identity, and Concept-formation](https://blog.higher-order.com/blog/2008/12/04/no-such-thing/)
- [On Scala, Functional Programming and Type-Classes](https://alexn.org/blog/2012/11/02/scala-functional-programming-type-classes/)
- [Phantom types - Make illegal states unrepresentable](https://blog.rhetoricalmusings.com/posts/phantom-types)
    - [Phantom types - Part 2](https://blog.rhetoricalmusings.com/posts/phantom-types-2/) 
- [Scala Exceptions - Try, try..catch and a caveat](https://yadukrishnan.live/scala-exceptions-try-trycatch-and-a-caveat)
- [Scala Functional Programming Tutorial - Baeldung](https://www.baeldung.com/scala/functional-programming-series)
- [Scala's isInstanceOf is an Anti-Pattern - Alexandru Nedelcu](https://alexn.org/blog/2019/08/11/isinstanceof-anti-pattern/)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [Shared State in Functional Programming](https://typelevel.org/blog/2018/06/07/shared-state-in-fp.html)
- [Some History of Functional Programming Languages](https://www.youtube.com/watch?v=QVwm9jlBTik)
- [Unleashing the Power: The Advantages of Functional Programming in the Digital Age](https://scalac.io/blog/unleashing-the-power-the-advantages-of-functional-programming-in-the-digital-age/)
- [What's Functional Programming All About?](https://www.lihaoyi.com/post/WhatsFunctionalProgrammingAllAbout.html)
- [What is Functional Programming? - Alexandru Nedelcu](https://alexn.org/blog/2017/10/15/functional-programming/)
- [What is functional programming and why it matters for your business?](https://scalac.io/blog/benefits-of-functional-programming/)
- [Why is functional programming awesome](https://abuseofnotation.github.io/fp/)
- [What is an Effect?](https://www.inner-product.com/posts/what-is-an-effect/)
- [What makes a function pure?](https://blog.kubukoz.com/what-makes-a-function-pure/)
- [Why Functional Programming Matters](https://blog.rhetoricalmusings.com/posts/why-fp-matters/)
- [Wikipedia][wiki]

[sample]: https://gitverse.ru/scalabook/fp
[wiki]: https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5

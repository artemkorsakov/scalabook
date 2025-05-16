{%
showPrev = true
prevPage = reduction.html
showNext = true
nextPage = anonymous_function.html
%}

# Функция высшего порядка

[Функция высшего порядка или Функция первого класса (_Higher-Order Function_ - HOF)](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%B8_%D0%BF%D0%B5%D1%80%D0%B2%D0%BE%D0%B3%D0%BE_%D0%BA%D0%BB%D0%B0%D1%81%D1%81%D0%B0)
часто определяется как функция, которая

- принимает другие функции в качестве входных параметров или
- возвращает функцию в качестве результата.

Рассмотрим пример: допустим, необходимо написать функцию `formatResult`,
вычисляющую модуль числа или его факториал и выводящую результат.

```dotty
def abs(n: Int): Int =
  if n < 0 then -n
  else n

def factorial(n: Int): Int =
  @annotation.tailrec
  def go(n: Int, acc: Int): Int =
    if n <= 0 then acc
    else go(n - 1, n * acc)

  go(n, 1)

def formatResult(name: String, n: Int, f: Int => Int): String =
  val msg = "The %s of %d is %d."
  msg.format(name, n, f(n))
```

Функция `formatResult` — это функция высшего порядка (HOF), которая принимает другую функцию, называемую `f`.
`f` присваивается тип, как и любому другому параметру.
Её тип, `Int => Int`, указывает на то, что `f` ожидает целочисленный аргумент и возвращает целое число.

Функция `abs` соответствует этому типу. Она принимает `Int` и возвращает `Int`.
Точно так же `factorial` соответствует типу `Int => Int`.
Поэтому можно передать `abs` или `factorial` в качестве аргумента `f` в `formatResult`:

```dotty
formatResult("absolute value", -42, abs)
// "The absolute value of -42 is 42."
formatResult("factorial", 7, factorial)
// "The factorial of 7 is 5040."
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

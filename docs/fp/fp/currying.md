{%
showPrev = true
prevPage = anonymous_function.html
showNext = true
nextPage = composition.html
%}

# Каррирование

Рассмотрим два определения `min`:

```dotty
def min(x: Int)(y: Int): Int = if x < y then x else y
def min2(x: Int, y: Int): Int = if x < y then x else y
```

Две функции, `min` и `min2`, очень тесно связаны, но есть тонкое различие: у них разные типы.
Функция `min2` принимает один аргумент, который является структурированным значением, состоящим из пары чисел;
ее тип задается как: `(Int, Int) => Int`.
Функция `min`, с другой стороны, принимает два аргумента по одному за раз.
Её тип задается как: `Int => (Int => Int)`.

Другими словами, `min` — это функция, которая принимает число и возвращает функцию (из чисел в числа).
Для каждого значения `x` выражение `min(x)` обозначает функцию,
которая принимает аргумент `y` и возвращает минимум из `x` и `y`.

Вот еще один пример. Функция `add`, определяемая как: `def add(x: Int)(y: Int) = x + y`
также имеет тип `Int => (Int => Int)`.
Для каждого `x` функция `add(x)` «добавляет `x` к переменным».
В частности, `add(1)` — это функция-последователь, которая увеличивает свой аргумент на `1`,
а `add(0)` — это функция тождественности чисел.

Этот простой прием для замены структурированных аргументов последовательностью
простых аргументов известен как [_«каррирование»_](https://ru.wikipedia.org/wiki/%D0%9A%D0%B0%D1%80%D1%80%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5)
в честь американского логика H. B. Curry.

Каррирование - преобразование функции от многих аргументов в набор функций, каждая из которых является функцией от одного аргумента.

Примеры каррирования и декаррирования:

```dotty
def curry[A, B, C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)

def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)
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

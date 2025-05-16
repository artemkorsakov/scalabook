{%
showPrev = true
prevPage = higher_order_function.html
showNext = true
nextPage = currying.html
%}

# Анонимная функция

При использовании HOF часто удобно иметь возможность вызывать эти функции
с помощью [анонимных функций](https://ru.wikipedia.org/wiki/%D0%90%D0%BD%D0%BE%D0%BD%D0%B8%D0%BC%D0%BD%D0%B0%D1%8F_%D1%84%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F) или функциональных литералов,
вместо того, чтобы предоставлять какую-либо существующую именованную функцию.

Лямбда-исчисление (также записываемое как λ-исчисление) -
это формальная система в математической логике для выражения вычислений, основанных на абстракции функций
и применении с использованием привязки переменных и подстановки.

Пример использования анонимных функций:

```dotty
def findFirst[A](as: Array[A], p: A => Boolean): Int =
  @annotation.tailrec
  def loop(n: Int): Int =
    if n >= as.length then -1
    else if p(as(n)) then n
    else loop(n + 1)

  loop(0)

findFirst(Array(7, 9, 13), (x: Int) => x == 9)
// 1
```

Синтаксис `(x: Int) => x == 9` представляет собой литерал функции или анонимную функцию.

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample

# Параллелизм

Параллелизм с помощью чистых функций.

Рассмотрим операцию суммирования чисел:

```scala
def sum(ints: Seq[Int]): Int =
  ints.foldLeft(0)((a, b) => a + b)
```

Здесь используется последовательное сворачивание, когда мы проходим по всей коллекции элемент за элементом,
каждый раз складывая текущий элемент к итоговому результату.



---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 7](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)


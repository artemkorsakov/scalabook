{%
showPrev = true
prevPage = currying.html
showNext = true
nextPage = ../data-structures.html
%}

# Композиция функций

Композиция функций в функциональном программировании (ФП) — это операция,
позволяющая объединить две или более функций в одну новую функцию.
Эта новая функция применяется последовательно: результат первой функции передается второй функции,
результат второй — третьей и так далее.
Композиция функций является важным инструментом в ФП,
так как она позволяет создавать сложные операции из простых частей, сохраняя чистоту и читаемость кода.

Композиция функций обычно обозначается символом ∘ (кружок) или специальной функцией,
например, `compose` в языках программирования.

## Основные свойства композиции функций:

- **Последовательное применение:** Каждая функция выполняется одна за другой, передавая результаты предыдущей функции следующей.
- **Неизменяемые данные:** Поскольку функции в ФП обычно являются чистыми (не меняют состояние программы), композиция также остается чистой операцией.
- **Коммутативность:** Порядок выполнения функций важен. Изменение порядка может привести к другому результату.

## Основная идея

Если у нас есть две функции:

- `f: A -> B`
- `g: B -> C`

То их композиция `g ∘ f` (читается как "`g` после `f`") — это новая функция `A -> C`,
которая сначала применяет `f` к аргументу, а затем `g` к результату `f`.

Функциональная композиция берет функцию типа `A => B`, функцию типа `B => C` и возвращает функцию типа `A => C`.
Функциональная композиция является ассоциативной операцией.
Имеем: `((f andThen g) andThen h) = f andThen (g andThen h)` для всех функций `f`, `g` и `h`.
Соответственно, нет необходимости ставить скобки при записи последовательностей композиций.

## Пример

```dotty
// Определение двух функций
val double: Int => Int = _ * 2
val increment: Int => Int = _ + 1

// Создание новой функции через композицию
val doubleThenIncrement = increment.compose(double)

// Применение составной функции
doubleThenIncrement(5)
// Ожидаемый вывод: 11 = 5 * 2 + 1
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

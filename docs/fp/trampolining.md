# Stackless Scala

Перевод статьи Runar Oli Bjarnason ["Stackless Scala With Free Monads"][trampolines]

## Для понимания

Исключение абстрактного хвостового вызова (Tail call elimination - TCE) в компиляторе Scala
ограничено саморекурсивными методами, но в остальном хвостовые вызовы не исключаются.
Это делает функции, состоящие из множества более мелких функций, склонными к переполнению стека.
Наличие общего механизма TCE было бы большим преимуществом в Scala,
особенно для функционального программирования.
Трамплининг — популярный метод, который можно использовать для TCE в языках,
изначально его не поддерживающих.
В этой статье дается введение в "батуты" в Scala и расширяется это решение,
позволяющее устранить любой вызов метода, даже вызов, который вообще не находится в хвостовой позиции.
Это полностью исключает использование стека вызовов в программах Scala.





---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Runar Oli Bjarnason - Stackless Scala With Free Monads][trampolines]

[trampolines]: https://blog.higher-order.com/assets/trampolines.pdf

# Что такое функциональное программирование?

[Википедия][Wikipedia] определяет функциональное программирование следующим образом:

_Функциональное программирование — парадигма программирования, 
в которой процесс вычисления трактуется как вычисление значений функций в математическом понимании последних._

_Функциональное программирование предполагает обходиться вычислением результатов функций от исходных данных 
и результатов других функций, и не предполагает явного хранения состояния программы. 
Соответственно, не предполагает оно и изменяемость этого состояния._

_В функциональном программировании функции рассматриваются как "граждане первого класса",
что означает, что они могут быть привязаны к именам (включая локальные идентификаторы), 
передаваться в качестве аргументов и возвращаться из других функций, как и любой другой тип данных. 
Это позволяет писать программы в декларативном и составном стиле, 
где небольшие функции объединяются модульным образом._

Также полезно знать, что опытные функциональные программисты рассматривают свой код математически, 
что объединение чистых функций вместе похоже на объединение ряда алгебраических уравнений.

Когда пишется функциональный код, вы чувствуете себя математиком, и как только понимаете парадигму, 
то хотите писать только чистые функции, которые всегда возвращают _значения_, 
а не исключения или `null`, чтобы можно было комбинировать чистые функции вместе. 
Ощущение, что вы пишете математические уравнения (выражения), является движущим желанием, 
заставляющим использовать только чистые функции и неизменяемые значения - 
это то, что используется в алгебре и других формах математики.

Функциональное программирование - это большая тема, и нет простого способа сжать её всю в одну главу. 
В следующих разделах будет представлен обзор основных тем и показаны некоторые инструменты, 
предоставляемые Scala для написания функционального кода.


---

**Ссылки:**

- [Харрисон - Введение в функциональное программирование](https://nsu.ru/xmlui/bitstream/handle/nsu/8874/Harrison.pdf?sequence=1)
- [A bite of functional programming (in Scala)](https://scalac.io/blog/functional-programming-in-scala/)
- [Business benefits of functional programming and how to make it work for your company](https://www.youtube.com/watch?v=EcGZdzECmIk)
- [FP vs. OO List Processing](https://blog.cleancoder.com/uncle-bob/2018/12/17/FPvsOO-List-processing.html)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming For The Rest of Us](https://www.defmacro.org/2006/06/19/fp.html)
- [Functional Programming in Scala, Second Edition, Chapter 1-2](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [SKB – Scala Recursion](https://leobenkel.com/2020/10/skb-scala-recursion/)
- [Unleashing the Power: The Advantages of Functional Programming in the Digital Age](https://scalac.io/blog/unleashing-the-power-the-advantages-of-functional-programming-in-the-digital-age)
- [What's Functional Programming All About?](https://www.lihaoyi.com/post/WhatsFunctionalProgrammingAllAbout.html)
- [What is Functional Programming? - Alexandru Nedelcu](https://alexn.org/blog/2017/10/15/functional-programming)
- [What is functional programming and why it matters for your business?](https://scalac.io/blog/benefits-of-functional-programming/)
- [Wikipedia][Wikipedia]

[Wikipedia]: https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5
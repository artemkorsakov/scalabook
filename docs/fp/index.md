# Функциональное программирование

[Функциональное программирование - FP](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5) -
это способ написания программных приложений, использующих только чистые функции и неизменяемые значения. 
_Чистая функция_ означает, что вывод функции зависит только от ее входных параметров
и она не имеет _побочных эффектов_.
Функция имеет побочный эффект, если она делает что-то кроме простого возврата результата, например:

- Изменение переменной
- Изменение структуры данных на месте
- Установка поля на объекте
- Генерация исключения или остановка с ошибкой
- Печать на консоль или чтение пользовательского ввода
- Чтение или запись в файл
- Рисование на экране

Рассмотрим основное отличие [императивного программирования - IP](https://ru.wikipedia.org/wiki/%D0%98%D0%BC%D0%BF%D0%B5%D1%80%D0%B0%D1%82%D0%B8%D0%B2%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5) 
от FP на примере вычислении суммы коллекции чисел.

В IP подходе сумма чисел вычислялась бы так:

```dotty
def sum(ints: List[Int]): Int =
  var sum = 0
  for i <- ints do sum += i
  sum
sum(List(1, 2, 3))
// res0: Int = 6
```

Этот код изменяет поле `var` в цикле `for` - использует изменяемое состояние.

Подход FP может выглядеть так:

```dotty
def sum(xs: List[Int]): Int =
  xs match
    case Nil => 0
    case x :: tail => x + sum(tail)
sum(List(1, 2, 3))
// res2: Int = 6
```

В FP подходе не используется изменяемое состояние.

В FP на Scala следует придерживаться следующих правил:

- [не использовать `null`](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#nothing-and-null)
- использовать только чистые функции
- использовать только `val` переменные
- не использовать class-ы, инкапсулирующие данные и поведение
- использовать `if` только в качестве выражений, а не операторов

Более формальное определение чистой функции:

Функция `f` с типом ввода `A` и типом вывода `B` (записанная на Scala как один тип: `A => B`) — 
это вычисление, которое связывает каждое значение `a` типа `A` ровно с одним значением `b` типа `B`, 
которое определено исключительно по значению `a`. 
Любое изменение состояния внутреннего или внешнего процесса не имеет отношения к вычислению результата `f(a)`.

## Функция высшего порядка

[Функция высшего порядка (_Higher-Order Function_ - HOF)](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%B8_%D0%BF%D0%B5%D1%80%D0%B2%D0%BE%D0%B3%D0%BE_%D0%BA%D0%BB%D0%B0%D1%81%D1%81%D0%B0)
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
// res3: String = "The absolute value of -42 is 42."
formatResult("factorial", 7, factorial)  
// res4: String = "The factorial of 7 is 5040." 
```

## Анонимная функция

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
// res5: Int = 1
```

Синтаксис `(x: Int) => x == 9` представляет собой литерал функции или анонимную функцию.

## Каррирование

[Каррированием](https://ru.wikipedia.org/wiki/%D0%9A%D0%B0%D1%80%D1%80%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5) 
называется преобразование функции от многих аргументов в набор функций, каждая из которых является функцией от одного аргумента.

Примеры каррирования, раскаррирования и композиции:

```dotty
def curry[A, B, C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)

def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)

def compose[A, B, C](f: B => C, g: A => B): A => C =
  a => f(g(a))
```

## Композиция функций

Композиция двух функций `f` и `g` — это функция `h` такая, что `h(x) = f(g(x))`. 

```dotty
type A
type B
type C

def f: A => B = ???
def g: B => C = ???
def h: A => C = f andThen g
```

Функциональная композиция берет функцию типа `A => B`, функцию типа `B => C` и возвращает функцию типа `A => C`.
Функциональная композиция является ассоциативной операцией. 
Имеем: `((f andThen g) andThen h) = f andThen (g andThen h)` для всех функций `f`, `g` и `h`. 
Соответственно, нет необходимости ставить скобки при записи последовательностей композиций.

---

**Ссылки:**

- [Харрисон - Введение в функциональное программирование](https://nsu.ru/xmlui/bitstream/handle/nsu/8874/Harrison.pdf?sequence=1)
- [A bite of functional programming (in Scala)](https://scalac.io/blog/functional-programming-in-scala/)
- [Business benefits of functional programming and how to make it work for your company](https://www.youtube.com/watch?v=EcGZdzECmIk)
- [FP vs. OO List Processing](https://blog.cleancoder.com/uncle-bob/2018/12/17/FPvsOO-List-processing.html)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming For The Rest of Us](https://www.defmacro.org/2006/06/19/fp.html)
- [Functional Programming in Scala, Second Edition, Chapter 1-2](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler, 2.1 Numbers](https://www.amazon.com/Introduction-functional-programming-Prentice-international/dp/0134841891)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [Unleashing the Power: The Advantages of Functional Programming in the Digital Age](https://scalac.io/blog/unleashing-the-power-the-advantages-of-functional-programming-in-the-digital-age)
- [What's Functional Programming All About?](https://www.lihaoyi.com/post/WhatsFunctionalProgrammingAllAbout.html)
- [What is Functional Programming? - Alexandru Nedelcu](https://alexn.org/blog/2017/10/15/functional-programming)
- [What is functional programming and why it matters for your business?](https://scalac.io/blog/benefits-of-functional-programming/)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5)

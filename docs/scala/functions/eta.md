# Eta расширение

Если посмотреть на [Scaladoc для метода `map`](https://scala-lang.org/api/3.x/scala/collection/immutable/List.html#map-fffff812) 
в классах коллекций Scala, то можно увидеть, что метод определен для приема функции:

```scala
def map[B](f: (A) => B): List[B]
           -----------
```

Действительно, в Scaladoc сказано: "`f` — это _функция_, применяемая к каждому элементу". 
Но, несмотря на это, каким-то образом в `map` можно передать _метод_, и он все еще работает:

```scala
def times10(i: Int) = i * 10
List(1, 2, 3).map(times10)
// res0: List[Int] = List(10, 20, 30)
```

Как это работает? Как можно передать метод в `map`, который ожидает функцию?

Технология, стоящая за этим, известна как Eta Expansion. 
Она преобразует выражение типа метода в эквивалентное выражение типа функции, и делает это легко и незаметно.

### Различия между методами и функциями

Исторически _методы_ были частью определения класса, хотя в Scala 3 методы могут быть вне классов, 
такие как [определения верхнего уровня](https://scalabook.gitflic.space/docs/scala/toplevel-definitions) 
и [методы расширения](https://scalabook.gitflic.space/docs/scala/methods/extension-methods).

В отличие от методов, _функции_ сами по себе являются полноценными объектами, что делает их 
[объектами первого класса](https://ru.wikipedia.org/wiki/%D0%9E%D0%B1%D1%8A%D0%B5%D0%BA%D1%82_%D0%BF%D0%B5%D1%80%D0%B2%D0%BE%D0%B3%D0%BE_%D0%BA%D0%BB%D0%B0%D1%81%D1%81%D0%B0).

Их синтаксис также отличается. 
В этом примере показано, как задать метод и функцию, которые выполняют одну и ту же задачу, 
определяя, является ли заданное целое число четным:

```scala
def isEvenMethod(i: Int) = i % 2 == 0         // метод
val isEvenFunction = (i: Int) => i % 2 == 0   // функция
```

Функция действительно является объектом, поэтому ее можно использовать так же, 
как и любую другую переменную, например, помещая в список:

```scala
val functions = List(isEvenFunction)
```

И наоборот, технически метод не является объектом, поэтому в Scala 2 метод нельзя было поместить в список, 
по крайней мере, напрямую, как показано в этом примере:

```scala
// В этом примере показано сообщение об ошибке в Scala 2
val methods = List(isEvenMethod)
^
error: missing argument list for method isEvenMethod
  Unapplied methods are only converted to functions when a function type is expected.
  You can make this conversion explicit by writing `isEvenMethod _` or `isEvenMethod(_)` instead of `isEvenMethod`.
```

Как показано в этом сообщении об ошибке, в Scala 2 существует ручной способ преобразования метода в функцию, 
но важной частью для Scala 3 является то, что технология Eta Expansion улучшена, 
поэтому теперь, когда попытаться использовать метод в качестве переменной, 
он просто работает — не нужно самостоятельно выполнять ручное преобразование:

```scala
val functions = List(isEvenFunction)  
val methods = List(isEvenMethod)    
```

Важно отметить следующее:

- Eta Expansion — технология Scala, позволяющая использовать методы так же, как и функции
- Технология была улучшена в Scala 3, чтобы быть почти полностью бесшовной


---

**Ссылки:**

- [Scala3 book](https://docs.scala-lang.org/scala3/book/fun-eta-expansion.html)

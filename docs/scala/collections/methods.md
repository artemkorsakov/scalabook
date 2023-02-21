# Методы в коллекциях

Важным преимуществом коллекций Scala является то, что они поставляются с десятками методов "из коробки", 
которые доступны для неизменяемых и изменяемых типов коллекций. 
Больше нет необходимости писать пользовательские циклы `for` каждый раз, когда нужно работать с коллекцией. 
При переходе от одного проекта к другому, можно обнаружить, что используются одни и те же методы.

В коллекциях доступны десятки методов, поэтому здесь показаны не все из них. 
Показаны только некоторые из наиболее часто используемых методов, в том числе:
- `map`
- `filter`
- `foreach`
- `head`
- `tail`
- `take`, `takeWhile`
- `drop`, `dropWhile`
- `reduce`

Следующие методы работают со всеми типами последовательностей, включая `List`, `Vector`, `ArrayBuffer` и т. д.. 
Примеры рассмотрены на `List`-е, если не указано иное.

> Важно напомнить, что ни один из методов в `List` не изменяет список. 
> Все они работают в функциональном стиле, то есть возвращают новую коллекцию с измененными результатами.


### Примеры распространенных методов

Для общего представления в примерах ниже показаны некоторые из наиболее часто используемых методов коллекций. 
Вот несколько методов, которые не используют лямбда-выражения:

```scala
val a = List(10, 20, 30, 40, 10)
// a: List[Int] = List(10, 20, 30, 40, 10)
a.distinct                      
// res0: List[Int] = List(10, 20, 30, 40)                      
a.drop(2)                
// res1: List[Int] = List(30, 40, 10)                
a.dropRight(2)             
// res2: List[Int] = List(10, 20, 30)             
a.head                            
// res3: Int = 10                            
a.headOption                      
// res4: Option[Int] = Some(value = 10)                      
a.init                       
// res5: List[Int] = List(10, 20, 30, 40)                       
a.intersect(List(19, 20, 21))  
// res6: List[Int] = List(20)  
a.last                       
// res7: Int = 10                       
a.lastOption                 
// res8: Option[Int] = Some(value = 10)                 
a.slice(2, 4)                 
// res9: List[Int] = List(30, 40)                 
a.tail                       
// res10: List[Int] = List(20, 30, 40, 10)                       
a.take(3)                    
// res11: List[Int] = List(10, 20, 30)                    
a.takeRight(2)               
// res12: List[Int] = List(40, 10)               
```

#### Функции высшего порядка и лямбда-выражения

Далее будут показаны некоторые часто используемые функции высшего порядка (HOF), 
которые принимают лямбды (анонимные функции). 
Для начала приведем несколько вариантов лямбда-синтаксиса, начиная с самой длинной формы, 
поэтапно переходящей к наиболее сжатой:

```scala
a.filter((i: Int) => i < 25)
// res13: List[Int] = List(10, 20, 10)
a.filter((i) => i < 25)     
// res14: List[Int] = List(10, 20, 10)     
a.filter(i => i < 25)       
// res15: List[Int] = List(10, 20, 10)       
a.filter(_ < 25)            
// res16: List[Int] = List(10, 20, 10)           
```

В этих примерах:

1. Первый пример показывает самую длинную форму. Такое многословие требуется редко, только в самых сложных случаях.
2. Компилятор знает, что `a` содержит `Int`, поэтому нет необходимости повторять это в функции.
3. Если в функции только один параметр, например `i`, то скобки не нужны.
4. В случае одного параметра, если он появляется в анонимной функции только раз, его можно заменить на `_`.

В главе [Анонимные функции](../functions/anonymous) представлена более подробная информация 
и примеры правил, связанных с сокращением лямбда-выражений. 

Примеры других HOF, использующих краткий лямбда-синтаксис:

```scala
a.dropWhile(_ < 25)
// res17: List[Int] = List(30, 40, 10)
a.filter(_ > 35)  
// res18: List[Int] = List(40)  
a.filterNot(_ < 25)
// res19: List[Int] = List(30, 40)
a.find(_ > 20)     
// res20: Option[Int] = Some(value = 30)     
a.takeWhile(_ < 30)
// res21: List[Int] = List(10, 20)
```

Важно отметить, что HOF также принимают в качестве параметров методы и функции, а не только лямбда-выражения. 
Вот несколько примеров, в которых используется метод с именем `double`. 
Снова показаны несколько вариантов лямбда-выражений:

```scala
def double(i: Int) = i * 2
a.map(i => double(i))
// res22: List[Int] = List(20, 40, 60, 80, 20)
a.map(double(_))
// res23: List[Int] = List(20, 40, 60, 80, 20)
a.map(double)                        
// res24: List[Int] = List(20, 40, 60, 80, 20)                       
```

В последнем примере, когда анонимная функция состоит из одного вызова функции, принимающей один аргумент, 
нет необходимости указывать имя аргумента, поэтому даже `_` не требуется. 

Наконец, HOF можно комбинировать:

```scala
a.filter(_ < 40)
 .takeWhile(_ < 30)
 .map(_ * 10)                        
// res25: List[Int] = List(100, 200)                      
```

> **P.S.** Пример призван показать только то, как принято последовательно вызывать функции на неизменяемых коллекциях.
Его недостаток в том, что обход коллекции происходит целых три раза.

### Пример данных

В следующих разделах используются такие списки:

```scala
val oneToTen = (1 to 10).toList
// oneToTen: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
val names = List("adam", "brandy", "chris", "david")                         
// names: List[String] = List("adam", "brandy", "chris", "david")                     
```

### map

Метод `map` проходит через каждый элемент в списке, применяя переданную функцию к элементу, 
по одному за раз; затем возвращается новый список с измененными элементами.

Вот пример применения метода `map` к списку `oneToTen`:

```scala
val doubles = oneToTen.map(_ * 2)    
// doubles: List[Int] = List(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)                     
```

Также можно писать анонимные функции, используя более длинную форму, например:

```scala
val doubles = oneToTen.map(i => i * 2)    
// doubles: List[Int] = List(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)                   
```

Однако в этом документе будет всегда использоваться первая, более короткая форма.

Вот еще несколько примеров применения метода `map` к `oneToTen` и `names`:

```scala
val capNames = names.map(_.capitalize)
// capNames: List[String] = List("Adam", "Brandy", "Chris", "David")
val nameLengthsMap = names.map(s => (s, s.length)).toMap
// nameLengthsMap: Map[String, Int] = Map("adam" -> 4, "brandy" -> 6, "chris" -> 5, "david" -> 5)
val isLessThanFive = oneToTen.map(_ < 5)                       
// isLessThanFive: List[Boolean] = List(true, true, true, true, false, false, false, false, false, false)                      
```

Как показано в последних двух примерах, совершенно законно (и распространено) использование `map` 
для возврата коллекции, которая имеет тип, отличный от исходного типа.

### filter

Метод `filter` создает новый список, содержащий только те элементы, которые удовлетворяют предоставленному предикату. 
Предикат или условие — это функция, которая возвращает `Boolean` (`true` или `false`). 
Вот несколько примеров:

```scala
val lessThanFive = oneToTen.filter(_ < 5)
// lessThanFive: List[Int] = List(1, 2, 3, 4)
val evens = oneToTen.filter(_ % 2 == 0)
// evens: List[Int] = List(2, 4, 6, 8, 10)
val shortNames = names.filter(_.length <= 4)                         
// shortNames: List[String] = List("adam")                        
```

Отличительной особенностью функциональных методов коллекций является то, 
что их можно объединять вместе для решения задач. 
Например, в этом примере показано, как связать `filter` и `map`:

```scala
oneToTen.filter(_ < 4).map(_ * 10)
// res26: List[Int] = List(10, 20, 30)
```

> Если `filter` используется перед `map`, `flatMap` или `foreach`, 
> то для лучшей производительности вместо него должен использоваться `withFilter`, 
> например, `oneToTen.withFilter(_ < 4).map(_ * 10)`

### foreach

Метод `foreach` используется для перебора всех элементов коллекции. 
Стоит обратить внимание, что `foreach` используется для побочных эффектов, таких как печать информации. 
Вот пример с `names`:

```scala
names.foreach(println)                         
// adam
// brandy
// chris
// david                        
```

### head

Метод `head` взят из Lisp и других более ранних языков функционального программирования. 
Он используется для доступа к первому элементу (головному (head) элементу) списка:

```scala
oneToTen.head 
// res28: Int = 1 
names.head                        
// res29: String = "adam"                       
```

`String` можно рассматривать как последовательность символов, 
т.е. строка также является коллекцией, а значит содержит соответствующие методы. 
Вот как `head` работает со строками:

```scala
"foo".head 
// res30: Char = 'f' 
"bar".head                    
// res31: Char = 'b'                    
```

На пустой коллекции `head` выдает исключение:

```scala
val emptyList = List[Int]()
// emptyList: List[Int] = List()
```
```scala
emptyList.head                         
// java.util.NoSuchElementException: head of empty list
// 	at scala.collection.immutable.Nil$.head(List.scala:662)
// 	at scala.collection.immutable.Nil$.head(List.scala:661)
// 	at repl.MdocSession$App.$init$$$anonfun$17(methods.md:252)                        
```

Чтобы не натыкаться на исключение вместо `head` желательно использовать `headOption`,
особенно при разработке в функциональном стиле:

```scala
emptyList.headOption
// res32: Option[Int] = None                        
```

`headOption` не генерирует исключение, а возвращает тип `Option` со значением `None`. 
Более подробно о функциональном стиле программирования будет рассказано 
в [соответствующей главе](../fp).

### tail

Метод `tail` также взят из Lisp и используется для вывода всех элементов в списке после `head`. 

```scala
oneToTen.head
// res33: Int = 1
oneToTen.tail
// res34: List[Int] = List(2, 3, 4, 5, 6, 7, 8, 9, 10)

names.head   
// res35: String = "adam"   
names.tail                 
// res36: List[String] = List("brandy", "chris", "david")                 
```

Так же, как и `head`, `tail` можно использовать со строками:

```scala
"foo".tail
// res37: String = "oo"
"bar".tail
// res38: String = "ar"
```

`tail` выбрасывает исключение `java.lang.UnsupportedOperationException`, если список пуст, 
поэтому, как и в случае с `head` и `headOption`, существует также метод `tailOption`, 
который предпочтительнее в функциональном программировании.

Список матчится, поэтому можно использовать такие выражения:

```scala
val x :: xs = names
// x: String = "adam"
// xs: List[String] = List("brandy", "chris", "david")
```

`x` - это `head` списка, а `xs` - `tail`.

Подобный pattern matching полезен во многих случаях, например, 
при написании метода суммирования с использованием рекурсии:

```scala
def sum(list: List[Int]): Int = list match
  case Nil => 0
  case x :: xs => x + sum(xs)
```

### take, takeRight, takeWhile

Методы `take`, `takeRight` и `takeWhile` предоставляют удобный способ "брать" (take) элементы из списка 
для создания нового. Примеры `take` и `takeRight`:

```scala
oneToTen.take(1)      
// res39: List[Int] = List(1)      
oneToTen.take(2)      
// res40: List[Int] = List(1, 2)      

oneToTen.takeRight(1) 
// res41: List[Int] = List(10) 
oneToTen.takeRight(2)                        
// res42: List[Int] = List(9, 10)                       
```

Обратите внимание, как эти методы работают с «пограничными» случаями, 
когда запрашивается больше элементов, чем есть в последовательности, или запрашивается ноль элементов:

```scala
oneToTen.take(Int.MaxValue)     
// res43: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)     
oneToTen.takeRight(Int.MaxValue)
// res44: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
oneToTen.take(0)                
// res45: List[Int] = List()                
oneToTen.takeRight(0)           
// res46: List[Int] = List()          
```

А это `takeWhile`, который работает с функцией-предикатом:

```scala
oneToTen.takeWhile(_ < 5)    
// res47: List[Int] = List(1, 2, 3, 4)    
names.takeWhile(_.length < 5)
// res48: List[String] = List("adam")
```

### drop, dropRight, dropWhile

`drop`, `dropRight` и `dropWhile` удаляют элементы из списка и, по сути, противоположны своим аналогам "take". 
Вот некоторые примеры:

```scala
oneToTen.drop(1)     
// res49: List[Int] = List(2, 3, 4, 5, 6, 7, 8, 9, 10)     
oneToTen.drop(5)     
// res50: List[Int] = List(6, 7, 8, 9, 10)     

oneToTen.dropRight(8)
// res51: List[Int] = List(1, 2)
oneToTen.dropRight(7)
// res52: List[Int] = List(1, 2, 3)
```

Пограничные случаи:

```scala
oneToTen.drop(Int.MaxValue)     
// res53: List[Int] = List()     
oneToTen.dropRight(Int.MaxValue)
// res54: List[Int] = List()
oneToTen.drop(0)                
// res55: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)                
oneToTen.dropRight(0)           
// res56: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)         
```

А это `dropWhile`, который работает с функцией-предикатом:

```scala
oneToTen.dropWhile(_ < 5)    
// res57: List[Int] = List(5, 6, 7, 8, 9, 10)    
names.dropWhile(_ != "chris")
// res58: List[String] = List("chris", "david")
```

### reduce

Метод `reduce` позволяет свертывать коллекцию до одного агрегируемого значения. 
Он принимает функцию (или анонимную функцию) и последовательно применяет эту функцию к элементам в списке.

Лучший способ объяснить `reduce` — создать небольшой вспомогательный метод. 
Например, метод `add`, который складывает вместе два целых числа, 
а также предоставляет хороший вывод отладочной информации:

```scala
def add(x: Int, y: Int): Int =
  val theSum = x + y
  println(s"received $x and $y, their sum is $theSum")
  theSum                          
```

Рассмотрим список:

```scala
val a = List(1,2,3,4)                         
```

вот что происходит, когда в `reduce` передается метод `add`:

```scala
a.reduce(add)                        
// received 1 and 2, their sum is 3
// received 3 and 3, their sum is 6
// received 6 and 4, their sum is 10
// res59: Int = 10                        
```

Как видно из результата, функция `reduce` использует `add` для сокращения списка `a` до единственного значения, 
в данном случае — суммы всех чисел в списке.

`reduce` можно использовать с анонимными функциями:

```scala
a.reduce(_ + _)     
// res60: Int = 10                   
```

Аналогично можно использовать другие функции, например, умножение:

```scala
a.reduce(_ * _)  
// res61: Int = 24                    
```

### Дальнейшее изучение коллекций

В коллекциях Scala есть десятки дополнительных методов, которые избавляют от необходимости писать еще один цикл `for`. 
Более подробную информацию о коллекциях Scala см. в разделе 
[Изменяемые и неизменяемые коллекции][collections-2.13] 
и [Архитектура коллекций Scala][architecture-of-scala-213-collections].
А также в [API][api].

> В качестве последнего примечания, при использовании Java-кода в проекте Scala, 
> коллекции Java можно преобразовать в коллекции Scala. 
> После этого, их можно использовать в выражениях `for`, 
> а также воспользоваться преимуществами методов функциональных коллекций Scala. 
> Более подробную информацию можно найти в разделе [Взаимодействие с Java](../with_java).


---

**Ссылки:**

- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Methods](https://docs.scala-lang.org/scala3/book/collections-methods.html)
- [Изменяемые и неизменяемые коллекции][collections-2.13]
- [Архитектура коллекций Scala][architecture-of-scala-213-collections]
- [collections API][api]

[collections-2.13]: https://docs.scala-lang.org/ru/overviews/collections-2.13/overview.html
[architecture-of-scala-213-collections]: https://docs.scala-lang.org/overviews/core/architecture-of-scala-213-collections.html
[api]: https://scala-lang.org/api/3.x/scala/collection.html
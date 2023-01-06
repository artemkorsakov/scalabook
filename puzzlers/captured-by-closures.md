# Captured by Closures

```scala
val funcs1 = collection.mutable.Buffer[() => Int]()
val funcs2 = collection.mutable.Buffer[() => Int]()

{
  val values = Seq(100, 110, 120)
  var j = 0
  for i <- values.indices
  do  
    funcs1 += (() => values(i))
    funcs2 += (() => values(j))
    j += 1
}
```

```scala
funcs1 foreach { f1 => println(f1()) }
```

```scala
funcs2 foreach { f2 => println(f2()) }
// java.lang.IndexOutOfBoundsException: 3
// 	at scala.collection.LinearSeqOps.apply(LinearSeq.scala:117)
//  ...
```

Когда `var` используется вместо `val`, функции закрываются по переменной, а не по значению. 
Поскольку `i` определен в `for-comprehension`, он определяется как `val`. 
Это означает, что каждый раз, когда `i` сохраняется где-то, его значение копируется, 
поэтому печатается ожидаемый результат:

```
100
110
120
```

При изменении `j` внутри цикла все три замыкания «видят» одну и ту же переменную `j`, а не ее копию. 
Поэтому после окончания цикла, когда `j` равно 3, выдается исключение `IndexOutOfBoundsException`. 

Исключения можно избежать, «зафиксировав» возвращаемое значение:

```scala
val funcs1 = collection.mutable.Buffer[() => Int]()
val funcs2 = collection.mutable.Buffer[() => Int]()

{
  val values = Seq(100, 110, 120)
  var j = 0
  for i <- values.indices
  do
    funcs1 += (() => values(i))
    val value = values(j)
    funcs2 += (() => value)
    j += 1
}
```

```scala
funcs1 foreach { f1 => println(f1()) }
// 100
// 110
// 120
funcs2 foreach { f2 => println(f2()) }
// 100
// 110
// 120
```

Присвоение значения выполняется немедленно и, таким образом, «захватывает» предполагаемый элемент значений.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-008)

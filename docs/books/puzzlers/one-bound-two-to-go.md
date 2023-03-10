# One Bound, Two to Go

```scala
def invert(v3: Int)(v2: Int = 2, v1: Int = 1): Unit =
  println(s"$v1, $v2, $v3")
  
def invert3 = invert(3) _

invert3(v1 = 2, v2 = 1)
// 1, 2, 3
```

```scala
invert3(v1 = 2)
// error:
// missing argument for parameter v2 of method apply in trait Function2: (v1: Int, v2: Int): Unit
// invert3(v1 = 2)
// ^^^^^^^^^^^^^^^
```

Тип `invert3` после eta-расширения (SLS §6.26.5) больше не метод, 
а функциональный объект, то есть экземпляр `Function2[Int, Int, Unit]`. 
Об этом также сообщает REPL: 

```text
scala> def invert3 = invert(3) _
def invert3: (Int, Int) => Unit
```

Этот функциональный объект имеет метод `apply` (унаследованный от `Function2[T1, T2, R]`) 
со следующей сигнатурой: `def apply (v1: T1, v2: T2): R` 
В частности, этот метод не определяет значений по умолчанию для своих аргументов! 

Как следствие, `invert3(v1 = 2)` приводит к ошибке времени компиляции 
(поскольку не хватает фактических аргументов для применения метода). 
Имена аргументов `v1` и `v2` — это имена, определенные в методе применения `Function2[T1, T2, R]`. 
Имена аргументов метода `apply` каждой функции с двумя аргументами имеют имена `v1` и `v2`, 
в частности эти имена никак не связаны с именами аргументов метода `invert3`. 
У метода `invert3` случайно есть аргументы с теми же именами, но, к сожалению, в другом порядке. 
`invert3(v1 = 2, v2 = 1)` печатает` 1, 2, 3`, 
так как параметр `v1` (соответствующий параметру `v2` в методе `invert`) имеет значение `2`, 
а параметр `v2` (соответствующий параметру `v1` в методе `invert`) равен `1`.


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-020)

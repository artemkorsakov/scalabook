# Implicitly Surprising

```scala
given z1: Int = 2

def addTo(n: Int) =
def add(x: Int)(y: Int)(using z: Int) = x + y + z
add(n) _

given z2: Int = 3
val addTo1 = addTo(1)
addTo1(2)
// val res0: Int = 5
addTo1(2)(3)
-- [E050] Type Error: ----------------------------------------------------------
1 |addTo1(2)(3)
|^^^^^^^^^
|method apply in trait Function1 does not take more parameters
|
| longer explanation available when compiling with `-explain`
1 error found
```

Когда eta расширение применяется к методу `add`, результатом является функция типа `Int => Int`, 
т. е. неявные параметры разрешаются до применения eta расширения. 
Поэтому неявное значение `z1 = 2` используется как значение неявного параметра `z`. 
Если бы неявный параметр был недоступен, компилятор выдал бы сообщение об ошибке: 

```scala
scala> def add(x: Int)(y: Int)(using z: Int) = x + y + z
def add(x: Int)(y: Int)(using z: Int): Int

scala> add(1) _
-- Error: ----------------------------------------------------------------------
1 |add(1) _
  |      ^
  |     no given instance of type Int was found for parameter z of method add
1 error found
```

Можно также указать явное значение для `z`, если в текущем контексте не определено неявное значение, 
но тогда тип должен быть указан в заполнителе. 

```scala
scala> val addTo1And3 = add(1)(_: Int)(using 3)
val addTo1And3: Int => Int = Lambda$9137/1728074700@1bd1b5e0
```

Это объясняет результат, т.е. вызов `addTo1(2)` возвращает `5 (1 + <param> + 2)`.
`addTo1(2)(3)` не компилируется, так как на объекте `5` (результат `addTo1(2)`) не определен метод `apply`. 

```text
-- [E050] Type Error: ----------------------------------------------------------
1 |addTo1(2)(3)
  |^^^^^^^^^
  |method apply in trait Function1 does not take more parameters
  |
  | longer explanation available when compiling with `-explain`
1 error found
```


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-019)

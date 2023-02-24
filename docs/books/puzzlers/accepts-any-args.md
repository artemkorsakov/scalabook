# Accepts Any Args

```scala
def acceptsAnyArgs(any1: Any)(any2: Any*): Unit =
  println(any1)
  println(any2)

acceptsAnyArgs("Psst", "hey", "world:")(4, 2)
// (Psst,hey,world:)
// ArraySeq(4, 2)
```

Возможно, это не широко известно и не разглашается, но Scala поддерживает "автоматическую корректировку" аргументов. 
Чтобы соответствовать требованию первого аргумента типа `Any`, компилятор преобразует вызов в:

```text
... 
acceptsAnyArgs (( "Psst" , "hey" , "world:" ))( 4 , 2 )   
```


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-039)

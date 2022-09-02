---
layout: puzzlers
title: "Accepts Any Args"
section: puzzlers
prev: a-view-to-a-shill
next: the-devil-is-in-the-defaults
---

## {{page.title}}

```scala mdoc
def acceptsAnyArgs(any1: Any)(any2: Any*): Unit =
  println(any1)
  println(any2)

acceptsAnyArgs("Psst", "hey", "world:")(4, 2)
```

Возможно, это не широко известно и не разглашается, но Scala поддерживает "автоматическую корректировку" аргументов. 
Чтобы соответствовать требованию первого аргумента типа `Any`, компилятор преобразует вызов в:

```text
... 
acceptsAnyArgs (( "Psst" , "hey" , "world:" ))( 4 , 2 )   
```


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-039)

---
layout: puzzlers
title: "Location, Location, Location"
section: puzzlers
prev: upstairs
next: now-you-see-me
---

## {{page.title}}

```scala mdoc
trait A:
  val audience: String
  println(s"Hello $audience")

class BMember(a: String = "World") extends A:
  val audience = a
  println(s"I repeat: Hello $audience")

class BConstructor(val audience: String = "World") extends A:
  println(s"I repeat: Hello $audience")

BMember("Readers")
BConstructor("Readers")
```

При создании экземпляров суперклассов или трейтов 
родительский конструктор можно визуализировать как выполняемый
перед первой строкой дочернего конструктора, но после определения класса.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-004)

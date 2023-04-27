# Одиночка

#### Назначение

Убедиться, что у класса есть только один экземпляр, и обеспечить глобальную точку доступа к нему.

#### Диаграмма

![Singleton](https://upload.wikimedia.org/wikipedia/commons/d/d7/Singleton_classdia.png)

#### Пример

Scala позволяет создавать одноэлементные объекты, используя ключевое слово `object`. 
По сути, одноэлементный объект создается автоматически при первом использовании, 
и всегда существует только один экземпляр.

```scala
object Singleton
val s = Singleton
```

В следующей одноэлементной реализации используется сопутствующий объект. 
Эта реализация актуальна только в том случае, если нам нужно иметь возможность усовершенствовать синглтон. 
В противном случае предпочтительнее краткое решение с использованием одноэлементного объекта (`object`).

```scala
class Singleton private () // private constructor

object Singleton:
  private val instance: Singleton = new Singleton
  def getInstance() = instance

val s = Singleton.getInstance()
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Singleton_pattern)

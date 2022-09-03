---
layout: dp
title: "Prototype"
section: dp
prev: creational/builder
next: creational/singleton
---

## {{page.title}}

#### Назначение

Задаёт виды создаваемых объектов с помощью экземпляра-прототипа 
и создаёт новые объекты путём копирования этого прототипа. 
Паттерн позволяет уйти от реализации и позволяет следовать принципу "программирование через интерфейсы". 
В качестве возвращающего типа указывается интерфейс/абстрактный класс на вершине иерархии, 
а классы-наследники могут подставить туда наследника, реализующего этот тип.

Проще говоря, это паттерн создания объекта через клонирование другого объекта вместо создания через конструктор.

#### Использование

Паттерн используется чтобы:
- избежать дополнительных усилий по созданию объекта стандартным путём 
(имеется в виду использование конструктора, 
так как в этом случае также будут вызваны конструкторы всей иерархии предков объекта), 
когда это непозволительно дорого для приложения.
- избежать наследования создателя объекта (object creator) в клиентском приложении, 
как это делает паттерн abstract factory.

Используйте этот шаблон проектирования, 
когда приложению безразлично как именно в ней создаются, компонуются и представляются продукты:
- инстанцируемые классы определяются во время выполнения, например с помощью динамической загрузки;
- избежать построения иерархий классов или фабрик, параллельных иерархии классов продуктов;
- экземпляры класса могут находиться в одном из нескольких различных состояний. 
Может оказаться удобнее установить соответствующее число прототипов и клонировать их, 
а не инстанцировать каждый раз класс вручную в подходящем состоянии.

#### Диаграмма

![Prototype](https://upload.wikimedia.org/wikipedia/ru/2/25/Prototype.gif)

#### Пример

```scala mdoc
class A(var state: Int)
extension (a: A) def copy = new A(a.state)
```

Использование паттерна фабричный метод:

```scala mdoc
def a = new A(2)
println(a.state)
def aCopy = a.copy
println(a.state)
```


---

**References:**
- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D1%82%D0%BE%D1%82%D0%B8%D0%BF_(%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F))
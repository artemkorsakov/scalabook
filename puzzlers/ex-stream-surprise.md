---
layout: puzzlers
title: "(Ex)Stream Surprise"
section: puzzlers
prev: heads-you-win
next: a-matter-of-context
---

## {{page.title}}

```scala mdoc:silent
val nats: LazyList[Int] = 1 #:: (nats map { _ + 1 })
val odds: LazyList[Int] = 1 #:: (odds map { _ + 1 } filter { _ % 2 != 0 })
```

```scala mdoc
nats filter { _ % 2 != 0 } take 2 foreach println
```

```scala mdoc:crash
odds take 2 foreach println
```

Выражение `nats filter { _ % 2 != 0 }` создает новый ленивый список, 
который извлекает элементы из `nats` и передает только те, которые соответствуют фильтру. 
Таким образом, вторым элементом отфильтрованного ленивого списка является третий элемент `nats`, а именно значение `3`. 
Последовательные элементы `nats` всегда можно вычислить, 
потому что следующий элемент `nats` — это просто текущий элемент плюс `1`. 
Однако в случае ленивого списка `odds` фильтр является частью рекурсивного определения ленивого списка. 
Это означает, что единственными значениями, которые могут быть возвращены как элементы `odds`, 
являются те, которые проходят фильтр. 
Это не позволяет ленивому списку вычислять свой второй элемент, 
то есть первый элемент `odds map { _ + 1 } filter { _ % 2 != 0 }`. 
Первая попытка с использованием `1` (первый элемент коэффициента) не проходит фильтр. 
Следующая попытка пытается использовать второй элемент `odds`, 
но это именно то значение, которое мы пытаемся вычислить. 
Таким образом, попытка вычислить второй элемент `odds` заканчивается рекурсивным вызовом самого себя, 
что приводит к `self-referential LazyList or a derivation thereof has no more elements`.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-063)
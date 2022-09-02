---
layout: puzzlers
title: "The Missing List"
section: puzzlers
prev: now-you-see-me
next: arg-arrgh
---

## {{page.title}}

```scala mdoc
def sumSizes(collections: Iterable[Iterable[_]]): Int = 
  collections.map(_.iterator.size).sum

sumSizes(List(Set(1, 2), List(3, 4)))
sumSizes(Set(List(1, 2), Set(3, 4)))
```

Несмотря на то, что `collections.map` может сопоставлять итерируемый объект с другим «хорошим» итерируемым объектом, 
поскольку при редизайне коллекций тип возвращаемого итерируемого объекта 
будет (обычно) соответствовать типу ввода. 
Что для `Set` означает... никаких дубликатов. 
И да, `foldLeft`, очевидно, был бы гораздо лучшим способом сделать это.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-006)

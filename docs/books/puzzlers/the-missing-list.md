# The Missing List

```scala
def sumSizes(collections: Iterable[Iterable[_]]): Int = 
  collections.map(_.iterator.size).sum

sumSizes(List(Set(1, 2), List(3, 4)))
// res0: Int = 4
sumSizes(Set(List(1, 2), Set(3, 4)))
// res1: Int = 2
```

Несмотря на то, что `collections.map` может сопоставлять итерируемый объект с другим «хорошим» итерируемым объектом, 
поскольку при редизайне коллекций тип возвращаемого итерируемого объекта 
будет (обычно) соответствовать типу ввода. 
Что для `Set` означает... никаких дубликатов. 
И да, `foldLeft`, очевидно, был бы гораздо лучшим способом сделать это.


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-006)

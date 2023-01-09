# Heads You Win...

```scala
import java.util.{List as JList, LinkedList}
import scala.jdk.CollectionConverters.*

def listFromJava: JList[Int] =
  val jlist = new java.util.LinkedList[Int]()
  jlist.add(1)
  jlist.add(2)
  jlist

def printHeadOrEmpty(s: collection.Seq[_]): Unit =
  s match
    case hd :: _ => println(hd)
    case _       => println("Empty :-(")

printHeadOrEmpty(listFromJava.asScala)
// Empty :-(
printHeadOrEmpty(listFromJava.asScala.toSeq)
// 1
```

Scala `CollectionConverters` превращает списки Java 
в экземпляры `collection.mutable.Buffer`, поскольку списки Java изменяемы. 
Сопоставление шаблона `hd :: _`, с другой стороны, является шаблоном конструктора, 
использующим case класс `::`, который является неизменяемым списком. 
Таким образом, первое совпадение не удается. 
Однако второе совпадение завершается успешно, так как `toSeq` возвращает неизменяемую последовательность. 
Чтобы сопоставить как неизменяемые, так и изменяемые последовательности, вместо этого используйте экстрактор `+:`.


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-061)

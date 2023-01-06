# Cast Away

```scala
import scala.jdk.CollectionConverters.*
def fromJava: java.util.Map[String, java.lang.Integer] = 
  val map = new java.util.HashMap[String, java.lang.Integer]()
  map.put("key", null)
  map

// watch out here...Integer is not Int!
val map = fromJava.asScala.asInstanceOf[scala.collection.Map[String, Int]]
println(map("key") == 0)
// true
```

```scala
println(map("key") == null)
// error:
// Values of types Int and Null cannot be compared with == or !=
// println(map("key") == null)
//         ^^^^^^^^^^^^^^^^^^
```

Мотивация для этой головоломки связана с взаимодействием с библиотеками Java, 
где может возникнуть соблазн «преобразовать» путем приведения типов в более «естественные» типы Scala. 
В данном конкретном случае — от `java.lang.Integer` к `Int` — 
типы, к сожалению, не совсем совпадают, и это, в конечном счете, источник головоломки. 
Scala автоматически обрабатывает преобразования, такие как `java.lang.Integer` в `Int`, используя автоупаковку. 
Если декомпилировать результирующий код с помощью декомпилятора Java, получим: 

```
Predef$.MODULE$.println(BoxesRunTime.boxToBoolean(BoxesRunTime.unboxToInt($outer.map().apply("key")) == 0));
Predef$.MODULE$.println(BoxesRunTime.boxToBoolean($outer.map().apply("key") == null));
```

Интересной частью здесь является `BoxesRunTime.unboxToInt` в первом вызове, 
который определяется как: 

```
public static int unboxToInt(Object i) {
  return i == null ? 0 : ((java.lang.Integer)i).intValue();
}
```

Эта логика, которая обрабатывает `null` как `0`, отличается как Java Integer-to-int unboxing, 
так и от scala.Int.unbox, которые оба вызывают исключение `NullPointerException` для `null`. 


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-028)

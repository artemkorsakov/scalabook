# A Case of Equality

```scala
trait SpyOnEquals:
  override def equals(x: Any) =
    println("DEBUG: In equals")
    super.equals(x)

case class CC()
case class CCSpy() extends SpyOnEquals

val cc1 = new CC() with SpyOnEquals
val cc2 = new CC() with SpyOnEquals
val ccspy1 = CCSpy()
val ccspy2 = CCSpy()

println(cc1 == cc2)
// DEBUG: In equals
// true
println(cc1.## == cc2.##) // null-safe hashCode()
// true // null-safe hashCode()
println(ccspy1 == ccspy2)
// DEBUG: In equals
// false
println(ccspy1.## == ccspy2.##)
// true
```

Для case классов генерируются методы `equals`, `hashCode` и `toString`, 
и для двух экземпляров case класса с одинаковыми элементами можно ожидать, 
что и `equals`, и `hashCode` вернут один и тот же результат. 
Однако в соответствии с SLS (§5.3.2) case класс неявно переопределяет методы 
`equals`, `hashCode` и `toString` класса `scala.AnyRef` только в том случае, 
если сам case класс не предоставляет определение для одного из этих методов 
и только если конкретное определение не дано в каком-либо базовом классе case класса (кроме `AnyRef`). 
В нашем примере у `CCSpy` базовый trait `SpyOnEquals` предоставляет метод `equals`, 
поэтому case класс не предоставляет собственного определения, 
и сравнение двух разных экземпляров возвращает `false`. 
Однако для метода `hashCode` не предусмотрено никакой реализации ни в классе `CCSpy`, 
ни в каком-либо базовом классе, 
поэтому здесь используется неявно переопределенная версия, 
которая возвращает одно и то же значение для равных элементов. 
Для case класса `CC` определение `equals` или `hashCode` не предоставляется, 
поэтому для обоих методов используются неявно переопределенные версии. 
Смешивание с `SpyOnEquals` при создании экземпляров классов на это не влияет.


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-011)

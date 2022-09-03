# Контекстные параметры по имени

Контекстные параметры также могут быть объявлены по имени, 
чтобы избежать использования предполагаемого расширения. 
Пример:

```scala
trait Codec[T]:
  def write(x: T): Unit

given intCodec: Codec[Int] =
  (x: Int) => println(s"$x has been written")

given optionCodec[T](using ev: => Codec[T]): Codec[Option[T]] with
  def write(xo: Option[T]) = xo match
    case Some(x) => ev.write(x)
    case None =>

val s = summon[Codec[Option[Int]]]

s.write(Some(33))
// 33 has been written
s.write(None)
```

Как и в случае [обычного параметра по имени](../methods/by-name-parameter), аргумент контекстного параметра `ev` оценивается по требованию. 
В приведенном выше примере, если значение параметра `xo` равно `None`, оно вообще не вычисляется.


---

**References:**
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/contextual/by-name-context-parameters.html)

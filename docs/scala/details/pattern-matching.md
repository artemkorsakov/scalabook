# Сопоставление с образцом

### Экстракторы

Экстракторы — это объекты, которые предоставляют метод `unapply` или `unapplySeq`:

```scala
def unapply[A](x: T)(implicit x: B): U
def unapplySeq[A](x: T)(implicit x: B): U
```

Экстракторы, предоставляющие метод `unapply`, называются экстракторами с фиксированной арностью. 
Экстракторы, предоставляющие метод `unapplySeq`, называются экстракторами с переменным числом параметров.

### Экстракторы с фиксированной арностью

Экстракторы с фиксированной арностью предоставляют следующую подпись:

```scala
def unapply[A](x: T)(implicit x: B): U
```

Тип `U` соответствует одному из следующих совпадений:

- Логическое совпадение
- Соответствие продукта

Или `U` соответствует типу `R`:

```scala
type R = {
  def isEmpty: Boolean
  def get: S
}
```

и `S` соответствует одному из следующих совпадений:

- одно совпадение
- совпадение по имени

Первая форма `unapply` имеет более высокий приоритет, 
а одиночное совпадение имеет более высокий приоритет, чем сопоставление на основе имени.

Использование экстрактора с фиксированной арностью применяется, если выполняется одно из следующих условий:

- `U = true`
- экстрактор используется в качестве соответствия продукта
- `U = Some[T]` (для совместимости со Scala 2)
- `U <: R`, а также `U <: { def isEmpty: false }`

#### Логическое совпадение

Пример:

```scala
object Even:
  def unapply(s: String): Boolean = s.size % 2 == 0
"even" match
  case s @ Even() => println(s"$s has an even number of characters")
  case s          => println(s"$s has an odd number of characters")
// even has an even number of characters
```

#### Соответствие продукта

Пример:

```scala
class FirstChars(s: String) extends Product:
  def _1 = s.charAt(0)
  def _2 = s.charAt(1)
  // Not used by pattern matching: Product is only used as a marker trait.
  def canEqual(that: Any): Boolean = ???
  def productArity: Int = ???
  def productElement(n: Int): Any = ???

object FirstChars:
  def unapply(s: String): FirstChars = new FirstChars(s)

"Hi!" match
  case FirstChars(char1, char2) =>
    println(s"First: $char1; Second: $char2")
// First: H; Second: i    
```

#### Одиночное совпадение

Пример:

```scala
class Nat(val x: Int):
  def get: Int = x
  def isEmpty = x < 0
object Nat:
  def unapply(x: Int): Nat = new Nat(x)
5 match
  case Nat(n) => println(s"$n is a natural number")
  case _      => ()
// 5 is a natural number  
```

#### Совпадение по имени

Пример:

```scala
object ProdEmpty:
  def _1: Int = ???
  def _2: String = ???
  def isEmpty = true
  def unapply(s: String): this.type = this
  def get = this
"" match
  case ProdEmpty(_, _) => ???
  case _ => println("not ProdEmpty")
// not ProdEmpty  
```


### Экстракторы с переменным числом параметров

Экстракторы с переменным числом параметров предоставляют следующую сигнатуру:

```scala
def unapplySeq[A](x: T)(implicit x: B): U
```

Тип `U` соответствует одному из следующих совпадений:

- соответствие последовательности
- соответствие последовательности продуктов

Или `U` соответствует типу `R`:

```scala
type R = {
  def isEmpty: Boolean
  def get: S
}
```

и `S` соответствует одному из двух совпадений выше.

Соответствие последовательности имеет более высокий приоритет, чем соответствие последовательности продуктов.

Использование экстрактора с переменным числом параметров применяется, если выполняется одно из следующих условий:

- экстрактор используется непосредственно как соответствие последовательности или соответствие продукта последовательности
- `U = Some[T]` (для совместимости со Scala 2)
- `U <: R`, а также `U <: { def isEmpty: false }`

#### Совпадение последовательности

Пример:

```scala
object CharList:
  def unapplySeq(s: String): Option[Seq[Char]] = Some(s.toList)
"example" match
  case CharList(c1, c2, c3, c4, _, _, _) =>
    println(s"$c1,$c2,$c3,$c4")
  case _ =>
    println("Expected *exactly* 7 characters!")
// e,x,a,m    
```

#### Соответствие последовательности продуктов

Пример:

```scala
class Foo(val name: String, val children: Int*)
object Foo:
  def unapplySeq(f: Foo): Option[(String, Seq[Int])] =
    Some((f.name, f.children))
def foo(f: Foo): Unit = f match
  case Foo(name, x, y, ns*) =>
    println(s"name = $name, x = $x, y = $y, ns = $ns")
  case Foo(name, ns*) =>
    println(s"name = $name, ns = $ns")
foo(new Foo("first", 2))
// name = first, ns = ArraySeq(2)
foo(new Foo("first", 2, 3))
// name = first, x = 2, y = 3, ns = ArraySeq()
foo(new Foo("first", 2, 3, 4))
// name = first, x = 2, y = 3, ns = ArraySeq(4)
```


---

**Ссылки:**

- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/changed-features/pattern-matching.html)

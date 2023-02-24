# Now You See Me, Now You Don't

```scala
trait A:
  val foo: Int
  val bar = 10
  println(s"In A: foo: $foo, bar: $bar")

class B extends A:
  val foo: Int = 25
  println(s"In B: foo: $foo, bar: $bar")

class C extends B:
  override val bar = 99
  println(s"In C: foo: $foo, bar: $bar")

new C
// In A: foo: 0, bar: 0
// In B: foo: 25, bar: 0
// In C: foo: 25, bar: 99
```

Обратите внимание, что `bar` — это `val`, переопределяемый в `C`. 
Компилятор Scala инициализирует `val`-ы только один раз, 
поэтому, поскольку `bar` будет инициализирован в `C`, 
он не инициализируется до этого времени 
и отображается как значение по умолчанию (в данном случае `0`) во время строительства суперкласса.


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-005)

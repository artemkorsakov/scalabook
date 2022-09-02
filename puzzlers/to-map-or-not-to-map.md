---
layout: puzzlers
title: "To Map, or Not to Map"
section: puzzlers
prev: if-at-first
next: private-lives
---

## {{page.title}}

```scala mdoc:silent
case class RomanNumeral(symbol: String, value: Int)

given Ordering[RomanNumeral] with
  def compare(a: RomanNumeral, b: RomanNumeral) = a.value compare b.value

val numerals = collection.immutable.SortedSet(
  RomanNumeral("M", 1000),
  RomanNumeral("C", 100),
  RomanNumeral("X", 10),
  RomanNumeral("I", 1),
  RomanNumeral("D", 500),
  RomanNumeral("L", 50),
  RomanNumeral("V", 5)
)
```

```scala mdoc
println("Roman numeral symbols for 1 5 10 50 100 500 1000:")
for (num <- numerals; sym = num.symbol) do print(s"$sym ")
numerals map (_.symbol) foreach (sym => print(s"$sym "))
```

Поскольку `RomanNumerals` упорядочены по их значениям, 
итерация по отсортированному набору цифр вернет их в этом порядке 
и приведет к печати представлений в соответствии со значением. 
Сопоставление чисел с их символами приведет к отсортированному набору строк, 
которые, естественно, будут упорядочены лексикографически. 
Таким образом, итерация по ним вернет их в этом порядке. 

Потенциально неожиданное переупорядочение элементов в итерируемом объекте можно было бы предотвратить, 
например, с помощью

```scala mdoc
numerals.view map { _.symbol } foreach (sym => print(s"$sym "))
```

или

```scala mdoc
numerals.toSeq map { _.symbol } foreach (sym => print(s"$sym "))
```

Обратите внимание, что первоначальный порядок объявления набора не влияет на порядок итерации. 
Также обратите внимание, что пример кода, использующий `map`, неэффективно перебирает числа дважды.


```scala mdoc
numerals foreach { num => print(s"${num.symbol} ") }
```

является более эффективным и выводит значения в ожидаемом порядке.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-013)

# Именованные параметры

При вызове метода при желании можно использовать имена параметров.
Например, `makeConnection` также можно вызывать следующими способами:

```scala
def makeConnection(timeout: Int = 5_000, protocol: String = "http") =
  println(f"timeout = ${timeout}%d, protocol = ${protocol}%s")
```

```scala
makeConnection(timeout=10_000)
// timeout = 10000, protocol = http
makeConnection(protocol="https")
// timeout = 5000, protocol = https
makeConnection(timeout=10_000, protocol="https")
// timeout = 10000, protocol = https
makeConnection(protocol="https", timeout=10_000)
// timeout = 10000, protocol = https
```

Именованные параметры особенно полезны, когда несколько параметров метода имеют один и тот же тип.
Без помощи IDE очень сложно понять, какие параметры установлены в значение `true` или `false`,
и поэтому код может быть трудночитаемым:

```scala
engage(true, true, true, false)
```

Гораздо более понятным выглядит использование именованных переменных:

```scala
engage(
  speedIsSet = true,
  directionIsSet = true,
  picardSaidMakeItSo = true,
  turnedOffParkingBrake = false
)
```


---

**References:**
- [Scala3 book, Method Features](https://docs.scala-lang.org/scala3/book/methods-most.html)

# Глава 1. Построение абстракций с помощью процедур

## 1.1 Элементы программирования

### 1.1.8. Процедуры как абстракции типа «черный ящик»

```scala
def sqrt(x: Double): Double = 
  def goodEnough(guess: Double, next: Double): Boolean =  math.abs(guess - next) < 0.001

  def average(a: Double, b: Double): Double = (a + b) / 2

  def improve(guess: Double): Double = average(guess, x / guess)

  def sqrtIter(guess: Double): Double =
    val next = improve(guess)
    if goodEnough(guess, next) then guess
    else sqrtIter(next)

  sqrtIter(1.0)

sqrt(2.0)
// res0: Double = 1.4142156862745097
sqrt(1000000) 
// res1: Double = 1000.0001533016629
```


---

**Ссылки:**

- [Глава 1.1.8](https://web.mit.edu/6.001/6.037/sicp.pdf#page=61)

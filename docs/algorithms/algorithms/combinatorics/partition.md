# Партиции, монеты и др.

## Монеты

**Алгоритм:**

Количество способов получить заданную сумму с помощью заданного набора монет.

[31 задача из Project Euler](https://projecteuler.net/problem=31)

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def countWays(coins: Array[Int], sum: Int): SafeLong =
  val ways = Array.fill(sum + 1)(SafeLong(0))
  ways(0) = 1
  coins.foreach: coin =>
    (coin to sum).foreach: i =>
      ways(i) += ways(i - coin)
  ways(sum)
  
countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), 200) 
// 73682
```

**Метрики:**

Вычисление "количества путей" для первых 10000 "сумм" занимает примерно 6 секунд.

```dotty
@main def countWaysBench(): Unit =
  (1 to 10_000).foreach: i =>
    countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), i)
      
// CPU Time: 5788 ms
// Allocation memory size: 17,18 GB      
```

## Партиции

**Алгоритм:**

[Количество способов](https://ru.wikipedia.org/wiki/%D0%A0%D0%B0%D0%B7%D0%B1%D0%B8%D0%B5%D0%BD%D0%B8%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%B0) 
записи n в виде суммы натуральных чисел.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def partition(number: Int): BigInt =
  val array = new Array[BigInt](number + 1)
  array(0) = BigInt(1)
  if number >= 1 then array(1) = BigInt(1)
  if number >= 2 then array(2) = BigInt(2)

  def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
    var k  = s
    var op = k * (3 * k - 1) / 2
    var p  = pS
    while op <= n do
      if (k + 1) % 2 == 0 then
        p += array(n - op)
      else
        p -= array(n - op)
      k += s
      op = k * (3 * k - 1) / 2
    p

  for n <- 3 to number do
    val p = partitionPart(1, n, BigInt(0))
    array(n) = partitionPart(-1, n, p)
  array(number)
end partition
  
partition(1000) 
// BigInt("24061467864032622473692149727991")
```

**Метрики:**

Вычисление "количества партиций" для чисел от 1 до 1000 занимает примерно 1 секунду.

```dotty
@main def partitionBench(): Unit =
  (1 to 1000).foreach: i =>
    partition(i)
      
// CPU Time: 1179 ms
// Allocation memory size: 820,09 MB      
```

## Цветные шары

**Алгоритм:**

У нас есть белые шары A, черные шары B и серые шары C. 
Сколько существует различных вариантов расположить эти шарики в ряд?

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
private val cache: mutable.Map[(Int, Int, Int), SafeLong] =
  mutable.Map.empty[(Int, Int, Int), SafeLong]

def countABCRows(a: Int, b: Int, c: Int): SafeLong =
  val array = Array(a, b, c)
  val min   = array.min
  if min < 0 then 0
  else
    val max = array.max
    val mid = array.sum - min - max
    cache.getOrElseUpdate((min, mid, max), count(min, mid, max))

private def count(min: Int, mid: Int, max: Int): SafeLong =
  if min == 0 && mid == 0 then 1
  else
    countABCRows(min - 1, mid, max) +
      countABCRows(min, mid - 1, max) +
      countABCRows(min, mid, max - 1)
  
countABCRows(2, 2, 2) 
// 90  
```

**Метрики:**

Вычисление количества вариантов расположить 100 белых, 100 черных и 100 серых шаров занимает примерно 1 секунду.

```dotty
@main def countABCRowsBench(): Unit =
  println(countABCRows(100, 100, 100)) // 142-значное число

// CPU Time: 1053 ms                
// Allocation memory size: 166,35 MB     
```

---

**Ссылки:**

- [The Art of Computer Programming - Donald E. Knuth, section 1.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.11.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Combinatorial Algorithms in Scala - kostyukov.net][kostyukov]

[kostyukov]: https://kostyukov.net/posts/combinatorial-algorithms-in-scala/

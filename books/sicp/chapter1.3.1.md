# Глава 1. Построение абстракций с помощью процедур

## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.1. Процедуры в качестве аргументов

#### Упражнение 1.29

> С помощью процедуры **smallest-divisor** найдите наименьший делитель следующих чисел: **199**, **1999**, **19999**.

Решение на Scala:

```scala
def divides(a: Long, b: Long): Boolean = a % b == 0

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, d + 1)

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

smallestDivisor(199)    // 199
smallestDivisor(1999)   // 1999
smallestDivisor(19999)  // 7
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-21.worksheet.sc)


#### Упражнение 1.30

#### Упражнение 1.31

#### Упражнение 1.32

#### Упражнение 1.33


---

**Ссылки:**
- [Упражнение 1.29 - 1.33](https://web.mit.edu/6.001/6.037/sicp.pdf#page=108)

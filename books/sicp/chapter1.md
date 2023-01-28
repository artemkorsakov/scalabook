# Глава 1. Построение абстракций с помощью процедур

### Упражнение 1.1

```scala
10                    // 10
(5 + 3 + 4)           // 12
(9 - 1)               // 8
(6 / 2)               // 3
((2 * 4) + (4 - 6))   // 6
val a = 3
val b = a + 1         // 4
(a + b + (a * b))     // 19
(a == b)              // false

if (b > a) && (b < (a * b)) then b else a                  // 4

if a == 4 then 6
else if b == 4 then 6 + 7 + a
else 25                                                    // 16
 
2 + (if (b > a) then b else a)                             // 6

((if a > b then a else if a < b then b else -1) * (a + 1)) // 16
```

[Scala worksheet]()

### Упражнение 1.2


---

**Ссылки:**
- [Упражнение 1.1 - 1.5](https://web.mit.edu/6.001/6.037/sicp.pdf#page=54)

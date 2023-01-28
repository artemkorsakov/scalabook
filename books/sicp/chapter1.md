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

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-1.worksheet.sc)

### Упражнение 1.2

Выражение 

![](https://latex.codecogs.com/svg.image?\frac{5%20+%204%20+%20(2%20-%20(3%20-%20(6%20+%20\frac{5}{4})))%20}{3(6%20-%202)(2%20-%207)})

в префиксной форме выглядит так:

```text
(/ (+ 5 4 (- 2 (- 3 (+ 6 (/ 4 5))))) (* 3 (- 6 2) (- 7 2)))
```


---

**Ссылки:**
- [Упражнение 1.1 - 1.5](https://web.mit.edu/6.001/6.037/sicp.pdf#page=54)

# Рандомизация данных

## Линейный конгруэнтный генератор

Линейный конгруэнтный генератор использует следующую зависимость для формирования псевдослучайных величин:

X<sub>n+1</sub> = (A * X<sub>n</sub> + B) mod M, где A, B и M - константы.

Величина X<sub>0</sub> называется _начальным_ числом.

Генератор можно реализовать на Scala так:

```scala
val A = 7
val B = 5
val M = 11
val generator = LazyList.iterate(0)(x => (A * x + B) % M)
generator.take(11).toList
// val res0: List[Int] = List(0, 5, 7, 10, 9, 2, 8, 6, 3, 4, 0)
```


---

## References

- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 2. Численные алгоритмы](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)

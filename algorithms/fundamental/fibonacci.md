# Числа Фибоначчи

[Числа Фибоначчи](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8) — 
элементы числовой последовательности `0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...`,
в которой первые два числа равны `0` и `1`, а каждое последующее число равно сумме двух предыдущих чисел.


## Заданное число Фибоначчи

Заданное число Фибоначчи можно получить с помощью меморизации из библиотеки [scalaz](https://github.com/scalaz/scalaz):

```scala
import scalaz.*

object Fibonacci:
  val memoizedFib: Int => BigInt = Memo.mutableHashMapMemo {
    case 0 => BigInt(0)
    case 1 => BigInt(1)
    case n => memoizedFib(n - 2) + memoizedFib(n - 1)
  }

memoizedFib(60)  // 1548008755920
```


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FFibonacci.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FFibonacciSuite.scala)


---

## References

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Memo.html)

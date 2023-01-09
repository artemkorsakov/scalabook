# Функция Аккермана

Следующая процедура вычисляет математическую функцию, называемую функцией Аккермана:

```scala
def functionAckermann(x: Int, y: Int): Int =
  if y == 0 then 0
  else if x == 0 then 2 * y
  else if y == 1 then 2
  else functionAckermann(x - 1, functionAckermann(x, y - 1))
```

Для малых `x` функция Аккермана равносильна следующим функциям:
- `functionAckermann(0, n) <=> 2*n`
- `functionAckermann(1, n) <=> 2^n`
- `functionAckermann(2, n) <=> 2^2^...^2 (n раз)`

---

## Ссылки

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Fothers%2FOtherAlgorithms.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Fothers%2FOtherAlgorithmsSuite.scala)
- [Функция Аккермана - Wiki](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F_%D0%90%D0%BA%D0%BA%D0%B5%D1%80%D0%BC%D0%B0%D0%BD%D0%B0)
- [Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ](https://web.mit.edu/6.001/6.037/sicp.pdf)

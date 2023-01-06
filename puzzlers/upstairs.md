# UPSTAIRS downstairs

```scala
val ij: (Int, Int) = (3, 4)
// ij: Tuple2[Int, Int] = (3, 4)
val (i, j): (Int, Int) = (3, 4)
// i: Int = 3
// j: Int = 4
val IJ: (Int, Int) = (3, 4)
// IJ: Tuple2[Int, Int] = (3, 4)
```

```scala
val (I, J): (Int, Int) = (3, 4)
val (`i`, `j`): (Int, Int) = (3, 4)
// error:
// Not found: I
// val (I, J): (Int, Int) = (3, 4)
//      ^
// error:
// Not found: J
// val (I, J): (Int, Int) = (3, 4)
//         ^
// error:
// Not found: i
// val (`i`, `j`): (Int, Int) = (3, 4)
//      ^^^
// error:
// Not found: j
// val (`i`, `j`): (Int, Int) = (3, 4)
//           ^^^
```

Первые три случая - это присвоение переменным значений кортежа. 
Последние два пытаются сопоставить `(3, 4)` с константами `I` и `J` 
или переменными `i` и `j`, которые должны быть определены ранее — 
Scala полагает, что переменные ВЕРХНЕГО РЕГИСТРА в сопоставлении с образцом являются константами.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-003)

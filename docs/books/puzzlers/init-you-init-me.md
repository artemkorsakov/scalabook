# Init You, Init Me

```scala
object XY:
  object X:
    val value: Int = Y.value + 1
  object Y:
    val value: Int = X.value + 1

println(if math.random > 0.5 then XY.X.value else XY.Y.value)
// 2
```

Рекурсивное определение двух полей `X.value` и `Y.value` допустимо и компилируется, 
но поскольку оно является рекурсивным, тип (`Int`) должен быть указан как минимум для `X.value`. 
При доступе к объекту `X` или `Y` значение его поля инициализируется 
(объекты не инициализируются до тех пор, пока к ним не будет осуществлен доступ). 
Если сначала инициализируется объект `X`, то инициализация значения его поля запускает инициализацию объекта `Y`. 
Чтобы инициализировать поле `Y.value`, осуществляется доступ к полю `X.value`. 
Виртуальная машина замечает, что инициализация объекта `X` уже запущена, 
и возвращает текущее значение `X.value`, равное нулю (значение по умолчанию для полей `Int`), 
поэтому во время выполнения не происходит переполнения стека. 
Как следствие, `Y.value` устанавливается равным `0 + 1 = 1`, а `X.value` — равным `1 + 1 = 2`. 
Однако, если сначала инициализируется объект `Y`, тогда `Y.value` инициализируется `2`, а `X.value` — `1`. 
В операторе `if` мы получаем доступ либо к `X.value`, либо к `Y.value` 
и принудительно инициализируем либо `X`, либо `Y`. 
Но, как мы видели выше, к какому бы объекту ни обращались первым, значение его поля инициализируется равным `2`, 
поэтому результат условного оператора всегда `2`.


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-010)

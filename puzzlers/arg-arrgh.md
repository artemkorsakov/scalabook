# Arg Arrgh!

```scala
def square[T : Numeric](n: T) = summon[Numeric[T]].times(n, n)

def twiceA[T](f: T => T, a: T) = f(f(a))
def twiceB[T](f: T => T)(a: T) = f(f(a))
def twiceC[T](a: T, f: T => T) = f(f(a))
def twiceD[T](a: T)(f: T => T) = f(f(a))
```

```scala
twiceA(square, 2)
// error:
// No implicit Ordering defined for Any.
// twiceA(square, 2)
//              ^
twiceB(square)(2)
// error:
// No implicit Ordering defined for Any.
// twiceB(square)(2)
//              ^
```

```scala
twiceC(2, square)
// res1: Int = 16
twiceD(2)(square)
// res2: Int = 16
```

Чтобы `square` можно было использовать в качестве аргумента, 
компилятор должен знать, что `T` привязан к (типу, который может быть неявно преобразован) к `Numeric`. 
Несмотря на то, что может показаться, что `2` как `Int` явно удовлетворяет этому условию, 
эта информация недоступна для компилятора до появления `2` в качестве аргумента. 
Только в последних двух версиях `T` связывается «достаточно рано», чтобы `square` был разрешен.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-007)

# A Matter of Context

```scala
def tmpDir(uniqueSuffix: String): String =
  """\\builder\tmp-""" + uniqueSuffix
def tmpDir2(uniqueSuffix: String): String =
  s"""\\builder\tmp-$uniqueSuffix"""
def tmpDir3(uniqueSuffix: String): String =
  s"\\builder\tmp-$uniqueSuffix"

println(tmpDir("42"))
// \\builder\tmp-42
println(tmpDir2("42"))
// \builder	mp-42
println(tmpDir3("42"))
// \builder	mp-42
```

При использовании строкового интерполятора `s` или любого другого метода интерполяции 
обработка escape-последовательностей фактически не определяется «строковым выражением», 
следующим сразу за идентификатором. 
Вместо этого, как [описано в документации](https://docs.scala-lang.org/overviews/core/string-interpolation.html), 
строковое выражение, следующее за идентификатором, всегда обрабатывается как многострочный строковый литерал, 
т.е. как если бы оно было заключено в тройные кавычки. 
Выражения `s"""\\builder\tmp-${uniqueSuffix}"""` и `s"\\builder\tmp-${uniqueSuffix}"` переписываются как: 

```scala
StringContext("""\\builder\tmp-""").s(uniqueSuffix)
```

Как интерпретируются управляющие последовательности - зависит от реализации интерполятора. 

В случае метода интерполяции `s` escape-последовательности интерпретируются так, 
как если бы они были в строке с одинарными кавычками, 
поэтому и второй, и третий операторы `println` печатают `\builder        mp-42`.


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-065)

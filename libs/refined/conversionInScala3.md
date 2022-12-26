# Неявные преобразования в Scala 3

В Scala 3 не все так просто:
[неявные преобразования типов довольно сильно переработаны](https://scalabook.gitflic.space/scala/abstractions/ca-implicit-conversions).

Поэтому даже "валидный" пример `val name: Name = "Алёна"` при компиляции выдаст ошибку: `Type Mismatch Error`.

Для того чтобы позволить неявное преобразование из `String` в `Name`
нужно для начала определить соответствующий `given` экземпляр, [как показано в документации](https://docs.scala-lang.org/scala3/book/ca-implicit-conversions.html)

При этом преобразования типов будут происходить во время выполнения, а не компиляции, поэтому этот способ небезопасен.
Но можно определить неявное преобразование в `Option`, что позволяет выполнять такое присваивание:

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

import scala.language.implicitConversions

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

given Conversion[String, Option[Name]] = RefinedTypeOps[Name, String].unapply(_)

val name0: Option[Name] = "€‡™µ"    // None 
val name1: Option[Name] = "12345"   // None
val name2: Option[Name] = "Alyona"  // None
val name3: Option[Name] = "Алёна18" // None
val name4: Option[Name] = "алёна"   // None
val name5: Option[Name] = "Алёна"   // Some(Алёна)
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FCompileTimeExample.sc&plain=1)

Таким образом проверка соответствия уточненным типам в Scala 3 во время компиляции
пока ещё не реализована, но это лишь вопрос времени,
учитывая мощную функциональность [метапрограммирования в Scala 3](https://docs.scala-lang.org/scala3/reference/metaprogramming/index.html).

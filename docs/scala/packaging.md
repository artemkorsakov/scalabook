# Пакеты и импорт

Scala использует `packages` для создания пространств имен, которые позволяют модульно разбивать программы. 
Scala поддерживает стиль именования пакетов, используемый в Java, 
а также нотацию пространства имен "фигурные скобки", используемую такими языками, как C++ и C#.

Подход Scala к импорту похож на Java, но более гибкий. С помощью Scala можно:

- импортировать пакеты, классы, объекты, `trait`-ы и методы
- размещать операторы импорта в любом месте
- скрывать и переименовывать участников при импорте

Эти особенности демонстрируются в следующих примерах.

### Создание пакета

Пакеты создаются путем объявления одного или нескольких имен пакетов в начале файла Scala. 
Например, если ваше доменное имя `acme.com` и вы работаете с пакетом `model` приложения с именем `myapp`, 
объявление пакета выглядит следующим образом:

```scala
package com.acme.myapp.model

class Person ...
```

По соглашению все имена пакетов должны быть строчными, 
а формальным соглашением об именах является `<top-level-domain>.<domain-name>.<project-name>.<module-name>`.

Хотя это и не обязательно, имена пакетов обычно совпадают с именами иерархии каталогов.
Поэтому, если следовать этому соглашению, класс `Person` в этом проекте будет найден 
в файле _MyApp/src/main/scala/com/acme/myapp/model/Person.scala_.

#### Использование нескольких пакетов в одном файле

Показанный выше синтаксис применяется ко всему исходному файлу: 
все определения в файле `Person.scala` принадлежат пакету `com.acme.myapp.model` 
в соответствии с `package` в начале файла.

В качестве альтернативы можно написать `package`, которые применяются только к содержащимся в них определениям:

```scala
package users:

  package administrators:  // полное имя пакета - users.administrators
    class AdminUser        // полное имя файла  - users.administrators.AdminUser

  package normalusers:     // полное имя пакета - users.normalusers
    class NormalUser       // полное имя файла  - users.normalusers.NormalUser
```

Обратите внимание, что за именами пакетов следует двоеточие, а определения внутри пакета имеют отступ.

Преимущество этого подхода заключается в том, что он допускает вложение пакетов 
и обеспечивает более очевидный контроль над областью действия и инкапсуляцией, особенно в пределах одного файла.

### Операторы импорта

Операторы импорта используются для доступа к сущностям в других пакетах. 
Операторы импорта делятся на две основные категории:

- импорт классов, `trait`-ов, объектов, функций и методов
- импорт `given` предложений

Первая категория операторов импорта аналогична тому, что использует Java, 
с немного другим синтаксисом, обеспечивающим большую гибкость. 
Пример:

```scala
import users.*                            // импортируется все из пакета `users`
import users.User                         // импортируется только класс `User`
import users.{User, UserPreferences}      // импортируются только два члена пакета
import users.{UserPreferences as UPrefs}  // переименование импортированного члена
```

Эти примеры предназначены для того, чтобы дать представление о том, как работает первая категория операторов `import`. 
Более подробно они объясняются в следующих подразделах.

Операторы импорта также используются для импорта `given` экземпляров в область видимости. 
Они обсуждаются в конце этой главы.

> `import` не требуется для доступа к членам одного и того же пакета.

#### Импорт одного или нескольких членов

В Scala импортировать один элемент из пакета можно следующим образом:

```scala
import scala.concurrent.Future
```

несколько:

```scala
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.blocking
```

При импорте нескольких элементов можно импортировать их более лаконично:

```scala
import scala.concurrent.{Future, Promise, blocking}
```

Если необходимо импортировать все из пакета `scala.concurrent`, используется такой синтаксис:

```scala
import scala.concurrent.*
```

#### Переименование элементов при импорте

Иногда необходимо переименовать объекты при их импорте, чтобы избежать конфликтов имен. 
Например, если нужно использовать Scala класс `List` вместе с `java.util.List`, 
то можно переименовать `java.util.List` при импорте:

```scala
import java.util.{List as JavaList}
```

Теперь имя `JavaList` можно использовать для ссылки на класс `java.util.List` 
и использовать `List` для ссылки на Scala класс `List`.

Также можно переименовывать несколько элементов одновременно, используя следующий синтаксис:

```scala
import java.util.{Date as JDate, HashMap as JHashMap, *}
```

В этой строке кода говорится следующее: 
"Переименуйте классы `Date` и `HashMap`, как показано, 
и импортируйте все остальное из пакета `java.util`, не переименовывая".

#### Скрытие членов при импорте

При импорте часть объектов можно скрывать. 
Следующий оператор импорта скрывает класс `java.util.Random`, 
в то время как все остальное в пакете `java.util` импортируется:

```scala
import java.util.{Random as _, *}
```

Если попытаться получить доступ к классу `Random`, то выдается ошибка, 
но есть доступ ко всем остальным членам пакета `java.util`:

```scala
val r = new Random   // не скомпилируется
new ArrayList        // доступ есть
```

##### Скрытие нескольких элементов

Чтобы скрыть в `import` несколько элементов, их можно перечислить перед использованием `*`:

```scala
import java.util.{List as _, Map as _, Set as _, *}
```

Перечисленные классы скрыты, но можно использовать все остальное в `java.util`:

```scala
val arr = new ArrayList[String]
// arr: ArrayList[String] = []
```

Поскольку эти Java классы скрыты, можно использовать классы Scala `List`, `Set` и `Map` без конфликта имен:

```scala
val a = List(1, 2, 3)
// a: List[Int] = List(1, 2, 3)
val b = Set(1, 2, 3)
// b: Set[Int] = Set(1, 2, 3)
val c = Map(1 -> 1, 2 -> 2)
// c: Map[Int, Int] = Map(1 -> 1, 2 -> 2)
```

#### Импорт можно использовать в любом месте

В Scala операторы импорта могут быть объявлены где угодно. 
Их можно использовать в верхней части файла исходного кода:

```scala
package foo

import scala.util.Random

class ClassA:
  def printRandom:
    val r = new Random   // класс Random здесь доступен
    // ещё код...
```

Также операторы импорта можно использовать ближе к тому месту, где они необходимы:

```scala
package foo

class ClassA:
  import scala.util.Random   // внутри ClassA
  def printRandom {
    val r = new Random
    // ещё код...

class ClassB:
  // класс Random здесь невидим
  val r = new Random   // этот код не скомпилится
```

#### "Статический" импорт

Если необходимо импортировать элементы способом, аналогичным подходу «статического импорта» в Java, 
то есть для того, чтобы напрямую обращаться к членам класса, не добавляя к ним префикс с именем класса, 
используется следующий подход.

Синтаксис для импорта всех статических членов Java класса `Math`:

```scala
import java.lang.Math.*
```

Теперь можно получить доступ к статическим методам класса `Math`, таким как `sin` и `cos`, 
без необходимости предварять их именем класса:

```scala
import java.lang.Math.*
val a = sin(0)
// a: Double = 0.0 
val b = cos(PI)
// b: Double = -1.0
```

#### Пакеты, импортированные по умолчанию

Два пакета неявно импортируются во все файлы исходного кода:

- `java.lang.*`
- `scala.*`

Члены `object Predef` также импортируются по умолчанию.

> Например, такие классы, как `List`, `Vector`, `Map` и т. д. можно использовать явно, не импортируя их - 
> они доступны, потому что определены в object Predef

#### Обработка конфликтов имен

Если необходимо импортировать что-то из корня проекта и возникает конфликт имен, 
достаточно просто добавить к имени пакета префикс `_root_`:

```scala
package accounts

import _root_.accounts.*
```

### Импорт `given`

Как будет показано в главе ["Контекстные абстракции"](https://scalabook.gitflic.space/docs/scala/abstractions),
для импорта экземпляров `given` используется специальная форма оператора `import`. 
Базовая форма показана в этом примере:

```scala
object A:
  class TC
  given tc as TC
  def f(using TC) = ???

object B:
  import A.*       // import all non-given members
  import A.given   // import the given instance
```

В этом коде предложение `import A.*` объекта `B` импортирует все элементы `A`, кроме `given` экземпляра `tc`. 
И наоборот, второй импорт, `import A.given`, импортирует только `given` экземпляр. 
Два предложения импорта также могут быть объединены в одно:

```scala
object B:
  import A.{given, *}
```

Селектор с подстановочным знаком `*` помещает в область видимости все определения, кроме `given`, 
тогда как селектор выше помещает в область действия все данные, включая те, которые являются результатом расширений.

Эти правила имеют два основных преимущества:

- более понятно, откуда берутся данные `given`. 
В частности, невозможно скрыть импортированные `given` в длинном списке других импортируемых подстановочных знаков.
- есть возможность импортировать все `given`, не импортируя ничего другого. 
Это особенно важно, поскольку `given` могут быть анонимными, 
поэтому обычное использование именованного импорта нецелесообразно.

#### Импорт по типу

Поскольку `given`-ы могут быть анонимными, не всегда практично импортировать их по имени, 
и вместо этого обычно используется импорт подстановочных знаков. 
Импорт по типу предоставляет собой более конкретную альтернативу импорту с подстановочными знаками, 
делая понятным то, что импортируется.
Этот код импортирует из `A` любой `given` тип, соответствующий `TC`:

```scala
import A.{given TC}
```

Если импортируется только один `given`, то фигурные скобки можно опустить:

```scala
import A.given TC
```

Импорт данных нескольких типов `T1,...,Tn` выражается несколькими `given` селекторами:

```scala
import A.{given T1, ..., given Tn}
```

Импорт всех `given` экземпляров параметризованного типа достигается аргументами с подстановочными знаками. 
Например, есть такой объект:

```scala
object Instances:
  given intOrd as Ordering[Int]
  given listOrd[T: Ordering] as Ordering[List[T]]
  given ec as ExecutionContext = ...
  given im as Monoid[Int]
```

Оператор `import` ниже импортирует экземпляры `intOrd`, `listOrd` и `ec`, но пропускает экземпляр `im`, 
поскольку он не соответствует ни одному из указанных шаблонов:

```scala
import Instances.{given Ordering[?], given ExecutionContext}
```

Импорт по типу можно смешивать с импортом по имени. 
Если оба присутствуют в предложении `import`, импорт по типу идет последним. 
Например, это предложение импорта импортирует `im`, `intOrd` и `listOrd`, но не включает `ec`:

```scala
import Instances.{im, given Ordering[?]}
```

#### Пример

В качестве конкретного примера представим, что у нас есть объект `MonthConversions`, 
который содержит два определения `given`:

```scala
object MonthConversions:
  trait MonthConverter[A]:
    def convert(a: A): String

  given intMonthConverter: MonthConverter[Int] with
    def convert(i: Int): String = 
      i match
        case 1 =>  "January"
        case 2 =>  "February"
        case _ =>  "Other"

  given stringMonthConverter: MonthConverter[String] with
    def convert(s: String): String = 
      s match
        case "jan" => "January"
        case "feb" => "February"
        case _ =>  "Other"
```

Чтобы импортировать эти `given`-ы в текущую область, используем два оператора `import`:

```scala
import MonthConversions.*
import MonthConversions.given MonthConverter[?]
```

Теперь создаем метод, использующий эти экземпляры:

```scala
def genericMonthConverter[A](a: A)(using monthConverter: MonthConverter[A]): String =
  monthConverter.convert(a)
```

Вызов метода:

```scala
genericMonthConverter(1)
// res1: String = "January"
genericMonthConverter("jan")
// res2: String = "January"
```

Как уже упоминалось ранее, одно из ключевых преимуществ синтаксиса "import given" состоит в том, 
чтобы прояснить, откуда берутся данные в области действия, 
и в `import` операторах выше ясно, что данные поступают из объекта `MonthConversions`.


---

**Ссылки:**

- [Scala3 book](https://docs.scala-lang.org/scala3/book/packaging-imports.html)

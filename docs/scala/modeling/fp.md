# Моделирование ФП

В этой главе представлено введение в моделирование предметной области 
с использованием функционального программирования (ФП). 

При моделировании с помощью ФП обычно используются следующие конструкции Scala:

- Enums
- Case classes
- Traits

### Введение

В ФП данные и операции над этими данными — это две разные вещи; их необязательно инкапсулировать вместе, как в ООП.

Концепция аналогична числовой алгебре. 
Когда вы думаете о целых числах, значения которых больше или равны нулю, 
то у вас есть набор возможных значений, который выглядит следующим образом:

```
0, 1, 2 ... Int.MaxValue
```

Игнорируя деление целых чисел, возможные операции над этими значениями такие:

```
+, -, *
```

Схема ФП реализуется аналогичным образом: 

- описывается свой набор значений (данные) 
- описываются операции, которые работают с этими значениями (функции)

> Как будет видно, рассуждения о программах в этом стиле сильно отличаются 
> от объектно-ориентированного программирования. 
> Отделение функциональности от данных позволяет проверять свои данные, не беспокоясь о поведении.

В этой главе мы смоделируем данные и операции для "пиццы" в пиццерии. 
Будет показано, как реализовать часть "данных" модели Scala/ФП, 
а затем - несколько различных способов организации операций с этими данными.

### Моделирование данных

В Scala достаточно просто описать модель данных:

- если необходимо смоделировать данные с различными вариантами, то используется конструкция `enum`
- если необходимо только сгруппировать сущности (или нужен более детальный контроль), то используются `case class`-ы

#### Описание вариантов

Данные, которые просто состоят из различных вариантов, таких как размер корочки, тип корочки и начинка, 
кратко моделируются с помощью конструкции `enum`:

```scala
enum CrustSize:
  case Small, Medium, Large
  
enum CrustType:
  case Thin, Thick, Regular
  
enum Topping:
  case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions
```

> Типы данных, которые описывают различные варианты (например, `CrustSize`), 
> также иногда называют типами суммы (_sum types_).

#### Описание основных данных

Пиццу можно рассматривать как составной контейнер с различными атрибутами, указанными выше. 
Мы можем использовать `case class`, чтобы описать, что пицца состоит из размеров корки, типа корки 
и, возможно, нескольких начинок:

```scala
import CrustSize.*
import CrustType.*
import Topping.*

case class Pizza(
  crustSize: CrustSize,
  crustType: CrustType,
  toppings: Seq[Topping]
)
```

> Типы данных, объединяющие несколько компонентов (например, `Pizza`), 
> также иногда называют типами продуктов (_product types_).

И все. Это модель данных для системы доставки пиццы в стиле ФП. 
Решение очень лаконично, поскольку оно не требует объединения модели данных с операциями с пиццей. 
Модель данных легко читается, как объявление дизайна для реляционной базы данных. 
Также очень легко создавать значения нашей модели данных и проверять их:

```scala
val myFavPizza = Pizza(Small, Regular, Seq(Cheese, Pepperoni))
// myFavPizza: Pizza = Pizza(
//   crustSize = Small,
//   crustType = Regular,
//   toppings = List(Cheese, Pepperoni)
// )
println(myFavPizza.crustType)
// Regular
```

##### Подробнее о модели данных

Таким же образом можно было бы смоделировать всю систему заказа пиццы. 
Вот несколько других `case class`-ов, которые используются для моделирования такой системы:

```scala
case class Address(
  street1: String,
  street2: Option[String],
  city: String,
  state: String,
  zipCode: String
)

case class Customer(
  name: String,
  phone: String,
  address: Address
)

case class Order(
  pizzas: Seq[Pizza],
  customer: Customer
)
```

##### "Узкие доменные объекты"

В своей книге [Functional and Reactive Domain Modeling](https://www.manning.com/books/functional-and-reactive-domain-modeling), 
Debasish Ghosh утверждает, 
что там, где специалисты по ООП описывают свои классы как "широкие модели предметной области", 
которые инкапсулируют данные и поведение, 
модели данных ФП можно рассматривать как "узкие объекты предметной области". 
Это связано с тем, что, как показано выше, модели данных определяются как `case class`-ы с атрибутами, 
но без поведения, что приводит к коротким и лаконичным структурам данных.

### Моделирование операций

Возникает интересный вопрос: 
поскольку ФП отделяет данные от операций над этими данными, то как эти операции реализуются в Scala?

Ответ на самом деле довольно прост: пишутся функции/методы, 
работающие со значениями смоделированных данных. 
Например, можно определить функцию, которая вычисляет цену пиццы.

```scala
def pizzaPrice(p: Pizza): Double = p match
  case Pizza(crustSize, crustType, toppings) =>
    val base  = 6.00
    val crust = crustPrice(crustSize, crustType)
    val tops  = toppings.map(toppingPrice).sum
    base + crust + tops
```

Можно заметить, что реализация функции просто повторяет форму данных: 
поскольку `Pizza` является `case class`-ом, используется сопоставление с образцом для извлечения компонентов,
а затем вызываются вспомогательные функции для вычисления отдельных цен.

```scala
def toppingPrice(t: Topping): Double = t match
  case Cheese | Onions => 0.5
  case Pepperoni | BlackOlives | GreenOlives => 0.75
```

Точно так же, поскольку `Topping` является перечислением, используется сопоставление с образцом, 
чтобы разделить варианты. Сыр и лук продаются по 50 центов за штуку, остальные — по 75.

```scala
def crustPrice(s: CrustSize, t: CrustType): Double =
  (s, t) match
    case (Small | Medium, _) => 0.25 // игнорируем значение t
    case (Large, Thin) => 0.50
    case (Large, Regular) => 0.75
    case (Large, Thick) => 1.00
```

Чтобы рассчитать цену корки, мы одновременно сопоставляем образцы как по размеру, так и по типу корки.

> Важным моментом во всех показанных выше функциях является то, что они являются чистыми функциями (pure functions): 
> они не изменяют данные и не имеют других побочных эффектов (таких, как выдача исключений или запись в файл). 
> Всё, что они делают - это просто получают значения и вычисляют результат.

### Как организовать функциональность?

При реализации функции расчета цены пиццы, описанной выше, не было сказано, где ее определять.
В Scala 3 вполне допустимо определить функцию на верхнем уровне файла. 
Тем не менее язык предоставляет множество отличных инструментов для организации логики 
в различных пространствах имен и модулях.

Существует несколько способов реализации и организации поведения:

- определить функции в сопутствующих объектах (companion object)
- использовать модульный стиль программирования
- использовать подход "функциональных объектов"
- определить функциональность в методах расширения

Эти различные решения показаны в оставшейся части этого раздела.

#### Companion Object

Первый подход — определить поведение (функции) в сопутствующем объекте.

> Как обсуждалось в разделе ["Companion objects"](https://scalabook.gitflic.space/docs/scala/modeling/companion-objects), 
> сопутствующий объект — это объект с тем же именем, что и у класса, и объявленный в том же файле, что и класс.

При таком подходе в дополнение к `enum` или `case class` также определяется companion object с таким же именем, 
который содержит поведение (функции).

```scala
case class Pizza(
  crustSize: CrustSize,
  crustType: CrustType,
  toppings: Seq[Topping]
)

// companion object для кейс класса Pizza
object Pizza:
  def price(p: Pizza): Double = ... // тоже самое, что и pizzaPrice

enum Topping:
  case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions

// companion object для перечисления Topping
object Topping:
  def price(t: Topping): Double = t match // тоже самое, что и toppingPrice
    case Cheese | Onions => 0.5
    case Pepperoni | BlackOlives | GreenOlives => 0.75
```

При таком подходе можно создать `Pizza` и вычислить ее цену следующим образом:

```scala
val pizza1 = Pizza(Small, Thin, Seq(Cheese, Onions))
Pizza.price(pizza1)
```

Группировка функциональности с помощью сопутствующих объектов имеет несколько преимуществ:

- связывает функциональность с данными и облегчает их поиск программистам (и компилятору).
- создает пространство имен и, например, позволяет использовать `price` в качестве имени метода, 
не полагаясь на перегрузку.
- реализация `Topping.price` может получить доступ к значениям перечисления, таким как `Cheese`, 
без необходимости их импорта.

Однако также есть несколько компромиссов, которые следует учитывать:

- модель данных тесно связывается с функциональностью. 
В частности, сопутствующий объект должен быть определен в том же файле, что и `case class`.
- неясно, где определять такие функции, как `crustPrice`, 
которые с одинаковым успехом можно поместить в сопутствующий объект `CrustSize` или `CrustType`.

### Модули

Второй способ организации поведения — использование "модульного" подхода. 
В книге ["Программирование на Scala"][programming_in_scala]
модуль определяется как "_небольшая часть программы с четко определенным интерфейсом и скрытой реализацией_". 
Давайте посмотрим, что это значит.

#### Создание интерфейса _PizzaService_

Первое, о чем следует подумать, — это "поведение" `Pizza`. 
Делая это, определяем `trait` `PizzaServiceInterface` следующим образом:

```scala
trait PizzaServiceInterface:

  def price(p: Pizza): Double

  def addTopping(p: Pizza, t: Topping): Pizza
  def removeAllToppings(p: Pizza): Pizza

  def updateCrustSize(p: Pizza, cs: CrustSize): Pizza
  def updateCrustType(p: Pizza, ct: CrustType): Pizza
```

Как показано, каждый метод принимает `Pizza` в качестве входного параметра вместе с другими параметрами, 
а затем возвращает экземпляр `Pizza` в качестве результата.

Когда пишется такой чистый интерфейс, можно думать о нем как о контракте, в котором говорится: 
"Все неабстрактные классы, расширяющие этот trait, _должны_ предоставлять реализацию этих сервисов".

На этом этапе также можно представить, что вы являетесь потребителем этого API. 
Когда вы это сделаете, будет полезно набросать некоторый пример "потребительского" кода, чтобы убедиться, 
что API выглядит так, как хотелось:

```scala
val p = Pizza(Small, Thin, Seq(Cheese))

val p1 = addTopping(p, Pepperoni)
val p2 = addTopping(p1, Onions)
val p3 = updateCrustType(p2, Thick)
val p4 = updateCrustSize(p3, Large)
```

Если с этим кодом все в порядке, как правило, можно начать набрасывать другой API, например API для заказов, 
но, поскольку сейчас рассматривается только `Pizza`, перейдем к созданию конкретной реализации этого интерфейса.

> Обратите внимание, что обычно это двухэтапный процесс. 
> На первом шаге набрасывается контракт API в качестве интерфейса. 
> На втором шаге создается конкретная реализация этого интерфейса. 
> В некоторых случаях в конечном итоге создается несколько конкретных реализаций базового интерфейса.

#### Создание конкретной реализации

Теперь, когда известно, как выглядит `PizzaServiceInterface`, можно создать конкретную реализацию, 
написав тело для всех методов, определенных в интерфейсе:

```scala
object PizzaService extends PizzaServiceInterface:

  def price(p: Pizza): Double = p match
    case Pizza(crustSize, crustType, toppings) =>
      val base  = 6.00
      val crust = crustPrice(crustSize, crustType)
      val tops  = toppings.map(toppingPrice).sum
      base + crust + tops

  def addTopping(p: Pizza, t: Topping): Pizza =
    p.copy(toppings = p.toppings :+ t)

  def removeAllToppings(p: Pizza): Pizza =
    p.copy(toppings = Seq.empty)

  def updateCrustSize(p: Pizza, cs: CrustSize): Pizza =
    p.copy(crustSize = cs)

  def updateCrustType(p: Pizza, ct: CrustType): Pizza =
    p.copy(crustType = ct)

  private def toppingPrice(t: Topping): Double = t match
    case Cheese | Onions => 0.5
    case Pepperoni | BlackOlives | GreenOlives => 0.75
  
  private def crustPrice(s: CrustSize, t: CrustType): Double = (s, t) match
    case (Small | Medium, _) => 0.25
    case (Large, Thin) => 0.50
    case (Large, Regular) => 0.75
    case (Large, Thick) => 1.00
    
end PizzaService
```

Хотя двухэтапный процесс создания интерфейса с последующей реализацией не всегда необходим, 
явное продумывание API и его использования — хороший подход.

Когда все готово, можно использовать `Pizza` и `PizzaService`:

```scala
import PizzaService.*
val p = Pizza(Small, Thin, Seq(Cheese))
// p: Pizza = Pizza(crustSize = Small, crustType = Thin, toppings = List(Cheese))
val p1 = addTopping(p, Pepperoni)
// p1: Pizza = Pizza(crustSize = Small, crustType = Thin, toppings = List(Cheese, Pepperoni))
val p2 = addTopping(p1, Onions)
// p2: Pizza = Pizza(crustSize = Small, crustType = Thin, toppings = List(Cheese, Pepperoni, Onions))
val p3 = updateCrustType(p2, Thick)
// p3: Pizza = Pizza(crustSize = Small, crustType = Thick, toppings = List(Cheese, Pepperoni, Onions))
val p4 = updateCrustSize(p3, Large)
// p4: Pizza = Pizza(crustSize = Large, crustType = Thick, toppings = List(Cheese, Pepperoni, Onions))
println(price(p4))
// 8.75
```

#### Функциональные объекты

В книге ["Программирование на Scala"][programming_in_scala] авторы определяют термин "Функциональные объекты"
как "объекты, которые не имеют никакого изменяемого состояния". 
Это также относится к типам в `scala.collection.immutable`. 
Например, методы в `List` не изменяют внутреннего состояния, а вместо этого создают в результате копию `List`.

Об этом подходе можно думать, как о "гибридном дизайне ФП/ООП", потому что:

- данные моделируются, используя неизменяемые `case class`-ы.
- определяется поведение (методы) того же типа, что и данные.
- поведение реализуется как чистые функции: они не изменяют никакого внутреннего состояния; скорее - возвращают копию.

> Это действительно гибридный подход: как и в дизайне ООП, методы инкапсулированы в класс с данными, 
> но, как это обычно бывает в дизайне ФП, методы реализованы как чистые функции, которые данные не изменяют.

##### Пример

Используя этот подход, можно напрямую реализовать функциональность пиццы в `case class`:

```scala
case class Pizza(
  crustSize: CrustSize,
  crustType: CrustType,
  toppings: Seq[Topping]
):

  // операции этой модели данных
  def price: Double = ... // такая же имплементация, как и выше в PizzaService.price

  def addTopping(t: Topping): Pizza =
    this.copy(toppings = this.toppings :+ t)

  def removeAllToppings: Pizza =
    this.copy(toppings = Seq.empty)

  def updateCrustSize(cs: CrustSize): Pizza =
    this.copy(crustSize = cs)

  def updateCrustType(ct: CrustType): Pizza =
    this.copy(crustType = ct)
```

Обратите внимание, что в отличие от предыдущих подходов, поскольку это методы класса `Pizza`, 
они не принимают ссылку `Pizza` в качестве входного параметра. 
Вместо этого у них есть собственная ссылка на текущий экземпляр пиццы - `this`.

Теперь можно использовать этот новый дизайн следующим образом:

```scala
Pizza(Small, Thin, Seq(Cheese))
  .addTopping(Pepperoni)
  .updateCrustType(Thick)
  .price
```

#### Методы расширения

Методы расширения - подход, который находится где-то между первым (определение функций в сопутствующем объекте) 
и последним (определение функций как методов самого типа).

Методы расширения позволяют создавать API, похожий на API функционального объекта, 
без необходимости определять функции как методы самого типа. 
Это может иметь несколько преимуществ:

- модель данных снова очень лаконична и не упоминает никакого поведения.
- можно задним числом развить функциональность типов дополнительными методами, не изменяя исходного определения.
- помимо сопутствующих объектов или прямых методов типов, 
методы расширения могут быть определены извне в другом файле.

Вернемся к примеру:

```scala
case class Pizza(
  crustSize: CrustSize,
  crustType: CrustType,
  toppings: Seq[Topping]
)

extension (p: Pizza)
  def price: Double =
    pizzaPrice(p) // имплементация выше

  def addTopping(t: Topping): Pizza =
    p.copy(toppings = p.toppings :+ t)

  def removeAllToppings: Pizza =
    p.copy(toppings = Seq.empty)

  def updateCrustSize(cs: CrustSize): Pizza =
    p.copy(crustSize = cs)

  def updateCrustType(ct: CrustType): Pizza =
    p.copy(crustType = ct)
```

В приведенном выше коде различные методы для пиццы определяются как методы расширения (extension methods). 
Код `extension (p: Pizza)` говорит о том, что мы хотим сделать методы доступными для экземпляров `Pizza`, 
и в дальнейшем ссылаемся на экземпляр, который расширяем, как `p`.

Таким образом, получается тот же API, что и раньше:

```scala
Pizza(Small, Thin, Seq(Cheese))
  .addTopping(Pepperoni)
  .updateCrustType(Thick)
  .price
```

При этом методы расширения можно определить в любом другом модуле. 
Как правило, если вы являетесь разработчиком модели данных, вы определяете свои методы расширения в сопутствующем объекте. 
Таким образом, они уже доступны всем пользователям. 
В противном случае методы расширения должны быть импортированы явно, чтобы их можно было использовать.

### Резюме функционального подхода

Определение модели данных в Scala/ФП, как правило, простое: 
моделируются варианты данных с помощью `enum`-ов и составных данных с помощью `case class`-ов. 
Затем, чтобы смоделировать поведение, определяются функции, которые работают со значениями модели данных. 
Были рассмотрены разные способы организации функций:

- можно поместить методы в сопутствующие объекты
- можно использовать модульный стиль программирования, разделяющий интерфейс и реализацию
- можно использовать подход "функциональных объектов" и хранить методы в определенном типе данных
- можно использовать методы расширения, чтобы снабдить модель данных функциональностью

---

**Ссылки:**

- [Scala3 book, domain modeling fp](https://docs.scala-lang.org/scala3/book/domain-modeling-fp.html)
- [Scala3 book, taste modeling](https://docs.scala-lang.org/scala3/book/taste-modeling.html)
- [Scala3 book, taste objects](https://docs.scala-lang.org/scala3/book/taste-objects.html)

[programming_in_scala]: https://www.artima.com/shop/programming_in_scala_5ed

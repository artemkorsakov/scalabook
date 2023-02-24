# Строитель

#### Назначение

Отделение построения сложного объекта от его представления, 
чтобы один и тот же процесс построения мог создавать разные представления.

#### Диаграмма

![Builder](https://upload.wikimedia.org/wikipedia/ru/2/28/Builder.gif)

#### Пример

```scala
class Pizza(var dough: String = "", var sauce: String = "", var topping: String = ""):
  def outputReceipt(): Unit =
    println(s"Dough: $dough\nSauce: $sauce\nTopping: $topping")
trait PizzaBuilder:
  private var maybePizza: Option[Pizza] = None
  
  def getPizza: Pizza = maybePizza.getOrElse(throw new Exception("Pizza has not been created yet"))
  
  def createPizza(): Unit =
    maybePizza = Some(new Pizza)
    
  def buildDough(dough: String): Unit =
    getPizza.dough = dough
    
  def buildSauce(sauce: String): Unit =
    getPizza.sauce = sauce
    
  def buildTopping(topping: String): Unit =
    getPizza.topping = topping
end PizzaBuilder
```

Использование паттерна строитель:

```scala
object HawaiianPizzaBuilder extends PizzaBuilder:
  def createHawaiianPizza(): Pizza =
    createPizza()
    buildDough("cross")
    buildSauce("mild")
    buildTopping("ham+pineapple")
    getPizza
object SpicyPizzaBuilder extends PizzaBuilder:
  def createSpicyPizza(): Pizza =
    createPizza()
    buildDough("pan baked")
    buildSauce("hot")
    buildTopping("pepperoni+salami")
    getPizza
HawaiianPizzaBuilder.createHawaiianPizza().outputReceipt()
// Dough: cross
// Sauce: mild
// Topping: ham+pineapple
SpicyPizzaBuilder.createSpicyPizza().outputReceipt()
// Dough: pan baked
// Sauce: hot
// Topping: pepperoni+salami
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Builder_pattern)

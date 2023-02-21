# Верхнее ограничение типа

Параметры типа и члены абстрактного типа могут быть ограничены определенными диапазонами.
Такие диапазоны ограничивают конкретные значение типа
и, возможно, предоставляют больше информации о членах таких типов.
Верхнее ограничение типа `T <: A` указывает на то что тип `T` относится к подтипу типа `A`.
Приведем пример, демонстрирующий верхнее ограничение для типа класса `PetContainer`:

```scala
abstract class Animal:
  def name: String

abstract class Pet extends Animal

class Cat extends Pet:
  override val name: String = "Cat"

class Dog extends Pet:
  override val name: String = "Dog"

class Lion extends Animal:
  override val name: String = "Lion"

class PetContainer[P <: Pet](p: P):
  def pet: P = p

val dogContainer = PetContainer[Dog](Dog())
val catContainer = PetContainer[Cat](Cat())
```

Класс `PetContainer` принимает тип `P`, который должен быть подтипом `Pet`.
`Dog` и `Cat` - это подтипы `Pet`, поэтому можно создать новые `PetContainer[Dog]` и `PetContainer[Cat]`.
Однако, если попытаться создать `PetContainer[Lion]`, то получим следующую ошибку:

```scala
val lionContainer = PetContainer[Lion](Lion())
// error:
// Type argument App0.this.Lion does not conform to upper bound App0.this.Pet
// val lionContainer = PetContainer[Lion](Lion())
//   
```

Это потому, что `Lion` не является подтипом `Pet`.


---

**Ссылки:**

- [Scala tour, Upper type bounds](https://docs.scala-lang.org/ru/tour/upper-type-bounds.html)

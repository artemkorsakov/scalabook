{%
showPrev = true
prevPage = divide.html
showNext = true
nextPage = co-bind.html
%}

# Divisible

**`Divisible`** — это класс типов, который расширяет возможности **`Divide`** 
и добавляет поддержку "единичного" элемента (аналог моноида для [контравариантных функторов](contravariant-functor.md)). 
Он позволяет не только [разделять данные](divide.md), но и комбинировать их с учётом нейтрального элемента.

`Divisible` определяется для типа `F[_]` и предоставляет две операции:

- **`divide`**:
    - Разделяет значение типа `C` на два компонента `A` и `B` с помощью функции `C => (A, B)`.
    - Комбинирует два значения типа `F[A]` и `F[B]` в значение типа `F[C]`.
- **`conquer`**:
    - Возвращает "нейтральный" элемент типа `F[A]`, который можно использовать как основу для комбинирования.

`Divisible` позволяет не только разделять данные, но и комбинировать их с учётом нейтрального элемента. 
Это полезно в ситуациях, где требуется собирать данные из нескольких частей, 
а также обрабатывать случаи, когда данные отсутствуют или не требуют обработки.

**Примеры использования**

- **Валидация данных**:
    - Если у вас есть валидаторы для нескольких полей, `Divisible` позволяет создать валидатор для составного типа, 
      а также обрабатывать случаи, когда некоторые поля отсутствуют.
- **Парсинг**:
    - Если у вас есть парсеры для нескольких частей данных, `Divisible` позволяет создать парсер для составного типа, 
      а также обрабатывать случаи, когда некоторые части данных отсутствуют.
- **Контекстные вычисления**:
    - В функциональных программах, где требуется декомпозиция данных, `Divisible` может использоваться для работы 
      с контекстами (например, `F[_]` может быть `Option`, `Either`, `List` и т.д.).

`Divisible` полезен в ситуациях, где требуется декомпозиция данных и работа с контекстами, 
а также поддержка нейтрального элемента. Он часто используется в библиотеках для функционального программирования, 
таких как **Cats** или **Scalaz**, для реализации сложных операций над типами.

## Формальное определение

`Divisible` - контравариантный аналог [`Applicative`](applicative.md). 
Он расширяет [`Divide`](divide.md) и [`InvariantApplicative`](invariant-applicative.md).

## Код

```dotty
trait Divisible[F[_]] extends Divide[F], InvariantApplicative[F]:
  def conquer[A]: F[A]

  override def xunit0[A](a: => A): F[A] = conquer

  override def cmap[A, B](fb: F[B])(f: A => B): F[A] =
    divide(conquer[Unit], fb)(c => ((), f(c)))
```

### Законы

- Тождественность (identity): `cmap(fa)(id) == fa`.
- Композиция (composition): `cmap(cmap(fc)(g))(f) == cmap(fc)(g(f(_)))`.
- Композиция (composition):
  ```dotty
  divide(divide(fa1, fa2)(delta), fa3)(delta) == 
  divide(fa1, divide(fa2, fa3)(delta))(delta)
  ```
  , где `delta: A => (A, A) = a => (a, a)`
- Тождественность справа: `divide(fa, conquer)(delta) == fa`.
- Тождественность слева: `divide(conquer, fa)(delta) == fa`.

## Упражнения

### Упражнение 2.15.1: Реализация Divisible для функции.

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Реализуйте экземпляр `Divisible` для типа `Function1[X, R]`, предполагая, что тип `R` образует моноид.

```dotty
given functionDivisible[R: Monoid]: Divisible[[X] =>> Function1[X, R]] with
  override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R = ???
  override def conquer[A]: A => R = ???
```

**Примеры:**

```dotty
case class Data(value: Int)

val isPositive: Data => Boolean = (d: Data) => d.value > 0
val isEven: Data => Boolean = (d: Data) => d.value % 2 == 0
val splitData: Data => (Data, Data) = (d: Data) => (d, d)

given Monoid[Boolean] with
  val empty: Boolean = true
  def combine(a: Boolean, b: Boolean): Boolean = a && b

val isValid: Data => Boolean =
  summon[Divisible[[X] =>> X => Boolean]]
    .divide(isPositive, isEven)(splitData)

isValid(Data(2))   // true && true = true
isValid(Data(-2))  // false && true = false
isValid(Data(3))   // true && false = false
```

@:note(🔍 Показать решение)

Реализация `divide` такая же, как в упражнении 2.14.1: 
используем функцию разбиения `f` для получения пары `(a, b)` из входного значения `c`, 
затем применяем `fa` к `a` и `fb` к `b`, после чего комбинируем результаты с помощью операции моноида для `R`. 
Реализация `conquer` создаёт функцию, которая для любого входного значения типа `A` 
возвращает пустой элемент моноида `Monoid[R].empty`. 
Это удовлетворяет законам `Divisible`, где `conquer` является нейтральным элементом для `divide`. 
Обратите внимание, что параметры `fa` и `fb` передаются по имени (`=>`) для поддержки ленивых вычислений, 
а `conquer` игнорирует свой вход, так как результат не зависит от него.

```dotty
given functionDivisible[R: Monoid]: Divisible[[X] =>> Function1[X, R]] with
  override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
    c => {
      val (a, b) = f(c)
      summon[Monoid[R]].combine(fa(a), fb(b))
    }

  override def conquer[A]: A => R = _ => summon[Monoid[R]].empty
```

@:@

## Схема

```mermaid
classDiagram
    class InvariantFunctor~A~{
        +imap[B](fa: F[A], f: A => B, g: B => A) F[B]
    } 
    class InvariantApplicative~A~{
        +xunit0[A](a: => A) F[A]
    }
    InvariantFunctor <|-- InvariantApplicative   
    class ContravariantFunctor~A~{
        +contramap[A, B](b: F[B])(f: A => B) F[A]
    }
    InvariantFunctor <|-- ContravariantFunctor
    class Divide~A~{
        +divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)) F[C]
    }
    ContravariantFunctor <|-- Divide
    class Divisible~A~{
        +conquer[A]: F[A]
    }
    Divide <|-- Divisible
    InvariantApplicative <|-- Divisible
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Divisible.html)

[sample]: https://gitverse.ru/scalabook/typeclasses

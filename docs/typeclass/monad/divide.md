{%
showPrev = true
prevPage = contravariant-functor.html
showNext = true
nextPage = divisible.html
%}

# Divide

**`Divide`** — это класс типов, который представляет собой обобщение операции, 
обратной по смыслу к [`Semigroup`](../monoid/semigroup.md) или [`Applicative`](applicative.md). 
В то время как `Semigroup` позволяет комбинировать значения (например, сложение или конкатенация), 
`Divide` позволяет "разделять" или "декомпозировать" значения.

`Divide` определяется как тип класса, который для типа `F[_]` предоставляет операцию `divide`. 
Эта операция принимает:

- Два значения типа `F[A]` и `F[B]`.
- Функцию, которая из значения типа `C` извлекает пару `(A, B)`.
- Возвращает значение типа `F[C]`.

Формально: `divide: (F[A], F[B], C => (A, B)) => F[C]`

`Divide` можно рассматривать как операцию, которая позволяет "собрать" значение типа `F[C]` 
из двух компонентов `F[A]` и `F[B]`, используя функцию, которая "разделяет" `C` на `A` и `B`.

**Примеры использования**

- **Валидация данных**:
  Если у вас есть валидаторы для полей `A` и `B`, то с помощью `Divide` можно создать 
  валидатор для составного типа `C`, который содержит оба поля.
- **Парсинг**:
  Если у вас есть парсеры для типов `A` и `B`, то `Divide` позволяет 
  создать парсер для типа `C`, который состоит из `A` и `B`.
- **Контекстные вычисления**:
  В функциональных программах, где требуется декомпозиция данных, 
  `Divide` может использоваться для работы с контекстами (например, `F[_]` может быть `Option`, `Either`, `List` и т.д.).

**Краткие примеры**

- **Валидация формы**:
    - У вас есть валидатор для имени пользователя (`F[String]`) и валидатор для возраста (`F[Int]`).
    - С помощью `Divide` вы можете создать валидатор для всей формы (`F[User]`), где `User` — это тип, содержащий имя и возраст.
- **Парсинг JSON**:
    - У вас есть парсер для поля `"name"` (`F[String]`) и парсер для поля `"age"` (`F[Int]`).
    - С помощью `Divide` вы можете создать парсер для всего JSON-объекта (`F[Person]`), где `Person` — это тип, содержащий оба поля.
- **Работа с контекстами**:
    - У вас есть два значения в контексте `Option`: `Option[String]` (имя) и `Option[Int]` (возраст).
    - С помощью `Divide` вы можете создать значение `Option[User]`, где `User` — это тип, содержащий оба поля.

## Формальное определение

`Divide` - контравариантный аналог [`Apply`](apply.md). 
Он расширяет [контравариантный функтор](contravariant-functor.md) и дополнительно реализует
операцию `divide`: 

`def divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)): F[C]`

Законы `Divide`:

- Тождественность (identity): Если определен метод идентификации `id` такой, что: `id(a) == a`,
  тогда `cmap(fa)(id) == fa`.
- Композиция (composition): Если определены два метода `f: A => B` и `g: B => C`,
  тогда `cmap(cmap(fc)(g))(f) == cmap(fc)(g(f(_)))`.
- Композиция (composition): `divide(divide(fa1, fa2)(delta), fa3)(delta) == divide(fa1, divide(fa2, fa3)(delta))(delta)`,
  где `delta: A => (A, A) = a => (a, a)`.

## Код

```dotty
trait Divide[F[_]] extends ContravariantFunctor[F]:
  def divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)): F[C]

  def tuple2[A, B](fa: => F[A], fb: => F[B]): F[(A, B)] = divide(fa, fb)(identity)
```

### Унарная функция

```dotty
given functionDivide[R: Monoid]: Divide[[X] =>> Function1[X, R]] with
  override def cmap[A, B](function: B => R)(f: A => B): A => R =
    a => function(f(a))

  override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
    c => {
      val (a, b) = f(c)
      summon[Monoid[R]].combine(fa(a), fb(b))
    }
```

### Законы

Композиция (composition):

`divide(divide(fa1, fa2)(delta), fa3)(delta) == divide(fa1, divide(fa2, fa3)(delta))(delta), где delta: A => (A, A) = a => (a, a)`

```dotty
... // Законы контравариантного функтора

def checkDivideComposition[F[_], A](
    fa1: F[A],
    fa2: F[A],
    fa3: F[A],
    a: A
)(using div: Divide[F]): ValidatedNel[String, Unit] =
  import div.*
  def delta: A => (A, A) = a => (a, a)

  Either.cond[String, Unit](
    divide(divide(fa1, fa2)(delta), fa3)(delta) ==
      divide(fa1, divide(fa2, fa3)(delta))(delta),
    (),
    "Не соблюдается Композиция Divide: divide(divide(fa1, fa2)(delta), fa3)(delta) == divide(fa1, divide(fa2, fa3)(delta))(delta), где delta: A => (A, A) = a => (a, a)"
  ).toValidatedNel
end checkDivideComposition
```

## Схема

```mermaid
classDiagram
    class InvariantFunctor~A~{
        +xmap[B](fa: F[A], f: A => B, g: B => A) F[B]
    } 
    class ContravariantFunctor~A~{
        +contramap[A, B](b: F[B])(f: A => B) F[A]
    }
    InvariantFunctor <|-- ContravariantFunctor
    class Divide~A~{
        +divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)) F[C]
    }
    ContravariantFunctor <|-- Divide
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Divide.html)

[sample]: https://gitverse.ru/scalabook/typeclasses

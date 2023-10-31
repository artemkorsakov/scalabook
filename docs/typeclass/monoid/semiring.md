# Полукольцо

В то время как [группа](group.md), [моноид](monoid.md) и [полугруппа](semigroup.md) 
определяются множеством и одной операцией, полукольцо определяется множеством и двумя операциями. 
Учитывая множество `R` и операции `+` и `*`, 
мы говорим, что `(R, +, *)` - это полукольцо, если оно удовлетворяет следующим свойствам:

- `(R, +)` является [коммутативным моноидом](cmonoid.md)
- `(R, *)` является [полугруппой](semigroup.md)

Для полугруппы должны соблюдаться законы коммутативного моноида по сложению:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ M`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность_ (_identity_): существует `∃ e ∈ M` такой, что для `∀ x ∈ M` выполняется `e + x = x + e = x`
- _Коммутативность_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`

и законы полугруппы по умножению:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x * y ∈ S`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x * y) * z = x * (y * z)`

## Описание

```dotty
trait MultiplicativeSemigroup[A]:
  def times(x: A, y: A): A

  def pow(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else Some(positivePow(x, n))

  private def positivePow(a: A, n: Int): A =
    @tailrec def loop(b: A, k: Int, extra: A): A =
      if k == 1 then times(b, extra)
      else
        val x = if (k & 1) == 1 then times(b, extra) else extra
        loop(times(b, b), k >>> 1, x)

    if n == 1 then a else loop(a, n - 1, a)
    
trait Semiring[A] extends CMonoid[A], MultiplicativeSemigroup[A]    
```

## Примеры

### Числа относительно сложения с 0 и умножения

`(Z, +, *)`

```dotty
given RingWithUnity[Int] with
  val empty: Int = 0
  val one: Int = 1
  def combine(x: Int, y: Int): Int = x + y
  def times(x: Int, y: Int): Int = x * y

  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.CMonoid

CMonoid.combine(100, CMonoid.empty[Int]) // 100
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)

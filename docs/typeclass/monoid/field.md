# Поле

## Формальное определение

Поля расширяют [Евклидово кольцо](euclidean-ring.md), добавляя операцию обратную умножению для всех элементов,
за исключением нуля по сложению.

`Field[A]` поддерживает следующие операции:

- `reciprocal` (`a.reciprocal`): обратная операция умножению. Мультипликативный обратный `a`, т.е. `one/a`.
- `div` (`/`, `a / b`): деление `a` на `b`.

Формально, _поле_ — алгебра над множеством \\(\mathbf{F}\\), 
образующая [коммутативную группу](abgroup.md) по сложению \\(\mathbf{+}\\) над \\(\mathbf{F}\\) с нейтральным элементом \\(\mathbf{0}\\) 
и коммутативную группу по умножению \\(\mathbf{\*}\\) над ненулевыми элементами \\(\mathbf{F} \backslash \begin{Bmatrix} \mathbf{0} \end{Bmatrix}\\), 
при выполняющемся свойстве дистрибутивности умножения относительно сложения. 
Подразумевается также применимость операции \\(\mathbf{\*}\\) к нулевому элементу по сложению 
с сохранением свойства дистрибутивности на всём множестве \\(\mathbf{F}\\).

Если раскрыть определение, то множество \\(\mathbf{F}\\) с введёнными на нём алгебраическими операциями 
сложения \\(\mathbf{+}\\) и умножения \\(\mathbf{\*}\\)
(\\(\mathbf{+}\colon \mathbf{F} \times \mathbf{F} \to \mathbf{F},\mathbf{\*}\colon \mathbf{F} \times \mathbf{F} \to \mathbf{F}\\), 
то есть \\(\forall a, b \in F \quad (a + b) \in F, (a \* b) \in F\\) 
называется _полем_ \\(\left< F, +, \* \right>\\), если выполнены следующие аксиомы:

- _Коммутативность сложения_: \\(\forall a, b \in F \quad a + b = b + a\\).
- _Ассоциативность сложения_: \\(\forall a, b, c \in F \quad (a + b) + c = a + (b + c)\\).
- _Существование нулевого элемента_: \\(\exists 0 \in F: \forall a \in F \quad a + 0 = a\\).
- _Существование противоположного элемента_: \\(\forall a \in F \quad \exists (-a) \in F: \quad a + (-a) = 0\\).
- _Коммутативность умножения_: \\(\forall a, b \in F \quad a \* b = b \* a\\).
- _Ассоциативность умножения_: \\(\forall a, b, c \in F \quad (a \* b) \* c = a \* (b \* c)\\).
- _Существование единичного элемента_: \\(\exists e \in F \backslash \begin{Bmatrix} 0 \end{Bmatrix}: \forall a \in F \quad  a \* e = a\\).
- _Существование обратного элемента для ненулевых элементов_: \\((\forall a \in F: a \neq 0) \quad \exists a^{-1} \in F: \quad a \* a^{-1} = e\\).
- _Дистрибутивность умножения относительно сложения_: \\(\forall a, b, c \in F \quad (a + b) \* c = (a \* c) + (b \* c)\\).

## Определение в виде кода на Scala

```dotty
trait Field[A] extends EuclideanRing[A]:
  extension (a: A) def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)
```

## Законы в виде кода на Scala

```dotty
```

## Примеры

### Рациональные числа со сложением и умножением

## Реализация

### Реализация в Spire

```dotty
import spire.math.Real.apply

15.reciprocal
// val res0: spire.math.Real = 1/15
15 / 3
// val res1: Int = 5
15.24.ceil
// val res2: spire.math.Real = 16
15.24.floor
// val res3: spire.math.Real = 15
15.24.round
// val res4: spire.math.Real = 15
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D0%B5_(%D0%B0%D0%BB%D0%B3%D0%B5%D0%B1%D1%80%D0%B0))

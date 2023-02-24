# Глава 1. Построение абстракций с помощью процедур

## 1.2. Процедуры и порождаемые ими процессы

### 1.2.2. Древовидная рекурсия

#### Упражнение 1.11

> Функция `f` определяется правилом: `f(n) = n`, если `n < 3`, 
> и `f(n) = f(n − 1) + f(n − 2) + f(n − 3)`, если `n ≥ 3`. 
> 
> Напишите процедуру, вычисляющую `f` с помощью рекурсивного процесса. 
> 
> Напишите процедуру, вычисляющую `f` с помощью итеративного процесса.

Рекурсивный процесс:

```scala
def recursion(n: Int): Int =
  if n < 3 then n
  else recursion(n - 1) + recursion(n - 2) + recursion(n - 3)
```

Итеративный процесс:

```scala
def iteration(n: Int): Int =
  def loop(a: Int, b: Int, c: Int, count: Int): Int =
    if count == 0 then c
    else loop(a + b + c, a, b, count - 1)
  loop(2, 1, 0, n)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-11.worksheet.sc)

#### Упражнение 1.12

> Приведенная ниже таблица называется треугольником Паскаля (Pascal’s triangle):
> 
> 1
> 
> 1 1
> 
> 1 2 1
> 
> 1 3 3 1
> 
> 1 4 6 4 1
> 
> . . .
> 
> Все числа по краям треугольника равны `1`, 
> а каждое число внутри треугольника равно сумме двух чисел над ним. 
> 
> Напишите процедуру, вычисляющую элементы треугольника Паскаля с помощью рекурсивного процесса.

```scala
def pascalsTriangle(n: Int, k: Int): Int =
  if n == 0 || k == n || k == 0 then 1
  else pascalsTriangle(n - 1, k - 1) + pascalsTriangle(n - 1, k)

pascalsTriangle(4, 2) // 6
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-12.worksheet.sc)

#### Упражнение 1.13

> Докажите, что **Fib(n)** есть целое число, ближайшее к **φ<sup>n</sup>/√5**, где **φ = (1 + √5)/2**. 
> 
> Указание: пусть **ψ = (1 − √5)/2**. 
> С помощью определения чисел Фибоначчи и индукции докажите, что **Fib(n) = (φ<sup>n</sup> − ψ<sup>n</sup>)/√5**.

Доказательство:

- Заметим, что **φ<sup>2</sup> = φ<sup>1</sup> + 1**:
  - **φ<sup>2</sup> = (1 + √5)<sup>2</sup>/4 = (6 + 2√5)/4 = 1 + (2 + 2√5)/4 = 1 + φ**
- а также, что **ψ<sup>2</sup> = ψ<sup>1</sup> + 1**:
  - **ψ<sup>2</sup> = (1 - √5)<sup>2</sup>/4 = (6 - 2√5)/4 = 1 + (2 - 2√5)/4 = 1 + ψ**  
- **n == 0**, тогда **Fib(0) = (φ<sup>0</sup> − ψ<sup>0</sup>)/√5 = (1 − 1)/√5 = 0** - верно.
- **n > 0**, 
  - **Fib(n) =** 
  - **Fib(n - 1) + Fib(n - 2) =**
  - **(φ<sup>n-1</sup> − ψ<sup>n-1</sup>)/√5 + (φ<sup>n-2</sup> − ψ<sup>n-2</sup>)/√5 =**
  - **((φ<sup>n-1</sup> + φ<sup>n-2</sup>) - (ψ<sup>n-1</sup> +  ψ<sup>n-2</sup>))/√5 =**
  - **(φ<sup>n-2</sup> * (φ + 1) - ψ<sup>n-2</sup> * (ψ +  1))/√5 =**
  - **(φ<sup>n-2</sup> * φ<sup>2</sup> - ψ<sup>n-2</sup> * ψ<sup>2</sup>)/√5 =**
  - **(φ<sup>n</sup> - ψ<sup>n</sup>)/√5**

---

**Ссылки:**
- [Упражнение 1.11 - 1.13](https://web.mit.edu/6.001/6.037/sicp.pdf#page=81)

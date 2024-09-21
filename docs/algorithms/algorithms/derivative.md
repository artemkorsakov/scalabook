# Символьное дифференцирование

Рассмотрим построение процедуры, которая производит символьное дифференцирование алгебраических выражений. 
Эта процедура принимает в качестве аргументов алгебраическое выражение и переменную,
и возвращает производную выражения по отношению к этой переменной. 
Например, если аргументами к процедуре служат \\(ax^{2} + bx + c\\) и x, 
процедура должна возвращать 2ax + b.

## Программа дифференцирования с абстрактными данными

Чтобы упростить задачу, рассмотрим простую программу символьного дифференцирования, 
которая работает с выражениями, построенными только при помощи операций сложения и умножения с двумя аргументами. 
Дифференцировать любое такое выражение можно, применяя следующие правила редукции:

- \\(\frac{\partial c}{\partial x} = 0\\) для константы либо переменной, отличной от x
- \\(\frac{\partial x}{\partial x} = 1\\)
- \\(\frac{\partial (u + v)}{\partial x} = \frac{\partial u}{\partial x} + \frac{\partial v}{\partial x}\\)
- \\(\frac{\partial (uv)}{\partial x} = u(\frac{\partial v}{\partial x}) + v(\frac{\partial u}{\partial x})\\)

Заметим, что два последних правила по сути своей рекурсивны. 
То есть, чтобы получить производную суммы, нам сначала нужно получить производные слагаемых и их сложить. 
Каждое из них в свою очередь может быть выражением, которое требуется разложить на составляющие. 
Разбивая их на все более мелкие части, мы в конце концов дойдем до стадии, 
когда все части являются либо константами, либо переменными, и их производные будут равны либо 0, либо 1.

## Составление выражения и производной от выражения

Определим выражение и выполняемые над ним операции:

```dotty
object Derivative:
  enum Operation:
    case Add, Mul

  enum Expression:
    case Number(value: Int)
    case Variable(name: String)
    case Complex(oper: Operation, left: Expression, right: Expression)
```

Предположим, что у нас уже есть процедуры, которые реализуют следующие селекторы, конструкторы и предикаты:

- `isNumber(e: String)`: Является ли `e` числом?
- `isVariable(e: String)` Является ли `e` переменной?
- `isSameVariable(v1: Variable, v2: Variable)` Являются ли `v1` и `v2` одной и той же переменной?
- `isSum(e: String)` Является ли `e` суммой?
- `addend(e: String)` Первое слагаемое суммы `e`.
- `augend(e: String)` Второе слагаемое суммы `e`.
- `isProduct(e: String)` Является ли `e` произведением?
- `multiplier(e: String)` Первый множитель произведения `e`.
- `multiplicand(e: String)` Второй множитель произведения `e`.
- `makeSum(exp1: Expression, exp2: Expression)` Строит сумму `exp1` и `exp2`.
- `makeProduct(exp1: Expression, exp2: Expression)` Строит произведение `exp1` и `exp2`.

При помощи этих процедур можно выразить правила получения выражения:

- Числовое выражение: если входная строка представляет собой число, 
  она преобразуется в `Expression.Number` со значением целого числа, полученным из строки.
- Выражение переменная: если входная строка представляет собой переменную, 
  она преобразуется в `Expression.Variable` со строковым значением имени переменной.
- Сложное выражение с операцией сложения: если входная строка представляет собой сумму (например, "a + b"), 
  она преобразуется в объект `Expression.Complex` с операцией `Operation.Add` и двумя подвыражениями: слагаемым (левый операнд) и добавляемым (правый операнд).
- Сложное выражение с операцией умножения: если входная строка представляет собой произведение (например, "a * b"), 
  она преобразуется в объект `Expression.Complex` с операцией `Operation.Mul` и двумя подвыражениями: множителем (левый операнд) и множимым (правый операнд). 
- Если входная строка не соответствует ни одному из этих шаблонов, функция возвращает значение `Left` с сообщением об ошибке, указывающим, что тип выражения неизвестен.

```dotty
def parseExpression(exp: String): Either[String, Expression] =
  if isNumber(exp) then Right(Expression.Number(exp.toInt))
  else if isVariable(exp) then Right(Expression.Variable(exp))
  else if isSum(exp) then
    for
      a1 <- parseExpression(addend(exp))
      a2 <- parseExpression(augend(exp))
    yield makeSum(a1, a2)
  else if isProduct(exp) then
    for
      m1 <- parseExpression(multiplier(exp))
      m2 <- parseExpression(multiplicand(exp))
    yield makeProduct(m1, m2)
  else Left(s"неизвестный тип выражения -- DERIV: $exp")
```

Правила дифференцирования можно выразить в виде следующей процедуры:

- Числа: производная от числа равна `0`.
- Переменные: если переменная совпадает с заданной, то производная равна `1`, иначе - `0`.
- Сложение: производная от суммы равна сумме производных слагаемых.
- Умножение: производная от произведения равна сумме произведений производной одного множителя на другой множитель и наоборот.

```dotty
def deriv(exp: Expression, variabe: Expression.Variable): Expression =
  exp match
    case _: Expression.Number => Expression.Number(0)
    case v: Expression.Variable if isSameVariable(v, variabe) =>
      Expression.Number(1)
    case _: Expression.Variable => Expression.Number(0)
    case Expression.Complex(Operation.Add, left, right) =>
      makeSum(deriv(left, variabe), deriv(right, variabe))
    case Expression.Complex(Operation.Mul, left, right) =>
      makeSum(
        makeProduct(deriv(left, variabe), right),
        makeProduct(left, deriv(right, variabe))
      )
```

Процедура `deriv` заключает в себе весь алгоритм дифференцирования. 
Поскольку она выражена в терминах абстрактных данных, она будет работать, 
как бы мы ни представили алгебраические выражения. 

## Представление алгебраических выражений

Представление данных для задачи дифференцирования будет следующим:

Число - непустая строка, содержащая только цифры:

```dotty
def isNumber(e: String): Boolean =
  e.nonEmpty && e.forall(_.isDigit)
```

Переменные — это символы.

```dotty
def isVariable(e: String): Boolean =
  e.length == 1 && e.headOption.exists(_.isLetter)
```

Сумма — это строка, содержащая "+":

```dotty
def isSum(e: String): Boolean = e.contains('+')
```

Первое слагаемое:

```dotty
def addend(e: String): String =
  e match
    case s"${f}+${s}" => f.trim
    case _            => ""
```

Второе слагаемое:

```dotty
def augend(e: String): String =
  e match
    case s"${f}+${s}" => s.trim
    case _            => ""
```

Произведение — это строка, содержащая "*":

```dotty
def isProduct(e: String): Boolean = e.contains('*')
```

Первый множитель произведения `e`:

```dotty
def multiplier(e: String): String =
  e match
    case s"${f}*${s}" => f.trim
    case _            => ""
```

Второй множитель произведения `e`:

```dotty
def multiplicand(e: String): String =
  e match
    case s"${f}*${s}" => s.trim
    case _            => ""
```

Две переменные одинаковы, если для представляющих их символов выполняется равенство:

```dotty
def isSameVariable(
    v1: Expression.Variable,
    v2: Expression.Variable
): Boolean =
  v1.name == v2.name
```

Формирование суммы и произведения:

```dotty
def makeSum(exp1: Expression, exp2: Expression): Expression =
  Expression.Complex(Operation.Add, exp1, exp2)

def makeProduct(exp1: Expression, exp2: Expression): Expression =
  Expression.Complex(Operation.Mul, exp1, exp2)
```

Таким образом, осталось только соединить это представление с алгоритмом, заключенным в процедуре `deriv`, 
и мы получаем работающую программу символьного дифференцирования. 
Посмотрим на некоторые примеры ее поведения:

```dotty
println(
  s"Производная от 5 равна ${deriv(parseExpression("5").getOrElse(Number(0)), x).show}"
)
// Производная от 5 равна 0
println(
  s"Производная от x равна ${deriv(parseExpression("x").getOrElse(Number(0)), x).show}"
)
// Производная от x равна 1
println(
  s"Производная от 2*x равна ${deriv(parseExpression("2*x").getOrElse(Number(0)), x).show}"
)
// Производная от 2*x равна ((0*x) + (2*1))
println(
  s"Производная от x+x равна ${deriv(parseExpression("x+x").getOrElse(Number(0)), x).show}"
)
// Производная от x+x равна (1 + 1)
println(
  s"Производная от x*y равна ${deriv(parseExpression("x*y").getOrElse(Number(0)), x).show}"
)
// Производная от x*y равна ((1*y) + (x*0))
```

Ответы, которые выдает программа, правильны; однако их нужно упрощать. 
Верно, что \\(\frac{\partial (xy)}{\partial x} = 1 \times y + x \times 0\\), но хотелось бы, чтобы программа знала, 
что \\(x \times 0 = 0, 1 \times y = y\\), а \\(0 + y = y\\). Ответом на последний пример должно быть просто y.

## Упрощение выражения

Доработаем процедуру `makeSum` для создания сложной операции суммы так, что если оба слагаемых являются числами, 
процедура вернет сумму. Кроме того, если одно из слагаемых равно `0`, то `makeSum` вернет другое слагаемое.

```dotty
def makeSum(exp1: Expression, exp2: Expression): Expression =
  (exp1, exp2) match
    case (Number(0), exp2)        => exp2
    case (exp1, Number(0))        => exp1
    case (Number(n1), Number(n2)) => Number(n1 + n2)
    case _                        => Complex(Add, exp1, exp2)
```

Подобным же образом мы изменим и `makeProduct`, так, чтобы встроить в него правила, 
что нечто, умноженное на `0`, есть `0`, а умноженное на `1` равно самому себе:

```dotty
def makeProduct(exp1: Expression, exp2: Expression): Expression =
  (exp1, exp2) match
    case (Number(0), _)           => Number(0)
    case (_, Number(0))           => Number(0)
    case (Number(1), exp2)        => exp2
    case (exp1, Number(1))        => exp1
    case (Number(n1), Number(n2)) => Number(n1 * n2)
    case _                        => Complex(Mul, exp1, exp2)
```

Вот как эта версия работает на тех же примерах:

```dotty
println(
  s"Производная от 5 равна ${deriv(parseExpression("5").getOrElse(Number(0)), x).show}"
)
// Производная от 5 равна 0
println(
  s"Производная от x равна ${deriv(parseExpression("x").getOrElse(Number(0)), x).show}"
)
// Производная от x равна 1
println(
  s"Производная от 2*x равна ${deriv(parseExpression("2*x").getOrElse(Number(0)), x).show}"
)
// Производная от 2*x равна 2
println(
  s"Производная от x+x равна ${deriv(parseExpression("x+x").getOrElse(Number(0)), x).show}"
)
// Производная от x+x равна 2
println(
  s"Производная от x*y равна ${deriv(parseExpression("x*y").getOrElse(Number(0)), x).show}"
)
// Производная от x*y равна y
println(
  s"Производная от x*x*x равна ${deriv(parseExpression("x*x*x").getOrElse(Number(0)), x).show}"
)
// Производная от x*x*x равна ((x*x) + (x*(x + x)))
```

Хотя это заметное улучшение, последний пример показывает (простейший пример - \\(3x^{2}\\)), что нужно многое еще сделать, 
прежде чем получится программа, приводящую выражения к форме, которую можно считать «простейшей». 
Задача алгебраического упрощения сложна, среди прочего, еще и потому, что форма, 
которая является простейшей для одних целей, может таковой не являться для других.

---

**Ссылки:**

- [Abelson Harold, Sussman Gerald Jay, Sussman Julie - Structure and Interpretation of Computer Programs][SICP]
- [Eli Bendersky's solution](https://eli.thegreenplace.net/tag/sicp)

[SICP]: https://web.mit.edu/6.001/6.037/sicp.pdf

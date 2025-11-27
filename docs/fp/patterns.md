{%
showPrev = true
prevPage = stream-processing/applications.html
showNext = true
nextPage = patterns/adt.html
%}

# Паттерны проектирования в ФП

В современной разработке мы постоянно сталкиваемся с необходимостью создавать **надёжные, поддерживаемые и расширяемые** системы. 
Две технологии, помогающие нам это сделать — 
это **Алгебраические типы данных (АТД)** и **Tagless Final**.

Представьте себе эволюцию:

### Этап 1: Программирование без шаблонов

Жёстко закодированные значения, сложно тестировать, невозможно расширять.

```dotty
def calculateTax(income: Double): Double =
  val database = new DatabaseConnection()
  val rate     = database.getTaxRate()
  income * rate
```

### Этап 2: Алгебраические типы данных

Чёткая структура данных, безопасность типов, сопоставление с шаблоном.

```dotty
enum TaxResult:
  case Calculated(amount: Double)
  case Exempt(reason: String)
  case Error(message: String)
```

### Этап 3: Tagless Final

Полное разделение бизнес-логики и реализации, лёгкое тестирование.

```dotty
trait TaxRepository[F[_]]:
  def getTaxRate: F[Double]

def calculateTax[F[_]: Monad](
    income: Double,
    repo: TaxRepository[F]
): F[TaxResult]
```

### Итого 

Алгебраические типы данных дают нам:

- **Безопасность** — компилятор проверяет корректность использования типов
- **Ясность** — структура данных точно отражает предметную область
- **Надёжность** — невозможно создать некорректное состояние

Tagless Final добавляет:

- **Гибкость** — одна бизнес-логика работает с разными реализациями
- **Тестируемость** — легко подменять реальные зависимости на тестовые
- **Композируемость** — программы собираются как из кубиков Лего

АТД и Tagless Final — не конкуренты, а **дополняющие друг друга подходы**:

- **АТД** помогают моделировать данные предметной области
- **Tagless Final** помогает структурировать вычисления над этими данными

```dotty
// АТД для моделирования данных
enum PaymentError:
  case InsufficientFunds(balance: Double)
  case NetworkError(message: String)

// Tagless Final для структурирования вычислений
trait PaymentService[F[_]]:
  def processPayment(amount: Double): F[Either[PaymentError, Unit]]
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]

[sample]: https://gitverse.ru/scalabook/fp

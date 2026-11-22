# Design Patterns in FP

In modern development, we constantly face the need to create **reliable, maintainable, and extensible** systems.
Two technologies that help us achieve this are:
**Algebraic Data Types (ADTs)** and **Tagless Final**.

Consider the evolution:

### Stage 1: Programming Without Patterns

Hardcoded values, hard to test, impossible to extend.

```dotty
def calculateTax(income: Double): Double =
  val database = new DatabaseConnection()
  val rate     = database.getTaxRate()
  income * rate
```

### Stage 2: Algebraic Data Types

Clear data structure, type safety, pattern matching.

```dotty
enum TaxResult:
  case Calculated(amount: Double)
  case Exempt(reason: String)
  case Error(message: String)
```

### Stage 3: Tagless Final

Complete separation of business logic and implementation, easy testing.

```dotty
trait TaxRepository[F[_]]:
  def getTaxRate: F[Double]

def calculateTax[F[_]: Monad](
    income: Double,
    repo: TaxRepository[F]
): F[TaxResult]
```

### Summary 

Algebraic data types give us:

- **Safety** — the compiler checks type correctness
- **Clarity** — data structure exactly reflects the domain
- **Reliability** — it is impossible to create an invalid state

Tagless Final adds:

- **Flexibility** — one business logic works with different implementations
- **Testability** — easily replace real dependencies with test doubles
- **Composability** — programs are assembled like Lego blocks

ADTs and Tagless Final are **not competitors, but complementary approaches**:

- **ADTs** help model domain data
- **Tagless Final** helps structure computations over that data

```dotty
// ADT for modeling data
enum PaymentError:
  case InsufficientFunds(balance: Double)
  case NetworkError(message: String)

// Tagless Final for structuring computations
trait PaymentService[F[_]]:
  def processPayment(amount: Double): F[Either[PaymentError, Unit]]
```

---

**Links:**

- [Link to repository with exercises][sample]

[sample]: https://gitverse.ru/scalabook/fp

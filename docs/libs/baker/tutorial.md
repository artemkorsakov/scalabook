{%
showPrev = true
prevPage = ../baker.html
showNext = true
nextPage = ../spire.html
%}

# Создание первого процесса оркестрации в Baker

> Весь код ниже представлен на версии Scala 2.13.18, 
> т.к. библиотека Baker пока не может использоваться на Scala 3 из-за проблем с макросами.

## Общая информация

Это руководство шаг за шагом проведет вас через полный процесс создания рабочего процесса оркестрации с использованием библиотеки Baker. 
Вы узнаете, как моделировать бизнес-процесс, определять взаимодействия с внешними сервисами, компилировать рецепт и выполнять его.

**Что вы создадите:** Процесс обработки заказа в интернет-магазине, который проверяет наличие товаров на складе 
и либо отправляет заказ, либо отменяет его.

## Введение: Бизнес-контекст

Представьте, что вы работаете инженером-программистом в современной компании электронной коммерции. 
Компания строит интернет-магазин, состоящий из множества микросервисов. 
Ваша задача — организовать (оркестровать) процесс обработки заказов.

**Бизнес-требования:**

> После того, как клиент разместил заказ, нам необходимо проверить наличие товаров на складе. 
> Информация об уровне запасов доступна через `StockService`. Если товаров достаточно, отправьте заказ, вызвав `ShippingService`. 
> Если товаров недостаточно, отмените заказ, вызвав `CancellationService`.

**Почему это хороший пример для Baker:** Здесь есть четкая последовательность шагов, 
ветвление логики (успех/неудача) и зависимость от внешних сервисов — классический сценарий для оркестрации.

## Шаг 1: Определение сенсорного события

### Что такое сенсорное событие?

**Сенсорное событие** — это событие, которое приходит в процесс из внешнего мира. 
Оно запускает (создает) новый экземпляр рецепта. Без сенсорного события процесс не может начаться. 
В нашем случае сенсорным событием является размещение заказа клиентом.

### Реализация события `OrderPlaced`

Создайте case-класс, представляющий событие размещения заказа:

```dotty
package ru.scalabook.baker

import ru.scalabook.baker.OrderPlaced._

case class OrderPlaced(
    orderId: OrderId,           // Уникальный идентификатор заказа
    customerId: CustomerId,     // Идентификатор клиента
    address: Address,           // Адрес доставки
    productIds: List[ProductId] // Список ID товаров в заказе
)

object OrderPlaced {
  // Здесь лучше использовать уточненные типы, но для простоты пока оставим просто алиасы
  type OrderId    = String
  type CustomerId = String
  type ProductId  = String
}
```

**Компоненты события (ингредиенты):**

| Поле         | Описание                                                                                         |
|--------------|--------------------------------------------------------------------------------------------------|
| `orderId`    | Уникальный идентификатор заказа. Используется для отслеживания и логирования.                    |
| `customerId` | Идентификатор клиента. Может использоваться для проверки кредитного лимита или истории заказов.  |
| `address`    | Структурированный объект с деталями доставки.                                                    |
| `productIds` | Список ID товаров, которые заказал клиент.                                                       |

### Определение класса `Address`

`Address` — это простой data-класс (ингредиент), который содержит детали адреса доставки:

```dotty
package ru.scalabook.baker

case class Address(
    street: String,  // Улица и номер дома
    city: String,    // Город
    zipCode: String, // Почтовый индекс
    country: String  // Страна
)
```

> **Примечание:** Ингредиенты в Baker неизменяемы и плоские (не поддерживают иерархию наследования). 
> Это сделано для простоты и предсказуемости.

## Шаг 2: Определение взаимодействий (интерфейсов)

### Что такое взаимодействие?

**Взаимодействие** — это единица работы, которую Baker выполняет в рамках процесса. Оно:

- Требует определенные **входные ингредиенты** (данные).
- Выдает одно или несколько **выходных событий** (результатов).
- Реализуется обычным кодом (HTTP-вызов, работа с БД, вычисления).

На этом этапе мы **только объявляем схемы взаимодействий** (их интерфейсы). Реализации будут предоставлены позже. 
Это ключевая особенность Baker: разделение декларативной логики (рецепт) и императивной реализации (код).

### 2.1 Взаимодействие `CheckStock` (проверка наличия)

**Назначение:** Проверить, есть ли все запрошенные товары на складе.

**Входные ингредиенты:**

- `orderId: OrderId` — идентификатор заказа (для логирования и трассировки)
- `productIds: List[ProductId]` — список товаров для проверки

**Выходные события:**

- `SufficientStock()` — если **все** товары есть в наличии
- `OrderHasUnavailableItems(unavailableProductIds: List[ProductId])` — если **некоторые** товары отсутствуют (с указанием каких именно)

**Код:**

```dotty
package ru.scalabook.baker

import com.ing.baker.recipe.scaladsl.{Event, Ingredient, Interaction}
import ru.scalabook.baker.OrderPlaced.{OrderId, ProductId}

object CheckStock {
  // Запечатанный трейт для типобезопасного представления результатов
  sealed trait Outcome
  case class SufficientStock() extends Outcome
  case class OrderHasUnavailableItems(unavailableProductIds: List[String])
      extends Outcome

  val interaction: Interaction = Interaction(
    name = "CheckStock", // Уникальное имя взаимодействия в рамках рецепта
    inputIngredients = Seq(
      Ingredient[OrderId](name = "orderId"),
      Ingredient[List[ProductId]](name = "productIds")
    ),
    output = Seq(
      Event[SufficientStock],
      Event[OrderHasUnavailableItems]
    )
  )
}
```

> **Важно:** Имена ингредиентов (`orderId`, `productIds`) должны точно совпадать с именами полей в событиях, которые их предоставляют. 
> Baker использует имена для связывания данных.

### 2.2 Взаимодействие `ShipOrder` (отправка заказа)

**Назначение:** Отправить заказ клиенту (вызвать службу доставки).

**Входные ингредиенты:**

- `orderId: OrderId` — идентификатор заказа
- `address: Address` — адрес доставки

**Выходное событие:**

- `OrderShipped()` — заказ успешно отправлен

**Код:**

```dotty
package ru.scalabook.baker

import com.ing.baker.recipe.scaladsl.{Event, Ingredient, Interaction}
import ru.scalabook.baker.OrderPlaced.OrderId

object ShipOrder {
  case class OrderShipped()

  val interaction: Interaction = Interaction(
    name = "ShipOrder",
    inputIngredients = Seq(
      Ingredient[OrderId](name = "orderId"),
      Ingredient[Address](name = "address")
    ),
    output = Seq(
      Event[OrderShipped]
    )
  )
}
```

### 2.3 Взаимодействие `CancelOrder` (отмена заказа)

**Назначение:** Отменить заказ из-за отсутствия товаров.

**Входные ингредиенты:**

- `orderId: OrderId` — идентификатор заказа
- `unavailableProductIds: List[ProductId]` — список недоступных товаров (получается из события `OrderHasUnavailableItems`)

**Выходное событие:**

- `OrderCancelled()` — заказ успешно отменен

**Код:**

```dotty
package ru.scalabook.baker

import com.ing.baker.recipe.scaladsl.{Event, Ingredient, Interaction}
import ru.scalabook.baker.OrderPlaced.{OrderId, ProductId}

object CancelOrder {

  case class OrderCancelled()

  val interaction: Interaction = Interaction(
    name = "CancelOrder",
    inputIngredients = Seq(
      Ingredient[OrderId](name = "orderId"),
      Ingredient[List[ProductId]](name = "unavailableProductIds")
    ),
    output = Seq(
      Event[OrderCancelled]
    )
  )
}
```

**Обратите внимание:** Входной ингредиент `unavailableProductIds` **не** предоставляется сенсорным событием `OrderPlaced`. 
Он станет доступен только **после** выполнения `CheckStock` в случае неудачи. 
Baker автоматически управляет такими зависимостями данных.

## Шаг 3: Определение рецепта (сборка процесса)

### Что такое рецепт?

**Рецепт** — это декларативное описание вашего бизнес-процесса. Он определяет:

- Какие сенсорные события могут запустить процесс.
- Какие взаимодействия входят в процесс.
- Какие события являются предусловиями для каждого взаимодействия (т.е. от каких результатов зависят следующие шаги).

### Сборка рецепта для интернет-магазина

```dotty
package ru.scalabook.baker

import com.ing.baker.recipe.scaladsl.{Event, Recipe}

object WebShopRecipe {
  val recipe: Recipe =
    Recipe("web-shop recipe") // Имя рецепта (должно быть уникальным)
      .withSensoryEvent(
        Event[OrderPlaced] // Сенсорное событие-триггер
      )
      .withInteractions(
        CheckStock.interaction, // Выполняется сразу (все входные данные есть в OrderPlaced)

        ShipOrder.interaction
          .withRequiredEvent( // Условие: выполнить только после SufficientStock
            Event[CheckStock.SufficientStock]
          ),

        CancelOrder.interaction
          .withRequiredEvent( // Условие: выполнить только после OrderHasUnavailableItems
            Event[CheckStock.OrderHasUnavailableItems]
          )
      )
}
```

### Как работает этот рецепт?

1. **Запуск:** При получении события `OrderPlaced` создается новый экземпляр процесса.
2. **Автоматическое выполнение:** Baker видит, что для `CheckStock.interaction` доступны 
   все входные ингредиенты (они есть в `OrderPlaced`), и немедленно вызывает его.
3. **Ветвление:**
    - Если `CheckStock` возвращает `SufficientStock` → Baker проверяет, какие взаимодействия ждут этого события. 
      Находит `ShipOrder.interaction`, который требует это событие. 
      Все его входные данные (`orderId`, `address`) уже есть в процессе (из `OrderPlaced`), поэтому `ShipOrder` выполняется.
    - Если `CheckStock` возвращает `OrderHasUnavailableItems` → Baker активирует `CancelOrder.interaction`, 
      который требует именно это событие. Входной ингредиент `unavailableProductIds` поступает из события `OrderHasUnavailableItems`.
4. **Завершение:** После выполнения `ShipOrder` или `CancelOrder` процесс считается завершенным (если нет других ожидающих взаимодействий).

> **Ключевая идея:** Вы не пишете `if/else` или циклы. Вы описываете **зависимости между событиями и взаимодействиями**, 
> а Baker вычисляет, что и когда выполнять.

## Шаг 4: Реализация взаимодействий (код)

Теперь, когда рецепт объявлен, нам нужно предоставить **реальные реализации** взаимодействий. 
Baker будет вызывать эти реализации во время выполнения процесса.

> **Важно:** Реализации могут быть асинхронными (возвращать `Future`, `IO` или любой другой эффект). 
> Baker поддерживает `cats.effect.IO`, `scala.concurrent.Future` и `ZIO`.

### 4.1 Реализация `CheckStock` (с имитацией случайного результата)

```dotty
package ru.scalabook.baker

import ru.scalabook.baker.OrderPlaced.{OrderId, ProductId}

import scala.concurrent.Future
import scala.util.Random

// Трейт определяет контракт (может быть полезен для внедрения зависимостей)
trait CheckStockTrait {
  def apply(
      orderId: OrderId,
      productIds: List[ProductId]
  ): Future[CheckStock.Outcome]
}

class CheckStockImpl extends CheckStockTrait {
  def apply(
      orderId: OrderId,
      productIds: List[ProductId]
  ): Future[CheckStock.Outcome] = {
    println(s"Checking stock for order: $orderId and products: $productIds")

    // Имитация случайного результата: 50% успех, 50% неудача
    // В реальном приложении здесь был бы HTTP-вызов к StockService или запрос в БД
    val randomNumber = new Random().nextInt(1000) + 1
    if (randomNumber < 500) {
      Future.successful(CheckStock.SufficientStock())
    } else {
      Future.successful(CheckStock.OrderHasUnavailableItems(productIds))
    }
  }
}
```

**Что здесь происходит:**

- Метод принимает `orderId` и `productIds` (имена параметров должны совпадать с объявленными в `inputIngredients`? 
  **Нет**, Baker сопоставляет по типам и позициям, но для читаемости лучше сохранять те же имена).
- Возвращает `Future[Outcome]` — асинхронный результат.
- В реальном коде здесь был бы вызов внешнего сервиса, возможно, с обработкой ошибок и таймаутов.

### 4.2 Реализация `ShipOrder`

```dotty
package ru.scalabook.baker

import ru.scalabook.baker.OrderPlaced.OrderId

import scala.concurrent.Future

trait ShipOrderTrait {
  def apply(orderId: OrderId, address: Address): Future[ShipOrder.OrderShipped]
}

class ShipOrderImpl extends ShipOrderTrait {
  def apply(
      orderId: OrderId,
      address: Address
  ): Future[ShipOrder.OrderShipped] = {
    println(s"Shipping order $orderId to $address")
    // Здесь был бы вызов API службы доставки (например, FedEx, DHL)
    Future.successful(ShipOrder.OrderShipped())
  }
}
```

### 4.3 Реализация `CancelOrder`

```dotty
package ru.scalabook.baker

import ru.scalabook.baker.OrderPlaced.{OrderId, ProductId}

import scala.concurrent.Future

trait CancelOrderTrait {
  def apply(
      orderId: OrderId,
      unavailableProductIds: List[ProductId]
  ): Future[CancelOrder.OrderCancelled]
}

class CancelOrderImpl extends CancelOrderTrait {
  override def apply(
      orderId: OrderId,
      unavailableProductIds: List[ProductId]
  ): Future[CancelOrder.OrderCancelled] = {
    println(
      s"Canceling order $orderId. The following products are unavailable: $unavailableProductIds"
    )
    // Здесь был бы вызов сервиса отмены заказов или обновление статуса в БД
    Future.successful(CancelOrder.OrderCancelled())
  }
}
```

## Шаг 5: Выполнение рецепта (запуск процесса)

### Компоненты выполнения

Для запуска процесса вам потребуются:

1. **Baker instance** — среда выполнения (здесь используется `InMemoryBaker` для простоты; в production — `AkkaBaker`).
2. **Реализации взаимодействий** — экземпляры классов, которые вы создали на шаге 4.
3. **Скомпилированный рецепт** — результат работы `RecipeCompiler`.
4. **Идентификатор экземпляра процесса** — уникальная строка для каждого запуска.
5. **Сенсорное событие** — экземпляр `OrderPlaced` с реальными данными.

### Полный код приложения

```dotty
package examples.scala.application

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.runtime.inmemory.InMemoryBaker
import com.ing.baker.runtime.model.InteractionInstance
import com.ing.baker.runtime.scaladsl.EventInstance
import examples.scala.events.OrderPlaced
import examples.scala.ingredients.Address
import examples.scala.interactions.{CancelOrderImpl, CheckStockImpl, ShipOrderImpl}
import examples.scala.recipes.WebShopRecipe

import java.util.UUID
import scala.concurrent.duration.DurationInt

class WebShopApp {
  def main(args: Array[String]): Unit = {

    // 1. Создаем экземпляры реализаций взаимодействий
    val interactions = List(
      new CancelOrderImpl(),
      new ShipOrderImpl(),
      new CheckStockImpl(),
    )

    // 2. Строим InMemoryBaker с этими реализациями
    //    InteractionInstance.unsafeFrom[IO] адаптирует наш Future-код к эффекту IO
    val bakerF = InMemoryBaker.build(implementations = interactions.map(InteractionInstance.unsafeFrom[IO]))
      .unsafeRunSync()

    // 3. Готовим тестовые данные
    val recipeInstanceId = UUID.randomUUID().toString  // Уникальный ID для этого запуска
    val sensoryEvent = EventInstance.unsafeFrom(orderPlaced)  // Упаковываем событие в формат Baker

    // 4. Запускаем процесс (эффект IO, который мы выполняем синхронно с помощью unsafeRunSync)
    for {
      // Компилируем рецепт (проверяем корректность)
      recipeId <- bakerF.addRecipe(RecipeCompiler.compileRecipe(recipe = WebShopRecipe.recipe), validate = true)

      // Создаем (bake) экземпляр процесса с указанным ID
      _ <- bakerF.bake(recipeId, recipeInstanceId)

      // Отправляем сенсорное событие в процесс
      _ <- bakerF.fireSensoryEventAndAwaitReceived(recipeInstanceId, sensoryEvent)

      // Ожидаем завершения процесса (максимум 5 секунд)
      _ <- bakerF.awaitCompleted(recipeInstanceId, timeout = 5.seconds)
    } yield recipeId
  }

  // Тестовые данные для сенсорного события
  private val orderPlaced = OrderPlaced(
    orderId = "123",
    customerId = "456",
    productIds = List("iPhone", "PlayStation5"),
    address = Address(
      street = "Hoofdstraat",
      city = "Amsterdam",
      zipCode = "1234AA",
      country = "The Netherlands"
    )
  )
}
```

### Пояснение ключевых моментов

| Метод                                 | Назначение                                                                                                             |
|---------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `InMemoryBaker.build()`               | Создает экземпляр Baker, который хранит состояние в памяти (для разработки и тестирования).                            |
| `addRecipe()`                         | Компилирует и регистрирует рецепт. Параметр `validate = true` проверяет, что для всех взаимодействий есть реализации.  |
| `bake()`                              | Создает **экземпляр процесса** (аналог new Process()). Процесс получает уникальный ID и начинает существовать.         |
| `fireSensoryEventAndAwaitReceived()`  | Отправляет сенсорное событие в процесс и ждет, пока оно будет обработано (но не весь процесс!).                        |
| `awaitCompleted()`                    | Ожидает полного завершения процесса (все взаимодействия выполнены).                                                    |

## Шаг 6: Запуск и наблюдение за результатами

Запустите функцию `main` в вашей IDE или через sbt. В консоли вы увидите один из двух вариантов вывода.

### Вариант 1: Товары есть на складе (успех)

```
Checking stock for order: 123 and products: [iPhone, PlayStation5]
Shipping order 123 to Address(Hoofdstraat, Amsterdam, 1234AA, The Netherlands)
```

**Что произошло:**

1. `CheckStock` сгенерировал случайное число < 500 → вернул `SufficientStock`.
2. Baker активировал `ShipOrder`, так как он зависит от `SufficientStock` и все его входные данные доступны.
3. `ShipOrder` выполнился и напечатал сообщение об отправке.

### Вариант 2: Товаров нет на складе (отмена)

```
Checking stock for order: 123 and products: [iPhone, PlayStation5]
Canceling order 123. The following products are unavailable: [iPhone, PlayStation5]
```

**Что произошло:**

1. `CheckStock` сгенерировал случайное число ≥ 500 → вернул `OrderHasUnavailableItems(productIds)`.
2. Baker активировал `CancelOrder`, так как он зависит от `OrderHasUnavailableItems`.
3. `CancelOrder` выполнился и напечатал сообщение об отмене с указанием недоступных товаров.

> **Обратите внимание:** В обоих случаях **только одно** из двух взаимодействий (`ShipOrder` или `CancelOrder`) выполняется. 
> Baker автоматически выбирает правильную ветвь на основе полученных событий.

## Заключение

Вы только что создали свой первый работающий процесс оркестрации на Baker.

---

**Ссылки** (в алфавитном порядке):

- [Baker](https://ing-bank.github.io/baker/)
- [Исходный код](https://github.com/ing-bank/baker/tree/master/examples/docs-code-snippets)

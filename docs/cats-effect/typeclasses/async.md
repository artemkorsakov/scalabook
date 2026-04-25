{%
showPrev = true
prevPage = temporal.html
%}

# Async

`Async` — это асинхронный [интерфейс внешних функций](https://en.wikipedia.org/wiki/Foreign_function_interface) (FFI) для приостановки операций с побочными эффектами, которые
завершаются в другом месте (часто в другом пуле потоков через API, подобный Future).
Этот класс типов позволяет нам упорядочивать асинхронные операции, не попадая
в [ад коллбэков](http://callbackhell.com/), а также дает нам возможность
переносить выполнение в другие контексты выполнения.

## FFI

Асинхронная задача — это задача, результаты которой вычисляются где-то в другом месте. Мы ожидаем
результатов этого выполнения, передавая ему коллбэк, который будет вызван с результатом. Это вычисление может завершиться неудачей, поэтому коллбэк имеет тип
`Either[Throwable, A] => ()`. Это ожидание носит исключительно семантический характер — потоки не блокируются, текущий поток просто отменяется до завершения коллбэка. Это приводит нас непосредственно к простейшему асинхронному FFI
```scala
trait Async[F[_]] {

def async_[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]
}
```

В качестве примера этого в действии мы можем посмотреть на определение `Async[F].fromFuture`,
которое использует `Future#onComplete` для вызова предоставленного коллбэка
```scala
def fromFuture[A](fut: F[Future[A]]): F[A] =
flatMap(fut) { f =>
flatMap(executionContext) { implicit ec =>

async_[A](cb => f.onComplete(t => cb(t.toEither)))

}

}
```

Однако `async_` несколько ограничен. Мы не можем выполнять никаких эффектов `F`
в процессе регистрации коллбэка, а также не можем зарегистрировать
финализатор для отмены асинхронной задачи в случае, если фибер,
работающий в режиме `async_`, будет отменен.

Поэтому `Async` также предоставляет более общий `async`.
```scala
trait Async[F[_]] {
def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A] = {
}
```

Как видите, он принимает тот же коллбэк, что и раньше, но на этот раз мы можем
выполнять эффекты, приостановленные в `F`. `Option[F[Unit]]` позволяет нам
возвращать финализатор, который будет вызван, если фибер будет отменен. Например, вот
упрощенная версия `Async[F].fromCompletableFuture`

```scala
def fromCompletableFuture[A](fut: F[CompletableFuture[A]]): F[A] =
flatMap(fut) { cf =>

async[A] { cb =>
delay {

//Вызываем коллбэк с результатом выполнения завершаемого фьючера
val stage = cf.handle[Unit] {

case (a, null) => cb(Right(a))

case (_, e) => cb(Left(e))

}


//Отменяем завершаемый фьючерс, если фибер отменен
Some(void(delay(stage.cancel(false))))

}

}
}
```

## Перенос выполнения в другой пул потоков

`Async` позволяет переносить выполнение в другой пул потоков.

```scala
trait Async[F[_]] {

//Получить текущий контекст выполнения

def executionContext: F[ExecutionContext]

//Вычислить fa в предоставленном контексте выполнения и

//затем сдвинуть выполнение обратно

def evalOn[A](fa: F[A], ec: ExecutionContext): F[A]

}
```

Если вы знакомы с монадой reader, то это очень похоже -
`executionContext` похож на `ask` и дает нам текущий контекст выполнения.

`evalOn` похож на `local` и позволяет нам _локально_ изменить контекст выполнения
для данного вычисления. После завершения этих вычислений
выполнение вернется в контекст, указанный `executionContext`

```scala
val printThread: IO[Unit] =

IO.executionContext.flatMap(IO.println(_))

for {

_ <- printThread //io-compute-1

_ <- IO.evalOn(printThread, someEc) //some-ec-thread

_ <- printThread //io-compute-1
} yield ()
```

## Здесь водятся драконы

`Async` также определяет функцию с именем `cont`. Она _не_ предназначена для вызова из пользовательского кода. Вам также _абсолютно не_ нужно понимать ее,
чтобы использовать `Async`. Однако дизайн очень поучителен для любопытного
читателя. Для начала рассмотрим определение `async` ещё раз:

```scala
trait Async[F[_]] {

def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A]
```

Предположим, мы попытаемся реализовать индуктивный экземпляр this для `OptionT`. У нас есть нечто похожее на

```scala
trait OptionTAsync[F[_]] extends Async[OptionT[F, *]] {
implicit def delegate: Async[F]

override def async[A](
k: (Either[Throwable, A] => Unit) => OptionT[F, Option[OptionT[F, Unit]]])

: OptionT[F, A] = OptionT.liftF(
delegate.async { (cb: Either[Throwable, A] => Unit) =>
val x: F[Option[Option[OptionT[F, Unit]]]] = k(cb).value
delegate.map(x) { (o: Option[Option[OptionT[F, Unit]]]) =>

o.flatten.map { (opt: OptionT[F, Unit]) => opt.value.void }

}

}

)
}
```

Это выглядит (довольно) разумно. Однако здесь скрывается тонкая проблема,
— использование `flatten`. Проблема в том, что `OptionT` (и аналогично `EitherT` и `IorT`) имеет дополнительный канал ошибок (где `x =
delegate.pure(None)`), который мы вынуждены отбрасывать при сопоставлении с чем-то
типа `F[Option[F[Unit]]]`.

Это фактически нарушает несколько законов `Async`:

```scala
def asyncRightIsSequencedPure[A](a: A, fu: F[Unit]) =

F.async[A](k => F.delay(k(Right(a))) >>
fu.as(None)) <-> (fu >> F.pure(a))

def asyncLeftIsSequencedRaiseError[A](e: Throwable, fu: F[Unit]) =

F.async[A](k => F.delay(k(Left(e))) >> fu.as(None)) <-> (fu >> F.raiseError(e))
```

В обоих случаях, если у нас есть `fu = OptionT.none[F, A]` (канал ошибки для `OptionT`), то
левая часть подавит ошибку, а правая — нет.

Возможным решением было бы
отменить [CPS](https://en.wikipedia.org/wiki/Continuation-passing_style)
вычисления, определив

```scala
def cont[A]: F[(Either[Throwable, A] => Unit, F[A])]
```

где первый элемент кортежа `Either[Throwable, A] => Unit` — это
коллбэк, используемый для получения результата асинхронного вычисления, как и раньше, а
второй элемент (который мы назовем `get`) похож на промис и может быть
использован для семантического блокирования ожидания результата асинхронного
вычисления.

Затем мы могли бы определить `async` через `cont`

```scala
def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A] =

F.uncancelable { poll =>

cont[A].flatMap {
case (cb, get) =>

k(cb).flatMap {
case Some(fin) => poll(get).onCancel(fin)

case None => poll(get)

}

}

```

В этом случае канал ошибки распространяется от `k(cb)`, поскольку мы напрямую используем `flatMap`
на результате.

Отлично! Мы закончили, верно? Ну... не совсем. Проблема в том, что небезопасно
вызывать параллельные операции, такие как `get.start`. Поэтому нам нужно использовать
ещё один трюк и ограничить область видимости операций с помощью полиморфизма более высокого ранга.

```scala
def cont[K, R](body: Cont[F, K, R]): F[R]

//где
trait Cont[F[_], K, R] {
def apply[G[_]](
implicit
G: MonadCancel[G, Throwable]): (Either[Throwable, K] => Unit, G[K], F ~> G) => G[R]
}
```

Эта странная формулировка означает, что в теле `Cont` находятся только операции до тех, которые определены в
`MonadCancel`. Третий элемент кортежа — это естественное преобразование, используемое для переноса `k(cb)` и любых финализаторов в
`G`. С учётом этого реализация фактически почти идентична тому, что мы
имели выше, но статически запрещает нам вызывать небезопасные операции.

```scala
def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A] = {
val body = new Cont[F, A, A] {
def apply[G[_]](implicit G: MonadCancel[G, Throwable]) = { (resume, get, lift) =>

G.uncancelable { poll =>

lift(k(resume)).flatMap {
case Some(fin) => poll(get).onCancel(lift(fin))

case None => poll(get)

}

}

}

cont(body)
}
```

Модель `Cont` также значительно проще в реализации внутри,
особенно в отношении отмены и финализации. В случае `cont` финализатор можно ввести только через `onCancel`, в отличие от `async`, где финализаторы можно ввести либо через `onCancel`, либо вернув `Some(finalizer)` из продолжения.


---

**Ссылки:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts

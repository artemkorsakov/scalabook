{%
showPrev = true
prevPage = concurrent/workerpool.html
showNext = true
nextPage = async.html
%}

# Temporal

`Temporal` расширяет `Concurrent` возможностью приостанавливать выполнение потока, ожидая его выполнения в течение
указанного времени.

```scala
firstThing >> Temporal[F].sleep(5.seconds) >> secondThing
```

Конечно, этого можно было бы достичь с помощью `Sync[F].delay(Thread.sleep(duration))`, но
это _очень плохая_ идея, поскольку она заблокирует поток в пуле вычислений (см.
документацию по модели потоков](../../../tempdoc/docs/thread-model.md) для получения более подробной информации о том, почему это
плохо). Вместо этого `Temporal[F]#sleep` присваивается собственный класс типов, и он является
примитивом реализации, который семантически блокирует выполнение
вызывающего потока, отменяя его планирование. Внутри используется планировщик для ожидания
указанного времени перед повторным планированием потока.

Возможность засыпать на заданную продолжительность позволяет нам определять мощные комбинаторы, зависящие от времени, такие как `timeoutTo`:

```scala
val data = fetchFromRemoteService.timeoutTo(2.seconds, cachedValue)
```


---

**Ссылки:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts

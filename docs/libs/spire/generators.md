# Генераторы псевдослучайных чисел

В состав Spire входит множество различных реализаций PRNG (_Pseudo-Random Number Generators_), 
расширяющих интерфейс `spire.random.Generator`. 
Генераторы — это изменяемые генераторы случайных чисел, которые поддерживают базовые операции, такие как `nextInt`. 
В отличие от Java, генераторы по умолчанию не являются потокобезопасными; 
синхронные экземпляры можно получить, вызвав метод `.sync`.

Spire поддерживает создание случайных экземпляров произвольных типов с использованием класса типов `spire.random.Dist[A]`. 
Эти экземпляры представляют собой стратегию получения случайных значений с помощью экземпляра `Generator`. 
Например:

```dotty
object DistDemo:
  import spire.math.Complex
  import spire.random.Dist
  import spire.std.double.DoubleAlgebra
  
  val rng = spire.random.rng.Cmwc5()
  rng.nextLong() 
  
  // генерирует ассоциативный массив с от 10 до 20 записей
  given Dist[Map[Int, Complex[Double]]] =
    Dist.map[Int, Complex[Double]](10, 20)
  val m = rng.next[Map[Int, Complex[Double]]]
  
  // генерирует double в интервале [0.0, 1.0)
  val n = rng.next[Double]
  
  // генерирует комплексное число с действительной и мнимой частями в интервале [0.0, 1.0)
  val q = rng.next[Complex[Double]]
```

В отличие от генераторов, экземпляры `Dist[A]` являются неизменяемыми и составными, 
поддерживая такие операции как `map`, `flatMap`, и `filter`. 
Предоставляется множество экземпляров по умолчанию, и для пользовательских типов легко создавать собственные экземпляры.

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)

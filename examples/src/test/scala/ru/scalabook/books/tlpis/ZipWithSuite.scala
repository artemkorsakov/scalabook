package ru.scalabook.books.tlpis

import munit.FunSuite

class ZipWithSuite extends FunSuite:
  test("Проверка zipWith2"):

    def zipWith2[A, B, C](t2: (LazyList[A], LazyList[B]))(
        f: (A, B) => C
    ): LazyList[C] =
      t2 match
        case (ha #:: ta, hb #:: tb) =>
          LazyList.cons(f(ha, hb), zipWith2((ta, tb))(f))
        case _ => LazyList.empty

    val nats   = LazyList.from(1)
    val random = LazyList.continually(math.random)
    val seq    = zipWith2((nats, random)) { (n, r) => if (r > 0.3) n else -n }

    println(seq.take(10).toList)

  test("Проверка zipWith"):
    assert(true)

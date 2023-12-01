package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HList.::
import weaver.FunSuite

object ZipWithSuite extends FunSuite:
  test("Check zipWith2"):
    def zipWith2[A, B, C](t2: (LazyList[A], LazyList[B]))(
        f: (A, B) => C
    ): LazyList[C] =
      t2 match
        case (ha #:: ta, hb #:: tb) =>
          LazyList.cons(f(ha, hb), zipWith2((ta, tb))(f))
        case _ => LazyList.empty

    val nats   = LazyList.from(1)
    val random = LazyList.continually(math.random)
    val seq = zipWith2((nats, random)) { (n, r) => if r > 0.3 then n else -n }

    println(seq.take(10).toList)
    success

  test("Check zipWith"):
    val nats   = LazyList.from(1)
    val random = LazyList.continually(math.random)
    val seq = ZipWith.zipWith(nats :^: random :^: KNil) { case n :: r :: HNil =>
      if r > 0.3 then n else -n
    }

    println(seq.take(10).toList)
    success

  test("Check foreach"):
    val a = LazyList(1, 2, 5, 3, 9, 10, 101)
    val b = LazyList("one", "two", "three", "four")
    val c = LazyList(true, false, false, true, true)

    ZipWith.zipped(a :^: b :^: c :^: KNil) foreach { case n :: s :: b :: HNil =>
      println(s * (if b then 1 else n))
    }

    // one
    // twotwo
    // threethreethreethreethree
    // four

    success

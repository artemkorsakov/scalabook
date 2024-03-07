package ru.scalabook.algorithms.heap

import weaver.FunSuite

object LeftistHeapSuite extends FunSuite:
  private val emptyLeftistHeap: LeftistHeap[Int] = LeftistHeap.empty
  private val oneElLeftistHeap: LeftistHeap[Int] = LeftistHeap.make(1)
  private val someElLeftistHeap: LeftistHeap[Int] =
    LeftistHeap.empty.insert(1).insert(2).insert(3)

  test("LeftistHeap.isEmpty"):
    expect.all(
      emptyLeftistHeap.isEmpty,
      !oneElLeftistHeap.isEmpty,
      !someElLeftistHeap.isEmpty
    )

  test("LeftistHeap.size"):
    expect.all(
      emptyLeftistHeap.rank == 0,
      oneElLeftistHeap.rank == 1,
      someElLeftistHeap.rank == 2
    )

  test("LeftistHeap.minOption"):
    expect.all(
      emptyLeftistHeap.minOption.isEmpty,
      oneElLeftistHeap.minOption.contains(1),
      someElLeftistHeap.minOption.contains(1)
    )

  test("LeftistHeap.insert"):
    val heap1 = emptyLeftistHeap.insert(5)
    val heap2 = oneElLeftistHeap.insert(5)
    val heap3 = someElLeftistHeap.insert(5)
    expect.all(
      heap1.minOption.contains(5),
      heap1.rank == 1,
      heap2.minOption.contains(1),
      heap2.rank == 1,
      heap3.minOption.contains(1),
      heap3.rank == 2
    )

  test("LeftistHeap.remove"):
    val maybeHeap1 = emptyLeftistHeap.remove
    val maybeHeap2 = oneElLeftistHeap.remove
    val maybeHeap3 = someElLeftistHeap.remove
    expect(maybeHeap1.isEmpty) and
      expect(maybeHeap2.contains(emptyLeftistHeap)) and
      matches(maybeHeap3):
        case Some(heap3) =>
          expect.all(
            heap3.minOption.contains(2),
            heap3.rank == 1
          )

  test("LeftistHeap.merge"):
    val heap = LeftistHeap.merge(oneElLeftistHeap, someElLeftistHeap)
    expect.all(
      heap.minOption.contains(1),
      heap.rank == 2
    )

end LeftistHeapSuite

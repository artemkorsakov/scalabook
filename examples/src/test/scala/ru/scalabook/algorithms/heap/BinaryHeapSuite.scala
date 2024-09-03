package ru.scalabook.algorithms.heap

import weaver.FunSuite

object BinaryHeapSuite extends FunSuite:
  private val emptyBinaryHeap: BinaryHeap[Int] = BinaryHeap.empty
  private val oneElBinaryHeap: BinaryHeap[Int] = BinaryHeap(1)
  private val someElBinaryHeap: BinaryHeap[Int] =
    BinaryHeap.empty.insert(1).insert(2).insert(3)

  test("BinaryHeap.isEmpty"):
    expect.all(
      emptyBinaryHeap.isEmpty,
      !oneElBinaryHeap.isEmpty,
      !someElBinaryHeap.isEmpty
    )

  test("BinaryHeap.size"):
    expect.all(
      emptyBinaryHeap.size == 0,
      oneElBinaryHeap.size == 1,
      someElBinaryHeap.size == 3
    )

  test("BinaryHeap.height"):
    expect.all(
      emptyBinaryHeap.height == 0,
      oneElBinaryHeap.height == 1,
      someElBinaryHeap.height == 2
    )

  test("BinaryHeap.minOption"):
    expect.all(
      emptyBinaryHeap.minOption.isEmpty,
      oneElBinaryHeap.minOption.contains(1),
      someElBinaryHeap.minOption.contains(1)
    )

  test("BinaryHeap.insert"):
    val heap1 = emptyBinaryHeap.insert(5)
    val heap2 = oneElBinaryHeap.insert(5)
    val heap3 = someElBinaryHeap.insert(5)
    expect.all(
      heap1.minOption.contains(5),
      heap1.size == 1,
      heap1.height == 1,
      heap2.minOption.contains(1),
      heap2.size == 2,
      heap2.height == 2,
      heap3.minOption.contains(1),
      heap3.size == 4,
      heap3.height == 3
    )

  test("BinaryHeap.remove"):
    val maybeHeap1 = emptyBinaryHeap.remove
    val maybeHeap2 = oneElBinaryHeap.remove
    val maybeHeap3 = someElBinaryHeap.remove
    expect(maybeHeap1.isEmpty) and
      expect(maybeHeap2.contains(emptyBinaryHeap)) and
      matches(maybeHeap3):
        case Some(heap3) =>
          expect.all(
            heap3.minOption.contains(2),
            heap3.size == 2,
            heap3.height == 2
          )

end BinaryHeapSuite

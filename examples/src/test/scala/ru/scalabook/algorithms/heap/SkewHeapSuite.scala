package ru.scalabook.algorithms.heap

import weaver.FunSuite

object SkewHeapSuite extends FunSuite:
  private val emptySkewHeap: SkewHeap[Int] = SkewHeap.empty
  private val oneElSkewHeap: SkewHeap[Int] = SkewHeap(1)
  private val someElSkewHeap: SkewHeap[Int] =
    SkewHeap.empty.insert(1).insert(2).insert(3)

  test("SkewHeap.isEmpty"):
    expect.all(
      emptySkewHeap.isEmpty,
      !oneElSkewHeap.isEmpty,
      !someElSkewHeap.isEmpty
    )

end SkewHeapSuite

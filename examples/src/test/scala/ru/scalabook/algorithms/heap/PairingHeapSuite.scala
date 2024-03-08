package ru.scalabook.algorithms.heap

import weaver.FunSuite

object PairingHeapSuite extends FunSuite:
  private val emptyPairingHeap: PairingHeap[Int] = PairingHeap.empty
  private val oneElPairingHeap: PairingHeap[Int] = PairingHeap(1)
  private val someElPairingHeap: PairingHeap[Int] =
    PairingHeap.empty.insert(1).insert(2).insert(3)

  test("PairingHeap.isEmpty"):
    expect.all(
      emptyPairingHeap.isEmpty,
      !oneElPairingHeap.isEmpty,
      !someElPairingHeap.isEmpty
    )

end PairingHeapSuite

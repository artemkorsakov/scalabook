package ru.scalabook.algorithms.structures

import weaver.FunSuite

object LinkedListSuite extends FunSuite:
  private val emptyList: LinkedList[Int] = LinkedList.Nil
  private val oneElList: LinkedList[Int] = LinkedList.Cons(1, emptyList)
  private val someElList: LinkedList[Int] =
    LinkedList.Cons(3, LinkedList.Cons(2, oneElList))

  test("LinkedList.headOption"):
    expect.all(
      emptyList.headOption.isEmpty,
      oneElList.headOption.contains(1),
      someElList.headOption.contains(3)
    )

  test("LinkedList.tailOption"):
    expect.all(
      emptyList.tailOption.isEmpty,
      oneElList.tailOption.contains(LinkedList.Nil),
      someElList.tailOption.contains(LinkedList.Cons(2, oneElList))
    )

  test("LinkedList.isEmpty"):
    expect.all(
      emptyList.isEmpty,
      !oneElList.isEmpty,
      !someElList.isEmpty
    )

  test("LinkedList.length"):
    expect.all(
      emptyList.length == 0,
      oneElList.length == 1,
      someElList.length == 3
    )

  test("LinkedList.prepend"):
    expect.all(
      emptyList.prepend(5) == LinkedList.Cons(5, emptyList),
      oneElList.prepend(5) == LinkedList.Cons(5, oneElList),
      someElList.prepend(5) == LinkedList.Cons(5, someElList)
    )

  test("LinkedList.append"):
    expect.all(
      emptyList.append(5) == LinkedList.Cons(5, LinkedList.Nil),
      oneElList.append(5) == LinkedList.Cons(
        1,
        LinkedList.Cons(5, LinkedList.Nil)
      ),
      someElList.append(5) == LinkedList.Cons(
        3,
        LinkedList.Cons(
          2,
          LinkedList.Cons(1, LinkedList.Cons(5, LinkedList.Nil))
        )
      )
    )

  test("LinkedList.get"):
    expect.all(
      emptyList.get(0).isEmpty,
      emptyList.get(2).isEmpty,
      oneElList.get(0).contains(1),
      oneElList.get(2).isEmpty,
      someElList.get(0).contains(3),
      someElList.get(2).contains(1)
    )

  test("LinkedList.contains"):
    expect.all(
      !emptyList.contains(1),
      !emptyList.contains(2),
      oneElList.contains(1),
      !oneElList.contains(2),
      someElList.contains(1),
      someElList.contains(2)
    )

  test("LinkedList.concat"):
    expect.all(
      emptyList.concat(emptyList) == emptyList,
      emptyList.concat(oneElList) == oneElList,
      emptyList.concat(someElList) == someElList,
      oneElList.concat(emptyList) == oneElList,
      oneElList.concat(oneElList) == LinkedList.Cons(1, oneElList),
      oneElList.concat(someElList) == LinkedList.Cons(1, someElList),
      someElList.concat(emptyList) == someElList,
      someElList.concat(oneElList) ==
        LinkedList.Cons(3, LinkedList.Cons(2, LinkedList.Cons(1, oneElList))),
      someElList.concat(someElList) ==
        LinkedList.Cons(3, LinkedList.Cons(2, LinkedList.Cons(1, someElList)))
    )

  test("LinkedList.filter"):
    expect.all(
      emptyList.filter(_ % 2 == 0) == emptyList,
      oneElList.filter(_ % 2 == 0) == emptyList,
      someElList.filter(_ % 2 == 0) == LinkedList.Cons(2, emptyList)
    )

  test("LinkedList.map"):
    expect.all(
      emptyList.map(_ % 2 == 0) == emptyList,
      oneElList.map(_ % 2 == 0) == LinkedList.Cons(false, LinkedList.Nil),
      someElList.map(_ % 2 == 0) == LinkedList.Cons(
        false,
        LinkedList.Cons(true, LinkedList.Cons(false, LinkedList.Nil))
      )
    )

  test("LinkedList.fold"):
    expect.all(
      emptyList.fold(10)(_ + _) == 10,
      oneElList.fold(10)(_ + _) == 11,
      someElList.fold(10)(_ + _) == 16
    )
end LinkedListSuite

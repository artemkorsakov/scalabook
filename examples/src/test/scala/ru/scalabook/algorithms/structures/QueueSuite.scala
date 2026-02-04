package ru.scalabook.algorithms.structures

import weaver.FunSuite

object QueueSuite extends FunSuite:
  private val emptyQueue: Queue[Int]  = Queue.empty[Int]
  private val oneElQueue: Queue[Int]  = emptyQueue.enqueue(1)
  private val someElQueue: Queue[Int] =
    emptyQueue.enqueue(1).enqueue(2).enqueue(3)

  test("Queue.frontOption"):
    expect.all(
      emptyQueue.frontOption.isEmpty,
      oneElQueue.frontOption.contains(1),
      someElQueue.frontOption.contains(1)
    )

  test("Queue.rearOption"):
    expect.all(
      emptyQueue.rearOption.isEmpty,
      oneElQueue.rearOption.contains(emptyQueue),
      someElQueue.rearOption.contains(Queue(Nil, List(2, 3)))
    )

  test("Queue.isEmpty"):
    expect.all(
      emptyQueue.isEmpty,
      !oneElQueue.isEmpty,
      !someElQueue.isEmpty
    )

  test("Queue.enqueue"):
    expect.all(
      emptyQueue.enqueue(5) == Queue(5 :: Nil, Nil),
      oneElQueue.enqueue(5) == Queue(5 :: 1 :: Nil, Nil),
      someElQueue.enqueue(5) == Queue(5 :: 3 :: 2 :: 1 :: Nil, Nil)
    )

  test("Queue.enqueue"):
    expect.all(
      emptyQueue.dequeue.isEmpty,
      oneElQueue.dequeue.contains((1, emptyQueue)),
      someElQueue.dequeue.contains((1, Queue(Nil, List(2, 3))))
    )
end QueueSuite

package ru.scalabook.algorithms.structures

import ru.scalabook.algorithms.structures.Stack.*
import weaver.FunSuite

object StackSuite extends FunSuite:
  test("Stack.isEmpty"):
    val stack = Stack.empty[Int]
    expect.all(
      stack.isEmpty,
      !stack.push(1).isEmpty,
      !stack.push(1).push(2).isEmpty
    )

  test("Stack.push"):
    val stack    = Stack.empty[Int]
    val newStack = stack.push(1)
    expect.all(
      newStack == NonEmptyStack(1, stack),
      newStack.push(2) == NonEmptyStack(2, newStack)
    )

  test("Stack.peek"):
    val stack = Stack.empty[Int]
    expect(stack.peek().isEmpty) and {
      val newStack = stack.push(1)
      expect(newStack.peek().contains((1, newStack)))
    }

  test("Stack.pop"):
    val stack = Stack.empty[Int]
    expect(stack.pop().isEmpty) and {
      val newStack = stack.push(1)
      expect(newStack.pop().contains((1, stack)))
    }
end StackSuite

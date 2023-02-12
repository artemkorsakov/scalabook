package algorithms.structures

import algorithms.structures.Stack.*
import munit.FunSuite

class StackSuite extends FunSuite:
  test("Stack.isEmpty") {
    val stack = Stack.empty[Int]
    assert(stack.isEmpty)
    assert(!stack.push(1).isEmpty)
    assert(!stack.push(1).push(2).isEmpty)
  }

  test("Stack.push") {
    val stack    = Stack.empty[Int]
    val newStack = stack.push(1)
    assertEquals(newStack, NonEmptyStack(1, stack))
    assertEquals(newStack.push(2), NonEmptyStack(2, newStack))
  }

  test("Stack.peek") {
    val stack = Stack.empty[Int]
    assertEquals(stack.peek(), None)
    val newStack = stack.push(1)
    assertEquals(newStack.peek(), Some((1, newStack)))
  }

  test("Stack.pop") {
    val stack = Stack.empty[Int]
    assertEquals(stack.pop(), None)
    val newStack = stack.push(1)
    assertEquals(newStack.pop(), Some((1, stack)))
  }

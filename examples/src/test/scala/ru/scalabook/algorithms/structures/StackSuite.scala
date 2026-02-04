package ru.scalabook.algorithms.structures

import ru.scalabook.algorithms.structures.Stack.*
import weaver.FunSuite

object StackSuite extends FunSuite:
  private val emptyStack: Stack[Int]  = EmptyStack
  private val oneElStack: Stack[Int]  = NonEmptyStack(1, EmptyStack)
  private val someElStack: Stack[Int] =
    NonEmptyStack(3, NonEmptyStack(2, NonEmptyStack(1, EmptyStack)))

  test("Stack.headOption"):
    expect.all(
      emptyStack.headOption.isEmpty,
      oneElStack.headOption.contains(1),
      someElStack.headOption.contains(3)
    )

  test("Stack.tailOption"):
    expect.all(
      emptyStack.tailOption.isEmpty,
      oneElStack.tailOption.contains(EmptyStack),
      someElStack.tailOption.contains(NonEmptyStack(
        2,
        NonEmptyStack(1, EmptyStack)
      ))
    )

  test("Stack.isEmpty"):
    expect.all(
      emptyStack.isEmpty,
      !oneElStack.isEmpty,
      !someElStack.isEmpty
    )

  test("Stack.length"):
    expect.all(
      emptyStack.length == 0,
      oneElStack.length == 1,
      someElStack.length == 3
    )

  test("Stack.push"):
    expect.all(
      emptyStack.push(5) == NonEmptyStack(5, emptyStack),
      oneElStack.push(5) == NonEmptyStack(5, oneElStack),
      someElStack.push(5) == NonEmptyStack(5, someElStack)
    )

  test("Stack.pop"):
    expect.all(
      emptyStack.pop().isEmpty,
      oneElStack.pop().contains((1, EmptyStack)),
      someElStack.pop().contains((
        3,
        NonEmptyStack(2, NonEmptyStack(1, EmptyStack))
      ))
    )

  test("Stack.peek"):
    expect.all(
      emptyStack.peek().isEmpty,
      oneElStack.peek().contains((1, oneElStack)),
      someElStack.peek().contains((3, someElStack))
    )
end StackSuite

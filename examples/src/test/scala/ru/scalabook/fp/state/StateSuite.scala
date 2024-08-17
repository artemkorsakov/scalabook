package ru.scalabook.fp.state

import weaver.SimpleIOSuite

enum Input:
  case Coin, Turn

case class Machine(locked: Boolean, candies: Int, coins: Int)

object Candy:
  private val updateMachine: Input => Machine => Machine =
    (i: Input) =>
      (s: Machine) =>
        (i, s) match
          case (_, Machine(_, 0, _))              => s // (г)
          case (Input.Coin, Machine(false, _, _)) => s // (в)
          case (Input.Turn, Machine(true, _, _))  => s // (в)
          case (Input.Coin, Machine(true, candy, coin)) => // (а)
            Machine(false, candy, coin + 1)
          case (Input.Turn, Machine(false, candy, coin)) => // (б)
            Machine(true, candy - 1, coin)

  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    for
      _ <- State.traverse(inputs)(input => State.modify(updateMachine(input)))
      s <- State.get
    yield (s.coins, s.candies)

object StateSuite extends SimpleIOSuite:
  import Input.*

  pureTest("State should update machine"):
    val initialMachine = Machine(true, 5, 10)

    val inputs = List(
      Coin,
      Turn,
      Coin,
      Coin,
      Coin,
      Turn,
      Turn,
      Turn,
      Coin,
      Turn,
      Coin,
      Turn
    )

    val runner                      = Candy.simulateMachine(inputs)
    val ((candies, coins), machine) = runner.run(initialMachine)

    expect.all(
      candies == 14,
      coins == 1,
      machine == Machine(true, 1, 14)
    )
end StateSuite

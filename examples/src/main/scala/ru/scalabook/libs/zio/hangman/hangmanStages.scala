package ru.scalabook.libs.zio.hangman

val hangmanStages = List(
  """
    #   --------
    #   |      |
    #   |
    #   |
    #   |
    #   |
    #   -
    #""".stripMargin('#'),
  """
    #   --------
    #   |      |
    #   |      0
    #   |
    #   |
    #   |
    #   -
    #""".stripMargin('#'),
  """
    #   --------
    #   |      |
    #   |      0
    #   |      |
    #   |      |
    #   |
    #   -
    #""".stripMargin('#'),
  """
    #   --------
    #   |      |
    #   |      0
    #   |     \|
    #   |      |
    #   |
    #   -
    #""".stripMargin('#'),
  """
    #   --------
    #   |      |
    #   |      0
    #   |     \|/
    #   |      |
    #   |
    #   -
    #""".stripMargin('#'),
  """
    #   --------
    #   |      |
    #   |      0
    #   |     \|/
    #   |      |
    #   |     /
    #   -
    #""".stripMargin('#'),
  """
    #   --------
    #   |      |
    #   |      0
    #   |     \|/
    #   |      |
    #   |     / \
    #   -
    #""".stripMargin('#')
)

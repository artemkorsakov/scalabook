package ru.scalabook.libs.zio.hangman

val hangmanStages = IndexedSeq(
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

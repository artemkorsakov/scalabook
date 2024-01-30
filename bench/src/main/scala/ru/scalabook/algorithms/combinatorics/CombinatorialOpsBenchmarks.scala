package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.CombinatorialOps.*

@main def xCombinationsBench(): List[List[Int]] =
  (1 to 1000).toList.xcombinations(2)

@main def xSubsetsBench(): List[List[Int]] =
  (1 to 10).toList.xsubsets

@main def xVariationsBench(): List[List[Int]] =
  (1 to 1000).toList.xvariations(2)

@main def xPermutationsBench(): List[List[Int]] =
  (1 to 5).toList.xpermutations

# Combinatorics

## Introduction

Combinatorics is a branch of mathematics that deals with the study and counting of various combinations and permutations of objects.
This branch has deep roots in the history of mathematics, starting from the works of Leonardo of Pisa (Fibonacci)
and continuing to modern research in the field of graph theory, probability theory, and algorithms.

**Main problems of combinatorics:**

- **Counting combinations:** In how many ways can we choose `k` objects from `n`?
- **Counting permutations:** In how many ways can we order `n` objects?
- **Counting partitions:** In how many ways can we partition a set into subsets?

**Main concepts of combinatorics:**

- **Factorial:** The product of all natural numbers from `1` to `n`. Denoted `n!`.
- **Permutation:** An ordered set of `n` elements. The number of permutations of `n` elements is `n!`.
- **Combination:** A subset of `k` elements chosen from `n` elements. The number of combinations is denoted `C(n, k)` or \\(\binom{n}{k}\\).
- **Arrangement:** An ordered set of `k` elements chosen from `n` elements. The number of arrangements is denoted `A(n, k)`.

**Methods of combinatorics:**

- **Multiplication principle:** If the first element can be chosen in `m` ways, and the second element in `n` ways,
  then the total number of ways to choose a pair of elements is `m * n`.
- **Addition principle:** If the first element can be chosen in `m` ways, and the second element in `n` ways,
  and these ways of choosing do not overlap, then the total number of ways to choose one element is `m + n`.
- **Recurrence relations:** A method that allows expressing the number of combinations in terms of smaller values.
- **Generating functions:** A method that allows representing combinatorial objects as formal power series.

---

**References:**

- [Source code][sample]
- [Combinatorics](https://ru.wikipedia.org/wiki/%D0%9A%D0%BE%D0%BC%D0%B1%D0%B8%D0%BD%D0%B0%D1%82%D0%BE%D1%80%D0%B8%D0%BA%D0%B0)

[sample]: https://gitverse.ru/scalabook/algorithms

# Mathematical Induction

Let `P(n)` be a statement concerning an integer `n`.
Suppose we need to prove that _the statement `P(n)` is true for all positive integers `n`_.
An important method for proving this is as follows:

- a) Prove that `P(1)` is true.
- b) Prove that "if `P(1), P(2), ..., P(n)` are true, then `P(n + 1)` is also true";
  this proof must hold for any positive integer `n`.

This method is called _proof by mathematical induction_.

## Example

The method of mathematical induction is a powerful tool for proving statements that depend on natural numbers.
It consists of two main steps: the base case and the inductive step.
Consider an example of a proof using this method.

### Statement

For any natural number n, the sum of the first n natural numbers is equal to \\(\frac{n(n + 1)}{2}\\).
That is: \\(1 + 2 + 3 + \ldots + n = \frac{n(n + 1)}{2}\\).

### Proof by Mathematical Induction

**Base Case:** Check the statement for n = 1.

\\(1 = \frac{1(1 + 1)}{2} = \frac{1 \times 2}{2} = 1\\)

The statement holds for n = 1.

**Inductive Hypothesis:** Assume that the statement holds for some arbitrary natural number k, i.e.:

\\(1 + 2 + 3 + \ldots + k = \frac{k(k + 1)}{2}\\)

**Inductive Step:** Now we need to prove that the statement holds for k + 1:

\\(1 + 2 + 3 + \ldots + k + (k + 1) = \frac{(k + 1)(k + 2)}{2}\\)

By the inductive hypothesis, we can replace the sum of the first k numbers:

\\(1 + 2 + 3 + \ldots + k + (k + 1) = \frac{k(k + 1)}{2} + (k + 1)\\)

Now simplify the right-hand side:

\\(\frac{k(k + 1)}{2} + (k + 1) = \frac{k(k + 1)}{2} + \frac{2(k + 1)}{2} = \frac{k(k + 1) + 2(k + 1)}{2} = \frac{(k + 1)(k + 2)}{2}\\)

Thus we have obtained:

\\(1 + 2 + 3 + \ldots + (k + 1) = \frac{(k + 1)(k + 2)}{2}\\)

**Conclusion:** It has been proven that if the statement holds for k, then it holds for k + 1 as well.
Since the base case is true, by the principle of mathematical induction the statement is true for all natural numbers n.

Thus, the proof that the sum of the first n natural numbers is equal to \\(\frac{n(n + 1)}{2}\\) for all \\(n \in \mathbb{N}\\) is complete.

---

**Links:**

- [Source code][sample]
- [The Art of Computer Programming — Donald E. Knuth][TAOCP]
- [Markov A.A., Nagorny N.M. — Theory of Algorithms](https://www.ozon.ru/product/teoriya-algorifmov-markov-andrey-andreevich-nagornyy-nikolay-makarovich-1799472833/)

[TAOCP]: https://www-cs-faculty.stanford.edu/~knuth/taocp.html
[sample]: https://gitverse.ru/scalabook/algorithms

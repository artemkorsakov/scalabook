# Asymptotic Notations

Asymptotic notations are mathematical tools
used to describe the behavior of functions as their arguments approach certain values, usually infinity.
They allow simplifying the analysis of complex functions by providing more intuitive and convenient forms for comparison and estimation.

## Asymptotic Behavior

A function \\(f(n)\\) is said to be asymptotically equivalent to function \\(g(n)\\) as \\(n \to \infty\\)
if: \\(\lim_{n \to \infty} \frac{f(n)}{g(n)} = 1\\).
Denoted as \\(f(n) \sim g(n)\\).

## Big O Notation: Symbol O ("Big O") — Upper Bound

The notation \\(g(n) = O(f(n))\\) means the following: there exist positive constants \\(M\\) and \\(n\_{0}\\)
such that the function \\(g(n)\\), expressed as \\(O(f(n))\\),
satisfies \\(\left|g(n)\right| \leq M \left|f(n)\right|\\) for all integers \\(n \geq n\_0\\).

This means that g(n) grows no faster than f(n) for large n.

### Some Properties of Big O

- **Formula 1**: \\(f(n) = O(f(n))\\).
- **Formula 2**: \\(c \cdot O(f(n)) = O(f(n))\\), if c is a constant.
- **Formula 3**: \\(O(f(n)) + O(f(n)) = O(f(n))\\).
- **Formula 4**: \\(O(O(f(n))) = O(f(n))\\).
- **Formula 5**: \\(O(f(n)) \cdot O(g(n)) = O(f(n)g(n))\\).
- **Formula 6**: \\(O(f(n)g(n)) = f(n) \cdot O(g(n))\\).

## \\(\Omega\\)-Notation: Symbol \\(\Omega\\) ("Big Omega") — Lower Bound

The statement \\(g(n) = \Omega(f(n))\\) means that there exist positive constants \\(L\\) and \\(n\_{0}\\),
such that \\(\left|g(n)\right| \geq L \left|f(n)\right|\\) for all integers \\(n \geq n\_0\\).

This means that g(n) grows no slower than f(n) for large n.

## \\(\Theta\\)-Notation: Symbol \\(\Theta\\) ("Big Theta") — Upper and Lower Bound

\\(g(n) = \Theta(f(n)) \Leftrightarrow g(n) = O(f(n))\\) and \\(g(n) = \Omega(f(n))\\)

If g(n) is bounded both above and below by some constants multiplied by f(n), we write: \\(g(n) = \Theta(f(n))\\).

This means that f(n) and g(n) grow at the same rate for large n.

## Examples of Asymptotic Notations

**Example 1:**

Consider the function \\( f(n) = 3n^2 + 2n + 1 \\).
As \\( n \to \infty \\): \\( f(n) \sim 3n^2 \\).
That is, \\( f(n) = O(n^2) \\) and \\( f(n) = \Omega(n^2) \\), therefore \\( f(n) = \Theta(n^2) \\).

**Example 2:**

For the function \\( f(n) = n \log n \\), we can say: \\( f(n) \sim n \log n \\).
This means that for large n, the function f(n) behaves like \\( n \log n \\).

**Example 3:**

Consider the function \\( f(n) = e^n \\) and \\( g(n) = n^k \\) for any fixed k.
As \\( n \to \infty \\): \\( f(n) \gg g(n) \\).
That is, \\( e^n \\) grows significantly faster than any polynomial expression \\( n^k \\).

## Applications of Asymptotic Notations

Asymptotic notations are widely used in various fields, including:

- **Algorithm Analysis**: For estimating time complexity and memory usage.
- **Combinatorics**: For estimating the number of combinations and permutations.
- **Probability Theory**: For analyzing distributions and limit theorems.
- **Physics and Engineering**: For simplifying complex mathematical models.

---

**Links:**

- [Source code][sample]
- [The Art of Computer Programming — Donald E. Knuth][TAOCP]
- [Algorithmica — Asymptotic Analysis](https://ru.algorithmica.org/cs/complexity/asymptotic/)
- [Markov A.A., Nagorny N.M. — Theory of Algorithms](https://www.ozon.ru/product/teoriya-algorifmov-markov-andrey-andreevich-nagornyy-nikolay-makarovich-1799472833/)

[TAOCP]: https://www-cs-faculty.stanford.edu/~knuth/taocp.html
[sample]: https://gitverse.ru/scalabook/algorithms

# Ackermann Function

**Algorithm:**

The following procedure computes a mathematical function called the Ackermann function
(**Exercise 1.10** from [SICP][sicp]):

`f(x,y) = `

- `0` when `y = 0`
- `2y` when `x = 0`
- `2` when `y = 1`
- `f(x-1, f(x, y-1))` in all other cases

For small `x`, the Ackermann function is equivalent to the following functions:

- `functionAckermann(0, n) <=> 2*n`
- `functionAckermann(1, n) <=> 2^n`
- `functionAckermann(2, n) <=> 2^2^...^2 (n times)`

The Ackermann function is known for being very fast to compute.

**Code:**

```dotty
def functionAckermann(x: Int, y: Int): Int =
  if y == 0 then 0
  else if x == 0 then 2 * y
  else if y == 1 then 2
  else functionAckermann(x - 1, functionAckermann(x, y - 1))

functionAckermann(3, 3)
// 65536
```

---

**References:**

- [Source code][sample]
- [Ackermann Function - Wiki](https://en.wikipedia.org/wiki/Ackermann_function)
- [SICP: Abelson H., Sussman G.J. - Structure and Interpretation of Computer Programs][sicp]

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sample]: https://gitverse.ru/scalabook/algorithms

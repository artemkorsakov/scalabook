# Newton's Method

Newton's method is an iterative numerical method for finding the root (zero) of a given function.
The solution is found by constructing successive approximations and is based on the principles of simple iteration.
Newton's method can also be used for solving optimization problems, in which it is required to determine the zero of the first derivative or the gradient in the case of a multidimensional space.

The algorithm for finding a numerical solution to the equation `f(x)=0` reduces to an iterative procedure for computing:

\\(x\_{n+1} = x\_{n} - \frac{f(x\_{n})}{f'(x\_{n})}\\)

**Algorithm:**

- An initial approximation \\(x\_{0}\\) is given.
- As long as the stopping condition is not met (e.g., the error is within the required limits), a new approximation is computed using the formula above.

To implement Newton's method as a procedure, we first need to express the concept of a derivative.
In general, if `g` is a function and `dx` is a small number, then
the derivative `Dg` of function `g` is a function,
the value of which at each point `x` is described by the formula (as `dx` approaches zero):
\\(\frac{g(x + \partial x) - g(x)}{\partial x} \\)

**Code:**

```dotty
object NewtonsMethod:
  private val dx = 1e-6

  def derivative(x: Double, g: Double => Double): Double =
    (g(x + dx) - g(x)) / dx
```

Using `derivative`, Newton's method can be expressed as a process of finding a fixed point:

```dotty
object NewtonsMethod:
  private val accuracy = 1e-6

  @scala.annotation.tailrec
  def newtonsMethod(g: Double => Double, guess: Double): Double =
    val next = transform(g)(guess)
    if math.abs(next - guess) < accuracy then next
    else newtonsMethod(g, next)

  private def transform(g: Double => Double): Double => Double =
    x => x - g(x) / derivative(x, g)
```

**Example:**

Consider the problem of finding positive x values for which \\(cos x=x^{3}\\).
This problem can be formulated as finding the zero of the function \\(f(x)=cos x - x^{3}\\).
Since \\(cos x \leqslant 1\\) for all x and \\(x^{3} > 1\\) for x > 1,
it is obvious that the solution lies between 0 and 1.
Let us take the initial approximation value \\(x\_{0}=0,5\\}.
Then:

```dotty
val g: Double => Double = x => math.cos(x) - x * x * x

newtonsMethod(g, 0.5) // 0.865474033101...
```

**Complexity:**

Derivative computation complexity: Newton's method uses a numerical approximation of the derivative,
which requires executing two calls to function `g` at each step.
This adds some constant complexity, but does not affect the asymptotic behavior.

Number of iterations: Newton's method typically has quadratic convergence, which means that
the number of iterations required to achieve a given accuracy decreases quadratically at each step.
This means that the number of correct digits in the approximation doubles at each step.

Thus, if the function `g` behaves well and the initial guess is close to the root,
the number of iterations will be O(log(log(1/ε))), where ε is the required accuracy.
This is because the number of correct digits doubles at each step.

- **Time** - O(log(log(1/ε)))
- **Memory** - O(1)

---

**References:**

- [Source code][sample]
- [Newton's method - Wiki](https://ru.wikipedia.org/wiki/%D0%9C%D0%B5%D1%82%D0%BE%D0%B4_%D0%9D%D1%8C%D1%8E%D1%82%D0%BE%D0%BD%D0%B0)
- [SICP: Abelson H., Sussman D. - Structure and Interpretation of Computer Programs][sicp]
- [Bird, Wadler - Introduction to Functional Programming, 2.1 Numbers](https://archive.org/details/introductiontofu0000bird/mode/2up)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sample]: https://gitverse.ru/scalabook/algorithms

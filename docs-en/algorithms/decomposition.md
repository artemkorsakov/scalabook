# Problem Decomposition

This chapter presents general approaches to solving problems by breaking them into simpler parts.
What unites them is a single idea: a complex problem can often be reduced to several simple subproblems,
each of which can be solved faster than the original problem as a whole, and then the results can be "stitched" together.

## General idea

Suppose we have a problem of size \\(n\\) that can be solved naively in \\(O(f(n))\\) time, where \\(f(n)\\) is "too large" a function.
Decomposition attempts to build a solution of the form

\\(O(g(n) + h(n)), \qquad g(n) \ll f(n), \quad h(n) \ll f(n),\\)

where \\(g\\) and \\(h\\) achieve their smaller size thanks to the problem being divided into blocks of size about \\(\sqrt n\\),
or the brute-force search being split into two independent halves.

For example, in Mo's algorithm, queries are divided into \\(\sqrt n\\) blocks by left boundary,
and the sweeping line works because there are exactly \\(2n\\) "interesting" event points.
Throughout this chapter, the key technique is to properly **order** the problem space (by coordinate, by block, by degree),
so that small pieces can be solved locally at a low cost.

## References

- [Source code][sample]
- [Algorithmica - Problem Decomposition](https://ru.algorithmica.org/cs/decomposition/)

[sample]: https://gitverse.ru/scalabook/algorithms

# Substring Search

In text matching, substring search is a very common requirement.
If we manually perform a substring search, we compare the first character of the sought substring with the first character of the text
in which we are matching.
If they match, we continue with the second character and so on.
Since we can move our eyes, we could start matching from the middle of the page
or from any place we like.
But if we are searching for the first occurrence, it will be sequential matching or searching.
In computers, matching is usually performed sequentially.
But this can be changed if there are other requirements.

Search can be performed by a wide range of algorithms.
Below we consider a few algorithms: the naive search algorithm and the Knuth-Morris-Pratt search algorithm.

### Naive Search Algorithm

**Algorithm:**

- **Step 1**: For the first character in the text, perform the following matching loop.
    - Compare the first character of the sought substring with the current character of the text.
    - If there is a match, compare the second character of the substring with the next character of the text.
      Continue until all characters of the substring match characters of the text.
      If there is a full match, return the index of the current text character, which in this case is `0`.
    - If there is no match, exit this matching loop.
- **Step 2**: Now move the pointer to the second character of the text and continue running the matching loop described above.
   Any partial match is lost in the process of searching with backtracking.
   If there is a full match, return the index, which in this case is `1`.
   If there is no full match, move the pointer to the third character.
- **Step 3**: Continue the steps described above until we reach an index that, when added to the substring length, gives the text length,
   i.e., after the last starting point there must be enough characters
   for the index to stay within bounds.

In the worst case, the time complexity of naive search is _O(mn)_,
where _m_ is the length of the sought substring and _n_ is the length of the text in which the search is performed.

**Estimation:**

- **Time** - O(mn)
- **Memory** - O(1)

**Code:**

```dotty
def naiveSubstringSearch(searchWord: String, source: String): Int =
  (0 to (source.length - searchWord.length))
    .find: i =>
      searchWord.indices.forall: j =>
        source(j + i) == searchWord(j)
    .getOrElse(-1)
```

### Knuth-Morris-Pratt Algorithm

**Algorithm:**

- Initialize variables.
    - `m` = length of the pattern (sought substring)
    - `n` = length of the text
    - `T` = prefix table
    - `i` = number of matching characters
- First, compute the prefix table. This is a preprocessing step that processes the pattern to find prefix matches of the pattern with the pattern itself.
  The pattern here is the substring to search for.
  It indicates which part of the last comparison can be reused if the comparison fails;
  this is an improvement over the naive search algorithm.
  `P` and `S` denote the pattern to search for and the text in which the search is performed, respectively.
  The prefix function is defined as the size of the largest prefix `P[0,1,...,l−1]` that is also a suffix `P[1,2,...,l]`.
  The prefix table is computed using the following subroutine, which has time complexity _O(m)_.
  ```text
  a. m ← length[P]
  b. T[1] ← 0
  c. k ← 0
  d. for l ← 2 to m do
       while k > 0 and P[k + 1] != p[l] do
         k ← T[k]
       end while
       if P[k + 1] = P[l] then
         k ← k + 1
       end if
       T[l] ← k
     end for
  e. return T
  ```
- Initialize variable: `i = 0` points to the start of the match.
- Compare the first character of the sought substring (pattern) with the first character of the text.
  If it is not a match, replace the value at `T[i]` with `i`. If it is a match, increment `i` by `1`.
- Then check if all characters of the pattern match the text characters.
  If they do not match, repeat the search process. If they do match, return the starting index of the matching substring in the text.
- Continue the steps described above to find the next match.
- The steps above can be represented by the following pseudocode:
  ```text
  a. n ← length[S]
  b. m ← length[P]
  c. T ← computePrefixTable(P), call the prefix computation subroutine above.
  d. i ← 0
  e. for j ← 1 to n do
       while i > 0 and P[i + 1] != S[j] do
         i ← T[i]
         if P[i + 1] = S[j] then
           i ← i + 1
         end if
         if i = m then
           i ← P[i]
         end if
       end while
     end for
  ```
- Returns a set of starting indices for all matching substrings in the text.
  The time complexity of prefix table computation is _O(m)_.
  Comparing the pattern with the text takes _O(n)_.
  Therefore, the total time complexity is _O(m+n)_.

To illustrate the algorithm above, let's compute the prefix table for `P = {xyxyxzx}`.
Initially, `m = length[P] = 7`, `T[1] = 0`, and `k = 0`.
As shown in the algorithm above, `m` is the length of the sought pattern or string,
`T` is the prefix table and `k` is the initial value of the potential, initialized to `0`.

**Step 1**: `l = 2`, `k = 0`, `T[2] = 0`

| l | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| P | x | y | x | y | x | z | x |
| T | 0 | 0 |   |   |   |   |   |


**Step 2**: `l = 3`, `k = 0`, `T[3] = 1`

| l | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| P | x | y | x | y | x | z | x |
| T | 0 | 0 | 1 |   |   |   |   |


**Step 3**: `l = 4`, `k = 1`, `T[4] = 2`

| l | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| P | x | y | x | y | x | z | x |
| T | 0 | 0 | 1 | 2 |   |   |   |


**Step 4**: `l = 5`, `k = 2`, `T[5] = 3`

| l | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| P | x | y | x | y | x | z | x |
| T | 0 | 0 | 1 | 2 | 3 |   |   |


**Step 5**: `l = 6`, `k = 3`, `T[6] = 1`

| l | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| P | x | y | x | y | x | z | x |
| T | 0 | 0 | 1 | 2 | 3 | 1 |   |


**Step 6**: `l = 7`, `k = 1`, `T[7] = 1`

| l | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| P | x | y | x | y | x | z | x |
| T | 0 | 0 | 1 | 2 | 3 | 1 | 1 |

Now let's consider an example implementation of the KMP algorithm.
We have `P = {xyxyxzx}` from the analysis above. Suppose `S = {yxzyxyxyxyxzxxy}`.
Now let's go through the KMP algorithm to check if `P` occurs in `S`.


**Step 1**: `j = 1`, `i = 0`, comparing `P[1]` with `S[1]`.
Since `P[1]` does not match `S[1]`, `P` shifts one position to the right.

Text ~~y~~ x z y x y x y x y x z x x y

Pattern ~~x~~ y x y x z x


**Step 2**: `j = 2`, `i = 0`, comparing `P[1]` with `S[2]`, there is a match.

Text y **x** z y x y x y x y x z x x y

Pattern **x** y x y x z x


**Step 3**: `j = 3`, `i = 1`, comparing `P[2]` with `S[3]`, no match.
Now, going back to `P` and comparing `P[1]` with `S[3]`, it's not a match.
So now we move to the next character in `S`, incrementing the index in `S`.

Text y x ~~z~~ y x y x y x y x z x x y

Pattern x ~~y~~ x y x z x


**Step 4**: `j = 4`, `i = 0`, comparing `P[1]` with `S[4]`, no match.
Then we move to the next index in `S`.

Text y x z ~~y~~ x y x y x y x z x x y

Pattern ~~x~~ y x y x z x


**Step 5**: `j = 5`, `i = 0`, comparing `P[1]` with `S[5]`, there is a match.
Since there is a match, we increment the `P` index for the next step.

Text y x z y **x** y x y x y x z x x y

Pattern **x** y x y x z x


**Step 6**: `j = 6`, `i = 1`, comparing `P[2]` with `S[6]`, there is a match.
For the next step, we increment the `P` index.

Text y x z y **x** **y** x y x y x z x x y

Pattern **x** **y** x y x z x


**Step 7**: `j = 7`, `i = 2`, comparing `P[3]` with `S[7]`, there is a match.
For the next step, we increment the `P` index.

Text y x z y **x** **y** **x** y x y x z x x y

Pattern **x** **y** **x** y x z x


**Step 8**: `j = 8`, `i = 3`, comparing `P[4]` with `S[8]`, there is a match.
For the next step, we increment the `P` index.

Text y x z y **x** **y** **x** **y** x y x z x x y

Pattern **x** **y** **x** **y** x z x


**Step 9**: `j = 9`, `i = 4`, comparing `P[5]` with `S[9]`, there is a match.
For the next step, we increment the `P` index.

Text y x z y **x** **y** **x** **y** **x** y x z x x y

Pattern **x** **y** **x** **y** **x** z x


**Step 10**: `j = 10`, `i = 5`, comparing `P[6]` with `S[10]`, no match.
So we go back to `P` and compare `P[4]` with `S[10]`, as the mismatch led to `i = prefixValue[5] = 3`.
See the prefix computation steps above.
Since there is a match, we also increment the `P` index for the next step.

Text y x z y **x** **y** **x** **y** **x** ~~y~~ x z x x y

Pattern **x** **y** **x** **y** **x** ~~z~~ x


**Step 11**: `j = 11`, `i = 4`, comparing `P[5]` with `S[11]`, there is a match.
Since there is a match, we increment the `P` index for the next step.

Text y x z y x y **x** **y** **x** **y** **x** z x x y

Pattern **x** **y** **x** **y** **x** z x


**Step 12**: `j = 12`, `i = 5`, comparing `P[6]` with `S[12]`, there is a match.
We increment the `P` index for the next step.

Text y x z y x y **x** **y** **x** **y** **x** **z** x x y

Pattern **x** **y** **x** **y** **x** **z** x


**Step 13**: `j = 13`, `i = 6`, comparing `P[7]` with `S[13]`. There is a match.
Since we no longer have characters remaining in `P`, this completes the process,
and we can return the index value, `value = currentIndex(S) + 1 − length(P) = 14 − 7 = 7`.

In this example, the total number of shifts = `i − m = 13 − 7 = 6`.

Text y x z y x y **x** **y** **x** **y** **x** **z** **x** x y

Pattern **x** **y** **x** **y** **x** **z** **x**

**Code:**

```dotty
def kmpSubstringSearch(searchWord: String, source: String): Int = {
  val prefixTab = prefixTable(searchWord)
  source.indices
    .foldLeft((-1, 0)) {
      case ((foundIndex, _), _) if foundIndex > 0 => (foundIndex, 0)
      case ((_, x), i) =>
        val stepsX = LazyList.iterate(x)(x => prefixTab(x - 1))
        val lowerX =
          stepsX
            .find(x => x == 0 || searchWord(x) == source(i))
            .getOrElse(0)
        val newX =
          if searchWord(lowerX) == source(i) then lowerX + 1
          else lowerX
        if newX == searchWord.length then (i - newX + 1, 0)
        else (-1, newX)
    }
    ._1
}

private def prefixTable(searchString: String): Vector[Int] =
  searchString.tail
    .foldLeft((0, Vector(0))) { case ((initialValue, prefixT), currentCharacter) =>
      val lowerValue =
        LazyList
          .iterate(initialValue)(initialValue => prefixT(initialValue - 1))
          .find(initialValue => initialValue == 0 || searchString(initialValue) == currentCharacter)
          .getOrElse(0)
      val newValue =
        if searchString(lowerValue) == currentCharacter then lowerValue + 1
        else lowerValue
      (newValue, prefixT :+ newValue)
    }
    ._2
```

---

**Links:**

- [Source code][sample]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Search algorithms](https://github.com/vkostyukov/scalacaster/tree/master/src/search)

[sample]: https://gitverse.ru/scalabook/algorithms

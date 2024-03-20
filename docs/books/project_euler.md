# Project Euler

## О Project Euler

[Project Euler][euler] – онлайн-платформа, предлагающая множество математических задач и упражнений, 
требующих алгоритмического и логического мышления, а также глубокого понимания основ математики. 

Задачи на Project Euler имеют растущую сложность и представляют собой постепенно усложняющееся исследование 
в области математики, алгоритмов и программирования. 
Цель платформы заключается в том, чтобы дать пользователям возможность развивать свои навыки в этих областях, 
а также предложить задачи, способствующие росту математической интуиции и креативному мышлению.

Ключевые особенности Project Euler:

- Бесплатная платформа с множеством математических задач и упражнений.
- Алгоритмический и логический фокус задач.
- Интерактивный и постоянно обновляемый характер задач.
- Возможность решать задачи с помощью различных языков программирования.
- [Онлайн-сообщества и форумы][euler_chat], где можно обсуждать решения и обмениваться идеями.

Project Euler – великолепный ресурс для тех, кто хочет развить свои навыки в области математики, алгоритмов и программирования, 
а также для тех, кто ищет увлекательный способ познания новых концепций и улучшения своих навыков.

## Цель данного раздела

В данном разделе наша цель – предоставить образовательный контент, 
демонстрирующий решения задач с использованием функционального программирования на Scala 3.

Данный раздел включает в себя решения первых ста задач, 
поскольку Project Euler разрешает публиковать решения только в образовательных целях 
и только для первых ста задач за пределами проекта. 
Для всех последующих задач необходимо будет обратиться напрямую к сайту Project Euler.

Причина такого ограничения заключается в том, 
что Project Euler строго регламентирует размещение решений задач за пределами проекта. 
Дополнительные сведения об этом правиле доступны на [самом сайте проекта][euler].

> Question: I learned so much solving problem XXX, so is it okay to publish my solution elsewhere?
> 
> It appears that you have answered your own question. There is nothing quite like that "Aha!" moment 
> when you finally beat a problem which you have been working on for some time. It is often through the best 
> of intentions in wishing to share our insights so that others can enjoy that moment too. 
> Sadly, that will rarely be the case for your readers. 
> Real learning is an active process and seeing how it is done is a long way from experiencing that epiphany of discovery. 
> Please do not deny others what you have so richly valued yourself.
> 
> However, the rule about sharing solutions outside of Project Euler does not apply to the first one-hundred problems, 
> as long as any discussion clearly aims to instruct methods, not just provide answers, 
> and does not directly threaten to undermine the enjoyment of solving later problems. 
> Problems 1 to 100 provide a wealth of helpful introductory teaching material and if you are able to respect our requirements, 
> then we give permission for those problems and their solutions to be discussed elsewhere.

## Решения

### Problem 1

**Задача:**

[Problem 1](https://projecteuler.net/problem=1)

> If we list all the natural numbers below 10 that are multiples of 3 or 5, 
> we get 3, 5, 6 and 9. The sum of these multiples is 23.
> Find the sum of all the multiples of 3 or 5 below 1000.

**Алгоритм:**

Метод `getSumOfNumbersDividedBy3Or5` вычисляет сумму всех чисел, меньших `n`, которые делятся на `3` или `5`.

Он делит работу на три части:

1. `getSumOfNumbersLessThanNDividedByK(n, 3)` - вычисляет сумму всех чисел, меньших `n`, которые делятся на `3`.
2. `getSumOfNumbersLessThanNDividedByK(n, 5)` - вычисляет сумму всех чисел, меньших `n`, которые делятся на `5`.
3. `getSumOfNumbersLessThanNDividedByK(n, 3 * 5)` - вычисляет сумму всех чисел, меньших `n`, которые делятся на `15` 
   (то есть на `3` и `5` одновременно).

Затем он складывает суммы первых двух и вычитает сумму третьей, чтобы избежать двойного подсчета чисел, 
которые делятся на `3` и `5` одновременно.

Метод `getSumOfNumbersLessThanNDividedByK` вычисляет сумму всех чисел, меньших `n`, которые делятся на `k`. 
Он использует формулу арифметической прогрессии для вычисления суммы.

**Код:**

```dotty
def getSumOfNumbersDividedBy3Or5(n: Int): Long =
  val sum3  = getSumOfNumbersLessThanNDividedByK(n, 3)
  val sum5  = getSumOfNumbersLessThanNDividedByK(n, 5)
  val sum15 = getSumOfNumbersLessThanNDividedByK(n, 3 * 5)
  sum3 + sum5 - sum15

private def getSumOfNumbersLessThanNDividedByK(n: Long, k: Long): Long =
  val l = (n - 1) / k
  k * l * (l + 1) / 2
  
getSumOfNumbersDividedBy3Or5(10) // 23
```

**Метрики:**

Вычисление `getSumOfNumbersDividedBy3Or5(1000)` занимает несколько миллисекунд.

### Problem 2

**Задача:**

[Problem 2](https://projecteuler.net/problem=2)

> ???

**Алгоритм:**

???

**Код:**

```dotty
```

**Метрики:**


---

![Текущие результаты](https://projecteuler.net/profile/Artem_Korsakov.png)

---

**Ссылки:**

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat

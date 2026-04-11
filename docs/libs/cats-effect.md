{%
showPrev = true
prevPage = refined.html
showNext = true
nextPage = cats-effect/getting-started.html
%}

# Cats effect

[**Cats Effect**][home] — это чистая асинхронная среда выполнения для Scala. 
Она даёт отличные возможности для написания высоконадёжных, быстрых и параллельных программ, 
оставаясь при этом верной принципам функционального программирования.

## Ключевые возможности

### **Асинхронность**

Монада `IO` позволяет захватывать и контролировать асинхронные эффекты, управляемые колбэками, за чистым синхронным интерфейсом. 
Хотя внешне она похожа на `Future`, `IO` выводит эту концепцию на новый уровень, 
давая полный контроль над семантикой и поведением вычислений. 
Пишите программы, которые бесшовно смешивают синхронный и асинхронный код без ущерба для понимания и композиции.

### **Конкурентность**

`IO` может обеспечивать высококонкурентные приложения, например, веб-сервисы, обрабатывающие десятки тысяч запросов в секунду. 
Конкурентность в `IO` реализована через волокна (fibers) — легковесные, прерываемые потоки, полностью управляемые средой выполнения. 
Волокна гораздо дешевле нативных потоков ОС, поэтому ваше приложение может запускать десятки миллионов волокон без особых усилий.

### **Трассировка**

`IO` собирает информацию во время выполнения программы, что позволяет легко отследить источник ошибок 
или исследовать программу по ходу её работы. 
Трассировку можно включить в продакшене без заметного влияния на производительность, 
что автоматически открывает мощные возможности, такие как расширенные исключения, упрощающие диагностику ошибок.

### **Безопасность**

Приложения реального мира часто работают с ресурсами: сетевыми соединениями, файловыми дескрипторами. 
Управление ресурсами — исключительно сложная задача в конкурентных приложениях. 
Одна небольшая ошибка может привести к утечке памяти или даже дедлоку. 
`IO` управляет жизненным циклом ресурсов, гарантируя их безопасное выделение 
и освобождение даже при возникновении исключений и отмен.

### **Композируемость**

Cats Effect следует принципам чисто функционального программирования: 
`IO` представляет собой _описание_ программы, а не запущенное вычисление. 
Это даёт вам полный контроль над тем, как и когда вычисляются эффекты. 
Из простых программ можно составлять более сложные, сохраняя способность рассуждать о поведении и сложности. 
Рефакторинг без страха!

### **Мощное абстрактное исчисление**

Cats Effect уникальным образом определяет, что значит быть функциональным эффектом. 
Это даёт ему непревзойдённую выразительную и композиционную силу, открывая целую экосистему расширяемости. 
Вы получаете возможность переопределять саму среду выполнения, 
привнося полную мощь экосистемы Cats для элегантного решения сложных задач: 
распределённой трассировки, внедрения зависимостей, отправки ошибок по нескольким каналам и многого другого!

Система абстракций Cats Effect — это результат многолетних исследований и опыта 
в области применения концепций функционального программирования в реальных приложениях на JVM и JavaScript.

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Cats Effect 3 for Beginners - Part-1](https://yadukrishnan.live/cats-effect-3-for-beginners-part-1)
- [Cats Effect 3 for Beginners - Part-2: Common APIs](https://yadukrishnan.live/common-apis-cats-effect-io-part-2)
- [Cats Effect 3 for Beginners - Part-3: Traversing Cats Effect IOs](https://yadukrishnan.live/traversing-cats-effect-ios-part-3)
- [Cats Effect 3 for Beginners - Part-4: Concurrent Execution in Cats Effect using Fibers](https://yadukrishnan.live/concurrent-execution-in-cats-effect-using-fibers)
- [Cats Effect 3 for Beginners - Part-5: Resource Handling in Cats Effect 3](https://yadukrishnan.live/resource-handling-in-cats-effect-3-part-5)
- [Cats Effect 3 for Beginners - Part-6: Cancellation of IO/Fiber in Cats Effect 3](https://yadukrishnan.live/cancellation-of-iofiber-in-cats-effect-3-part-6)
- [Cats Effect 3 for Beginners - Part-7: Synchronisation and Concurrent Access in Cats Effect 3](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)
- [Cats Effect 3 for Beginners - Part-8: Common Operators in Cats Effect 3](https://yadukrishnan.live/common-operators-in-cats-effect-3-part-8)
- [Cats Effect 3 for Beginners - Part-9: Error Handling in Cats Effect](https://yadukrishnan.live/error-handling-in-cats-effect-part-9)
- [Cats effect documentation][doc]
- [Cats Effect vs ZIO](https://softwaremill.com/cats-effect-vs-zio/)
- [A practical guide to error handling in Scala Cats and Cats Effect](https://softwaremill.com/practical-guide-to-error-handling-in-scala-cats-and-cats-effect/)

[home]: https://typelevel.org/cats-effect/
[doc]: https://typelevel.org/cats-effect/docs/getting-started
[sample]: https://gitverse.ru/scalabook/cats

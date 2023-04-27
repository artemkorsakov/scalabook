# Наблюдатель

#### Назначение

Определение зависимости "один ко многим" между объектами, 
чтобы при изменении состояния одного объекта все его иждивенцы уведомлялись и обновлялись автоматически.

Шаблон также известен как публикация/подписка, что указывает на структуру шаблона: 
объект играет роль издателя, любое количество объектов может подписаться на издателя, 
тем самым получая уведомление всякий раз, когда в издателе происходит определенное событие. 
Обычно издатель передает свой экземпляр в уведомлении. 
Это позволяет подписчику запрашивать у издателя любую соответствующую информацию, 
например, чтобы иметь возможность синхронизировать состояние.

#### Диаграмма

![Observer](https://upload.wikimedia.org/wikipedia/commons/b/bd/Observer_UML_smal.png?uselang=ru)

#### Пример

```scala
trait Subject[T] { self: T =>
  import scala.collection.mutable
  private val observers: mutable.ListBuffer[T => Unit] =
    mutable.ListBuffer.empty[T => Unit]

  def subscribe(obs: T => Unit): Unit =
    observers.addOne(obs)

  def unsubscribe(obs: T => Unit): Unit =
    observers.subtractOne(obs)

  protected def publish(): Unit = observers.foreach(obs => obs(self))
}
```

```scala
trait Sensor(val label: String):
  var value: Double = _
  def changeValue(v: Double): Unit = value = v

// Pattern specific code
trait SensorSubject extends Sensor with Subject[Sensor]:
  override def changeValue(v: Double): Unit =
    super.changeValue(v)
    publish()

class Display(label: String):
  def notify(s: Sensor): Unit =
    println(s"$label ${s.label} ${s.value}")
```

```scala
val s1: SensorSubject = new Sensor("s1") with SensorSubject
val d1: Display = new Display("d1")
val d2: Display = new Display("d2")
s1.subscribe(d1.notify)
s1.subscribe(d2.notify)
s1.changeValue(10)
// d1 s1 10.0
// d2 s1 10.0
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Observer_pattern)

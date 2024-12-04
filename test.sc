final case class Queue[+A](in: List[A], out: List[A]):
  def frontOption: Option[A] = dequeue.map(_._1)

  def rearOption: Option[Queue[A]] = dequeue.map(_._2)

  lazy val isEmpty: Boolean = in.isEmpty && out.isEmpty

  def enqueue[B >: A](x: B): Queue[B] = Queue(x :: in, out)

  def dequeue: Option[(A, Queue[A])] = out match
    case hd :: tl => Some((hd, Queue(in, tl)))
    case Nil => in.reverse match
        case hd :: tl => Some((hd, Queue(Nil, tl)))
        case Nil      => None

object Queue:
  def empty[A]: Queue[A] = Queue(Nil, Nil)

val emptyQueue: Queue[Int]    = Queue.empty[Int]
val nonEmptyQueue: Queue[Int] = emptyQueue.enqueue(1).enqueue(2)

println("frontOption emptyQueue: " + emptyQueue.frontOption)
println("frontOption nonEmptyQueue: " + nonEmptyQueue.frontOption)
println("rearOption emptyQueue: " + emptyQueue.rearOption)
println("rearOption nonEmptyQueue: " + nonEmptyQueue.rearOption)
println("isEmpty emptyQueue: " + emptyQueue.isEmpty)
println("isEmpty nonEmptyQueue: " + nonEmptyQueue.isEmpty)
println("enqueue emptyQueue: " + emptyQueue.enqueue(5))
println("enqueue nonEmptyQueue: " + nonEmptyQueue.enqueue(5))
println("dequeue emptyQueue: " + emptyQueue.dequeue)
println("dequeue nonEmptyQueue: " + nonEmptyQueue.dequeue) 

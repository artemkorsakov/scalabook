enum LinkedList[+A]:
  case Nil
  case Cons(head: A, tail: LinkedList[A])

  lazy val headOption: Option[A] = this match
    case Nil        => None
    case Cons(h, _) => Some(h)

  lazy val tailOption: Option[LinkedList[A]] = this match
    case Nil           => None
    case Cons(_, tail) => Some(tail)

  lazy val lastOption: Option[A] = this match
    case Nil          => None
    case Cons(h, Nil) => Some(h)
    case Cons(h, t)   => t.lastOption

  lazy val initOption: Option[LinkedList[A]] = this match
    case Nil          => None
    case Cons(h, Nil) => Some(Nil)
    case Cons(h, t)   => t.initOption.map(Cons(h, _))

  lazy val isEmpty: Boolean = this match
    case Nil => true
    case _   => false

  lazy val length: Int = this match
    case Nil           => 0
    case Cons(_, tail) => 1 + tail.length

  def prepend[B >: A](b: B): LinkedList[B] = Cons(b, this)

  def append[B >: A](b: B): LinkedList[B] = this match
    case Nil              => Cons(b, Nil)
    case Cons(head, tail) => Cons(head, tail.append(b))

  def get(i: Int): Option[A] =
    if i < 0 then None
    else if i == 0 then headOption
    else tailOption.flatMap(_.get(i - 1))

  def contains[B >: A](x: B): Boolean = this match
    case Nil                        => false
    case Cons(head, _) if head == x => true
    case Cons(_, tail)              => tail.contains(x)

  def concat[B >: A](list: LinkedList[B]): LinkedList[B] = this match
    case Nil              => list
    case Cons(head, tail) => Cons(head, tail.concat(list))

  def filter(p: A => Boolean): LinkedList[A] = this match
    case Nil                         => Nil
    case Cons(head, tail) if p(head) => Cons(head, tail.filter(p))
    case Cons(_, tail)               => tail.filter(p)

  def map[B](f: A => B): LinkedList[B] = this match
    case Nil              => Nil
    case Cons(head, tail) => Cons(f(head), tail.map(f))

  def fold[B](init: B)(op: (B, A) => B): B = this match
    case Nil              => init
    case Cons(head, tail) => tail.fold(op(init, head))(op)

  def take(n: Int): LinkedList[A] = this match
    case Cons(head, tail) if n > 0 => Cons(head, tail.take(n - 1))
    case _ => Nil

  def drop(n: Int): LinkedList[A] = this match
    case Cons(head, tail) if n > 0 => tail.drop(n - 1)
    case _ => Nil

  def takeWhile(p: A => Boolean): LinkedList[A] = this match
    case Cons(head, tail) if p(head) => Cons(head, tail.takeWhile(p))
    case _ => Nil

  def dropWhile(p: A => Boolean): LinkedList[A] = this match
    case Cons(head, tail) if p(head) => tail.dropWhile(p)
    case _ => this
end LinkedList

val emptyList: LinkedList[Int] = LinkedList.Nil
val nonEmptyList: LinkedList[Int] =
  LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil))



println(emptyList.initOption)
println(nonEmptyList.initOption)

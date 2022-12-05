package typeclass.monad

trait IsEmpty[F[_]] extends PlusEmpty[F]:
  def isEmpty[A](fa: F[A]): Boolean

object IsEmpty:
  def apply[F[_]: IsEmpty]: IsEmpty[F] = summon[IsEmpty[F]]

  given IsEmpty[List] with
    override def isEmpty[A](fa: List[A]): Boolean                = fa.isEmpty
    override def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2
    override def empty[A]: List[A]                               = List.empty[A]

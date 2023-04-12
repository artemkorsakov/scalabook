transparent trait S
trait Kind
object Var extends Kind, S
object Val extends Kind, S

val condition: Boolean = true

val x = Set(if condition then Val else Var)

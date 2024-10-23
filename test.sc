enum Expr:
  self =>

  case Zero
  case Succ(expr: Expr)
  case Pred(expr: Expr)
  case Add(expr1: Expr, expr2: Expr)

  def normalize: Expr =
    self match
      case Succ(Pred(e))     => e.normalize
      case Pred(Succ(e))     => e.normalize
      case Succ(e)           => Succ(e.normalize)
      case Pred(e)           => Pred(e.normalize)
      case Add(Zero, expr2)  => expr2
      case Add(Succ(e1), e2) => Succ(Add(e1, e2))
      case Add(Pred(e1), e2) => Pred(Add(e1, e2))
      case _                 => self

  def size: Int =
    self match
      case Zero        => 0
      case Succ(e)     => 1 + e.size
      case Pred(e)     => 1 + e.size
      case Add(e1, e2) => 1 + e1.size + e2.size
end Expr

import Expr.*
val expr = Succ(Pred(Succ(Pred(Pred(Zero)))))
println(expr.normalize)
println(expr.size)

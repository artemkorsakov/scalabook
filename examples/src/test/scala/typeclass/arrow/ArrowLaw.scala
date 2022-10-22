package typeclass.arrow

import typeclass.common.Runner2

trait ArrowLaw extends StrongLaw, SplitLaw, CategoryLaw:
  def checkArrowLaw[=>:[_, _]: Arrow: Runner2, A, B, C, D, E, F](
      gad: A =>: D,
      gab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(a: A, c: C, d: D)(using
      fcb: C => B,
      fcd: C => D,
      fba: B => A,
      fde: D => E,
      fef: E => F
  )(using PF: Arrow[Function1]): Unit =
    checkStrongLaw[=>:, A, B, C, D, E, F](gad, gab)(a, c, d)
    checkSplitLaw[=>:, A, B, C, D](gab, bc, cd, f1, f2, f3)(a)
    checkCategoryLaw[=>:, A, B, C, D](gab, bc, cd, f1, f2, f3)(a)
